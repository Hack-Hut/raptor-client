package monitors;

import buildToolsConfigGenerator.auditors.audisp;
import monitors.audisp.Worker;
import utils.Exec;
import utils.Log;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

/**
 *  This class is similar to auditd, but is a bit more advanced because it multiplexes the data
 *  allowing realtime computation.
 *
 *  Auditd will do it's magic and produce a large log file, potentially +100Gb.
 *  Rather than parsing this log file after the build has completed, wasting up to 20 minutes.
 *  This class will tell auditd to pass the audit logs to audisp(atcher), where audisp will then
 *  pass the logs to the audisp-remote plugin. Audisp-remote will then get configured to open a local
 *  port and use TCP to forward the information to a remote port. In this case the remote port will be
 *  controlled by raptor-client's audisp.Worker thread.
 *
 *  Note
 *  This method is not guaranteed to work, since audisp-remote plugin is not all ways installed on the build
 *  machine. You should not assume that the build environment is compatible with this class. Therefore if you
 *  use this class, expect it to fail and set a fall back option of using auditd.
 */
public class Audisp implements MonitorInterface, AuditInterface {

    private static final String NEW_AU_REMOTE_CONF_LOCATION = "/audisp/au-remote.conf";
    private static final String NEW_AUDISP_REMOTE_CONF_LOCATION = "/audisp/audisp-remote.conf";
    private static final String NEW_AUDITD_LOCATION = "/audisp/auditd.conf";

    private static final String AU_REMOTE_CONF = "/etc/audisp/plugins.d/au-remote.conf";
    private static final String AUDISP_CONF = "/etc/audisp/audisp-remote.conf";
    private static final String AUDITD_CONF = "/etc/audit/auditd.conf";

    private static final String[] EXECVE64 = {"sudo", "auditctl", "-a", "always,exit", "-F", "arch=b64", "-S", "execve"};
    private static final String[] EXECVE32 = {"sudo", "auditctl", "-a", "always,exit", "-F", "arch=b32", "-S", "execve"};
    private static final String[] DEL_RULES = {"sudo", "auditctl", "-D"};
    private static final String[] SHOW_RULES = {"sudo", "auditctl", "-l"};

    private static final String[] PS = {"ps", "-A"};

    private static final String[] START_AUDITD = {"sudo", "systemctl", "start", "auditd"};
    private static final String[] STOP_AUDITD = {"sudo", "systemctl", "stop", "auditd"};
    private static final String[] DISABLE_AUDITD = {"sudo", "auditd", "-s", "disable"};
    private static final String[] ENABLE_AUDITD = {"sudo", "auditd", "-s", "enable"};
    private static final String[] STATUS_AUDITD = {"sudo", "systemctl", "status", "auditd"};

    private static final String AUDIT_SRC_LOC = utils.SystemOps.getCWD() + "/resources/audit-userspace/";

    private Worker worker;
    private audisp auditParser; // TODO: refactor this, the name is stupid!

    public boolean setup(){
        Log.info("Starting configurations for the auditd remote multiplexing.");
        if (!findRequiredExecutables()){
            compileAudit();
        }
        stopExistingAuditd();
        if (!updateConfigFiles()){
            Log.error("Failed to start audisp, could not swap the configuration files.");
            return false;
        }
        chownConfig();
        if (!startAuditd()){
            Log.error("Failed to start auditd");
            return false;
        }
        addRules();
        if(checkIfRunning()){
            Log.info("Audisp multiplexer running at 127.0.0.1:4987");
            return true;
        }

        return false;
    }

    public boolean start(){
        getAudsipWorker();
        Thread workerThread = new Thread(worker);
        workerThread.start();
        return true;
    }

