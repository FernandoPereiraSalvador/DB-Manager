package DBManager;

import java.io.BufferedReader;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * Utility class containing various input/output and message display methods.
 *
 * @author Fernando Pereira
 */
public class Utilities {

    private final static BufferedReader consoleInput = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));

    //.........................................................................
    // Display a message on the console
    //.........................................................................
    public static void showMessageC(String message) {
        System.out.println(message);
    }

    //.........................................................................
    // Read an integer from the console
    //.........................................................................
    public static int readIntC(String message) {
        return Integer.parseInt(readTextC(message));
    }

    //.........................................................................
    // Read a double from the console
    //.........................................................................
    public static double readDoubleC(String message) {
        return Double.parseDouble(readTextC(message));
    }

    //.........................................................................
    // Read a text input from the console
    //.........................................................................
    public static String readTextC(String message) {
        try {
            System.out.print(message);
            return consoleInput.readLine();
        } catch (IOException ex) {
            return "";
        }
    }

    //.........................................................................
    // Read a text input using a GUI dialog
    //.........................................................................
    public static String readTextG(String message) {
        String input = JOptionPane.showInputDialog(message);
        if (input == null) {
            return "";
        }
        return input;
    }

    //.........................................................................
    // Read an integer input using a GUI dialog
    //.........................................................................
    public static int readIntG(String message) {
        int value = Integer.parseInt(readTextG(message));
        return value;
    }

    //.........................................................................
    // Read a double input using a GUI dialog
    //.........................................................................
    public static double readDoubleG(String message) {
        return Double.parseDouble(readTextG(message));
    }

    //.........................................................................
    // Display a message using a GUI dialog
    //.........................................................................
    public static void showMessageG(String message) {
        JOptionPane.showMessageDialog(null, message);
    }

    //.........................................................................
    // Copy bytes from source to destination
    //.........................................................................
    public static void copyBytes(byte[] destination, int startDestination, byte[] source, int startSource, int length) {
        for (int i = 0; i <= length - 1; i++) {
            destination[startDestination + i] = source[startSource + i];
        }
    }

    //.........................................................................
    // Concatenate multiple byte arrays
    //.........................................................................
    public static byte[] concatenateBytes(byte[]... list) {
        int totalBytes = 0;
        for (byte[] arr : list) {
            totalBytes += arr.length;
        }

        byte[] result = new byte[totalBytes];
        int startDestination = 0;

        for (byte[] arr : list) {
            copyBytes(result, startDestination, arr, 0, arr.length);
            startDestination += arr.length;
        }

        return result;
    }

    /**
     * Print a title surrounded by asterisks.
     *
     * @param title The title to be printed.
     */
    public static void printMenuTitle(String title) {
        int titleLength = title.length();
        int totalWidth = titleLength + 4; // 2 asterisks on each side of the title

        // Print the top part of the asterisk box
        System.out.print("*".repeat(totalWidth));
        System.out.println();

        // Print the title inside the box
        System.out.println("* " + title + " *");

        // Print the bottom part of the asterisk box
        System.out.print("*".repeat(totalWidth));
        System.out.println();
    }

}
