package monitors;

import java.util.Set;

public interface AuditInterface {
    Set<String> getExecutables();
    boolean start();
    boolean stop();
}
