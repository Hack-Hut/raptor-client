package monitors.audisp;

import utils.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class Worker implements Runnable{

    private Set<String> executables = new HashSet<>();
    private boolean isRunning = true;

    public void stop(){
        isRunning = false;
    }

    private void start() throws IOException{
        ServerSocket ss = new ServerSocket(4789);
        Socket client = ss.accept();
        worker(client);
    }

    private void worker(Socket client) throws IOException {
        try{
            Log.debug("Audisp-remote: Connection established");
            InputStream inputStream = client.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String response = bufferedReader.readLine();
            while(response != null && isRunning){
                parseString(response);
                response = bufferedReader.readLine();
            }
        }catch (IOException e){
            Log.error(e.toString());
        }
        client.close();
    }

    private void parseString(String auditLine){
        try {
            String end = auditLine.split("exe=\"")[1];
            executables.add(end.split("\"")[0]);
        }catch (ArrayIndexOutOfBoundsException ignored){}
    }

    public Object[] getExecutables(){
        return executables.toArray();
    }

    public void run(){
        utils.misc.setThreadName(Thread.currentThread(), this.getClass());
        try {
            start();
        } catch (IOException e) {
            Log.error("Audisp-remote failed with the following error");
            Log.error(e.toString());
        }
    }

    public static void main(String args[]) throws IOException {
        Worker worker = new Worker();
        worker.start();
    }
}