    public boolean stop(){
        //TODO stop audisp
        execute(STOP_AUDITD);
        worker.stop();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Log.error(e.toString());
            Thread.currentThread().interrupt();
        }
        return checkIfRunning();
    }

    @Override
    public boolean generateConfigurationFiles() {
        getAuditParser();
        return auditParser.generateConfigurationFiles();
    }

    public Object[] getExecutables(){
        return worker.getExecutables();
    }

    private void getAudsipWorker(){
        worker = new Worker();
    }

    /**
     * Makes sure that we have audisp-remote and audispd to make sure that we can multiplex.
     */
    private boolean findRequiredExecutables(){
        String[] audispLocations = {"/usr/bin/audisp", "/bin/audisp", "/sbin/audisp"};

        boolean audispFound = false;
        boolean audispRemoteFound = false;

        for(String location : audispLocations){
            if(utils.FileOperations.exists(location + "d")){
                audispFound = true;
            }
            if(utils.FileOperations.exists(location + "-remote")){
                audispRemoteFound = true;
            }
        }
        Log.debug("Audisp-Remote: " + audispRemoteFound);
        Log.debug("Audisp: " + audispFound);
        return (audispRemoteFound && audispFound);
    }

    private void compileAudit(){
        Log.warn("Failed to find all necessary files for auditd. Recompiling code Auditd.");
        String[] autogenCmd = {AUDIT_SRC_LOC + "autogen.sh"};
        String[] configure = {AUDIT_SRC_LOC + "configure"};
        String[] make = {"make", "-C", AUDIT_SRC_LOC, "-j", String.valueOf(utils.SystemOps.getCPUCores())};
        String[] makeInstall = {"sudo", "make", "install" , "-C", AUDIT_SRC_LOC};

        Log.info("Generating audit-userspace config");
        executeAndWait(autogenCmd);

        Log.info("Configuring auditd-userspace");
        executeAndWait(configure);

        Log.info("Compiling auditd-userspace");
        executeAndWait(make);

        Log.info("Install auditd-userspace");
        executeAndWait(makeInstall);
    }

    private boolean stopExistingAuditd(){
        Exec stop = new Exec(STOP_AUDITD);
        stop.execute();
        int returnCode = stop.waitForProcessToDie();
        if(returnCode == 0){
            Log.debug("Success: " + Arrays.toString(STOP_AUDITD));
            return true;
        }
        Log.error("Failed: " + Arrays.toString(STOP_AUDITD));
        Log.error("returned with exit code: " + stop.getPid());
        return false;
    }

    private boolean updateConfigFiles(){
        return (swapConfigFiles(NEW_AU_REMOTE_CONF_LOCATION, AU_REMOTE_CONF)
                && swapConfigFiles(NEW_AUDISP_REMOTE_CONF_LOCATION, AUDISP_CONF)
                && swapConfigFiles(NEW_AUDITD_LOCATION, AUDITD_CONF));
    }

    private void chownConfig(){
        String[] auRemote = {"sudo", "chown", "root", AU_REMOTE_CONF};
        String[] auditd = {"sudo", "chown", "root", AUDISP_CONF};
        String[] audisp = {"sudo", "chown", "root", AUDITD_CONF};
        executeAndWait(auRemote);
        executeAndWait(auditd);
        executeAndWait(audisp);
    }

    private boolean swapConfigFiles(String from, String to){
        URL fileUrl = getClass().getResource(from);
        try(
            FileOutputStream out = new FileOutputStream(to);
            BufferedInputStream in = new BufferedInputStream(fileUrl.openStream()))
        {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                out.write(dataBuffer, 0, bytesRead);
            }
            out.close();
            Log.debug("Inserting JAR configuration file " + from + " to " + to);
            return true;
        } catch (IOException e) {
            Log.error("Failed writing " + from + " to " + to);
            Log.error(e.toString());
        }
        return false;
    }

    private boolean startAuditd(){
        executeAndWait(START_AUDITD);
        executeAndWait(ENABLE_AUDITD);
        executeAndWait(START_AUDITD);
        return checkIfRunning();
    }

    private boolean checkIfRunning(){
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Log.error("Audisp interruption");
            Log.error(e.toString());
            Thread.currentThread().interrupt();
        }

        Exec ps = new Exec(PS);
        ps.disableShowOutput();
        ps.execute();
        ps.getOutput();
        String output = ps.getStdout();
        output = output.toLowerCase();
        return (output.contains("auditd") && output.contains("audisp"));
    }

    private void addRules(){
        execute(DEL_RULES);
        execute(EXECVE32);
        execute(EXECVE64);
    }

    private void execute(String[] command){
        Exec cmd = new Exec(command);
        cmd.execute();
    }

    private void executeAndWait(String[] command){
        Exec cmd = new Exec(command);
        Log.debug("Executing " + Arrays.toString(command));
        cmd.execute();
        cmd.getOutput();
        if(!cmd.getStdout().isEmpty()) Log.debug(cmd.getStdout());
        if(!cmd.getStderr().isEmpty()) Log.debug(cmd.getStderr());
    }

    private void getAuditParser(){
        Object[] executableList = worker.getExecutables();
        auditParser = new audisp(executableList);
    }

    public static void main(String[] args){
        Audisp dispatcher = new Audisp();
        dispatcher.start();
    }
}
