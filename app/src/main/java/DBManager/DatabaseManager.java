package DBManager;

import static DBManager.Utilities.readTextC;
import static DBManager.Utilities.printMenuTitle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.NodeList;
import static DBManager.Utilities.readDoubleC;
import static DBManager.Utilities.readIntC;

public class DatabaseManager {

    String server;
    String port;
    String user;
    String pass;
    String dbname;
    String db;

    DatabaseManager() {
        this.server = "default_server";
        this.port = "default_port";
        this.user = "default_user";
        this.pass = "default_pass";
        this.dbname = "default_dbname";
        this.db = "mariadb";
    }

    DatabaseManager(String server, String port, String user, String pass, String dbname, String db) {
        this.server = server;
        this.port = port;
        this.user = user;
        this.pass = pass;
        this.dbname = dbname;
        this.db = db;

    }

    /**
     * Connects to a MySQL database using the provided connection details.
     *
     * @return A Connection object if the connection is successful, or null if
     * there is an error.
     */
    public Connection connectDatabase() {

        Connection conn = null;

        // Build the connection URL with the server, port, database name, and other configurations.
        String connectionUrl = "jdbc:mysql://" + getServer() + ":" + getPort();
        connectionUrl += "/" + getDbname();
        connectionUrl += "?useUnicode=true&characterEncoding=UTF-8";
        connectionUrl += "&user=" + getUser();
        connectionUrl += "&password=" + getPass();

        try {
            // Load the MySQL JDBC driver
            if ("mysql".equals(getDb())) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if ("mariadb".equals(getDb())) {
                Class.forName("org.mariadb.jdbc.Driver");
            } else {
                System.out.println("Error: The specified database is not supported or not recognized. Use 'mysql' or 'mariadb'.");
                return null;
            }

            // Attempt to establish a connection to the database
            conn = DriverManager.getConnection(connectionUrl, getUser(), getPass());
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error opening the database: " + e.getMessage());
        }

        return conn;
    }

