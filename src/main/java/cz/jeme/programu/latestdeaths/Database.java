package cz.jeme.programu.latestdeaths;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Level;

public class Database {
    public static final String DEATHS_TABLE_NAME = "deaths";
    public static final String NAMES_TABLE_NAME = "player_names";
    private static final String JDBC_PREFIX = "jdbc:mariadb";
    private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    private String deathsTableNamePrefixed = null;
    private String namesTableNamePrefixed = null;

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            LatestDeaths.serverLog("Couldn't find driver \"" + JDBC_DRIVER + "\"", e);
        }
    }

    public Database() {
        reload();
    }

    public void reload() {
        String tablePrefix = LatestDeaths.config.getString("mariadb.table-prefix");
        deathsTableNamePrefixed = tablePrefix + DEATHS_TABLE_NAME;
        namesTableNamePrefixed = tablePrefix + NAMES_TABLE_NAME;

        createTables(tablePrefix);
    }

    private Connection openConnection() throws SQLException {
        String server = LatestDeaths.config.getString("mariadb.server");
        String port = LatestDeaths.config.getString("mariadb.port");
        String databaseName = LatestDeaths.config.getString("mariadb.database-name");
        String user = LatestDeaths.config.getString("mariadb.user");
        String password = LatestDeaths.config.getString("mariadb.password");
        Connection connection;
        DatabaseMetaData meta;
        String driver;
        String jdbcVersion;
        try {
            connection = DriverManager.getConnection(String.format("%s://%s:%s/%s", JDBC_PREFIX, server, port, databaseName), user, password);
            meta = connection.getMetaData();
            driver = meta.getDriverName() + " (" + meta.getDriverVersion() + ")";
            jdbcVersion = "(" + meta.getJDBCMajorVersion() + "." + meta.getJDBCMinorVersion() + ")";
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't open connection with database!", e);
            throw e;
        }

        if (LatestDeaths.config.getBoolean("logging.log-connection")) {
            LatestDeaths.serverLog(Level.INFO, "Opened connection with database (" + user + "@" + server + ":" + port + ")");
        }
        if (LatestDeaths.config.getBoolean("logging.log-driver")) {
            LatestDeaths.serverLog(Level.INFO, "Using " + driver);
        }
        if (LatestDeaths.config.getBoolean("logging.log-jdbc")) {
            LatestDeaths.serverLog(Level.INFO, "Using JDBC " + jdbcVersion);
        }
        return connection;
    }

    private void createTables(String tablePrefix) {
        String namesStatementStr = "CREATE TABLE IF NOT EXISTS " + namesTableNamePrefixed + "("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "uuid UUID UNIQUE NOT NULL, "
                + "name VARCHAR(20) NOT NULL"
                + ");";
        String deathsStatementStr = "CREATE TABLE IF NOT EXISTS " + deathsTableNamePrefixed + "("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "player_uuid UUID NOT NULL, "
                + "dimension VARCHAR(7) NOT NULL, "
                + "x DOUBLE NOT NULL, "
                + "y DOUBLE NOT NULL, "
                + "z DOUBLE NOT NULL, "
                + "cause VARCHAR(25) NOT NULL, "
                + "killer VARCHAR(60), "
                + "killer_player_uuid UUID, "
                + "block_source VARCHAR(60), "
                + "entity_source VARCHAR(60), "
                + "player_source_uuid UUID, "
                + "date DATETIME NOT NULL, "
                + "CONSTRAINT " + "fk_" + tablePrefix + "player_uuid "
                + "FOREIGN KEY (player_uuid) REFERENCES " + namesTableNamePrefixed + " (uuid)"
                + ");";

        PreparedStatement namesStatement;
        PreparedStatement deathsStatement;
        Connection connection = null;
        try {
            connection = openConnection();
            namesStatement = connection.prepareStatement(namesStatementStr);
            namesStatement.execute();
            namesStatement.close();

            deathsStatement = connection.prepareStatement(deathsStatementStr);
            deathsStatement.execute();
            deathsStatement.close();
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't create tables!", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void closeConnection(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
            if (LatestDeaths.config.getBoolean("logging.log-connection")) {
                LatestDeaths.serverLog(Level.INFO, "Closed connection with database");
            }
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't close connection with database!", e);
        }
    }

    public void createDeath(Death death) {
        String statementStr = "INSERT INTO " + deathsTableNamePrefixed
                + "(player_uuid, dimension, x, y, z, cause, killer, killer_player_uuid," +
                " block_source, entity_source, player_source_uuid, date) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        PreparedStatement statement;
        Connection connection = null;
        try {
            connection = openConnection();
            statement = connection.prepareStatement(statementStr);

            statement.setString(1, death.uuid().toString());
            statement.setString(2, death.dimension().name());
            statement.setDouble(3, death.x());
            statement.setDouble(4, death.y());
            statement.setDouble(5, death.z());
            statement.setString(6, death.cause().name());
            statement.setString(7, nullate(death.killer(), Enum::name));
            statement.setString(8, nullate(death.killerPlayerUuid(), UUID::toString));
            statement.setString(9, nullate(death.blockSource(), Enum::name));
            statement.setString(10, nullate(death.entitySource(), Enum::name));
            statement.setString(11, nullate(death.playerSourceUuid(), UUID::toString));
            statement.setTimestamp(12, new Timestamp(death.date().getTime()));

            statement.execute();
            statement.close();
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't create death!", e);
        } finally {
            closeConnection(connection);
        }
    }

    public List<Death> readDeaths(UUID uuid, int count) {
        String statementStr = "SELECT "
                + "player_uuid, dimension, x, y, z, cause, killer, killer_player_uuid," +
                " block_source, entity_source, player_source_uuid, date "
                + "FROM " + deathsTableNamePrefixed + " WHERE player_uuid = ? ORDER BY date DESC LIMIT ?;";

        PreparedStatement statement;
        List<Death> deaths = new ArrayList<>();
        Connection connection = null;
        try {
            connection = openConnection();
            statement = connection.prepareStatement(statementStr);

            statement.setString(1, uuid.toString());
            statement.setInt(2, count);

            ResultSet result = statement.executeQuery();

            while (result.next()) {
                World.Environment dimension = nullate(result.getString(2), name -> Enum.valueOf(World.Environment.class, name));
                DamageCause cause = nullate(result.getString(6), name -> Enum.valueOf(DamageCause.class, name));
                EntityType killer = nullate(result.getString(7), name -> Enum.valueOf(EntityType.class, name));
                Material blockSource = nullate(result.getString(9), name -> Enum.valueOf(Material.class, name));
                EntityType entitySource = nullate(result.getString(10), name -> Enum.valueOf(EntityType.class, name));

                Death death = new Death(
                        nullate(result.getString(1), UUID::fromString),
                        dimension,
                        result.getDouble(3),
                        result.getDouble(4),
                        result.getDouble(5),
                        cause,
                        killer,
                        nullate(result.getString(8), UUID::fromString),
                        blockSource,
                        entitySource,
                        nullate(result.getString(11), UUID::fromString),
                        new Date(result.getTimestamp(12).getTime())
                );
                deaths.add(death);
            }
            statement.close();
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't read deaths!", e);
        } finally {
            closeConnection(connection);
        }
        return deaths;
    }

    public void updateName(Player player) {
        if (createName(player)) return;
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        String statementStr = "UPDATE " + namesTableNamePrefixed + " SET "
                + "name = ? WHERE uuid = ?;";
        PreparedStatement statement;
        Connection connection = null;
        try {
            connection = openConnection();
            statement = connection.prepareStatement(statementStr);
            statement.setString(1, name);
            statement.setString(2, uuid.toString());
            statement.execute();
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't update name for player " + name + " (" + uuid + ")!", e);
        } finally {
            closeConnection(connection);
        }
    }

    private boolean createName(Player player) {
        String name = player.getName();
        UUID uuid = player.getUniqueId();
        String statementStr = "SELECT 1 FROM " + namesTableNamePrefixed
                + " WHERE uuid = ?;";

        PreparedStatement statement;
        Connection connection = null;
        try {
            connection = openConnection();
            statement = connection.prepareStatement(statementStr);
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            if (!result.next()) {
                String createNameStatementStr = "INSERT INTO " + namesTableNamePrefixed
                        + " (uuid, name) VALUES(?, ?);";
                PreparedStatement createNameStatement = connection.prepareStatement(createNameStatementStr);
                createNameStatement.setString(1, uuid.toString());
                createNameStatement.setString(2, name);
                createNameStatement.execute();
                createNameStatement.close();
                return true;
            }
            return false;
        } catch (SQLException e) {
            LatestDeaths.serverLog("Couldn't create name record for player " + name + " (" + uuid + ")!", e);
            return true;
        } finally {
            closeConnection(connection);
        }
    }

    public static <I, R> R nullate(I input, Function<I, R> function) {
        return (input == null) ? null : function.apply(input);
    }
}