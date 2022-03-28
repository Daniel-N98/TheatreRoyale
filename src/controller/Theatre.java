package controller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;

import data_access.DataAccess;
import models.Patron;
import models.Performance;
import util.InputReader;

public class Theatre {

    public final static int seatsCircle = 80;
    public final static int seatsStalls = 120;
    
    private DataAccess dataAccess;

    private final InputReader inputReader;
    
    // The patron that is currently using the application
    private Patron patron;

    /**
     * Theatre constructor
     * Initialtizes global variables and calls displayInterface() to print out the main menu
     */
    public Theatre() {
        this.dataAccess = new DataAccess();
        this.inputReader = new InputReader();
        this.patron = new Patron();
        
        displayInterface();
    }

    /**
     * Print out the main menu interface and prompt the user to enter an option from within the menu.
     * The corresponding method will be called from the option the user entered, or the application will exit.
     * 
     * This menu is the initial display for this application, and will be available until the application is closed or exited with the menu option.
     */
    private void displayInterface() {
        int option;
        boolean exit = false;
        do {
            
            // Interface displayed to the user through the console window
            System.out.println("===========================================");
            System.out.println("| Enter the number to select an option..  |");
            System.out.println("|                                         |");
            System.out.println("| 1 - Browse all available shows          |");
            System.out.println("| 2 - Find show by title                  |");
            System.out.println("| 3 - Find shows by date                  |");
            System.out.println("| 4 - View basket                         |");
            System.out.println("| 5 - Exit                                |");
            System.out.println("|                                         |");
            System.out.println("===========================================");
            System.out.print("\n> ");
            
            // Ensure a digit is provided
            try {
                option = inputReader.getNextInt("");
            } catch (InputMismatchException e) {
                System.out.println("Error: You must enter a digit");
                return;
            }
            // Switch the option and call the correct method, or exit.
            dataAccess = new DataAccess(); // Reopen the database connection
            switch (option) {
                case 1 -> browseShows();
                case 2 -> findShowByName();
                case 3 -> findShowsByDate();
                case 4 -> printBasketMenu();
                case 5 -> exit = true;
            }
        } while (!exit);

        System.out.println("Closing..");
        System.exit(0); // Exit the program
    }


    /**
     * Call the procedure to retrieve all currently scheduled shows, and pass them to the printResults
     * method to print them to the console.
     */
    private void browseShows() {
        
        ResultSet rs = dataAccess.getShows();
        printResults(rs);
    }

    /**
     * Call the procedure to retrieve all scheduled shows by name, and pass them to the printResults
     * method to print them to the console.
     */
    private void findShowByName() {
        
        String name = inputReader.getNextText("\nEnter the show name:\n> ");
        ResultSet rs = dataAccess.getShowByName(name);

        printResults(rs);
    }

    /**
     * Call the procedure to retrieve all scheduled shows on a specific date, and pass them to the printResults
     * method to print them to the console.
     */
    private void findShowsByDate() {
        // Method to be updated to agree with the new date/time format
        String date = inputReader.getNextText("\nEnter the date of which you'd like to see shows for [dd-MM-yy]\n> ");
        ResultSet rs = dataAccess.getShowByDate(date.toString());
        
        printResults(rs);
    }

