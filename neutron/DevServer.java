package neutron;
import java.util.*;
import jpm.*;

public class DevServer {
    private static int port;
    private static boolean running;
    private static final List<Runnable> tasks = new ArrayList<>();
    private static ClassExecuter DevServer;
    public static boolean create(int p) {
        port = p;
        DevServer = jpm.requireFatal("dev.Dev","""
            DevServer class not found. Make sure DevServer is included in the classpath.
                > jpm install dev exec
                > jpx dev init %d
                > jpm run -hot "(resources/**.html)" "cp -r resources/* out/ && dev"
            """.formatted(port));
        return true;
    }
    public static int on(String event, Runnable task) {
        if (DevServer == null) return -1;
        if (running) return tasks.add(task) ? tasks.size() - 1 : -1;
        try {
            Runnable runTasks = () -> tasks.forEach(Runnable::run);
            DevServer.factory("create", int.class, port, String.class, event, Runnable.class, runTasks);
        } catch (Exception e) {
            System.err.println("DevServer could not be started. " + e);
            return -1;
        }
        running = true;
        return tasks.add(task) ? tasks.size() - 1 : -1;
    }
    public static void off(int index) {
        if (DevServer == null || index < 0 || index >= tasks.size()) return;
        tasks.remove(index);
        if (tasks.isEmpty()) {
            try {
                DevServer.call("destroy");
            } catch (Exception e) {
                System.err.println("DevServer could not be destroyed. " + e);
            }
            port = 0;
            DevServer = null;
            running = false;
        }
    }
}