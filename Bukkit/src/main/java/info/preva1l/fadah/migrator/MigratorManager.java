package info.preva1l.fadah.migrator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MigratorManager {
    private final Map<String, Migrator> migrators = new ConcurrentHashMap<>();

    public void loadMigrator(Migrator migrator) {
        migrators.put(migrator.getMigratorName(), migrator);
    }

    public List<String> getMigratorNames() {
        return migrators.keySet().stream().toList();
    }

    public boolean migratorExists(String migratorName) {
        return migrators.containsKey(migratorName);
    }

    public Migrator getMigrator(String migratorName) {
        return migrators.get(migratorName);
    }
}
