package raptorClient.master;

public class Completeness extends raptorClient.master.MasterController{
    public Completeness(int buildId, String stage, String[] buildCommand){
        super(buildId, stage, buildCommand);
    }
    public boolean startMonitor(){
        return true;
    }
    public boolean stopMonitor(){
        return true;
    }
    public boolean pingSlave(){
        return true;
    }
    public boolean cleanMachine(){
        return true;
    }
    public boolean uploadResults(){
        return true;
    }
    public boolean killSlaves(){
        return true;
    }
    public boolean processResults() {
        return  true;
    }
    public boolean executeBuild() {
        return  true;
    }
    public boolean connectToSlaves() {
        return  true;
    }


}