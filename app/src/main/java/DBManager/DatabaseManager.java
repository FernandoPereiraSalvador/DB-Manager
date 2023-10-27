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
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(connectionUrl, getUser(), getPass());
        } catch (Exception e) {
            System.out.println("Error al abrir la base de datos: " + e);
        }

        return conn;
    }

    public void showTables() {
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error en la conexion");
            return;
        }

        try {

            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet tables = metaData.getTables(getDbname(), null, null, null);

            while (tables.next()) {
                System.out.println(String.format("%-15s %-15s %-15s", tables.getString(1), tables.getString(3), tables.getString(4)));
            }

        } catch (Exception e) {
            System.out.println("Error al obtener las tablas: " + e);
        }

    }

    public void insertIntoTable(String table) {
        // TO-DO: agregar un nuevo registro

        // Pasos
        // 1. Conectarse a la base de datos (si no est� conectada)
        Connection conn = connectDatabase();
        // 2. Obtener las columnas y tipos de datos de cada columna

        // 4. Componer la consulta de inserci�n
        StringBuilder query = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(getDbname(), null, table, null);

            // Variables para controlar si se han agregado columnas y valores
            boolean hasColumns = false;

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                if ("int".equalsIgnoreCase(dataType)) {
                    int valor = leerEnteroC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append(valor);
                    hasColumns = true;
                } else if ("double".equalsIgnoreCase(dataType)) {
                    double valor = leerRealC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append(valor);
                    hasColumns = true;
                } else if ("String".equalsIgnoreCase(dataType) || "varchar".equalsIgnoreCase(dataType)) {
                    String valor = leerTextoC("Introduzca el valor para " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append("'" + valor + "'");
                    hasColumns = true;
                }
            }

            if (hasColumns) {
                System.out.println(query);
                query.append(")").append(values).append(");");

                System.out.println(query);

                // Insertar
                Statement st = conn.createStatement();
                System.out.println(st.executeUpdate(query.toString()) + " filas han sido insertadas");
            } else {
                System.out.println("No se han encontrado columnas para insertar.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
        // 3. Solicitar valores al usuario
        // 4. Componer la consulta de inserci�n 
        // Ten en cuenta
        // - Tipos de datos de cada columna
        // - Valores predeterminados
        // - manejar errores
        // - Mostrar el ID generado (si existe)
    }

    public void showDescTable(String table) {

        System.out.println("Descripcion de la tabla: " + table);

        // TO-DO: Show info about tables, keys and foreign keys
        Connection conn = connectDatabase();

        try {
            DatabaseMetaData metaData = conn.getMetaData();

            ResultSet tableInfo = metaData.getTables(getDbname(), null, table, null);

            while (tableInfo.next()) {
                System.out.println(tableInfo.getString("TABLE_NAME"));
                System.out.println(tableInfo.getString("TABLE_TYPE"));
            }

            ResultSet primaryKeysInfo = metaData.getPrimaryKeys(getDbname(), null, table);

            while (primaryKeysInfo.next()) {
                System.out.println(primaryKeysInfo.getString("COLUMN_NAME"));
                System.out.println(primaryKeysInfo.getString("PK_NAME"));
            }

            ResultSet foreignKeysInfo = metaData.getImportedKeys(getDbname(), null, table);

            while (foreignKeysInfo.next()) {

                System.out.println(foreignKeysInfo.getString("FKTABLE_NAME"));
                System.out.println(foreignKeysInfo.getString("FKCOLUMN_NAME"));
                System.out.println(foreignKeysInfo.getString("PKTABLE_NAME"));
                System.out.println(foreignKeysInfo.getString("PKCOLUMN_NAME"));
            }

        } catch (Exception e) {
            System.out.println("Error");
        }

    }

    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            if (command.startsWith("desc table") || (command.startsWith("description table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.showDescTable(tableName);
                }
            } else if (command.startsWith("insert table") || (command.startsWith("ins table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.insertIntoTable(tableName);
                }
            } else if ("show tables".equals(command) || "sh tables".equals(command)) {
                this.showTables();
            } else {
                System.out.println("Comando incorrecto");
            }

        } while (!command.equals("quit"));
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
