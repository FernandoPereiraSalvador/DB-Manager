package DBManager;

import static DBManager.Utilities.readTextC;

/**
 * The class 'ServerManager' is the main class that launches the application. Contiene el método 'main' that receives information from the server and initiates a instance of 'ConnectionManager' to manage the connections.
 */
public class ServerManager {

    /**
     * It is the main function of the class 'ServerManager'
     *
     * @param args
     */
    public static void main(String[] args) {

        String user, pass, ip, port, bd;

        ConnectionManager cm = null;

        do {

            // Request the database to use.
            bd = readTextC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Base de datos (mysql / mariadb): " + ConsoleColors.BLACK);

            // Requests the IP address of the server.
            ip = readTextC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Server: " + ConsoleColors.RESET);

            // Request the server port.
            port = readTextC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Port: " + ConsoleColors.RESET);

            // Requests the username for the connection.
            user = readTextC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Username: " + ConsoleColors.RESET);

            // Requests the user's password for the connection.
            pass = readTextC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Password: " + ConsoleColors.BLACK);
            System.out.print(ConsoleColors.RESET);

            if (bd != null && !bd.isEmpty() && ip != null && !ip.isEmpty() && port != null && !port.isEmpty() && user != null && !user.isEmpty() && pass != null) {
                // Initialize a Connection Manager instance with the provided data.
                try {
                    cm = new ConnectionManager(ip, port, user, pass, bd);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if ("".equals(ip) && "".equals(port) && "".equals(user) && "".equals(pass) && "".equals(bd)) {
                // Initializes a ConnectionManager instance with no data.
                try {
                    cm = new ConnectionManager();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Por favor, ingrese todos los datos requeridos.");
            }

        } while (cm == null || cm.connectDBMS() == null);

        // Start the interactive shell.
        cm.startShell();

    }

}
