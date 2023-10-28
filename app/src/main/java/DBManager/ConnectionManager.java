package DBManager;

import static DBManager.Utilidades.tituloMenu;
import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionManager {

    // Atributos de la clase
    String server;
    String port;
    String user;
    String pass;
    String db;

    /**
     * Constructor predeterminado de `ConnectionManager`. Inicializa los atributos de conexión con valores por defecto.
     */
    ConnectionManager() {
        this.server = "default_server";
        this.port = "default_port";
        this.user = "default_user";
        this.pass = "default_pass";
        this.db = "mariadb";

    }

    /**
     * Constructor parametrizado de `ConnectionManager`. Permite establecer los valores de los atributos de conexión al crear una instancia de la clase.
     *
     * @param server Dirección del servidor.
     * @param port Puerto de la base de datos.
     * @param user Nombre de usuario para la conexión.
     * @param pass Contraseña para la conexión.
     */
    ConnectionManager(String server, String port, String user, String pass,String db) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.db = db;
    }

    /**
     * Establece una conexión con el sistema de gestión de bases de datos (DBMS).
     *
     * @return Una instancia de la clase Connection que representa la conexión exitosa con el DBMS. Devuelve null si la conexión no se pudo establecer.
     */
    public Connection connectDBMS() {

        Connection connection = null;

        try {
            // Construye la URL de conexión utilizando la dirección del servidor y el puerto.
            String url_connection = "jdbc:mysql://" + server + ":" + port;

            // Carga el controlador de la base de datos MySQL.
            if("mysql".equals(getDb())){
               Class.forName("com.mysql.cj.jdbc.Driver"); 
            }else if("mariadb".equals(getDb())){
                Class.forName("org.mariadb.jdbc.Driver");
            }else{
                System.out.println("Error: La base de datos especificada no es compatible o no se reconoce. Use 'mysql' o 'mariadb'.");
                return null;
            }         

            // Establece la conexión utilizando la URL, nombre de usuario y contraseña.
            connection = DriverManager.getConnection(url_connection, user, pass);

            // Imprime un mensaje de éxito si la conexión se estableció con éxito.
            System.out.println("Conexion establecida con exito.");
        } catch (SQLException e) {
            System.err.println("Error durante la conexion: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connection;

    }

    /**
     * Muestra información relevante sobre la base de datos y el controlador JDBC utilizados en la conexión. Esta información incluye el nombre y la versión del producto de la base de datos, así como el nombre y la versión del controlador JDBC.
     */
    public void showInfo() {

        // Establece una conexión.
        Connection conn = connectDBMS();

        if (conn != null) {

            try {
                // Obtiene un objeto DatabaseMetaData para acceder a metadatos de la base de datos.
                DatabaseMetaData metaData = conn.getMetaData();

                // Muestra los datos
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
     * Muestra una lista de las bases de datos disponibles en el servidor de la base de datos. Este método se conecta al servidor y utiliza metadatos para recuperar y mostrar los nombres de las bases de datos.
     */
    public void showDatabases() {
        // Establece una conexión con la base de datos.
        Connection conn = connectDBMS();

        if (conn != null) {

            try {
                // Obtiene un objeto DatabaseMetaData para acceder a metadatos de la base de datos.
                DatabaseMetaData metaData = conn.getMetaData();

                // Recupera un conjunto de resultados que contiene los nombres de las bases de datos.
                ResultSet bases_datos = metaData.getCatalogs();

                // Itera a través de los resultados y muestra los nombres de las bases de datos.
                while (bases_datos.next()) {
                    System.out.println(bases_datos.getString("TABLE_CAT"));
                }
            } catch (SQLException e) {
                System.out.println("Error: " + e.getMessage());
            }

        } else {
            System.out.println("Error con la conexión");
        }
    }

    /**
     * Muestra las opciones disponibles en el menú del shell.
     */
    public void menu() {

        tituloMenu("Bases de datos");

        System.out.println("Opciones disponibles:");
        System.out.println("1. Mostrar bases de datos: sh db o show databases");
        System.out.println("2. Mostrar informacion de la base de datos: info");
        System.out.println("3. Cambiar a una base de datos específica: use <database_name>");
        System.out.println("4. Salir: quit");

    }

    /**
     * Inicia un shell interactivo que permite al usuario ingresar comandos relacionados con la base de datos. Este método recoge los comandos del usuario, procesa las solicitudes y realiza acciones correspondientes, como mostrar bases de datos, información del servidor, o cambiar a una base de datos específica.
     */
    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {
            
            menu();

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            switch (command) {
                // Muestra las bases de datos existentes
                case "sh db":
                case "show databases":
                    this.showDatabases();
                    break;

                // Muestra información sobre la base de datos
                case "info":
                    this.showInfo();
                    break;

                // Sale del shell
                case "quit":
                    break;
                default:
                    String[] subcommand = command.split(" ");
                    switch (subcommand[0]) {
                        // Entra en el shell de una base de datos en concreto
                        case "use":
                            DatabaseManager dbManager = null;

                            if (this.server != null && this.port != null && this.user != null && this.pass != null && subcommand[1] != null) {
                                dbManager = new DatabaseManager(this.server, this.port, this.user, this.pass, subcommand[1], this.db);
                            } else {
                                dbManager = new DatabaseManager();
                            }

                            dbManager.connectDatabase();
                            dbManager.startShell();

                        // Opción desconocida
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
