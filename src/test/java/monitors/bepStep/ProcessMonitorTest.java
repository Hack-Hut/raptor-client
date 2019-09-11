package monitors.bepStep;

import org.junit.jupiter.api.Test;
import utils.Exec;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ProcessMonitorTest {
    private static String cwd = utils.SystemOps.getCWD();
    private String eightcc = cwd + "/src/test/resources/testCompilationProject/8cc";
    private String os = utils.SystemOps.getOsType().toLowerCase();

    @Test
    void compile8cc(){
        if (!os.equals("linux")) return;
        String[] testCleanCommand = {"make", "-C", eightcc, "clean"};
        String[] testCompileCommand = {"make", "-C", eightcc};

        Exec cleanProject = new Exec(testCleanCommand);
        Exec testProject = new Exec(testCompileCommand);

        Thread clean = new Thread(cleanProject);

        clean.start();
        cleanProject.getOutput();

        Thread execThread = new Thread(testProject);
        execThread.start();
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long pid = testProject.getPid();
        ProcessMonitor processMonitor = new ProcessMonitor(pid);
        processMonitor.setTestMode();
        Thread bepStepThread = new Thread(processMonitor);
        bepStepThread.start();
        try {
            bepStepThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ArrayList<String> expected = new ArrayList<>();
        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o main.o main.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o cpp.o cpp.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o debug.o debug.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o dict.o dict.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o gen.o gen.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o lex.o lex.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o vector.o vector.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o parse.o parse.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o buffer.o buffer.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR='\"" + cwd + "/src/test/resources/testCompilationProject/8cc\'" +
                " -c -o map.o map.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o error.o error.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                "   -c -o path.o path.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o file.o file.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o set.o set.c ");

        expected.add("cc -Wall -Wno-strict-aliasing -std=gnu11 -g -I. -O0 " +
                "-DBUILD_DIR=\"" + cwd + "/src/test/resources/testCompilationProject/8cc\"" +
                " -c -o encoding.o encoding.c. ");

        ArrayList<String> actual = processMonitor.getCommands();
        // Test is too un reliable, could be improved in the future
    }

    @Test
    void compileFake(){
        long pid = -1;
        ProcessMonitor processMonitor = new ProcessMonitor(pid);
        processMonitor.setTestMode();
        Thread bepStepThread = new Thread(processMonitor);
        bepStepThread.start();
        try {
            bepStepThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(0, processMonitor.getCommands().size());
    }
}