    /**
     * Print out the results of the provided ResultSet in as a formatted string
     * 
     * Create and store each result as a performance and initialize the object variables.
     * 
     * Once all results have been printed, call addToBasket() to prompt the user to add to basket, or return to the main menu
     * 
     * @param rs ResultSet to print
     */
    private boolean printResults(ResultSet rs) {
        ArrayList<Performance> performancsInSearch = new ArrayList<>(); // All performances found in this search result will be stored here
        try {                
            while (rs.next()) {
                // Iterate through all elements within the ResultSet and print them to the console. 
                System.out.println("\n[Name: " + rs.getString("show_title") +
                        "\n Description: " + rs.getString("show_description") +
                        "\n Date: " + rs.getString("perf_date") +
                        "\n Genre: " + rs.getString("show_genre") +
                        "\n Language: " + rs.getString("primary_language") +
                        "\n Ticket cost: £" + rs.getString("show_ticketPrice") + 
                        "\n ID: " + rs.getInt("perfID") + "]\n");
                
                // Create a performance object and initialize variables
                Performance performance = new Performance(rs.getInt("perfID"), rs.getString("perf_date"),rs.getDouble("show_ticketPrice")); 
                // perfID needs to be returned from the SQL procedure
                
                performancsInSearch.add(performance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addToBasket(performancsInSearch);
        dataAccess.close(); // Close the connection to the database
        return true;
    }
    
    /**
     * Promts the user to enter their details in order to use the checkout system
     * Registers the customer in the database, as well and initalizing the patron object properties.
     * 
     * ** Customer ID can NOT YET be stored **
     */
    private void registerCustomer() {
        // Prompt user to enter their details
        String fname = inputReader.getNextText("> Enter your first name..");
        String lname = inputReader.getNextText("> Enter your last name..");
        String houseNo = inputReader.getNextText("> Enter your house number..");
        String streetName = inputReader.getNextText("> Enter your street name..");
        String postalCode = inputReader.getNextText("> Enter your postal code..");
        
        // Attempts to store the details in the database
        int CID = -1;
        CID = dataAccess.registerCustomer(fname, lname, houseNo, streetName, postalCode);
        if (CID < 0) {
        	System.out.println("Failed to register.");
        	return;
        }
        
        dataAccess = new DataAccess();
        // Initialize patron variables with the values entered above
        patron.setfName(fname);
        patron.setlName(lname);
        patron.setAddress_no(houseNo);
        patron.setAddress_st(streetName);
        patron.setPost_code(postalCode); 
        
        // Retrive the information for this customer and store the ID inside the object.
        
        // Below cannot be used until users details can be stored
        
        ResultSet rs = dataAccess.getCustomerData(CID);
        try {
            patron.setID(rs.getInt("customerID"));
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Prompts the user to enter a performanceID to add a performance to their basket, or return to the main menu.
     * Adds a performance if it exists from the search results ArrayList to the users basket 
     * 
     * @param performanceIDs search results from last search
     */
    private void addToBasket(ArrayList<Performance> performanceIDs) {
        int idSelected = 0;
        try {
            // ID to be selected from the performanceIDs ArrayLisy
            idSelected = inputReader.getNextInt("> Enter the 'Performance ID' to add a performance to your basket, or 'Cancel' to return to the menu\n");
        }catch (NumberFormatException e) {
            System.out.println("Returning to the menu");
            return;
        }
        
        // Ensure the performanceID is within the users search results
        for (Performance performance : performanceIDs) {
            if (performance.getID() == idSelected) { 
                // ID user entered is equal to this performance, add it to basket and return
                patron.addToBasket(performance);
                return;
            }
        }
        // ID user entered did not equal any of the performanceIDs within their search results
        System.out.println("There is no performance with this ID in your search results..");
        addToBasket(performanceIDs); // Recall this method to prompt the user to enter another ID or return to the main menu
    }
    
    /**
     * Calls the printBasket() method to print out all tickets within a users basket to the screen
     * 
     * Prints the Basket Menu, and calls the method the user selects
     * If a customer is NOT registered in the system when "Checking out", they will be promoted to enter their details.
     */
    private void printBasketMenu() {
        
        patron.printBasket(); // Prints out all tickets within a users basket
        
        System.out.println("===========================================");
        System.out.println("| Enter the number to select an option..  |");
        System.out.println("|                                         |");
        System.out.println("| 1 - Remove a ticket from basket         |");
        System.out.println("| 2 - Checkout your basket                |");
        System.out.println("| 3 - Return to main menu                 |");
        System.out.println("|                                         |");
        System.out.println("===========================================");
        
        int option = inputReader.getNextInt(""); // Prompt the user to enter an option from the above menu
        switch (option) {
        case 1:
            patron.removeFromBasketByID(inputReader.getNextInt("Enter the 'Performance ID' to remove a performance from your basket\n")); // Remove a ticket from the users basket
            break;
        case 2:
            if (patron.getfName() == null) { // Check if the current user has already entered their details
                registerCustomer(); // Prompt user to enter their details
            }
            patron.checkoutBasket(); // Empty method, to be implemented
            break;
        }
    }

    public int getSeatsCircle() {
        return seatsCircle;
    }

    public int getSeatsStalls() {
        return seatsStalls;
    }
}
