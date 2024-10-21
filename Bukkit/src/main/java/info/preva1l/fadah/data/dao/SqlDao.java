package info.preva1l.fadah.data.dao;

import info.preva1l.fadah.data.DatabaseType;
import info.preva1l.fadah.data.handler.HikariHandler;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class SqlDao<T> implements Dao<T> {

    private final String name;
    private final HikariHandler handler;

    private final Map<Statement, String> statements = new HashMap<>();

    public SqlDao(@NotNull String name, @NotNull HikariHandler handler) {
        this.name = name;
        this.handler = handler;
    }

    @NotNull
    protected String getName() {
        return name;
    }

    @NotNull
    protected HikariHandler getHandler() {
        return handler;
    }

    @NotNull
    protected DatabaseType getType() {
        return handler.getType();
    }

    @NotNull
    public String sql(@NotNull Statement type) {
        final String sql = this.statements.get(type);
        if (sql == null) {
            throw new NotImplementedException();
        }
        return sql;
    }

    protected void statement(@NotNull Statement type, @NotNull String sql) {
        this.put(type, sql);
    }

    protected void statement(@NotNull Statement type, @NotNull String sql, @NotNull DatabaseType database1, @NotNull String sql1) {
        if (handler.getType() == database1) {
            this.put(type, sql1);
        } else {
            this.put(type, sql);
        }
    }

    protected void statement(@NotNull Statement type, @NotNull String sql, @NotNull DatabaseType database1, @NotNull String sql1, @NotNull DatabaseType database2, @NotNull String sql2) {
        if (handler.getType() == database1) {
            this.put(type, sql1);
        } else if (handler.getType() == database2) {
            this.put(type, sql2);
        } else {
            this.put(type, sql);
        }
    }

    protected void statement(@NotNull Statement type, @NotNull String sql, @NotNull DatabaseType database1, @NotNull String sql1, @NotNull DatabaseType database2, @NotNull String sql2, @NotNull DatabaseType database3, @NotNull String sql3) {
        if (handler.getType() == database1) {
            this.put(type, sql1);
        } else if (handler.getType() == database2) {
            this.put(type, sql2);
        } else if (handler.getType() == database3) {
            this.put(type, sql3);
        } else {
            this.put(type, sql);
        }
    }

    private void put(@NotNull Statement type, @NotNull String sql) {
        if (handler.getType() == DatabaseType.POSTGRESQL) {
            this.statements.put(type, sql.replace('`', '"'));
        } else {
            this.statements.put(type, sql);
        }
    }

    @Override
    public Optional<T> get(UUID id) {
        return handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.SELECT))) {
                return Optional.ofNullable(select(id, con, stmt));
            }
        }, "Failed to get " + this.name + "!", Optional.empty());
    }

    @Nullable
    protected T select(UUID id, Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public List<T> getAll() {
        return handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.SELECT_ALL))) {
                return selectAll(con, stmt);
            }
        }, "Failed to get all " + this.name + "!", List.of());
    }

    protected List<T> selectAll(Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public void save(T t) {
        handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.INSERT))) {
                insert(t, con, stmt);
            }
        }, "Failed to add item to " + this.name + "!");
    }

    protected void insert(T t, Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public void update(T t, String[] params) {
        handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.UPDATE))) {
                update(t, params, con, stmt);
            }
        }, "Failed to update item from " + this.name + "!");
    }

    protected void update(T t, String[] params, Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public void delete(T t) {
        handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.DELETE))) {
                delete(t, con, stmt);
            }
        }, "Failed to delete item from " + this.name + "!");
    }

    protected void delete(T t, Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    @Override
    public void deleteSpecific(T t, Object o) {
        handler.connect(con -> {
            try (PreparedStatement stmt = con.prepareStatement(sql(Statement.DELETE_SPECIFIC))) {
                deleteSpecific(t, o, con, stmt);
            }
        }, "Failed to delete item from " + this.name + "!");
    }

    protected void deleteSpecific(T t, Object o, Connection con, PreparedStatement stmt) throws SQLException {
        throw new NotImplementedException();
    }

    public enum Statement {
        SELECT,
        SELECT_ALL,
        INSERT,
        UPDATE,
        DELETE,
        DELETE_SPECIFIC
    }
}
