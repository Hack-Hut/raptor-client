package stage.master;

public class BuildMonitor extends stage.master.MasterController{
    public BuildMonitor(int buildId, String stage, String[] buildCommand){
        super(buildId, stage, buildCommand);
    }
    public boolean startMonitors(){
        return true;
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
