package DBManager;

import static DBManager.Utilidades.leerEnteroC;
import static DBManager.Utilidades.leerRealC;
import static DBManager.Utilidades.leerTextoC;
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

            while (tables.next()) {
                System.out.println(String.format("%-15s %-15s %-15s", tables.getString(1), tables.getString(3), tables.getString(4)));
            }

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

        // 4. Compose insert query
        StringBuilder query = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(table, null, null, null);

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                if ("int".equalsIgnoreCase(dataType)) {
                    int valor = leerEnteroC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    query.append(columnName).append(", ");
                    values.append(valor).append(", ");
                } else if ("double".equalsIgnoreCase(dataType)) {
                    double valor = leerRealC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    query.append(columnName).append(", ");
                    values.append(valor).append(", ");
                } else if ("String".equalsIgnoreCase(dataType)) {
                    String valor = leerTextoC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    query.append(columnName).append(", ");
                    values.append(valor).append(", ");
                }

                // Eliminar la coma y el espacio final de las cadenas query y values
                query.setLength(query.length() - 2);
                values.setLength(values.length() - 2);
                
                // Hacer la consulta
                query.append(") ").append(values).append(");");
                
                // Insertar
                Statement st = conn.createStatement();
                int affectedRows = st.executeUpdate(query.toString());
                System.out.println(affectedRows + " filas han sido insertadas");
            }
        } catch (Exception e) {

        }
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
