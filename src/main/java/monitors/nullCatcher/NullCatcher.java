package monitors.nullCatcher;

import utils.Log;

import java.io.*;
import java.util.ArrayList;

import static utils.Exec.executeCommandGetOutput;

public class NullCatcher {
    private final String ORIG_LOCATION = "/dev/null";
    private final String BACKUP_LOCATION = ORIG_LOCATION + ".bk";
    private static final String CWD = new File("").getAbsolutePath();
    private static final String LOG_LOCATION = CWD + "/logs/null-catcher.log";

    public boolean start(){
        // Check to see if /dev/null is the real one or the text one.
        Log.debug("Attempting to start " + ORIG_LOCATION);
        if (isDevNullSpecialChar("/dev/null")){
            Log.debug("Replacing the original " + ORIG_LOCATION + " file.");
            if (!createNewDevNull()){
                Log.error("Failed to create dev/null file.");
                return false;
            }
        }
        clearDevNull();
        if (!testDevNullCatcher()) {
            Log.error("Read from " + ORIG_LOCATION + " but did not find the test text.");
            return false;
        }
        return true;
    }

    public boolean stop(){
        Log.info("Replacing " + ORIG_LOCATION + " with the original");
        utils.FileOperations.mv(ORIG_LOCATION, LOG_LOCATION);
        utils.FileOperations.mv(BACKUP_LOCATION, ORIG_LOCATION);
        return true;
    }

    private boolean isDevNullSpecialChar(String path){
        final String[] cmd = {"file", path};
        for (String line: executeCommandGetOutput(cmd)) {
            if (line.contains("character special")) return true;
        }
        return false;
    }

    private boolean createNewDevNull(){
        // assumes that dev/null is normal
        Log.debug("Creating new " + ORIG_LOCATION +" file");
        if (utils.FileOperations.exists(BACKUP_LOCATION)){
            if (isDevNullSpecialChar(BACKUP_LOCATION)) {
                Log.warn("Cannot back up " + ORIG_LOCATION + " because there is already a file there at " + BACKUP_LOCATION);
                return false;
            }
        }
        if (!utils.FileOperations.mv(ORIG_LOCATION, BACKUP_LOCATION)){
            Log.error("Could not backup original " + ORIG_LOCATION);
            return false;
        }
        if (isDevNullSpecialChar(ORIG_LOCATION)){
        Log.error("Failed to move " + ORIG_LOCATION);
        }
        if (!utils.FileOperations.createNewFile(ORIG_LOCATION)) {
            Log.error("Failed to create empty file at " + ORIG_LOCATION);
            return false;
        }
        return true;
    }

    private boolean clearDevNull(){
        return utils.FileOperations.clearFile(ORIG_LOCATION);
    }

    private boolean testDevNullCatcher(){
        ArrayList<String> lines = new ArrayList<>();
        String searchTerm = "RAPTOR TEST";
        lines.add(searchTerm);

        // write to dev null
        try {
            utils.FileOperations.writeToFile(ORIG_LOCATION, lines);
        } catch (IOException e) {
            Log.error("Test /dev/null failed");
            Log.error("Failed to write to /dev/null");
            Log.error(e.toString());
        }

        // read from dev null
        File file = new File(ORIG_LOCATION);
        try (FileInputStream fis = new FileInputStream(file)) {
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);

            String line;
            while((line = br.readLine()) != null){
                if (line.contains(searchTerm)) {
                    br.close();
                    return true;
                }
            }
        br.close();
        } catch (IOException e) {
            Log.error("Failed to read from " + ORIG_LOCATION);
            Log.error(e.toString());
        }
        return false;
    }

}
