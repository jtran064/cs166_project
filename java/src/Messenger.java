/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.awt.EventQueue;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 */
public class Messenger {

    // reference to physical database connection.
    private Connection _connection = null;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * Creates a new instance of Messenger
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @param username the user name used to login to the database
     * @param password the user login password
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public Messenger(String dbname, String dbport, String user, String passwd) throws SQLException {

        System.out.print("Connecting to database...");
        try {
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        } catch (Exception e) {
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }//end catch
    }//end Messenger

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate(String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the update instruction
        stmt.executeUpdate(sql);

        // close the instruction
        stmt.close();
    }//end executeUpdate

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult(String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        /*
         ** obtains the metadata object for the returned result set.  The metadata
         ** contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()) {
            if (outputHeader) {
                for (int i = 1; i <= numCol; i++) {
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i = 1; i <= numCol; ++i)
                System.out.print(rs.getString(i).trim() + "\t");
            System.out.println();
            ++rowCount;
        }//end while
        stmt.close();
        return rowCount;
    }//end executeQuery

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     *
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
        // creates a statement object 
        Statement stmt = this._connection.createStatement();

        // issues the query instruction 
        ResultSet rs = stmt.executeQuery(query);

        /* 
         ** obtains the metadata object for the returned result set.  The metadata 
         ** contains row and column info. 
         */
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();
        int rowCount = 0;

        // iterates through the result set and saves the data returned by the query. 
        boolean outputHeader = false;
        List<List<String>> result = new ArrayList<List<String>>();
        while (rs.next()) {
            List<String> record = new ArrayList<String>();
            for (int i = 1; i <= numCol; ++i)
                record.add(rs.getString(i).trim());
            result.add(record);
        }//end while 
        stmt.close();
        return result;
    }//end executeQueryAndReturnResult

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the number of results
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery(String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery(query);

        int rowCount = 0;

        // iterates through the result set and count nuber of results.
        if (rs.next()) {
            rowCount++;
        }//end while
        stmt.close();
        return rowCount;
    }

    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current
     * value of sequence used for autogenerated keys
     *
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int getCurrSeqVal(String sequence) throws SQLException {
        Statement stmt = this._connection.createStatement();

        ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')\n", sequence));
        if (rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup() {
        try {
            if (this._connection != null) {
                this._connection.close();
            }//end if
        } catch (SQLException e) {
            // ignored.
        }//end try
    }//end cleanup

    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println(
                    "Usage: " +
                            "java [-classpath <classpath>] " +
                            Messenger.class.getName() +
                            " <dbname> <port> <user>");
            return;
        }//end if

        Greeting();
        Messenger esql = null;
        try {
            // use postgres JDBC driver.
            Class.forName("org.postgresql.Driver").newInstance();
            // instantiate the Messenger object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            esql = new Messenger(dbname, dbport, user, "");

            boolean keepon = true;

//            CreateUser(esql);
            // ListContacts(esql);
            // ListBlocked(esql);
            // AddToContact(esql);
//             CheckUser(esql);
//            GetMessages(esql);
//            AllUsersInChat(esql);
//            RemoveFromContact(esql);
//            ListContacts(esql);
//            AddNewPrivateChat(esql);
//            AddNewMessageToChat(esql);
//            GetAllMessagesInChat(esql);
            GetMessages(esql);
            DeleteChat(esql);
            GetMessages(esql);

            // while(keepon) {
            // // These are sample SQL statements
            // System.out.println("MAIN MENU");
            // System.out.println("---------");
            // System.out.println("1. Create user");
            // System.out.println("2. Log in");
            // System.out.println("9. < EXIT");
            // String authorisedUser = null;
            // switch (readChoice()){
            // case 1: CreateUser(esql); break;
            // case 2: authorisedUser = LogIn(esql); break;
            // case 9: keepon = false; break;
            // default : System.out.println("Unrecognized choice!"); break;
            // }//end switch
            // if (authorisedUser != null) {
            // boolean usermenu = true;
            // while(usermenu) {
            // System.out.println("MAIN MENU");
            // System.out.println("---------");
            // System.out.println("1. Add to contact list");
            // System.out.println("2. Browse contact list");
            // System.out.println("3. Write a new message");
            // System.out.println(".........................");
            // System.out.println("9. Log out");
            // switch (readChoice()){
            // case 1: AddToContact(esql); break;
            // case 2: ListContacts(esql); break;
            // case 3: NewMessage(esql); break;
            // case 9: usermenu = false; break;
            // default : System.out.println("Unrecognized choice!"); break;
            // }
            // }
            // }
            // }//end while
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            // make sure to cleanup the created table and close the connection.
            try {
                if (esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup();
                    System.out.println("Done\n\nBye !");
                }//end if
            } catch (Exception e) {
                // ignored.
            }//end try
        }//end try
    }//end main

    public static void Greeting() {
        System.out.println(
                "\n\n*******************************************************\n" +
                        "              User Interface      	               \n" +
                        "*******************************************************\n");
    }//end Greeting

