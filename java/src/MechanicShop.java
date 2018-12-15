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
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
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
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
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
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
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
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
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
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
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

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

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

//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
 //Added functions to avoid repeating code

	//For all these, we might need to check for NULLs as well
	public static boolean phoneCheck(String input){ //FIXME: make sure to accept only format that exisiting data is in!!! (CHECK THE DATA FILES)
		if(input.length() == 13 && input.charAt(0) == '(' && input.charAt(4) == ')' && input.charAt(8) == '-' ){ //FIXME: Is it less than 13 or less than equal to 13?
			
			return true;
		}
		else{
			return false; //Might need to adjust length
		}
	}

	public static boolean charCheck16(String input){
		if(input.length() <= 16 && input.length() > 0){ //FIXME: Is it less than 16 or less than equal to 16?
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean vinCheck(String input){ //FIXME: VIN HAS TO BE 11 to 17 CHARACTERS LONG. I assume due to index starting at 0, this translates to 10 to 17
		if(input.length() <= 17 && input.length() > 10 ){
			return true;
		}
		else{
			return false;
		}
	}

	public static boolean charCheck32(String input){
		if(input.length() <= 32 && input.length() > 0){ //FIXME: Is it less than 32 or less than equal to 32?
			return true;
		}
		else{
			return false;
		}
	}

	public static boolean addrCheck(String input){
		if(input.length() <= 256 && input.length() > 0){ //FIXME: Is it less than 256 or less than equal to 256?
			return true;
		}
		else{
			return false;
		}
	}
 
 public static boolean experienceCheck(String input){ //ASSUMPTION: Mechanics can have 0 years of experience (newbies) and 99 is the max (they retire otherwise)
		if(input.length() == 0)
    {
      return false;
    }
    if (!input.matches("[0-9]+")) { 
      return false;
    }
    int inputInt = Integer.parseInt(input);
		if(inputInt >= 0 && inputInt < 100){
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean yearCheck(String input){
    if(input.length() == 0)
    {
      return false;
    }
    if (!input.matches("[0-9]+")) { 
      return false;
    }
		int inputInt = Integer.parseInt(input);
		if(inputInt >= 1970){
			return true;
		}
		else{
			return false;
		}
	}
	public static boolean boolCheck(String input){
		if(input.equals("Y") || input.equals("N")){
//       System.out.print(input);
			return true;
		}
		else{
			return false;
		}
	}

	public static boolean choiceCheck(String input){
		if(input.equals("1") || input.equals("2")){
			return true;
		}
		else{
			return false;
		}
	}
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 
	public static void AddCustomer(MechanicShop esql){//1
 try{	
				System.out.print("\tEnter first name: $");
				String f_name = in.readLine();
				while(!charCheck32(f_name)){
					System.out.print("\nERROR: First name too long or too short! Enter first name again: $");
					f_name = in.readLine();
				}
				System.out.print("\tEnter last name: $");
				String l_name = in.readLine();
				while(!charCheck32(l_name)){
					System.out.print("\nERROR: Last name too long or too short! Enter last name again: $");
					l_name = in.readLine();
				}
				System.out.print("\tEnter phone number as (###)###-#### (replace the '#' with numbers): $");
				String phone = in.readLine();
				while(!phoneCheck(phone)){
					System.out.print("\nERROR: Phone number does not match required format! Enter phone number again: $");
					phone = in.readLine();
				}
				System.out.print("\tEnter address: $");
				String address = in.readLine();
				while(!addrCheck(address)){
					System.out.print("\nERROR: Address is too long or too short! Enter address again: $");
					address = in.readLine();
				}
		
				//Gets the largest ID value and adds 1 so we have our ID for new entry
				String query = "SELECT MAX(id) AS maxID FROM Customer";
				List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(query);
				int maxIDint = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;

        System.out.print("INSERT INTO Customer (id,fname,lname,phone,address) VALUES (" + maxIDint + "," + f_name + "," + l_name + "," + phone + "," + address + ")\n");
        
				esql.executeUpdate("INSERT INTO Customer(id,fname,lname,phone,address) VALUES (" + maxIDint + ",'" + f_name + "','" + l_name + "','" + phone + "','" + address + "')");
        
        AddCar(esql);
      }
			catch(Exception e){
				System.err.println(e.getMessage());
			}
	}
//------------------------------------------------------------------------------------------------	
	public static void AddMechanic(MechanicShop esql){//2
		try{
			System.out.print("\tEnter first name: $");
			String f_name = in.readLine();
			while(!charCheck32(f_name)){
				System.out.print("\nERROR: First name too long or too short! Enter first name again: $");
				f_name = in.readLine();
			}
     		System.out.print("\tEnter last name: $");
			String l_name = in.readLine();
			while(!charCheck32(l_name)){
				System.out.print("\nERROR: Last name too long or too short! Enter last name again: $");
				l_name = in.readLine();
			} 
     		System.out.print("\tEnter experience in terms of years as a number (0 to 99): $");
			String experience = in.readLine();
			while(!experienceCheck(experience)){
				System.out.print("\nERROR: Experience is less than 0 or greater than 99! Enter years of experience again: $");
				experience = in.readLine();
			}
			
			//int experienceInt = Integer.parseInt(experience);
		 


		// 	//I want to find the largest ID in the system and add 1 to it. That will be our unique id. I have no idea how to do this though. Once we get it, we can add the value in
		String query = "SELECT MAX(id) AS maxID FROM Mechanic";
		List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(query);
		int maxIDint = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;



     	// 	//Statement statement = conn.createStatement();
     	// 	esql.executeUpdate("INSERT INTO Mechanic " + "VALUES (1,f_name,l_name,experience)");
      
              System.out.print("INSERT INTO Mechanic (id,fname,lname,experience) VALUES (" + maxIDint + "," + f_name + "," + l_name + "," + experience + ")\n");
        
				esql.executeUpdate("INSERT INTO Mechanic(id,fname,lname,experience) VALUES (" + maxIDint + ",'" + f_name + "','" + l_name + "','" + experience + "')");
      
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public static void AddCar(MechanicShop esql){//3
		try{
			System.out.print("\tEnter vehicle identification number (11 to 17 characters long): $"); //ASUMPTION: Vin is between 11 and 17 long according to https://www.autocheck.com/vehiclehistory/autocheck/en/vinbasics
			 String vin = in.readLine();
			 while(!vinCheck(vin)){
				System.out.print("\nERROR: VIN is less than 11 or greater than 17! Enter VIN again: $");
				vin = in.readLine();
			 }
        
        //CHECK IF VIN ALREADY EXISTS IN SYSTEM!!!!!
			 String searchVin = "SELECT DISTINCT car.vin FROM Car car WHERE car.vin = '";
        searchVin += vin + "';";
			 List<List<String>> existingCarsTable = esql.executeQueryAndReturnResult(searchVin);
			 System.out.print(existingCarsTable);
       if(existingCarsTable.size() != 0 ){
				System.out.print("\nERROR: Car already in system! Going back to menu!\n");
				return;
			 }
        
     		System.out.print("\tEnter make: $");
			String make = in.readLine();
			while(!charCheck32(make)){
				System.out.print("\nERROR: Make character length is less than 1 or greater than 32! Enter make again: $");
				make = in.readLine();
			}
     		System.out.print("\tEnter model: $");
			String model = in.readLine();
			while(!charCheck32(model)){
				System.out.print("\nERROR: Model character length is less than 1 or greater than 32! Enter model again: $");
				model = in.readLine();
			}
     		System.out.print("\tEnter year: $");
			String year = in.readLine();
			while(!yearCheck(year)){
				System.out.print("\nERROR: Year is less than 1970! Enter year again with value equal to or greater than 1970: $");
				year = in.readLine();
			}

      //How do we know who owns the car????

			 //Statement statement = conn.createStatement();
				 //statement.executeUpdate("INSERT INTO Car " + "VALUES (vin,make,model,year)");
				 
			//ask for an owner to assign as according to ER diagram, a car must have an owner
			System.out.print("\tEnter the last name of the customer who owns this car: $");
			String l_name = in.readLine();
				while(!charCheck32(l_name)){
					System.out.print("\nERROR: Last name too long or too short! Enter last name again: $");
					l_name = in.readLine();
				}
			String findCustomerQuery = "SELECT * FROM Customer customer WHERE UPPER(customer.lname) = UPPER('" + l_name + "')"; //This makes sure if someone enters all upper or mix of upper or lower we still find the person eg BOB == bob == bOb
			List<List<String>> customersTable = esql.executeQueryAndReturnResult(findCustomerQuery);

			if(customersTable.size() == 0){
				System.out.print("\nNo customer found! Would you like to make a new customer (you can't insert a car without a customer)? (Enter 'Y' or 'N'): $");
				String choice_1 = in.readLine(); //Note: The variable is declared a few lines above!
				while(!boolCheck(choice_1)){
					System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
					choice_1 = in.readLine();
				}
				if(choice_1.equals("Y")){
					AddCustomer(esql);
					return;
				}
				else{
					return;
				}
			}
			else{ //customers exist
				for(int i = 0; i < customersTable.size(); ++i){
					//Index | Fname | Lname | Phone | Address
					System.out.println("INDEX: " + Integer.toString(i + 1) + " FIRST NAME: " + customersTable.get(i).get(1) + " LAST NAME: " + customersTable.get(i).get(2) + " PHONE: " + customersTable.get(i).get(3) + " ADDRESS: " + customersTable.get(i).get(4) + "\n");//CHECK HERE IF OUTOFBOUND OCCURS 
				}
        
				System.out.print("\nIs the customer you are looking for listed? (Enter 'Y' or 'N'): $");
				String choice_2 = in.readLine();
				while(!boolCheck(choice_2)){
					System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
					choice_2 = in.readLine();
				}
				if(choice_2.equals("Y")){
					System.out.print("\nEnter index of the customer: $");
           String indexLength = in.readLine();
              while(indexLength.length() == 0 || !indexLength.matches("[0-9]+")){
                  System.out.print("\nInvalid length! Enter index of the customer: $");
					        indexLength = in.readLine();
               }
               int custIndex = Integer.parseInt(indexLength);
               System.out.print(custIndex + "\n");
               int tableSize = customersTable.size();
               System.out.print(tableSize + "\n");
               while(custIndex < 1 || custIndex > tableSize){ //CHECK TO SEE IF THIS IS THE CORRECT BOUND!!!!!
               System.out.print("\nERROR: Invalid index value. Please enter a valid index value: $");
               custIndex = Integer.parseInt(in.readLine());
					}
           
           int currCustID = Integer.parseInt(customersTable.get(custIndex - 1).get(0));

					System.out.print("INSERT INTO Car (vin,make,model,year) VALUES (" + vin + "," + make + "," + model + "," + year + ")\n");
        
				  esql.executeUpdate("INSERT INTO Car(vin,make,model,year) VALUES ('" + vin + "','" + make + "','" + model + "'," + year + ")");
          
          String q = "SELECT MAX(ownership_id) FROM Owns";
				  List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(q);
				  int maxIDint = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;
                                       
          System.out.print("INSERT INTO Owns(ownership_id,customer_id,car_vin) VALUES (" + maxIDint + "," + currCustID + "," + vin + ")\n");
        
				  esql.executeUpdate("INSERT INTO Owns(ownership_id,customer_id,car_vin) VALUES (" + maxIDint + "," + currCustID + ",'" + vin + "')");                             

					//statement.executeUpdate("INSERT INTO Car " + "VALUES (vin,make,model,year)"); //FIXME: Also what happens if a car already exists?

					//Adding an Owns record
					//Find the largest OwnsID (ownership_id)
					//Insert new Owns record using the customer ID, VIN, and ownership ID
						//currCustID is the customerID (I think). Use this in the owns function!
				
					//statement.executeUpdate("INSERT INTO Owns " + "VALUES (vin,make,model,year)")
				}
				else{
					System.out.print("\nWould you like to make a new customer (you can't insert a car without a customer)? (Enter 'Y' or 'N'): $");
					String choice_3 = in.readLine(); //Note: The variable is declared a few lines above!
					while(!boolCheck(choice_3)){
					System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
					choice_3 = in.readLine();
					}
					if(choice_3 == "Y"){
						AddCustomer(esql);
						return;
					}
					else{
						return; //goes to menu
					}
				}
			}


//			 System.out.print("INSERT INTO Car (vin,make,model,year) VALUES (" + vin + "," + make + "," + model + "," + year + ")\n");
        
//				esql.executeUpdate("INSERT INTO Car(vin,make,model,year) VALUES (" + vin + ",'" + make + "','" + model + "','" + year + "')");
   
   
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
 
//-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public static void InsertServiceRequest(MechanicShop esql){//4
		try{
   //DECLARE VARIABLES HERE SINCE THEY DON'T WORK BELOW
   String vin = "";
   int custIDVal = 0;
			//int custIndex = -1
			System.out.print("\tEnter last name of customer: $");
				String l_name = in.readLine();
				while(!charCheck32(l_name)){
					System.out.print("\nERROR: Last name too long or too short! Enter last name again: $");
					l_name = in.readLine();
				}
			String findCustomerQuery = "SELECT * FROM Customer customer WHERE UPPER(customer.lname) = UPPER('" + l_name + "')"; //This makes sure if someone enters all upper or mix of upper or lower we still find the person eg BOB == bob == bOb
			List<List<String>> customersTable = esql.executeQueryAndReturnResult(findCustomerQuery);

			if(customersTable.size() == 0){
				//No customer found. Ask if insert new one? If yes, do it and continue. Else, go back to menu
				System.out.print("\nNo customer found! Would you like to make a new customer? (Enter 'Y' or 'N'): $");
				String choice_1 = in.readLine(); //Note: The variable is declared a few lines above!
					while(!boolCheck(choice_1)){
						System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
						choice_1 = in.readLine();
					}
					if(choice_1.equals("Y")){
						//make new customer
						AddCustomer(esql); //Note: What should happen after this runs? As it stands, if this runs, the query above is outdated so we'd need to run it again. Maybe make a function?
						//Maybe run insert service request function again and then break? Needs testing!
						InsertServiceRequest(esql); //try this and see how it works
						return;
					}
					else{
             System.out.print("\nreturning to menu\n");
						return; //go to menu
					}
			}
			else{
				
				for(int i = 0; i < customersTable.size(); ++i){
					//Index | Fname | Lname | Phone | Address

					System.out.println("INDEX: " + Integer.toString(i + 1) + " FIRST NAME: " + customersTable.get(i).get(1) + " LAST NAME: " + customersTable.get(i).get(2) + " PHONE: " + customersTable.get(i).get(3) + " ADDRESS: " + customersTable.get(i).get(4) + "\n");//CHECK HERE IF OUTOFBOUND OCCURS 
				}
				System.out.print("\nIs the customer you are looking for listed? (Enter 'Y' or 'N'): $");
				String choice_2 = in.readLine();
				while(!boolCheck(choice_2)){
					System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
					choice_2 = in.readLine();
				}
				
				if(choice_2.equals("Y")){
					System.out.print("\nEnter index of the customer: $");
           String indexLength = in.readLine();
              while(indexLength.length() == 0 || !indexLength.matches("[0-9]+")){
                  System.out.print("\nInvalid length! Enter index of the customer: $");
					        indexLength = in.readLine();
               }
               int custIndex = Integer.parseInt(indexLength);
					//int custIDint = Integer.parseInt(custID);
					int tableSize = customersTable.size();
					while(custIndex < 1 || custIndex > tableSize){ //CHECK TO SEE IF THIS IS THE CORRECT BOUND!!!!!
						System.out.print("\nERROR: Invalid index value. Please enter a valid index value: $");
						custIndex = Integer.parseInt(in.readLine());
					}
           
           			int currCustID = Integer.parseInt(customersTable.get(custIndex - 1).get(0));

					String getCars = "SELECT owns.car_vin, car.make, car.model, car.year FROM Owns owns, Car car WHERE owns.customer_id = " + currCustID + " AND car.vin = owns.car_vin;";
					List<List<String>> ownedCarsTable = esql.executeQueryAndReturnResult(getCars);

					if(ownedCarsTable.size() == 0){
						System.out.print("\n!!!!!!!!!!!!!! IF WE SEE THIS, THERE IS A CUSTOMER WITH NO CAR!!!!!!!!!!!!!!\n");
						//AddCar(esql);
						return;
					}
					//else{
						//Choose exisitng car or add new one
						for(int i = 0; i < ownedCarsTable.size(); ++i){
							System.out.println("INDEX: " + Integer.toString(i + 1) + " CAR VIN: " + ownedCarsTable.get(i).get(0) + " CAR MAKE: " + ownedCarsTable.get(i).get(1) + " CAR MODEL: " + ownedCarsTable.get(i).get(2) + " CAR YEAR: " + ownedCarsTable.get(i).get(3));	
						}
						System.out.print("\nTo select a car and create a service request, enter '1'. If you would like to add a new car and enter a service request, enter '2': $");
						String newChoice = in.readLine();
						while(!choiceCheck(newChoice)){
							System.out.print("\nERROR: Invalid choice! To select a car and create a service request, enter '1'. If you would like to add a new car and enter a service request for it, enter '2': $");
							newChoice = in.readLine();
						}
						if(newChoice.equals("2")){
							
							AddCar(esql);
							//So we added a car. If we run the below, we should get the latest ownershrip_ID of the car we just put in
							
							getCars = "SELECT MAX(owns.ownership_id) FROM Owns owns, Car car WHERE owns.customer_id = " + currCustID + " AND car.vin = owns.car_vin;"; 
							List<List<String>> custLastCar = esql.executeQueryAndReturnResult(getCars);

							String ownedID = custLastCar.get(0).get(0);
							
							
							getCars = "SELECT car_vin FROM Owns WHERE ownership_id = " + ownedID + ";";
							List<List<String>> custCar = esql.executeQueryAndReturnResult(getCars);
							
							vin = custCar.get(0).get(0);

							//DEBUG
							System.out.print(vin);


							//proceed to add request
						}
						if(newChoice.equals("1")){
							//Select a car and then create a service request
               //System.out.println("ELSE IF");
							System.out.print("\n Enter the index value of the car you would like to create request for: $");
                                               
							
                                        
              int carIndex = Integer.parseInt(in.readLine());
               //System.out.println(carIndex);
							int carTableSize = ownedCarsTable.size();
							while(carIndex < 1 || carIndex > carTableSize){ //CHECK TO SEE IF THIS IS THE CORRECT BOUND!!!!!
								System.out.print("\nERROR: Invalid index value. Please enter a valid index value: $");
								carIndex = Integer.parseInt(in.readLine());
							}

							vin = ownedCarsTable.get(carIndex - 1).get(0);
                                                 //System.out.println(vin);


							
							//Index is within bounds, parse through and get the VIN of the car we want
						}
							//We have the vin from above

							String currRidquery = "SELECT MAX(rid) FROM Service_Request";
							List<List<String>> currRid = esql.executeQueryAndReturnResult(currRidquery);
							int rid = Integer.parseInt(currRid.get(0).get(0)) + 1;

							//We now have the rid

							//The date util gives us the date
							

							//ADD DATE UTIL
							Date currDate = new Date();

							SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy");

							String today = ft.format(currDate);

							//String insertQuery = "INSERT INTO Service_Request " vin;
							//Get customer id and keep it to add to insert (we can get it from the Own table where vin matches)'

							
							String quickID = "SELECT customer_id FROM Owns WHERE car_vin = '" + vin + "';";
							List<List<String>> custIDTable = esql.executeQueryAndReturnResult(quickID);
							custIDVal = Integer.parseInt(custIDTable.get(0).get(0));


							System.out.print("\n Enter the odometer value of the car you would like to create request for: $");
							int odometerVal = Integer.parseInt(in.readLine());

							System.out.print("\n Enter the complaint from the customer: $");
							String complaint = in.readLine();

							//generate query with custIdVal, vin, rid, complaint, and date
                                    
                                    
         
         
         
             System.out.print("INSERT INTO Service_Request(rid,customer_id,car_vin,date,odometer,complain) VALUES (" + rid + "," + custIDVal + "," + vin + "," + today + "," + odometerVal + "," + complaint + ")\n");
        
		        esql.executeUpdate("INSERT INTO Service_Request(rid,customer_id,car_vin,date,odometer,complain) VALUES (" + rid + ",'" + custIDVal + "','" + vin + "','" + today + "','" + odometerVal + "','" + complaint + "')");
					//}

				}
				else{ //Might want to change it else if?
					System.out.print("\nWould you like to make a new customer record? (Enter 'Y' or 'N'): $");
					String choice_3 = in.readLine();
					while(!boolCheck(choice_3)){
						System.out.print("\nERROR: Invalid value entered. Please enter 'Y' or 'N': $");
						choice_3 = in.readLine();
					}
					if(choice_3.equals("Y")){
						//make new customer
						AddCustomer(esql);
						InsertServiceRequest(esql); //try this and see how it works
						return;
					}
					else{
               System.out.print("\nreturning to menu\n");
						return; //go to menu
					}
				}

			}

		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
 
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		try{
						//Ask for service request number and employee ID
          System.out.print( "\tEnter service request number: ");
		  String serveRequestString = in.readLine();
      while(serveRequestString.length() == 0 || !serveRequestString.matches("[0-9]+")){
                  System.out.print("\nInvalid number inputted. Enter a valid service request number: $");
					        serveRequestString = in.readLine();
               }
		  int serveRequestNum = Integer.parseInt(serveRequestString);
          //int temp = Integer.parseInt(serveRequestNum);
		  //while(serveRequestNum.length() == 0 || !serveRequestNum.matches("[0-9]+"))
		  while(serveRequestNum < 0 || serveRequestNum > 100000)
          {
            System.out.print("\nInvalid number inputted. Enter service request number: ");
			//serveRequestNum = in.readLine();
			serveRequestNum = Integer.parseInt(in.readLine());
		  }
		  


          System.out.print( "\tEnter employee ID: ");
          String employeeID = in.readLine();
          while(employeeID.length() == 0 || !employeeID.matches("[0-9]+"))
          {
            System.out.print("\nInvalid number inputted. Enter employee ID: ");
            employeeID = in.readLine();
		  }
		  System.out.print("\nRUNNING SEARCH");
		  //String findServiceRequest = "SELECT * FROM Service_Request WHERE rid = " + serveRequestNum + ";";
		  String findServiceRequest = "SELECT * FROM Service_Request WHERE rid = " + serveRequestNum + ";";
		  List<List<String>> serviceTable = esql.executeQueryAndReturnResult(findServiceRequest);

		  System.out.print("\n SEARCH HAS RUN");
		  			
		if(serviceTable.size() == 0){
				System.out.print("\nNo service requests found! Returning to menu\n");
						return; //go to menu
		}
		System.out.print("\n SERVICE REQUEST FOUND\n");
		System.out.print("Request: rid =" + serviceTable.get(0).get(0) + ", customer ID =" + serviceTable.get(0).get(1) + ", car vin =" + serviceTable.get(0).get(2) + "\n");								  
	
	String findEmployeeID = "SELECT * FROM Mechanic mechanic WHERE mechanic.id = '" + employeeID + "';";
		  List<List<String>> employeeTable = esql.executeQueryAndReturnResult(findEmployeeID);
		  				
	if(employeeTable.size() == 0){
				System.out.print("\nNo employeeID found! Returning to menu\n");
						return; //go to menu
	}
	System.out.print("\nEMPLOYEE ID FOUND\n");
	System.out.print("Mechanic: mechanic ID = " + employeeTable.get(0).get(0) + "\n");
 
 
       String query = "SELECT MAX(wid) AS maxID FROM Closed_Request";
		    List<List<String>> maxIDStr = esql.executeQueryAndReturnResult(query);
		    int maxIDint = Integer.parseInt(maxIDStr.get(0).get(0)) + 1;
 
       Date currDate = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy");
        String today = ft.format(currDate);
					//Make sure the closing date is not before the service request date (assuming requests can be closed on the same day)
				//ask for comment
        System.out.print( "\tEnter comment: ");
        String comment = in.readLine();
        while(comment.length() == 0)
          {
            System.out.print("\nInvalid length. Enter comment: ");
            comment = in.readLine();
		      }
				//ask for bill
        System.out.print( "\tEnter bill: $");
        String bill = in.readLine();
        while(bill.length() == 0 || !bill.matches("[0-9]+"))
          {
            System.out.print("\nInvalid input. Enter bill number: $");
            bill = in.readLine();
		      }
       System.out.print("INSERT INTO Closed_Request(wid,rid,mid,date,comment,bill) VALUES (" + maxIDint + "," + serveRequestNum + "," + employeeID + "," + today + "," + comment + "," + bill + ")\n");
			esql.executeUpdate("INSERT INTO Closed_Request(wid,rid,mid,date,comment,bill) VALUES (" + maxIDint + ",'" + serveRequestNum + "','" + employeeID + "','" + today + "','" + comment + "','" + bill + "')");
		}
		catch(Exception e){
			System.err.println(e.getMessage());
		}
	}
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------		
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try{
   //ASSUMING THAT IT MEANS EACH CUSTOMER'S BILL < 100, AND NOT TOTAL CUSTOMER'S BILL < 100
   //Look through closed requests bills and if that number is < 100, store the customers name that belongs to that customers sid
     			String query = "SELECT customer.fname, customer.lname ,close.date, close.comment, close.bill FROM Customer customer, Service_Request service, Closed_Request close WHERE customer.id = service.customer_id AND service.rid = close.rid AND close.bill < 100";
     			List<List<String>> rows = esql.executeQueryAndReturnResult(query);
     		
        System.out.println( "-> Customer First Name and Last Name, Closed Request Date, Comment, and Bill");
       for(int i = 0; i < rows.size(); ++i){
				System.out.println((i + 1) + "); Fname: " + rows.get(i).get(0) + "; Lname: " + rows.get(i).get(1) + "; date: " + rows.get(i).get(2) + "; comment: " + rows.get(i).get(3) + "; bill: $" + rows.get(i).get(4));
      }
          
   		}catch(Exception e){
     			System.err.println(e.getMessage());
   		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try{
         //For each customer, count the number of car vins they have in the owns relation. If the customer sid is connected to more than 20 vins, then store it in a list.

          String query = "SELECT customer.id, customer.fname, customer.lname FROM (SELECT owns.customer_id FROM Owns owns GROUP BY owns.customer_id HAVING COUNT(owns.customer_id) > 20) AS owntwenty, Customer customer WHERE customer.id = owntwenty.customer_id";
     			List<List<String>> rows = esql.executeQueryAndReturnResult(query);
     		  
            for(int i = 0; i < rows.size(); ++i){
				System.out.println((i + 1) + ") ID: " + rows.get(i).get(0) + "; Fname: " + rows.get(i).get(1) + "; Lname: " + rows.get(i).get(1));
      }
            
   		}catch(Exception e){
     			System.err.println(e.getMessage());
   		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try{
       //For each car, check to see if the cars year is less than 1995. Then, check to see if the odometer reading for those cars service requests is lower than 50000 miles

	 		String query = "SELECT DISTINCT car.make, car.model, car.year FROM Car car, Service_Request service WHERE car.vin = service.car_vin AND car.year < 1995 AND service.odometer < 50000";
      List<List<String>> rows = esql.executeQueryAndReturnResult(query);
	 		
      for(int i = 0; i < rows.size(); ++i){
				System.out.println((i + 1) + ") make: " + rows.get(i).get(0) + "; model: " + rows.get(i).get(1) + "; year: " + rows.get(i).get(2));
      }                   
                         
	   }catch(Exception e){
	 		System.err.println(e.getMessage());
    }
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		try{      
      //Count the the number of service requests for each vin. Order cars based on number of service requests with the car with the most service requests at the top. User input for k determines how far you loop through the car table
       String query = "SELECT car.make, car.model, COUNT(*) FROM Service_Request service, Car car WHERE service.car_vin = car.vin GROUP BY car.vin ORDER BY COUNT(*) DESC LIMIT ";      
     
      System.out.print("\tEnter a value for K: $");
         String input = in.readLine();
         while(input.length() == 0 || !input.matches("[0-9]+"))
         {
           System.out.print("\tValue incorrectly entered. Enter a value for K: $");
         input = in.readLine();
         }
         query += input;
      List<List<String>> rows = esql.executeQueryAndReturnResult(query);
	 		
      for(int i = 0; i < rows.size(); ++i){
				System.out.println((i + 1) + ") make: " + rows.get(i).get(0) + "; model: " + rows.get(i).get(1) + "; # of service requests: " + rows.get(i).get(2));
      }                     
                         
	   }catch(Exception e){
	 		System.err.println(e.getMessage());
    }
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//10	
		try{
   //Sum up all of the bills for each customer. Order customers based on total bill with the highest bill at the top

	 		String query = "SELECT customer.id, customer.fname, customer.lname, billSum FROM (SELECT SUM(close.bill) AS billSum, service.customer_id FROM Service_Request service, Closed_Request close WHERE service.rid = close.rid GROUP BY service.customer_id) AS all_request, Customer customer WHERE all_request.customer_id = customer.id ORDER BY all_request.billSum DESC";
      List<List<String>> rows = esql.executeQueryAndReturnResult(query);
	 		
      for(int i = 0; i < rows.size(); ++i){
				System.out.println((i + 1) + ") ID: " + rows.get(i).get(0) + "; Fname:" + rows.get(i).get(1) + "; Lname:" + rows.get(i).get(2)  + "; TotalBill: $" + rows.get(i).get(3));
      }                   
                         
	   }catch(Exception e){
	 		System.err.println(e.getMessage());
    }
	}
 
}
