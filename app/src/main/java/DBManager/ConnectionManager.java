package DBManager;

import java.sql.*;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

class ConnectionManager {

    String server;
    String port;
    String user;
    String pass;

    ConnectionManager() {
        this.server = "default_server";
        this.port = "default_port";
        this.user = "default_user";
        this.pass = "default_pass";

    }

    ConnectionManager(String server, String port, String user, String pass) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
    }

    public Connection connectDBMS() {
        // TO-DO:  Create a connection, 
        //         Returns this or null.
        // Remember error management

        Connection connection = null;

        try {
            String url_connection = "jdbc:mysql://" + server + ":" + port;
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url_connection,user,pass);
            System.out.println("Conexion establecida con exito.");
        } catch (SQLException e) {
            System.err.println("Error durante la conexion: " + e.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ConnectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connection;

    }

    public void showInfo() {
        // TO-DO: show server info
        Connection conn = connectDBMS();

        if(conn != null){
            
            try{
                DatabaseMetaData metaData = conn.getMetaData();
                
                System.out.println("Database product name: " +metaData.getDatabaseProductName());
                System.out.println("Database product version: " + metaData.getDatabaseProductVersion());
                System.out.println("Driver name: " +metaData.getDriverName());
                System.out.println("Driver version: " + metaData.getDriverVersion());
            }catch(SQLException e){
                
            }
          
            
            
        }else{
            System.out.println("Error");
        }
    }

    public void showDatabases() {
        // TO-DO: Show databases in your server
        Connection conn = connectDBMS();

        if(conn != null){
            
            try{
                DatabaseMetaData metaData = conn.getMetaData();
                ResultSet bases_datos = metaData.getCatalogs();
                
                while(bases_datos.next()){
                    System.out.println(bases_datos.getString("TABLE_CAT"));
                }
            }catch(SQLException e){
                
            }
          
            
            
        }else{
            System.out.println("Error");
        }
    }

    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            switch (command) {
                case "sh db":
                case "show databases":
                    this.showDatabases();
                    break;

                case "info":
                    this.showInfo();
                    break;

                case "quit":
                    break;
                default:
                    // Com que no podem utilitzar expressions
                    // regulars en un case (per capturar un "use *")
                    // busquem aquest cas en el default:

                    String[] subcommand = command.split(" ");
                    switch (subcommand[0]) {
                        case "use":
                        // TO-DO:
                        // Creem un objecte de tipus databaseManager per connectar-nos a
                        // la base de dades i iniciar una shell de manipulació de BD..
                            
                            DatabaseManager dbManager = new DatabaseManager();
                            dbManager.connectDatabase();
                            dbManager.startShell();

                        default:
                            System.out.println(ConsoleColors.RED + "Unknown option" + ConsoleColors.RESET);
                            break;
                    }
            }

        } while (!command.equals("quit"));

    }

}
