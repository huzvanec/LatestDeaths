package cz.jeme.programu.latestdeath;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

public class SqliteDB {

	private final String jdbcUrl = "jdbc:sqlite:";
	private final String databaseName = "data.dabatase";
	private String dataFolderPath = null;
	private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Connection conn = null;
	
	
	public SqliteDB(File dataFolder) throws SQLException, ClassNotFoundException {
		if (dataFolder == null) {
			throw new IllegalArgumentException("Param dataFolder can't be null!");
		}
		Class.forName("org.sqlite.JDBC");
		
		dataFolderPath = dataFolder.getAbsolutePath().replace("\\", "/") + "/" + databaseName;
		openConnection();
		if (conn != null) {
			DatabaseMetaData meta = conn.getMetaData();
			Bukkit.getLogger().info("Driver is named: " + meta.getDriverName());
			Bukkit.getLogger().info("Connection to the database has been established.");
			Bukkit.getLogger().info("Path is: " + dataFolderPath);
		}
		createDeathTable();
	}

	private void createDeathTable() throws SQLException {
		PreparedStatement myStatement = null;
		String sql = "CREATE TABLE IF NOT EXISTS Deaths "
				+ "("
				+ "id INTEGER PRIMARY KEY, "
				+ "name TEXT NOT NULL, "
				+ "dimension TEXT NOT NULL, "
				+ "xPos INTEGER NOT NULL, "
				+ "yPos INTEGER NOT NULL, "
				+ "zPos INTEGER NOT NULL, "
				+ "deathCause TEXT NOT NULL, "
				+ "killerEntity TEXT, "
				+ "shooterEntity TEXT, "
				+ "date TEXT NOT NULL"
				+ ");";
		myStatement = conn.prepareStatement(sql);
		myStatement.execute();
	}

	public void addDeath(Death death) throws SQLException {
		PreparedStatement myStatement = null;
		String sql = "INSERT INTO Deaths"
				+ "(name, dimension, xPos, yPos, zPos, deathCause, killerEntity, shooterEntity, date) "
				+ "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";
		
		myStatement = conn.prepareStatement(sql);
		
		myStatement.setString(1, death.getName());
		myStatement.setString(2, death.getDimension());
		myStatement.setInt(3, death.getxPos());
		myStatement.setInt(4, death.getyPos());
		myStatement.setInt(5, death.getzPos());
		myStatement.setString(6, death.getDeathCause());
		myStatement.setString(7, death.getKillerEntity());
		myStatement.setString(8, death.getShooterEntity());
		myStatement.setString(9, dateFormatter.format(death.getDate()));
		
		myStatement.execute();
		myStatement.close();
	}
	
	public List<Death> readDeaths(String name, int count) throws SQLException, ParseException {
		PreparedStatement myStatement = null;
		String sql = "SELECT name, dimension, xPos, yPos, zPos, deathCause, killerEntity, shooterEntity, date "
				+ "FROM Deaths WHERE name = ? ORDER BY date DESC LIMIT ?;";
		
		myStatement = conn.prepareStatement(sql);
		
		myStatement.setString(1, name);
		myStatement.setInt(2, count);
		
		ResultSet result = myStatement.executeQuery();
		
		List<Death> deaths = new ArrayList<Death>();
		while (result.next()) {
			Death death = new Death(
					result.getString(1),
					result.getString(2),
					result.getInt(3),
					result.getInt(4),
					result.getInt(5),
					result.getString(6),
					result.getString(7),
					result.getString(8),
					dateFormatter.parse(result.getString(9)));
			deaths.add(death);
		}
		myStatement.close();
		return deaths;
		
	}
	
//	private void closeConnection() throws SQLException  {
//		conn.close();
//	}
	
	private Connection openConnection() throws SQLException {
		conn = DriverManager.getConnection(jdbcUrl + dataFolderPath);
		return conn;
	}

//	public static void main(String[] args) throws ClassNotFoundException, SQLException {
//		SqliteDB myDB = new SqliteDB(new File(""));
//	}

} 