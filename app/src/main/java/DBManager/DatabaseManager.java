package DBManager;

import static DBManager.Utilidades.leerEnteroC;
import static DBManager.Utilidades.leerRealC;
import static DBManager.Utilidades.leerTextoC;
import static DBManager.Utilidades.tituloMenu;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Scanner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.util.ArrayList;
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
     * Conecta con una base de datos MySQL utilizando los detalles de conexión proporcionados.
     *
     * @return Un objeto Connection si la conexión es exitosa, o null si hay un error.
     */
    public Connection connectDatabase() {

        Connection conn = null;

        // Construye la URL de conexión con el servidor, puerto, nombre de la base de datos y otras configuraciones.
        String connectionUrl = "jdbc:mysql://" + getServer() + ":" + getPort();
        connectionUrl += "/" + getDbname();
        connectionUrl += "?useUnicode=true&characterEncoding=UTF-8";
        connectionUrl += "&user=" + getUser();
        connectionUrl += "&password=" + getPass();

        try {
            // Carga el controlador JDBC de MySQL
            if ("mysql".equals(getDb())) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if ("mariadb".equals(getDb())) {
                Class.forName("org.mariadb.jdbc.Driver");
            } else {
                System.out.println("Error: La base de datos especificada no es compatible o no se reconoce. Use 'mysql' o 'mariadb'.");
                return null;
            }

            // Intenta establecer una conexión a la base de datos
            conn = DriverManager.getConnection(connectionUrl, getUser(), getPass());
        } catch (Exception e) {
            System.out.println("Error al abrir la base de datos: " + e.getMessage());
        }

        return conn;
    }

    /**
     * Muestra la lista de tablas en la base de datos actual.
     */
    public void showTables() {
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error en la conexion");
            return;
        }

        try {

            // Obtiene los metadatos de la base de datos
            DatabaseMetaData metaData = conn.getMetaData();

            // Obtiene la lista de tablas en la base de datos actual
            ResultSet tables = metaData.getTables(getDbname(), null, null, null);

            // Itera a través de las tablas y muestra información relevante
            while (tables.next()) {
                System.out.println(String.format("%-15s %-15s %-15s", tables.getString(1), tables.getString(3), tables.getString(4)));
            }

        } catch (Exception e) {
            System.out.println("Error al obtener las tablas: " + e);
        }

    }

    /**
     * Inserta un nuevo registro en la tabla especificada.
     *
     * @param table El nombre de la tabla en la que se insertará el registro.
     */
    public void insertIntoTable(String table) {

        // Establecer una conexión a la base de datos
        Connection conn = connectDatabase();

        // Construir la parte inicial de la consulta SQL
        StringBuilder query = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder values = new StringBuilder("VALUES (");

        try {
            // Obtener información sobre las columnas de la tabla
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(getDbname(), null, table, null);

            // Variable para controlar si se han encontrado columnas para insertar
            boolean hasColumns = false;

            // Iterar a través de las columnas de la tabla
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");

                // Solicitar valores al usuario, teniendo en cuenta el tipo de dato
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
                    values.append("'").append(valor).append("'");
                    hasColumns = true;
                }
            }

            // Componer la consulta de inserción
            if (hasColumns) {
                System.out.println(query);
                query.append(")").append(values).append(");");

                System.out.println(query);

                // Insertar el nuevo registro en la tabla
                Statement st = conn.createStatement();
                System.out.println(st.executeUpdate(query.toString()) + " filas han sido insertadas");
            } else {
                System.out.println("No se han encontrado columnas para insertar.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e);
        }

    }

    /**
     * Importa y ejecuta sentencias SQL desde un archivo especificado.
     *
     * @param ruta La ruta del archivo SQL a importar y ejecutar.
     */
    public void importSQLFile(String ruta) {

        // Establecer una conexión a la base de datos
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error en la conexión");
            return;
        }

        try {
            // Leer el contenido del archivo SQL y dividirlo en sentencias individuales
            List<String> sqlStatements = new ArrayList<>();
            String sqlContent = new String(Files.readAllBytes(Paths.get(ruta)));
            String[] statements = sqlContent.split(";");

            // Filtrar y agregar las sentencias no vacías a la lista
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    sqlStatements.add(statement);
                }
            }

            // Crear un objeto Statement y agregar las sentencias
            Statement statement = conn.createStatement();
            for (String sql : sqlStatements) {
                statement.addBatch(sql);
            }

            // Ejecutar el lote de sentencias SQL
            int[] result = statement.executeBatch();

            System.out.println("Se ejecutaron " + result.length + " sentencias SQL del archivo.");

        } catch (IOException | SQLException e) {
            System.out.println("Error al importar el archivo SQL: " + e.getMessage());
        }
    }

    /**
     * Muestra información detallada sobre la tabla especificada, incluyendo detalles de las columnas, claves primarias y claves foráneas.
     *
     * @param table El nombre de la tabla de la cual se mostrará la descripción.
     */
    public void showDescTable(String table) {
        System.out.println("Descripcion de la tabla: " + table);

        // Establecer una conexión a la base de datos
        Connection conn = connectDatabase();

        try {
            // Obtener metadatos de la base de datos
            DatabaseMetaData metaData = conn.getMetaData();

            // Obtener información de la tabla, incluyendo el nombre y tipo de tabla
            ResultSet tableInfo = metaData.getTables(getDbname(), null, table, null);

            while (tableInfo.next()) {
                System.out.println("Nombre de la tabla: " + tableInfo.getString("TABLE_NAME"));
                System.out.println("Tipo de tabla: " + tableInfo.getString("TABLE_TYPE"));
            }

            // Obtener información sobre las columnas de la tabla
            ResultSet columnsInfo = metaData.getColumns(getDbname(), null, table, null);

            System.out.println("Columnas:");
            while (columnsInfo.next()) {
                String columnName = columnsInfo.getString("COLUMN_NAME");
                String columnType = columnsInfo.getString("TYPE_NAME");
                System.out.println("Nombre de columna: " + columnName + " ( " + columnType + " ) ");
            }

            // Obtener información sobre las claves primarias de la tabla
            ResultSet primaryKeysInfo = metaData.getPrimaryKeys(getDbname(), null, table);

            System.out.println("Claves primarias:");
            while (primaryKeysInfo.next()) {
                System.out.println("Columna: " + primaryKeysInfo.getString("COLUMN_NAME") + " ( " + primaryKeysInfo.getString("PK_NAME") + " ) ");
            }

            // Obtener información sobre las claves foráneas relacionadas con la tabla
            ResultSet foreignKeysInfo = metaData.getImportedKeys(getDbname(), null, table);

            System.out.println("Claves foraneas:");
            while (foreignKeysInfo.next()) {
                System.out.println("Nombre de tabla foranea: " + foreignKeysInfo.getString("FKTABLE_NAME"));
                System.out.println("Columna foranea: " + foreignKeysInfo.getString("FKCOLUMN_NAME"));
                System.out.println("Tabla principal: " + foreignKeysInfo.getString("PKTABLE_NAME"));
                System.out.println("Columna principal: " + foreignKeysInfo.getString("PKCOLUMN_NAME"));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Muestra el contenido de una tabla especificada en la base de datos.
     *
     * @param nombre_tabla El nombre de la tabla cuyo contenido se mostrará.
     */
    public void showContent(String nombre_tabla) {
        // Establecer una conexión a la base de datos
        Connection conn = connectDatabase();

        if (conn == null) {
            System.out.println("Error al conectarse a la base de datos");
            return;
        }

        try {
            // Crear una declaración SQL
            Statement statement = conn.createStatement();

            // Construir una consulta SQL para seleccionar todos los registros de la tabla
            String query = "SELECT * FROM " + nombre_tabla;

            // Ejecutar la consulta SQL y obtener el resultado
            ResultSet result = statement.executeQuery(query);

            // Obtener información sobre el resultado, como metadatos de columnas
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();

            while (result.next()) {
                // Recorrer los registros y mostrar los valores de las columnas
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
            // Cerrar el resultado después de su uso
            result.close();
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exporta los datos de una tabla de la base de datos a un archivo SQL en la ubicación especificada.
     *
     * @param tabla El nombre de la tabla cuyos datos se exportarán.
     * @param ruta La ruta del archivo de destino donde se exportarán los datos en formato SQL.
     */
    public void exportSql(String tabla, String ruta) {
        // Conectar a la base de datos
        Connection conn = connectDatabase();

        // Verificar la conexión a la base de datos
        if (conn == null) {
            System.out.println("Error en la conexión");
            return;
        }

        try {
            // Crear una declaración SQL
            Statement statement = conn.createStatement();

            // Construir una consulta SQL para seleccionar todos los registros de la tabla
            String query = "SELECT * FROM " + tabla;

            // Ejecutar la consulta SQL y obtener el resultado
            ResultSet result = statement.executeQuery(query);

            // Obtener metadatos de las columnas del resultado
            ResultSetMetaData metadata = result.getMetaData();
            int column_contador = metadata.getColumnCount();

            // Crear un StringBuilder para almacenar los datos SQL exportados
            StringBuilder sql_datos = new StringBuilder();

            while (result.next()) {
                // Componer la instrucción SQL INSERT INTO para cada registro
                sql_datos.append("INSERT INTO ").append(tabla).append(" (");

                for (int i = 1; i <= column_contador; i++) {
                    String nombre_columna = metadata.getColumnName(i);
                    sql_datos.append(nombre_columna);
                    if (i < column_contador) {
                        sql_datos.append(", ");
                    }
                }

                sql_datos.append(") VALUES (");

                for (int i = 1; i < column_contador; i++) {
                    String column_valor = result.getString(i);
                    sql_datos.append("'").append(column_valor).append("'");
                    if (i < column_contador) {
                        sql_datos.append(", ");
                    }
                }

                sql_datos.append(");").append(System.lineSeparator());

            }

            // Escribir los datos SQL exportados en el archivo en la ubicación especificada
            Files.write(Paths.get(ruta), sql_datos.toString().getBytes());

            System.out.println("Datos de la tabla " + tabla + " exportados exitosamente a " + ruta);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exporta los datos de una tabla de la base de datos a un archivo CSV en la ubicación especificada.
     *
     * @param tabla El nombre de la tabla cuyos datos se exportarán.
     * @param ruta La ruta del archivo de destino donde se exportarán los datos en formato CSV.
     */
    public void exportCsv(String tabla, String ruta) {
        // Conectar a la base de datos
        Connection conn = connectDatabase();

        // Verificar la conexión a la base de datos
        if (conn == null) {
            System.out.println("Error en la conexión");
            return;
        }

        try {
            // Crear una declaración SQL
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + tabla);

            // Crear un FileWriter para escribir en el archivo CSV
            FileWriter writer = new FileWriter(ruta);

            // Obtener metadatos de las columnas del resultado
            ResultSetMetaData metadata = result.getMetaData();
            int column_contador = metadata.getColumnCount();

            // Escribir encabezados de columnas en el archivo CSV
            for (int i = 1; i < column_contador; i++) {
                String column_nombre = metadata.getColumnName(i);
                writer.append(column_nombre);
                if (i < column_contador) {
                    writer.append(",");
                }
            }

            writer.append(System.lineSeparator());

            // Escribir datos en el archivo CSV
            while (result.next()) {
                for (int i = 1; i < column_contador; i++) {
                    String column_valor = result.getString(i);
                    writer.append(column_valor);

                    if (i < column_contador) {
                        writer.append(",");
                    }
                }
                writer.append(System.lineSeparator());
            }
            // Cerrar el archivo y mostrar un mensaje de éxito
            writer.close();
            System.out.println("Datos de la tabla " + tabla + " exportados exitosamente como CSV a " + ruta);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Exporta los datos de una tabla de la base de datos a un archivo JSON en la ubicación especificada.
     *
     * @param tabla El nombre de la tabla cuyos datos se exportarán.
     * @param ruta La ruta del archivo de destino donde se exportarán los datos en formato JSON.
     */
    public void exportJson(String tabla, String ruta) {

        // Conectar a la base de datos
        Connection conn = connectDatabase();

        // Verificar la conexión a la base de datos
        if (conn == null) {
            System.out.println("Error en la conexión");
            return;
        }

        try {
            // Crear una lista para almacenar los datos en formato JSON
            List<String> data = new ArrayList<>(); // Cambio el tipo de lista

            // Crear una declaración SQL
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + tabla);

            // Obtener metadatos de las columnas del resultado
            ResultSetMetaData metadata = result.getMetaData();
            int column_contador = metadata.getColumnCount();

            while (result.next()) {
                // Crear un StringBuilder para construir objetos JSON para cada fila
                StringBuilder data_fila = new StringBuilder("{");
                for (int i = 1; i <= column_contador; i++) {
                    String column_name = metadata.getColumnName(i);
                    String column_valor = result.getString(i);
                    if (i > 1) {
                        data_fila.append(",");
                    }
                    data_fila.append("\"").append(column_name).append("\":\"").append(column_valor).append("\"");
                }
                data_fila.append("}");
                data.add(data_fila.toString());
            }

            // Crear un FileWriter para escribir en el archivo JSON
            FileWriter writer = new FileWriter(ruta);

            // Escribir cada objeto JSON en una línea separada en el archivo
            for (String json_dato : data) {
                writer.write(json_dato);
                writer.write(System.lineSeparator());
            }

            // Cerrar el archivo y mostrar un mensaje de éxito
            writer.close();
            System.out.println("Datos de la tabla " + tabla + " exportados exitosamente como JSON a " + ruta);

        } catch (IOException | SQLException e) {
            System.out.println("Error: " + e);
        }
    }

    /**
     * Exporta los datos de una tabla de la base de datos a un archivo JSON en la ubicación especificada.
     *
     * @param tabla El nombre de la tabla cuyos datos se exportarán.
     * @param ruta La ruta del archivo de destino donde se exportarán los datos en formato JSON.
     */
    public void exportXml(String tabla, String ruta) {

        // Conectar a la base de datos
        Connection conn = connectDatabase();

        // Verificar la conexión a la base de datos
        if (conn == null) {
            System.out.println("Error en la conexión");
            return;
        }

        try {
            // Crear un objeto DocumentBuilderFactory para trabajar con XML
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = (Document) dBuilder.newDocument();

            // Crear el elemento raíz del documento XML
            Element rootElement = doc.createElement(tabla);
            doc.appendChild(rootElement);

            // Crear una declaración SQL para obtener los datos de la tabla
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("SELECT * FROM " + tabla);

            // Obtener los metadatos de las columnas del resultado
            ResultSetMetaData metadata = result.getMetaData();
            int column_contador = metadata.getColumnCount();

            while (result.next()) {
                // Crear un elemento XML para cada fila de datos
                Element rowElement = doc.createElement("row");
                rootElement.appendChild(rowElement);

                for (int i = 1; i <= column_contador; i++) {
                    // Obtener el nombre de la columna y el valor de la celda
                    String column_name = metadata.getColumnName(i);
                    String column_value = result.getString(i);

                    // Crear un elemento XML para cada par nombre-valor y agregarlo a la fila
                    Element columnElement = doc.createElement(column_name);
                    columnElement.appendChild(doc.createTextNode(column_value));
                    rowElement.appendChild(columnElement);
                }
            }

            // Crear un FileWriter para escribir en el archivo XML
            FileWriter writer = new FileWriter(ruta);

            // Configurar un Transformer para escribir el documento XML en el archivo
            TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            DOMSource source = new javax.xml.transform.dom.DOMSource(doc);
            StreamResult resultFile = new javax.xml.transform.stream.StreamResult(writer);
            transformer.transform(source, resultFile);

            // Cerrar el archivo y mostrar un mensaje de éxito
            writer.close();
            System.out.println("Datos de la tabla " + tabla + " exportados exitosamente como XML a " + ruta);

        } catch (IOException | SQLException | ParserConfigurationException | TransformerException | DOMException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Imprime las opciones del menú del shell.
     */
    public void menu() {

        System.out.println();

        tituloMenu(getDbname());

        System.out.println("Opciones disponibles:");
        System.out.println("1. Describir una tabla: desc table <table_name>");
        System.out.println("2. Insertar en una tabla: insert table <table_name>");
        System.out.println("3. Mostrar tablas: show tables");
        System.out.println("4. Importar un archivo SQL: import <file_path.sql>");
        System.out.println("5. Mostrar contenido de una tabla: show <table_name>");
        System.out.println("6. Exportar contenido a SQL: export sql <table_name> <output_file.sql>");
        System.out.println("7. Exportar contenido a CSV: export csv <table_name> <output_file.csv>");
        System.out.println("8. Exportar contenido a JSON: export json <table_name> <output_file.json>");
        System.out.println("9. Exportar contenido a XML: export xml <table_name> <output_file.xml>");
        System.out.println("10. Salir: quit");
    }

    /**
     * Inicia un shell interactivo para ejecutar comandos relacionados con la base de datos. Los comandos disponibles incluyen descripción de tablas, inserción de registros, mostrar tablas, importar y exportar datos en diferentes formatos.
     */
    public void startShell() {

        Scanner keyboard = new Scanner(System.in);
        String command;

        do {

            menu();

            System.out.print(ConsoleColors.GREEN_BOLD_BRIGHT + "# (" + this.user + ") on " + this.server + ":" + this.port + "> " + ConsoleColors.RESET);
            command = keyboard.nextLine();

            // Describir tabla
            if (command.startsWith("desc table") || (command.startsWith("description table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.showDescTable(tableName);
                }
            } // Insertar manualmente en una tabla
            else if (command.startsWith("insert table") || (command.startsWith("ins table"))) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println(ConsoleColors.RED + "Error: desc table <table_name>" + ConsoleColors.RESET);
                } else {
                    String tableName = subcommand[2];
                    this.insertIntoTable(tableName);
                }
            } // Mostrar tablas
            else if ("show tables".equals(command) || "sh tables".equals(command)) {
                this.showTables();
            } // Importar un sql y ejecutarlo
            else if (command.startsWith("import")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println("Formato incorrecto");
                } else {
                    String ruta = subcommand[1];
                    if (ruta.toLowerCase().endsWith(".sql")) {
                        this.importSQLFile(ruta);
                    } else {
                        System.out.println("El archivo proporcionado no es un sql");
                    }
                }

            } // Mostrar el contenido de una tabla
            else if (command.startsWith("show")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 2) {
                    System.out.println("Formato incorrecto");
                } else {
                    this.showContent(subcommand[1]);
                }
            } // Exportar el contenido de una tabla en un archivo sql
            else if (command.startsWith("export sql")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Formato incorrecto");
                } else {
                    this.exportSql(subcommand[2], subcommand[3]);
                }
            } // Exportar el contenido de una tabla en un archivo csv
            else if (command.startsWith("export csv")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Formato incorrecto");
                } else {
                    this.exportCsv(subcommand[2], subcommand[3]);
                }
            } // Exportar el contenido de una tabla en un archivo json
            else if (command.startsWith("export json")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Formato incorrecto");
                } else {
                    this.exportJson(subcommand[2], subcommand[3]);
                }
            } // Exportar el contenido de una tabla en un archivo xml
            else if (command.startsWith("export xml")) {
                String[] subcommand = command.split(" ");
                if (subcommand.length < 4) {
                    System.out.println("Formato incorrecto");
                } else {
                    this.exportXml(subcommand[2], subcommand[3]);
                }
            } // Se sale del shell de la base de datos
            else if (command.startsWith("quit")) {

            } // Se ha insertado un comando incorrecto
            else {
                System.out.println("Comando incorrecto");
            }

        } while (!command.equals("quit"));
    }

    /**
     * Obtiene el nombre del servidor de la base de datos.
     *
     * @return El nombre del servidor.
     */
    public String getServer() {
        return server;
    }

    /**
     * Establece el nombre del servidor de la base de datos.
     *
     * @param server El nombre del servidor que se establecerá.
     */
    public void setServer(String server) {
        this.server = server;
    }

    /**
     * Obtiene el número de puerto para la conexión a la base de datos.
     *
     * @return El número de puerto.
     */
    public String getPort() {
        return port;
    }

    /**
     * Establece el número de puerto para la conexión a la base de datos.
     *
     * @param port El número de puerto que se establecerá.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * Obtiene el nombre de usuario para la conexión a la base de datos.
     *
     * @return El nombre de usuario.
     */
    public String getUser() {
        return user;
    }

    /**
     * Establece el nombre de usuario para la conexión a la base de datos.
     *
     * @param user El nombre de usuario que se establecerá.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Obtiene la contraseña para la conexión a la base de datos.
     *
     * @return La contraseña.
     */
    public String getPass() {
        return pass;
    }

    /**
     * Establece la contraseña para la conexión a la base de datos.
     *
     * @param pass La contraseña que se establecerá.
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * Obtiene el nombre de la base de datos.
     *
     * @return El nombre de la base de datos.
     */
    public String getDbname() {
        return dbname;
    }

    /**
     * Establece el nombre de la base de datos.
     *
     * @param dbname El nombre de la base de datos que se establecerá.
     */
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    /**
     * Devuelve el tipo de base de datos
     *
     * @return Un string con el tipo de base de datos usado
     */
    public String getDb() {
        return db;
    }

    /**
     * Establece el tipo de base de datos
     *
     * @param db Un string con el tipo de base de datos usado
     */
    public void setDb(String db) {
        this.db = db;
    }

}
