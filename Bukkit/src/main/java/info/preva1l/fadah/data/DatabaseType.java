package info.preva1l.fadah.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DatabaseType {
    MONGO("mongodb", "MongoDB"),
    MYSQL("mysql", "MySQL"),
    MARIADB("mariadb", "MariaDB"),
    POSTGRESQL("postgresql", "PostgreSQL"),
    SQLITE("sqlite", "SQLite"),
    H2("h2", "H2");
    private final String id;
    private final String friendlyName;

    public boolean isLocal() {
        return this == SQLITE || this == H2;
    }
}