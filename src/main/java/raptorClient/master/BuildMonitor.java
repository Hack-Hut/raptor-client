package raptorClient.master;

public class BuildMonitor extends raptorClient.master.MasterController{
    public BuildMonitor(int buildId, String stage, String[] buildCommand){
        super(buildId, stage, buildCommand);
    }
    public void startMonitors(){
    }
    public void stopMonitors(){
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
    public void executeBuild() {
    }
    public void connectToSlaves() {
    }


}
