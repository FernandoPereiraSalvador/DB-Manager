package DBManager;

import static DBManager.Utilities.printMenuTitle;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {

    String server;
    String port;
    String user;
    String pass;
    String db;

    /**
     * Default constructor for `ConnectionManager`. Initializes connection
     * attributes with default values.
     */
    ConnectionManager() {
        this.server = "default_server";
        this.port = "default_port";
        this.user = "default_user";
        this.pass = "default_pass";
        this.db = "mariadb";

    }

    /**
     * Parameterized constructor for `ConnectionManager`. Allows you to set the
     * values of the connection attributes when creating an instance of the
     * class.
     *
     * @param server Address of the server.
     * @param port Database port.
     * @param user Username for the connection.
     * @param pass Password for the connection.
     */
    ConnectionManager(String server, String port, String user, String pass, String db) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.db = db;
    }

    /**
      * Establishes a connection with the database management system
      * (DBMS).
      *
      * @return An instance of the Connection class that represents the connection
      * successful with the DBMS. Returns null if the connection could not be established.
      */
    public Connection connectDBMS() {

        Connection connection = null;

        try {
            // Construct the connection URL using the server address and port.
            String url_connection = "jdbc:mysql://" + server + ":" + port;

            // Load the MySQL database driver.
            if ("mysql".equals(getDb())) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if ("mariadb".equals(getDb())) {
                Class.forName("org.mariadb.jdbc.Driver");
            } else {
                System.out.println("Error: La base de datos especificada no es compatible o no se reconoce. Use 'mysql' o 'mariadb'.");
                return null;
            }

            // Establishes the connection using the URL, username and password.
            connection = DriverManager.getConnection(url_connection, user, pass);

            // Print a success message if the connection is established successfully.
            System.out.println("Conexion establecida con exito.");
        } catch (SQLException e) {
            System.err.println("Error durante la conexion: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connection;

    }

    /**
     * Displays relevant information about the database and the controller
      * JDBC used in the connection. This information includes the name and
      * database product version, as well as name and version
      * from the JDBC driver.
     */
    public void showInfo() {

        // Establish a connection.
        Connection conn = connectDBMS();

        if (conn != null) {

            try {
                // Gets a DatabaseMetaData object to access database metadata.
                DatabaseMetaData metaData = conn.getMetaData();

                // Display the data
                System.out.println("Database product name: " + metaData.getDatabaseProductName());
                System.out.println("Database product version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver name: " + metaData.getDriverName());
                System.out.println("Driver version: " + metaData.getDriverVersion());
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } else {
            System.out.println("Error");
        }
    }

    /**
      * Displays a list of databases available on the server
      * database. This method connects to the server and uses metadata
      * to retrieve and display database names.
      */
    public void showDatabases() {
        // Establishes a connection to the database.
        Connection conn = connectDBMS();

        if (conn != null) {

            try {
                // Gets a DatabaseMetaData object to access database metadata.
                DatabaseMetaData metaData = conn.getMetaData();

                // Retrieves a result set containing the database names.
                ResultSet bases_datos = metaData.getCatalogs();

                // Iterates through the results and displays the database names.
                while (bases_datos.next()) {
                    System.out.println(bases_datos.getString("TABLE_CAT"));
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } else {
            System.out.println("Error con la conexi�n");
        }
    }

    /**
     * Shows the options available in the shell menu.
     */
    public void menu() {

        System.out.println();

        printMenuTitle("Databases");

        System.out.println("Available options:");
        System.out.println("1. Show databases: sh db or show databases");
        System.out.println("2. Show database information: info");
        System.out.println("3. Switch to a specific database: use <database_name>");
        System.out.println("4. Exit: quit");

    }

    /**
     * Starts an interactive shell that allows the user to enter commands
      * database related. This method collects the commands of the
      * user, processes requests and performs corresponding actions,
      * how to display databases, server information, or switch to one
      * specific database.
     */
    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {

            menu();

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            switch (command) {
                // Show existing databases
                case "sh db":
                case "show databases":
                    this.showDatabases();
                    break;

                // Shows information about the database
                case "info":
                    this.showInfo();
                    break;

                // Exit the shell
                case "quit":
                    break;
                default:
                    String[] subcommand = command.split(" ");
                    switch (subcommand[0]) {
                        // Enter the shell of a specific database
                        case "use":
                            DatabaseManager dbManager = null;

                            if (this.server != null && this.port != null && this.user != null && this.pass != null && subcommand[1] != null) {
                                dbManager = new DatabaseManager(this.server, this.port, this.user, this.pass, subcommand[1], this.db);
                            } else {
                                dbManager = new DatabaseManager();
                            }

                            dbManager.connectDatabase();
                            dbManager.startShell();

                        // Unknown option
                        default:
                            System.out.println(ConsoleColors.RED + "Unknown option" + ConsoleColors.RESET);
                            break;
                    }
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

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

}
