package raptorClient.master;

public class BuildMonitor extends raptorClient.master.MasterController{
    public BuildMonitor(int buildId, String stage, String[] buildCommand){
        super(buildId, stage, buildCommand);
    }
    public boolean startMonitors(){
        return true;
    }
    public boolean stopMonitors(){
        return true;
    }
    public boolean pingSlave(){
        return true;
    }
    public void cleanMachine(){
    }
    public void uploadResults(){
    }
    public void killSlaves(){
    }
    public void processResults() {
    }
    public boolean executeBuild() {
        return true;
    }
    public void connectToSlaves() {
    }


}