    /*
     * Reads the users choice given from the keyboard
     * @int
     **/
    public static int readChoice() {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            } catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        } while (true);
        return input;
    }//end readChoice

    /*
     * Creates a new user with privided login, passowrd and phoneNum
     * An empty block and contact list would be generated and associated with a user
     **/
    public static void CreateUser(Messenger esql) {
        try {
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();
            System.out.print("\tEnter user phone: ");
            String phone = in.readLine();

            //Creating empty contact\block lists for a user
            esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
            int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
            esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
            int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");

            String query = String.format("INSERT INTO USR (phoneNum, login, password, status, block_list, contact_list) VALUES ('%s','%s','%s', '%s',%s,%s)\n", phone, login, password, "\n", block_id, contact_id);

            esql.executeUpdate(query);
            System.out.println("User successfully created!");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    /*
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     **/
    public static String LogIn(Messenger esql) {
        try {
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();

            String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'\n", login, password);
            int userNum = esql.executeQuery(query);
            if (userNum > 0)
                return login;
            return null;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }//end

    public static void AddToContact(Messenger esql) {
        try {
            String login = "Eve";
            String addedContact = "Jimmy";
            String query = String.format(
                    "INSERT INTO User_list_contains (list_id, list_member)" +
                            "VALUES((SELECT Usr.contact_list\n" +
                            "FROM Usr WHERE login = '%s'), '%s')\n", login, addedContact);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void AddNewPrivateChat(Messenger esql) {
        try {
            String login = "Eve";
            String target = "Jimmy";
            String query = String.format("INSERT INTO chat(chat_type, init_sender) VALUES ('private', '%s')", login);
            esql.executeUpdate(query);
            int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
            String query2 = String.format("INSERT INTO chat_list(chat_id, member) VALUES (%d, '%s')", chat_id, login);
            esql.executeUpdate(query2);
            String query3 = String.format("INSERT INTO chat_list(chat_id, member) VALUES (%d, '%s')", chat_id, target);
            esql.executeUpdate(query3);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static boolean IsInitSender(Messenger esql, String author, String chat){
        try{
            String query = String.format("SELECT * FROM chat WHERE init_sender='%s' AND chat_id=%s", author, chat);
            int userNum = esql.executeQuery(query);
            if(userNum == 0){
                return false;
            } else {
                return true;
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;
        }
    }//end

    public static void DeleteChat(Messenger esql) {
        try {
            String login = "Judy";
            String chatId = "5006";
            if(IsInitSender(esql, login, chatId)) {
                String query = String.format("DELETE FROM message WHERE chat_id=%s", chatId);
                esql.executeUpdate(query);
                query = String.format("DELETE FROM chat_list WHERE chat_id=%s", chatId);
                esql.executeUpdate(query);
                query = String.format("DELETE FROM chat WHERE chat_id=%s", chatId);
                esql.executeUpdate(query);
            } else {
                throw new Exception("Not Init User");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void AddNewMessageToChat(Messenger esql) {
        try {
            String login = "Eve";
            String message = "TEST";
            String chat_id = "5001";
            String query = String.format("INSERT INTO message(msg_text, msg_timestamp, sender_login, chat_id)\n" +
                    "VALUES ('%s', (select LOCALTIMESTAMP(2)), '%s', '%s')", message, login, chat_id);
            esql.executeUpdate(query);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static List<List<String>> ListContacts(Messenger esql) {
        try {
            String login = "Eve";
            String query = String.format(
                    "SELECT Usr.login, Usr.status\n" +
                            "FROM Usr WHERE login IN\n(" +
                            "SELECT User_list_contains.list_member \n" +
                            "FROM User_list_contains WHERE list_id=\n(" +
                            "SELECT Usr.contact_list \n" +
                            "FROM Usr WHERE login = '%s'))\n", login);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
            return null;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }//end

    public static void NewMessage(Messenger esql) {
        // Your code goes here.
        // ...
        // ...
    }//end 

    public static void ListBlocked(Messenger esql) {
        try {
            String login = "Eve";
            String query = String.format(
                    "SELECT Usr.login\n" +
                            "FROM Usr WHERE login IN\n(" +
                            "SELECT User_list_contains.list_member \n" +
                            "FROM User_list_contains WHERE list_id=\n(" +
                            "SELECT Usr.block_list \n" +
                            "FROM Usr WHERE login = '%s'))\n", login);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void CheckUser(Messenger esql) {
        try {
            String login = "Jimmy";
            String query = String.format(
                    "SELECT *\n" +
                            "FROM Usr WHERE login = '%s'\n", login);
            System.out.println(query);
//            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println(esql.executeQueryAndReturnResult(query));
            boolean i = esql.executeQueryAndReturnResult(query).isEmpty();
            if (i) {
                System.out.println("YES");
            } else {
                System.out.println("NO");
            }
//            System.out.println(i);
////            System.out.println("Number Outputs: " + userNum);
//            System.out.println();
        } catch (Exception e) {
            System.err.println("ERROR");
            System.err.println(e.getMessage());
        }
    }//end

    public static void GetMessages(Messenger esql) {
        try {
            String login = "Hermina";
            String query = String.format(
                    "SELECT *\n" +
                            "FROM chat WHERE chat_id IN (\n" +
                            "SELECT chat_id\n" +
                            "FROM chat_list WHERE member = '%s')\n", login);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void AllUsersInChat(Messenger esql) {
        try {
            String chatId = "0";
            String query = String.format(
                    "SELECT member\n" +
                            "FROM chat_list WHERE chat_id = '%s'\n", chatId);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void GetAllMessagesInChat(Messenger esql) {
        try {
            String chatId = "5001";
            String query = String.format(
                    "SELECT *\n" +
                    "FROM message WHERE chat_id = %s\n" +
                    "ORDER BY msg_timestamp ASC\n", chatId);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end


    public static void RemoveFromContact(Messenger esql) {
        try {
            String list = "7781";
            String target = "Zora.Boyle";
            String query = String.format(
                    "DELETE FROM user_list_contains\n" +
                            "WHERE list_id = %s and list_member = '%s'\n", list, target);
            System.out.println(query);
            int userNum = esql.executeQueryAndPrintResult(query);
            System.out.println("Number Outputs: " + userNum);
            System.out.println();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }//end

    public static void Query6(Messenger esql) {
        // Your code goes here.
        // ...
        // ...
    }//end Query6

}//end Messenger
