package DBManager;

// Imports per a entrada de dades
import java.util.Scanner;

/**
 * La clase 'ServerManager' es la clase principal que lanza la aplicaci�n. Contiene el m�todo 'main' que recopila informaci�n del servidor e inicia una instancia de 'ConnectionManager' para gestionar las conexiones.
 */
public class ServerManager {

    /**
     * Es la funcion principal de la clase 'ServerManager'
     * @param args 
     */
    public static void main(String[] args) {

        ConnectionManager cm;

        Scanner keyboard = new Scanner(System.in);

        String user, pass, ip, port, bd;

        do {
            
            // Solicita la base de datos a utilizar.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Base de datos (mysql / mariadb): " + ConsoleColors.BLACK);
            bd = keyboard.nextLine();
            
            // Solicita la direcci�n IP del servidor.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Server: " + ConsoleColors.RESET);
            ip = keyboard.nextLine();
            
            // Solicita el puerto del servidor.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Port: " + ConsoleColors.RESET);
            port = keyboard.nextLine();
            
            // Solicita el nombre de usuario para la conexi�n.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Username: " + ConsoleColors.RESET);
            user = keyboard.nextLine();
            
            // Solicita la contrase�a del usuario para la conexi�n.
            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# Password: " + ConsoleColors.BLACK);
            pass = keyboard.nextLine();
            System.out.print(ConsoleColors.RESET);
            
            cm = null;
            
            if (ip != null && port != null && user != null && pass != null) {
                // Inicializa una instancia de ConnectionManager con los datos proporcionados.
                try{
                    cm = new ConnectionManager(ip, port, user, pass,bd);
                }catch(Exception e){
                    System.out.println("Error: " + e.getMessage());
                }
                
            } else if (ip == null && port == null && user == null && pass == null){
                // Inicializa una instancia de ConnectionManager sin datos.
                try{
                    cm = new ConnectionManager();
                }catch(Exception e){
                    System.out.println("Error: " + e.getMessage());
                }
            }else{
                System.out.println("Por favor, ingrese todos los datos requeridos.");
            }

        } while (cm.connectDBMS() == null);
        
        // Inicia el shell interactivo.
        cm.startShell();

    }

}
