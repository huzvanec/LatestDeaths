package cz.jeme.programu.latestdeaths;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.io.File;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class Database {

    public static final String DATABASE_NAME = "deaths.db";
    public static final String TABLE_NAME = "Deaths";
    public static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";
    private final String databasePath;
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final Connection connection;

    public Database(File dataFolder) throws SQLException, ClassNotFoundException {
        if (dataFolder == null) {
            throw new IllegalArgumentException("dataFolder can't be null!");
        }

        // Load sqlite JDBC driver
        Class.forName("org.sqlite.JDBC");

        databasePath = dataFolder.getAbsolutePath() + File.separator + DATABASE_NAME;
        connection = openConnection();

        if (connection == null) {
            throw new NullPointerException("Connection is null!");
        }

        DatabaseMetaData meta = connection.getMetaData();
        LatestDeaths.serverLog(Level.INFO, "JDBC version: " + meta.getJDBCMajorVersion() + "." + meta.getJDBCMinorVersion());
        LatestDeaths.serverLog(Level.INFO, "Driver: " + meta.getDriverName() + " (" + meta.getDriverVersion() + ")");
        LatestDeaths.serverLog(Level.INFO, "Established connection to " + databasePath);
        createDeathTable();
    }

    private void createDeathTable() throws SQLException {
        PreparedStatement statement;
        String statementStr = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                + "id INTEGER PRIMARY KEY, "
                + "uuid TEXT NOT NULL, "
                + "dimension TEXT NOT NULL, "
                + "x REAL NOT NULL, "
                + "y REAL NOT NULL, "
                + "z REAL NOT NULL, "
                + "cause TEXT NOT NULL, "
                + "killer TEXT, "
                + "killer_player_uuid TEXT, "
                + "block_source TEXT, "
                + "entity_source TEXT, "
                + "player_source_uuid TEXT, "
                + "date TEXT NOT NULL"
                + ");";
        statement = connection.prepareStatement(statementStr);
        statement.execute();
    }

    public void addDeath(Death death) throws SQLException {
        String statementStr = "INSERT INTO Deaths"
                + "(uuid, dimension, x, y, z, cause, killer, killer_player_uuid," +
                " block_source, entity_source, player_source_uuid, date) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        PreparedStatement statement = connection.prepareStatement(statementStr);

        statement.setString(1, death.getUuid().toString());
        statement.setString(2, death.getDimension().name());
        statement.setDouble(3, death.getX());
        statement.setDouble(4, death.getY());
        statement.setDouble(5, death.getZ());
        statement.setString(6, death.getCause().name());
        statement.setString(7, getEnumName(death.getKiller()));
        statement.setString(8, uuidToString(death.getKillerPlayerUuid()));
        statement.setString(9, getEnumName(death.getBlockSource()));
        statement.setString(10, getEnumName(death.getEntitySource()));
        statement.setString(11, uuidToString(death.getPlayerSourceUuid()));
        statement.setString(12, DATE_FORMATTER.format(death.getDate()));

        statement.execute();
        statement.close();
    }

    public List<Death> readDeaths(UUID uuid, int count) throws SQLException, ParseException {
        String statementStr = "SELECT "
                + "uuid, dimension, x, y, z, cause, killer, killer_player_uuid," +
                " block_source, entity_source, player_source_uuid, date "
                + "FROM " + TABLE_NAME + " WHERE uuid = ? ORDER BY date DESC LIMIT ?;";

        PreparedStatement statement = connection.prepareStatement(statementStr);

        statement.setString(1, uuid.toString());
        statement.setInt(2, count);

        ResultSet result = statement.executeQuery();

        List<Death> deaths = new ArrayList<>();
        while (result.next()) {
            World.Environment dimension = enumValueOf(World.Environment.class, result.getString(2));
            DamageCause cause = enumValueOf(DamageCause.class, result.getString(6));
            EntityType killer = enumValueOf(EntityType.class, result.getString(7));
            Material blockSource = enumValueOf(Material.class, result.getString(9));
            EntityType entitySource = enumValueOf(EntityType.class, result.getString(10));

            Death death = new Death(
                    uuidFromString(result.getString(1)),
                    dimension,
                    result.getDouble(3),
                    result.getDouble(4),
                    result.getDouble(5),
                    cause,
                    killer,
                    uuidFromString(result.getString(8)),
                    blockSource,
                    entitySource,
                    uuidFromString(result.getString(11)),
                    DATE_FORMATTER.parse(result.getString(12))
            );
            deaths.add(death);
        }
        statement.close();
        return deaths;
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_JDBC_PREFIX + databasePath);
    }

    public static String getEnumName(Enum<?> enumValue) {
        return (enumValue != null) ? enumValue.name() : null;
    }

    public static UUID uuidFromString(String value) {
        return (value != null) ? UUID.fromString(value) : null;
    }

    public static <T extends Enum<T>> T enumValueOf(Class<T> enumClass, String value) {
        if (value == null) {
            return null;
        }
        return Enum.valueOf(enumClass, value);
    }

    public static String uuidToString(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return uuid.toString();
    }
}