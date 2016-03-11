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

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class MessengerGui {

    // reference to physical database connection.
    private Connection _connection = null;
    private static MessengerGui instance = null;
    private static String user = null;
    private static String block_list;
    private static String contact_list;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * Creates a new instance of MessengerGui
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @param username the user name used to login to the database
     * @param password the user login password
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public MessengerGui (String dbname, String dbport, String user, String passwd) throws SQLException {

        System.out.print("Connecting to database...");
        try{
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            instance = this;
            System.out.println("Done");
        }catch (Exception e){
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }//end catch
    }//end MessengerGui

    public static synchronized MessengerGui getInstance() {
        return instance;
    }

    public static synchronized String getUser(){
        return user;
    }

    public static String getBlockList() {
        return block_list;
    }

    public static String getContactList() {
        return contact_list;
    }

    //don't think I'd need synchronized for simple project
    public static void setUser(String username, String block, String contact){
        user = username;
        block_list = block;
        contact_list = contact;
    }

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate (String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the update instruction
        stmt.executeUpdate (sql);

        // close the instruction
        stmt.close ();
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
    public int executeQueryAndPrintResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        /*
         ** obtains the metadata object for the returned result set.  The metadata
         ** contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()){
            if(outputHeader){
                for(int i = 1; i <= numCol; i++){
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i=1; i<=numCol; ++i)
                System.out.print (rs.getString (i).trim()  + "\t");
            System.out.println ();
            ++rowCount;
        }//end while
        stmt.close ();
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
    public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
        // creates a statement object 
        Statement stmt = this._connection.createStatement (); 

        // issues the query instruction 
        ResultSet rs = stmt.executeQuery (query); 

        /* 
         ** obtains the metadata object for the returned result set.  The metadata 
         ** contains row and column info. 
         */ 
        ResultSetMetaData rsmd = rs.getMetaData (); 
        int numCol = rsmd.getColumnCount (); 
        int rowCount = 0; 

        // iterates through the result set and saves the data returned by the query. 
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>(); 
        while (rs.next()){
            List<String> record = new ArrayList<String>(); 
            for (int i=1; i<=numCol; ++i) 
                record.add(rs.getString (i).trim());
            result.add(record); 
        }//end while 
        stmt.close (); 
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
    public int executeQuery (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        int rowCount = 0;

        // iterates through the result set and count nuber of results.
        if(rs.next()){
            rowCount++;
        }//end while
        stmt.close ();
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
        Statement stmt = this._connection.createStatement ();

        ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')\n", sequence));
        if (rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup(){
        try{
            if (this._connection != null){
                this._connection.close ();
            }//end if
        }catch (SQLException e){
            // ignored.
        }//end try
    }//end cleanup

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
            }catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        }while (true);
        return input;
    }//end readChoice

    /*
     * Creates a new user with privided login, passowrd and phoneNum
     * An empty block and contact list would be generated and associated with a user
     **/
    public void CreateUser(String login, String password, String phone) throws Exception {
        try{
            //Creating empty contact\block lists for a user
            executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
            int block_id = getCurrSeqVal("user_list_list_id_seq");
            executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
            int contact_id = getCurrSeqVal("user_list_list_id_seq");

            String query = String.format("INSERT INTO USR (phoneNum, login, password, status, block_list, contact_list) " +
                    "VALUES ('%s','%s','%s', '%s',%s,%s)\n", phone, login, password, "", block_id, contact_id);

            executeUpdate(query);
            System.out.println ("User successfully created!");
        }catch(Exception e){
            throw new Exception("That username already exists!");
        }
    }//end

    /*
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     **/
    public String LogIn(String login, String password){
        try{
            String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'\n", login, password);
            int userNum = executeQuery(query);
            if (userNum > 0){
                System.out.println("Login authorized");
                return login;
            }
            return null;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public void AddToContact(String addedContact) throws Exception{
        String query = String.format(
                "INSERT INTO User_list_contains (list_id, list_member)" +
                "VALUES((SELECT Usr.contact_list\n" +
                "FROM Usr WHERE login = '%s'), '%s')\n", user, addedContact);
        System.out.println(query);
        executeUpdate(query);
    }//end

    public void AddToBlock(String blockedContact) throws Exception{
        String query = String.format(
                "INSERT INTO User_list_contains (list_id, list_member)" +
                "VALUES((SELECT Usr.block_list\n" +
                "FROM Usr WHERE login = '%s'), '%s')\n", user, blockedContact);
        System.out.println(query);
        executeUpdate(query);
    }//end

    public List<List<String>> ListContacts(){
        try{
            String query = String.format(
                    "SELECT Usr.login, Usr.status\n" +
                    "FROM Usr WHERE login IN\n(" +
                    "SELECT User_list_contains.list_member \n" +
                    "FROM User_list_contains WHERE list_id=\n(" +
                    "SELECT Usr.contact_list \n" +
                    "FROM Usr WHERE login = '%s'))\n", user);
            System.out.println(query);
            // int userNum = executeQueryAndPrintResult(query);
            // System.out.println("Number Outputs: " + userNum);
            // System.out.println();
            return executeQueryAndReturnResult(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public List<List<String>> ListBlocked(){
        try{
            String query = String.format(
                    "SELECT Usr.login\n" +
                    "FROM Usr WHERE login IN\n(" +
                    "SELECT User_list_contains.list_member \n" +
                    "FROM User_list_contains WHERE list_id=\n(" +
                    "SELECT Usr.block_list \n" +
                    "FROM Usr WHERE login = '%s'))\n", user);
            System.out.println(query);
            // int userNum = executeQueryAndPrintResult(query);
            // System.out.println("Number Outputs: " + userNum);
            // System.out.println();
            return executeQueryAndReturnResult(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public List<List<String>> ListMessages(){
        try{
            String query = String.format(
                    "SELECT *\n" +
                    "FROM chat WHERE chat_id IN (\n" +
                    "SELECT chat_id\n" +
                    "FROM chat_list WHERE member = '%s')\n", user);
            System.out.println(query);
             int userNum = executeQueryAndPrintResult(query);
             System.out.println();
            // System.out.println("Number Outputs: " + userNum);
            return executeQueryAndReturnResult(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public List<List<String>> AllUsersInChat(String chatId){
        try{
            // String chatId= "0";
            String query = String.format(
                    "SELECT member\n" +
                    "FROM chat_list WHERE chat_id = '%s'\n", chatId);
            System.out.println(query);
             int userNum = executeQueryAndPrintResult(query);
             System.out.println();
            // System.out.println("Number Outputs: " + userNum);
            // System.out.println();
            return executeQueryAndReturnResult(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public List<List<String>> GetUser(String login){
        try{
            String query = String.format(
                    "SELECT *\n" +
                    "FROM Usr WHERE login = '%s'\n", login);
            System.out.println(query);
//            int userNum = executeQueryAndPrintResult(query);
//            System.out.println("Number Outputs: " + userNum);
//            System.out.println();
            List<List<String>> ret = executeQueryAndReturnResult(query);
            return ret;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public void RemoveFromContact(String target){
        try{
            String query = String.format(
                    "DELETE FROM user_list_contains\n" +
                            "WHERE list_id = %s and list_member = '%s'\n", getContactList(), target);
            System.out.println(query);
            executeUpdate(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    public void RemoveFromBlocked(String target){
        try{
            String query = String.format(
                    "DELETE FROM user_list_contains\n" +
                            "WHERE list_id = %s and list_member = '%s'\n", getBlockList(), target);
            System.out.println(query);
            executeUpdate(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    public void AddNewPrivateChat(String target){
        try{
            //check if

            String query = String.format("INSERT INTO chat(chat_type, init_sender) VALUES ('private', '%s')\n", getUser());
            executeUpdate(query);
            int chat_id = getCurrSeqVal("chat_chat_id_seq");
            String query2 = String.format("INSERT INTO chat_list(chat_id, member) VALUES (%d, '%s')\n", chat_id, getUser());
            executeUpdate(query2);
            String query3 = String.format("INSERT INTO chat_list(chat_id, member) VALUES (%d, '%s')\n", chat_id, target);
            executeUpdate(query3);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    public List<List<String>> GetAllMessagesInChat(String chatId) {
        try {
            String query = String.format(
                    "SELECT *\n" +
                            "FROM message WHERE chat_id = %s\n" +
                            "ORDER BY msg_timestamp ASC\n", chatId);
            System.out.println(query);
            int userNum = executeQueryAndPrintResult(query);
            System.out.println();
//            System.out.println("Number Outputs: " + userNum);
            List<List<String>> ret = executeQueryAndReturnResult(query);
            return ret;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }//end
}//end MessengerGui
