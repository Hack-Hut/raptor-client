package stage.master;

public class Semmle extends stage.master.MasterController{
    public Semmle(int buildId, String stage, String[] buildCommand){
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
    public void connectToSlaves() {
    }
    public boolean executeBuild() {
        return true;
    }

}
