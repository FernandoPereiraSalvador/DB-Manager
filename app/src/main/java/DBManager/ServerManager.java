package DBManager;

// Imports per a entrada de dades
import java.util.Scanner;

/**
 * La clase 'ServerManager' es la clase principal que lanza la aplicación. Contiene el método 'main' que recopila información del servidor e inicia una instancia de 'ConnectionManager' para gestionar las conexiones.
 */
public class ServerManager {

    /**
     * Es la funcion principal de la clase 'ServerManager'
     * @param args 
     */
    public static void main(String[] args) {

        ConnectionManager cm;

        Scanner keyboard = new Scanner(System.in);

        String user, pass, ip, port;

        do {
            
            // Solicita la dirección IP del servidor.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Server: " + ConsoleColors.RESET);
            ip = keyboard.nextLine();
            
            // Solicita el puerto del servidor.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Port: " + ConsoleColors.RESET);
            port = keyboard.nextLine();
            
            // Solicita el nombre de usuario para la conexión.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Username: " + ConsoleColors.RESET);
            user = keyboard.nextLine();
            
            // Solicita la contraseña del usuario para la conexión.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Password: " + ConsoleColors.BLACK);
            pass = keyboard.nextLine();
            System.out.print(ConsoleColors.RESET);

            if (ip != null && port != null && user != null && pass != null) {
                // Inicializa una instancia de ConnectionManager con los datos proporcionados.
                cm = new ConnectionManager(ip, port, user, pass);
            } else {
                // Inicializa una instancia de ConnectionManager sin datos.
                cm = new ConnectionManager();
            }

        } while (cm.connectDBMS() == null);
        
        // Inicia el shell interactivo.
        cm.startShell();

    }

}
