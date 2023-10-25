package DBManager;

import java.sql.*;
import java.util.Scanner;

import java.util.ArrayList;

class DatabaseManager {

    String server;
    String port;
    String user;
    String pass;
    String dbname;

    DatabaseManager() {
        this.server = "default_server";
        this.port = "default_port";
        this.user = "default_user";
        this.pass = "default_pass";
        this.dbname = "default_dbname";
    }

    DatabaseManager(String server, String port, String user, String pass, String dbname) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.dbname = dbname;

    }

    public Connection connectDatabase() {
        // TO-DO:  Connect to a specific database

        Connection conn = null;

        String connectionUrl = "jdbc:mysql://" + getServer() + ":" + getPort();

        connectionUrl += "/" + getDbname();
        connectionUrl += "?useUnicode=true&characterEncoding=UTF-8";
        connectionUrl += "&user=" + getUser();
        connectionUrl += "&password=" + getPass();

        try {
            conn = DriverManager.getConnection(connectionUrl, getUser(), getPass());
        } catch (Exception e) {

        }

        return conn;
    }

    public void showTables() {
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error al abrir la base de datos");
            return;
        }

        try {

            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet tables = metaData.getTables("Jocs", null, null, null);

            System.out.println(String.format("%-15s %-15s %-15s", tables.getString(1), tables.getString(3), tables.getString(4)));

        } catch (Exception e) {
            System.out.println("Error al obtener las tablas");
        }

    }

    public void insertIntoTable(String table) {
        // TO-DO: add a new record

        // Steps
        // 1. Connect to the DB (if not)
        Connection conn = connectDatabase();
        // 2. Get columns and data types of each column
        // 3. Ask the user for values
        // 4. Compose insert query 
        // Notice that
        // - Data types of each type
        // - Notice about default values
        // - manage errors
        // - Show generated id (if exists)
    }

    public void showDescTable(String table) {
        // TO-DO: Show info about tables, keys and foreign keys
        Connection conn = connectDatabase();

        try {
            DatabaseMetaData metaData = conn.getMetaData();

        } catch (Exception e) {

        }

    }

    public void startShell() {

        // TO-DO: Inicia la shell del mode base de dades
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getDbname() {
        return dbname;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

}
