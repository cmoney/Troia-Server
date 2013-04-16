package com.datascience.utils.storage;

import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;
import java.util.Properties;

/**
 * User: artur
 * Date: 4/10/13
 */
public abstract class DBStorage {

	private static Logger logger = Logger.getLogger(DBStorage.class);
	protected static int VALIDATION_TIMEOUT = 2;
	protected static List<String> TABLES;

	protected String dbUrl;
	protected String dbName;
	protected Connection connection;
	protected Properties connectionProperties;

	public DBStorage(String dbUrl, String driverClass, Properties connectionProperties, String dbName) throws ClassNotFoundException {
		this.dbUrl = dbUrl;
		this.dbName = dbName;
		this.connectionProperties = connectionProperties;
		Class.forName(driverClass);
	}

	public void execute() throws SQLException {
		connectDB();
		dropDatabase();
		createDatabase();
		for (String tableName : TABLES){
			createTable(tableName);
			createIndex(tableName);
		}
		close();
	}

	protected void dropDatabase() throws SQLException {
		logger.info("Deleting database " + dbName);
		executeSQL("DROP DATABASE " + dbName + ";");
		logger.info("Database deleted successfully");
	}

	protected void createDatabase() throws SQLException{
		logger.info("Creating database");
		executeSQL("CREATE DATABASE "+ dbName + " CHARACTER SET utf8 DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT COLLATE utf8_general_ci;");
		executeSQL("USE " + dbName + ";");
		logger.info("Database created successfully");
	}

	protected void createTable(String tableName) throws SQLException{
		executeSQL("CREATE TABLE " + tableName + " (id VARCHAR(100) NOT NULL PRIMARY KEY, value LONGTEXT) DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;");
		logger.info("Table " + tableName + " successfully created");
	}

	protected void createIndex(String tableName) throws  SQLException{
		executeSQL("CREATE INDEX " + tableName + "Index on " + tableName + " (id);");
		logger.info("Index on table " + tableName + " successfully created");
	}

	private void executeSQL(String sql) throws SQLException {
		PreparedStatement stmt = initStatement(sql);
		stmt.executeUpdate();
		cleanup(stmt, null);
	}

	public void connectDB() throws SQLException {
		String dbPath = String.format("%s%s?useUnicode=true&characterEncoding=utf-8", dbUrl, dbName);
		logger.info("Trying to connect with: " + dbPath);
		connection = DriverManager.getConnection(dbPath, connectionProperties);
		logger.info("Connected to " + dbPath);
	}

	public void ensureConnection() throws SQLException {
		if (!connection.isValid(VALIDATION_TIMEOUT)) {
			connectDB();
		}
	}

	public void close() throws SQLException {
		logger.info("closing db connections");
		connection.close();
	}

	protected PreparedStatement initStatement(String command) throws SQLException {
		ensureConnection();
		return connection.prepareCall(command);
	}

	protected void cleanup(Statement sql, ResultSet result) throws SQLException {
		if (sql != null) {
			sql.close();
		}
		if (result != null) {
			result.close();
		}
	}

	public Connection getConnection(){
		return connection;
	}
}