    /**
     * Displays the list of tables in the current database.
     */
    public void showTables() {
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Get the metadata of the database
            DatabaseMetaData metaData = conn.getMetaData();

            // Get the list of tables in the current database
            ResultSet tables = metaData.getTables(getDbname(), null, null, null);

            // Iterate through the tables and display relevant information
            while (tables.next()) {
                System.out.println(String.format("%-15s %-15s %-15s", tables.getString(1), tables.getString(3), tables.getString(4)));
            }

        } catch (SQLException e) {
            System.out.println("Error getting tables: " + e.getMessage());
        }

    }

    /**
     * Inserts a new record into the specified table.
     *
     * @param table The name of the table where the record will be inserted.
     */
    public void insertIntoTable(String table) {

        // Establish a connection to the database
        Connection conn = connectDatabase();

        // Build the initial part of the SQL query
        StringBuilder query = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        try {
            // Get information about the columns of the table
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(getDbname(), null, table, null);

            // Variable to control if columns have been found for insertion
            boolean hasColumns = false;

            // Iterate through the columns of the table
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                // Request values from the user, taking into account the data type
                if ("int".equalsIgnoreCase(dataType)) {
                    int value = readIntC("Enter the value for " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append(value);
                    hasColumns = true;
                } else if ("double".equalsIgnoreCase(dataType)) {
                    double value = readDoubleC("Enter the value for " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append(value);
                    hasColumns = true;
                } else if ("String".equalsIgnoreCase(dataType) || "varchar".equalsIgnoreCase(dataType)) {
                    String value = readTextC("Enter the value for " + columnName + " ( " + dataType + " ):");
                    if (hasColumns) {
                        query.append(", ");
                        values.append(", ");
                    }
                    query.append(columnName);
                    values.append("'").append(value).append("'");
                    hasColumns = true;
                }
            }

            // Compose the insertion query
            if (hasColumns) {
                System.out.println(query);
                query.append(")").append(values).append(");");

                System.out.println(query);

                // Insert the new record into the table
                Statement st = conn.createStatement();
                System.out.println(st.executeUpdate(query.toString()) + " rows have been inserted");
            } else {
                System.out.println("No columns found for insertion.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Imports and executes SQL statements from a specified file.
     *
     * @param path The path of the SQL file to import and execute.
     */
    public void importSQLFile(String path) {

        // Establish a connection to the database
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Read the content of the SQL file and split it into individual statements
            List<String> sqlStatements = new ArrayList<>();
            String sqlContent = new String(Files.readAllBytes(Paths.get(path)));
            String[] statements = sqlContent.split(";");

            // Filter and add non-empty statements to the list
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    sqlStatements.add(statement);
                }
            }

            // Create a Statement object and add the statements
            Statement statement = conn.createStatement();
            for (String sql : sqlStatements) {
                statement.addBatch(sql);
            }

            // Execute the batch of SQL statements
            int[] result = statement.executeBatch();

            System.out.println("Executed " + result.length + " SQL statements from the file.");

        } catch (IOException | SQLException e) {
            System.out.println("Error importing the SQL file: " + e.getMessage());
        }
    }

    /**
     * Displays detailed information about the specified table, including column
     * details, primary keys, and foreign keys.
     *
     * @param table The name of the table for which the description will be
     * shown.
     */
    public void showDescTable(String table) {
        System.out.println("Table description: " + table);

        // Establish a connection to the database
        Connection conn = connectDatabase();

        try {
            // Get metadata from the database
            DatabaseMetaData metaData = conn.getMetaData();

            // Get information about the table, including the name and type of table
            ResultSet tableInfo = metaData.getTables(getDbname(), null, table, null);

            while (tableInfo.next()) {
                System.out.println("Table name: " + tableInfo.getString("TABLE_NAME"));
                System.out.println("Table type: " + tableInfo.getString("TABLE_TYPE"));
            }

            // Get information about the columns of the table
            ResultSet columnsInfo = metaData.getColumns(getDbname(), null, table, null);

            System.out.println("Columns:");
            while (columnsInfo.next()) {
                String columnName = columnsInfo.getString("COLUMN_NAME");
                String columnType = columnsInfo.getString("TYPE_NAME");
                System.out.println("Column name: " + columnName + " ( " + columnType + " ) ");
            }

            // Get information about the primary keys of the table
            ResultSet primaryKeysInfo = metaData.getPrimaryKeys(getDbname(), null, table);

            System.out.println("Primary keys:");
            while (primaryKeysInfo.next()) {
                System.out.println("Column: " + primaryKeysInfo.getString("COLUMN_NAME") + " ( " + primaryKeysInfo.getString("PK_NAME") + " ) ");
            }

            // Get information about foreign keys related to the table
            ResultSet foreignKeysInfo = metaData.getImportedKeys(getDbname(), null, table);

            System.out.println("Foreign keys:");
            while (foreignKeysInfo.next()) {
                System.out.println("Foreign table name: " + foreignKeysInfo.getString("FKTABLE_NAME"));
                System.out.println("Foreign column: " + foreignKeysInfo.getString("FKCOLUMN_NAME"));
                System.out.println("Primary table: " + foreignKeysInfo.getString("PKTABLE_NAME"));
                System.out.println("Primary column: " + foreignKeysInfo.getString("PKCOLUMN_NAME"));
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays the content of a specified table in the database.
     *
     * @param table_name The name of the table whose content will be displayed.
     */
    public void showContent(String table_name) {
        // Establish a connection to the database
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error connecting to the database");
            return;
        }

        try {
            // Create an SQL statement
            Statement statement = conn.createStatement();

            // Build an SQL query to select all records from the table
            String query = "SELECT * FROM " + table_name;

            // Execute the SQL query and get the result
            ResultSet result = statement.executeQuery(query);

            // Get information about the result, such as column metadata
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (result.next()) {
                // Iterate through the records and display the values of the columns
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    String columnValue = result.getString(i);
                    System.out.print(columnName + ": " + columnValue);
                    if (i < columnCount) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
            }
            // Close the result after use
            result.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exports data from a database table to an SQL file at the specified
     * location.
     *
     * @param table The name of the table whose data will be exported.
     * @param path The destination file path where the data will be exported in
     * SQL format.
     */
    public void exportSql(String table, String path) {
        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create an SQL statement
            Statement statement = conn.createStatement();

            // Build an SQL query to select all records from the table
            String query = "SELECT * FROM " + table;

            // Execute the SQL query and get the result
            ResultSet result = statement.executeQuery(query);

            // Get metadata of the columns from the result
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            // Create a StringBuilder to store the exported SQL data
            StringBuilder sqlData = new StringBuilder();

            while (result.next()) {
                // Compose the SQL INSERT INTO statement for each record
                sqlData.append("INSERT INTO ").append(table).append(" (");

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metadata.getColumnName(i);
                    sqlData.append(columnName);
                    if (i < columnCount) {
                        sqlData.append(", ");
                    }
                }

                sqlData.append(") VALUES (");

                for (int i = 1; i <= columnCount; i++) {
                    String columnValue = result.getString(i);
                    sqlData.append("'").append(columnValue).append("'");
                    if (i < columnCount) {
                        sqlData.append(", ");
                    }
                }

                sqlData.append(");").append(System.lineSeparator());

            }

            // Write the exported SQL data to the file at the specified location
            Files.write(Paths.get(path), sqlData.toString().getBytes());

            System.out.println("Data from table " + table + " successfully exported to " + path);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exports data from a database table to a CSV file at the specified
     * location.
     *
     * @param table The name of the table whose data will be exported.
     * @param path The destination file path where the data will be exported in
     * CSV format.
     */
    public void exportCsv(String table, String path) {
        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create an SQL statement
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + table);

            // Create a FileWriter to write to the CSV file
            FileWriter writer = new FileWriter(path);

            // Get metadata of the columns from the result
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            // Write column headers to the CSV file
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metadata.getColumnName(i);
                writer.append(columnName);
                if (i < columnCount) {
                    writer.append(",");
                }
            }

            writer.append(System.lineSeparator());

            // Write data to the CSV file
            while (result.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnValue = result.getString(i);
                    writer.append(columnValue);

                    if (i < columnCount) {
                        writer.append(",");
                    }
                }
                writer.append(System.lineSeparator());
            }
            // Close the file and display a success message
            writer.close();
            System.out.println("Data from table " + table + " successfully exported as CSV to " + path);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exports data from a database table to a JSON file at the specified
     * location.
     *
     * @param table The name of the table whose data will be exported.
     * @param path The destination file path where the data will be exported in
     * JSON format.
     */
    public void exportJson(String table, String path) {

        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create a list to store data in JSON format
            List<String> data = new ArrayList<>();

            // Create a FileWriter to write to the JSON file
            FileWriter writer = new FileWriter(path);

            writer.write("[");

            // Create an SQL statement
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + table);

            // Get metadata of the columns from the result
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (result.next()) {
                // Create a StringBuilder to build JSON objects for each row
                StringBuilder row_data = new StringBuilder("{");
                for (int i = 1; i <= columnCount; i++) {
                    String column_name = metadata.getColumnName(i);
                    String column_value = result.getString(i);
                    if (i > 1) {
                        row_data.append(",");
                    }
                    row_data.append("\"").append(column_name).append("\":\"").append(column_value).append("\"");
                }
                row_data.append("}");
                data.add(row_data.toString());
            }

            // Write each JSON object on a separate line in the file
            for (int i = 0; i < data.size(); i++) {
                writer.write(data.get(i));
                if (i < data.size() - 1) {
                    writer.write(",");
                }
            }

            writer.write("]");

            // Close the file and display a success message
            writer.close();
            System.out.println("Data from table " + table + " successfully exported as JSON to " + path);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e);
        }

    }

    /**
     * Exports data from a database table to an XML file at the specified
     * location.
     *
     * @param table The name of the table whose data will be exported.
     * @param path The destination file path where the data will be exported in
     * XML format.
     */
    public void exportXml(String table, String path) {

        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create a DocumentBuilderFactory object to work with XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Create the root element of the XML document
            Element rootElement = doc.createElement(table);
            doc.appendChild(rootElement);

            // Create an SQL statement to retrieve data from the table
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + table);

            // Get metadata of the columns from the result
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (result.next()) {
                // Create an XML element for each data row
                Element rowElement = doc.createElement("row");
                rootElement.appendChild(rowElement);

                for (int i = 1; i <= columnCount; i++) {
                    // Get the column name and cell value
                    String columnName = metadata.getColumnName(i);
                    String columnValue = result.getString(i);

                    // Create an XML element for each name-value pair and add it to the row
                    Element columnElement = doc.createElement(columnName);
                    columnElement.appendChild(doc.createTextNode(columnValue));
                    rowElement.appendChild(columnElement);
                }
            }

            // Create a FileWriter to write to the XML file
            FileWriter writer = new FileWriter(path, StandardCharsets.UTF_8);

            // Configure a Transformer to write the XML document to the file
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult resultFile = new StreamResult(writer);
            transformer.transform(source, resultFile);

            // Close the file and display a success message
            writer.close();
            System.out.println("Data from table " + table + " successfully exported as XML to " + path);

        } catch (IOException | SQLException | ParserConfigurationException | TransformerException | DOMException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Imports data from a CSV file to a database table.
     *
     * @param table The name of the table where the data will be imported.
     * @param path The file path of the CSV file containing the data to be
     * imported.
     */
    public void importCsv(String table, String path) {
        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create an SQL statement for inserting data
            String insertQuery = "INSERT INTO " + table + " VALUES (";
            for (int i = 0; i < getColumnCount(table); i++) {
                insertQuery += "?,";
            }
            insertQuery = insertQuery.substring(0, insertQuery.length() - 1); // Remove the last comma
            insertQuery += ")";
            PreparedStatement preparedStatement = conn.prepareStatement(insertQuery);

            // Read the CSV file
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;
            boolean skipFirstLine = true; // Variable to skip the first line
            while ((line = reader.readLine()) != null) {
                if (skipFirstLine) {
                    skipFirstLine = false;
                    continue; // Skip the first line
                }
                String[] data = line.split(",");
                for (int i = 0; i < data.length; i++) {
                    preparedStatement.setString(i + 1, data[i]);
                }
                preparedStatement.executeUpdate();
            }

            // Close the file and the database connection
            reader.close();
            preparedStatement.close();
            conn.close();

            System.out.println("Data from CSV file successfully imported to table " + table);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Imports JSON data into a database table.
     *
     * @param table The name of the table where the data will be imported.
     * @param path The file path of the JSON file containing the data to be
     * imported.
     */
    public void importJson(String table, String path) {
        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Read the JSON file
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder jsonStringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                jsonStringBuilder.append(line);
            }
            bufferedReader.close();

            // Parse the JSON
            JSONArray jsonArray = new JSONArray(jsonStringBuilder.toString());

            // Prepare the SQL statement for insertion
            String insertQuery = "INSERT INTO " + table + " (";
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String columnName = keys.next();
                    insertQuery += columnName;
                    if (keys.hasNext()) {
                        insertQuery += ",";
                    }
                }
                if (i == 0) {
                    insertQuery += ") VALUES (";
                } else if (i < jsonArray.length() - 1) {
                    insertQuery += "), (";
                } else {
                    insertQuery += ");";
                }
            }

            // Execute the insertion
            Statement statement = conn.createStatement();
            statement.executeUpdate(insertQuery);

            // Display a success message
            System.out.println("Data successfully imported from " + path + " to table " + table);
        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Gets the column count of a given table.
     *
     * @param table The name of the table.
     * @return The number of columns in the table.
     * @throws SQLException If an SQL exception occurs.
     */
    private int getColumnCount(String table) throws SQLException {
        // This function retrieves the number of columns in the table
        Connection conn = connectDatabase();
        String query = "SELECT * FROM " + table;
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        return preparedStatement.getMetaData().getColumnCount();
    }

    /**
     * Imports XML data into a database table.
     *
     * @param table The name of the table where the data will be imported.
     * @param path The file path of the XML file containing the data to be
     * imported.
     */
    public void importXml(String table, String path) {
        // Connect to the database
        Connection conn = connectDatabase();

        // Check the database connection
        if (conn == null) {
            System.out.println("Error in connection");
            return;
        }

        try {
            // Create a DocumentBuilderFactory object to work with XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // Parse the XML file
            File xmlFile = new File(path);
            Document doc = dBuilder.parse(xmlFile);

            // Get the root of the XML document
            Element rootElement = doc.getDocumentElement();

            // Get the list of "row" elements representing data rows
            NodeList rowElements = rootElement.getElementsByTagName("row");

            // Create an SQL statement for data insertion
            Statement statement = conn.createStatement();

            for (int i = 0; i < rowElements.getLength(); i++) {
                Element rowElement = (Element) rowElements.item(i);

                // Create an SQL statement for data insertion
                String insertQuery = "INSERT INTO " + table + " (";
                NodeList columnElements = rowElement.getChildNodes();
                for (int j = 0; j < columnElements.getLength(); j++) {
                    Element columnElement = (Element) columnElements.item(j);
                    String columnName = columnElement.getNodeName();
                    insertQuery += columnName;
                    if (j < columnElements.getLength() - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ") VALUES (";

                for (int j = 0; j < columnElements.getLength(); j++) {
                    Element columnElement = (Element) columnElements.item(j);
                    String columnValue = columnElement.getTextContent();
                    insertQuery += "'" + columnValue + "'";
                    if (j < columnElements.getLength() - 1) {
                        insertQuery += ",";
                    }
                }
                insertQuery += ");";

                // Execute the data insertion
                statement.executeUpdate(insertQuery);
            }

            // Display a success message
            System.out.println("Data successfully imported from " + path + " to table " + table);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Prints the menu options for the shell.
     */
    public void menu() {

        System.out.println();

        printMenuTitle(getDbname());

        System.out.println("Available options:");
        System.out.println("1. Describe a table: desc table <table_name>");
        System.out.println("2. Insert into a table: insert table <table_name>");
        System.out.println("3. Show tables: show tables");
        System.out.println("4. Import an SQL file: import <file_path.sql>");
        System.out.println("5. Show content of a table: show <table_name>");
        System.out.println("6. Export content to SQL: export sql <table_name> <output_file.sql>");
        System.out.println("7. Export content to CSV: export csv <table_name> <output_file.csv>");
        System.out.println("8. Export content to JSON: export json <table_name> <output_file.json>");
        System.out.println("9. Export content to XML: export xml <table_name> <output_file.xml>");
        System.out.println("10. Exit: quit");
    }

    /**
     * Initiates an interactive shell to execute commands related to the
     * database. Available commands include table description, record insertion,
     * table display, and data import/export in various formats.
     */
    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {

            menu();

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            // Describe table
            if (command.startsWith("desc table") || (command.startsWith("description table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.showDescTable(tableName);
                }
            } // Manually insert into a table
            else if (command.startsWith("insert table") || (command.startsWith("ins table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.insertIntoTable(tableName);
                }
            } // Show tables
            else if ("show tables".equals(command) || "sh tables".equals(command)) {
                this.showTables();
            } else if (command.startsWith("import csv")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.importCsv(subcommand[2], subcommand[3]);
                }
            } else if (command.startsWith("import xml")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.importXml(subcommand[2], subcommand[3]);
                }
            } else if (command.startsWith("import json")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.importJson(subcommand[2], subcommand[3]);
                }
            } // Import an SQL file and execute it
            else if (command.startsWith("import")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println("Incorrect format");
                } else {
                    String path = subcommand[1];
                    if (path.toLowerCase().endsWith(".sql")) {
                        this.importSQLFile(path);
                    } else {
                        System.out.println("The provided file is not an SQL file");
                    }
                }

            } // Show the content of a table
            else if (command.startsWith("show")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println("Incorrect format");
                } else {
                    this.showContent(subcommand[1]);
                }
            } // Export the content of a table to an SQL file
            else if (command.startsWith("export sql")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.exportSql(subcommand[2], subcommand[3]);
                }
            } // Export the content of a table to a CSV file
            else if (command.startsWith("export csv")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.exportCsv(subcommand[2], subcommand[3]);
                }
            } // Export the content of a table to a JSON file
            else if (command.startsWith("export json")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.exportJson(subcommand[2], subcommand[3]);
                }
            } // Export the content of a table to an XML file
            else if (command.startsWith("export xml")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Incorrect format");
                } else {
                    this.exportXml(subcommand[2], subcommand[3]);
                }
            } // Exit the database shell
            else if (command.startsWith("quit")) {

            } // Incorrect command entered
            else {
                System.out.println("Incorrect command");
            }

        } while (!command.equals("quit"));
    }

    /**
     * Gets the name of the database server.
     *
     * @return The server name.
     */
    public String getServer() {
        return server;
    }

    /**
     * Sets the name of the database server.
     *
     * @param server The server name to be set.
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Gets the port number for the database connection.
     *
     * @return The port number.
     */
    public String getPort() {
        return port;
    }

    /**
     * Sets the port number for the database connection.
     *
     * @param port The port number to be set.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Gets the username for the database connection.
     *
     * @return The username.
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the username for the database connection.
     *
     * @param user The username to be set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Gets the password for the database connection.
     *
     * @return The password.
     */
    public String getPass() {
        return pass;
    }

    /**
     * Sets the password for the database connection.
     *
     * @param pass The password to be set.
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Gets the name of the database.
     *
     * @return The name of the database.
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * Sets the name of the database.
     *
     * @param dbname The name of the database to be set.
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * Returns the type of database.
     *
     * @return A string with the type of database used.
     */
    public String getDb() {
        return db;
    }

    /**
     * Sets the type of database.
     *
     * @param db A string with the type of database used.
     */
    public void setDb(String db) {
        this.db = db;
    }

}
