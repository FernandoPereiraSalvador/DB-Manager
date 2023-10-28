package DBManager;

// Imports per a entrada de dades
import static DBManager.Utilidades.leerTextoC;

/**
 * La clase 'ServerManager' es la clase principal que lanza la aplicación. Contiene el método 'main' que recopila información del servidor e inicia una instancia de 'ConnectionManager' para gestionar las conexiones.
 */
public class ServerManager {

    /**
     * Es la funcion principal de la clase 'ServerManager'
     *
     * @param args
     */
    public static void main(String[] args) {

        String user, pass, ip, port, bd;

        ConnectionManager cm = null;

        do {

            // Solicita la base de datos a utilizar.
            bd = leerTextoC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Base de datos (mysql / mariadb): " + ConsoleColors.BLACK);

            // Solicita la dirección IP del servidor.
            ip = leerTextoC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Server: " + ConsoleColors.RESET);

            // Solicita el puerto del servidor.
            port = leerTextoC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Port: " + ConsoleColors.RESET);

            // Solicita el nombre de usuario para la conexión.
            user = leerTextoC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Username: " + ConsoleColors.RESET);

            // Solicita la contraseña del usuario para la conexión.
            pass = leerTextoC(ConsoleColors.GREEN_BOLD_BRIGHT + "# Password: " + ConsoleColors.BLACK);
            System.out.print(ConsoleColors.RESET);

            if (bd != null && !bd.isEmpty() && ip != null && !ip.isEmpty() && port != null && !port.isEmpty() && user != null && !user.isEmpty() && pass != null) {
                // Inicializa una instancia de ConnectionManager con los datos proporcionados.
                try {
                    cm = new ConnectionManager(ip, port, user, pass, bd);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }

            } else if ("".equals(ip) && "".equals(port) && "".equals(user) && "".equals(pass) && "".equals(bd)) {
                // Inicializa una instancia de ConnectionManager sin datos.
                try {
                    cm = new ConnectionManager();
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            } else {
                System.out.println("Por favor, ingrese todos los datos requeridos.");
            }

        } while (cm == null || cm.connectDBMS() == null);

        // Inicia el shell interactivo.
        cm.startShell();

    }

}
