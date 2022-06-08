package me.andarguy.authorizer.handler;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableInfo;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import me.andarguy.authorizer.Authorizer;
import me.andarguy.authorizer.model.Account;
import me.andarguy.authorizer.model.AuthorizationRequest;
import me.andarguy.authorizer.settings.Settings;

import java.sql.SQLException;
import java.util.*;

public class DatabaseHandler extends Handler implements Loadable {

    static {
        // requireNonNull prevents the shade plugin from excluding the drivers in minimized jar.
        Objects.requireNonNull(com.mysql.cj.jdbc.Driver.class);
        Objects.requireNonNull(com.mysql.cj.conf.url.SingleConnectionUrl.class);

        Objects.requireNonNull(org.postgresql.Driver.class);
    }

    @Getter
    private Dao<Account, String> playerDao;

    @Getter
    private Dao<AuthorizationRequest, String> requestDao;

    @Getter
    private JdbcPooledConnectionSource connection;

    public DatabaseHandler(Authorizer plugin) {
        super(plugin);
    }

    private boolean connectDatabase() {
        try {
            switch (Settings.DATABASE_STORAGE_TYPE.asString().toLowerCase(Locale.ROOT)) {
                case "mysql": {
                    this.connection = new JdbcPooledConnectionSource(
                            "jdbc:mysql://" + Settings.DATABASE_HOSTNAME.asString() + "/" + Settings.DATABASE_DATABASE.asString() + Settings.DATABASE_CONNECTION_PARAMETERS.asString(), Settings.DATABASE_USER.asString(), Settings.DATABASE_PASSWORD.asString()
                    );
                    break;
                }
                case "postgresql": {
                    this.connection = new JdbcPooledConnectionSource(
                            "jdbc:postgresql://" + Settings.DATABASE_HOSTNAME.asString() + "/" + Settings.DATABASE_DATABASE.asString() + Settings.DATABASE_CONNECTION_PARAMETERS.asString(), Settings.DATABASE_USER.asString(), Settings.DATABASE_PASSWORD.asString()
                    );
                    break;
                }
                default: {
                    Authorizer.getLogger().error("Wrong database type.");
                    return false;
                }
            }
        } catch (SQLException exception) {
            Authorizer.getLogger().error("Cannot connect to database, check configuration!");
            return false;
        }

        try {
            TableUtils.createTableIfNotExists(this.connection, Account.class);
            this.playerDao = DaoManager.createDao(this.connection, Account.class);
            this.repairDatabase(this.playerDao);
        } catch (SQLException e) {
            Authorizer.getLogger().error("An exception occurred while trying to create 'users' database.");
            return false;
        }
        try {
            TableUtils.createTableIfNotExists(this.connection, AuthorizationRequest.class);
            this.requestDao = DaoManager.createDao(this.connection, AuthorizationRequest.class);
            this.repairDatabase(this.requestDao);
        } catch (SQLException e) {
            Authorizer.getLogger().error("An exception occurred while trying to create 'auth_req' database.");
            return false;
        }

        return true;
    }

    public void repairDatabase(Dao<?, ?> dao) throws SQLException {
        TableInfo<?, ?> tableInfo = dao.getTableInfo();

        Set<FieldType> columns = new HashSet<>();
        Collections.addAll(columns, tableInfo.getFieldTypes());

        String table = tableInfo.getTableName(), database = Settings.DATABASE_DATABASE.asString();

        String columnsRequest;
        switch (Settings.DATABASE_STORAGE_TYPE.asString()) {
            case "postgresql": {
                columnsRequest = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG = '" + database + "' AND TABLE_NAME = '" + table + "';";
                break;
            }
            case "mysql": {
                columnsRequest = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = '" + table + "';";
                break;
            }
            default: {
                Authorizer.getLogger().error("Incorrect database type was occurred!");
                throw new SQLException();
            }
        }

        try (GenericRawResults<String[]> queryResult = dao.queryRaw(columnsRequest)) {
            queryResult.forEach(result -> columns.removeIf(column -> column.getColumnName().equalsIgnoreCase(result[0])));

            columns.forEach(column -> {
                try {
                    StringBuilder builder = new StringBuilder("ALTER TABLE " + table + " ADD ");
                    String columnDefinition = column.getColumnDefinition();
                    DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
                    if (columnDefinition == null) {
                        List<String> dummy = List.of();
                        databaseType.appendColumnArg(column.getTableName(), builder, column, dummy, dummy, dummy, dummy);
                    } else {
                        databaseType.appendEscapedEntityName(builder, column.getColumnName());
                        builder.append(" ").append(columnDefinition).append(" ");
                    }

                    dao.executeRawNoArgs(builder.toString());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean reload() {
        try {
            if (this.connection != null) this.connection.close();
            return this.connectDatabase();
        } catch (Exception e) {
            return false;
        }

    }
}
