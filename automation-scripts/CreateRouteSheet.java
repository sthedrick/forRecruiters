/*
  Load client info and exception days
   then retrieve routing information from API http://dev.virtualearth.net/REST/V1/Routes?")
   Produce a route sheet for each route including meal counts
   
   Code also includes other ways to read client source files if needed 

 * TODO
    * ERROR - check end date and remove or put NO as needed - it's assuming gpsCoords knows all
    * add header info - GUI site to design header?
    * totals
    *   footer info (total clients, stops)
    * driving best route, rearrange clients
    * convert to pdf
    * Remarks - 
        no bread - in sugar free/etc
    * little 2's, little B's
    * Get info from BigIn API
        GPS alert, gate code - read from bigin
    * Best format for exception file, text file with names from bigin?  
        CustId doesn't show in interface
        No good place for exception dates in bigin
    * colors

    * daily route sheets
    * billing info - post final YES list - custid/date
    
    
 */
package createRouteSheet;

import java.io.BufferedReader;
import java.io.FileReader;
import org.json.*;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.net.URL;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.*;
//import java.text.*;
import util17.*;

public class CreateRouteSheet
{
    public static final Date theMonday = new Date("12/6/2021"); // could pass as arg
    public static final boolean optimizeOrder = true; //optimize order of stops; (true doesn't work yet)
    public static DBAccessUtil util = new DBAccessUtil();
    public static DBAccess source = new DBAccess("npmowClients");
    public static HashMap _uniqueId_entityHandle = new HashMap(3000);
    
    public static final DateFormat df = new SimpleDateFormat("MM/dd/yyyy");

    static HashMap<String, Client> clientMap = new HashMap<>(200);

    public static HashSet<String> exceptionSet = new HashSet();  //name~date
    public static HashMap<String, Date> extendedSet = new HashMap<>(); //name,holdStartDate
    public static LinkedList<String> wayPoints = new LinkedList<>();
//    public static LinkedList<String> directionsList = new LinkedList<>();
    public static int[][] mealCount = new int[7][10];

    public static void main(String args[])
    {   
        Date start = new Date();
        System.out.println("Conversion started at " + start);
        
        boolean useMSAccess = false;
        

//        loadClientInfoFromMSAccessClient(); //from gpsCoords
//        for (int route = 1; route <= 9; route++) {
////            addWaypointsFromAccess(route);
//            directionsList = getRoute(wayPoints);
//            printRoute(route);
//        }
        
        //BigIn version (Access Table is biginContacts - exported from current clients view)
        loadExceptionsFromMSAccess();
        loadClientInfoFromMSAccessBigIn(); //loads into clientMap.put(thisClient.name, thisClient);
//        for (int route = 1; route <= 9; route++) 
        int route = 1;
        {
            addWaypointsFromAccessBigIn(route); //into LinkedList<String> wayPoints
            Route thisRoute = getRoute(wayPoints); //returns LinkedList <String> directionsList
            printRouteBigIn(thisRoute, route);
        }

        
//        System.out.println("Test Code - list of clients");
//        for (Client thisClient : clientMap.values()) {
//            
//            String name = thisClient.name;
//            String address = thisClient.address;
//            
//            System.out.format("Name %s\tAddress: %s%n", name, address);
//        }
        
        
        //loop through each route and add page breaks or dump to individual files
        
//        int route = 1;
//        printTotals();
        
        source.disconnect();
        
        Date end = new Date();
        System.out.println(" Conversion ended at " + end + "\n");
        util.printDateDifference(start, end);
    }
    
    public static void loadClientInfoFromMSAccess()
    {
        // Query source database table
        String tableName = "clients";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "]";
        ResultSet rs = source.performQuery(sql);
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int entityHandle = 0;
        
        
        try
        {
            
            // Loop through the result set
            while (rs.next())
            {
                
                // Read in data from database table
                
                Client thisClient = new Client();
                
                thisClient.clientID = rs.getInt("clientID");
                String first = util.parseNull(rs.getString("first"));
                String last = util.parseNull(rs.getString("last"));
                thisClient.name = (first + " " + last).trim();
                thisClient.address = rs.getString("addr");
                thisClient.phone = util.parseNull(rs.getString("phone"));
                thisClient.instructions = util.parseNull(rs.getString("instructions"));
                thisClient.sugarFree = util.parseNull(rs.getString("sugarFree"));
                
                clientMap.put(thisClient.name, thisClient);
                
                //for testing, we'll read the same file, retrieve client by ID, then add on route, stop
//                int route = rs.getInt("route");
//                String stop = rs.getString("stop");
//                
                
//                
                // #################################################################################
                // #################################################################################
                
//                util.counter(counter);
                counter++;
                if (counter % printDateFeq == 0)
                {
                    Date end = new Date();
                    util.printDateDifference(start, end);
                    start = end;
                }
            }
            rs.close();
            source.closeStatement();
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + entityHandle);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    public static void loadExceptionsFromMSAccess()
    {
        // Query source database table
        String tableName = "MealExceptions";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "]";
        ResultSet rs = source.performQuery(sql);
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int entityHandle = 0;
        
        
        try
        {
            
            // Loop through the result set
            while (rs.next())
            {
                
                // Read in data from database table
                
                /*
                ClientID	ClientName	NoDate	ResumeDate
855	Jim Reynolds	5/26/2021	
                */
                
                int clientID = rs.getInt("clientID");
                Date exceptionDate = rs.getDate("NoDate");
                Date resumeDate = rs.getDate("ResumeDate");
                
                //if no resumeDate, assume that it's only that one day
                
                String hash;
                if (resumeDate == null) {
                    hash = clientID + "~" + df.format(exceptionDate);
                    exceptionSet.add(hash);
//                    System.out.println("exception entry: " + hash);
                } else {
                    Date theDate = exceptionDate;
                    while (theDate.before(resumeDate)) {                        
                        hash = clientID + "~" + df.format(theDate);
//                        System.out.println(hash);
                        exceptionSet.add(hash);
                        theDate = addDays(theDate, 1);
                    }
                }
                
                
                // #################################################################################
                // #################################################################################
                
//                util.counter(counter);
                counter++;
                if (counter % printDateFeq == 0)
                {
                    Date end = new Date();
                    util.printDateDifference(start, end);
                    start = end;
                }
            }
            rs.close();
            source.closeStatement();
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + entityHandle);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    public static void loadClientInfoFromMSAccessClient()
    {
        // Query source database table
        String tableName = "clients";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "]";
        ResultSet rs = source.performQuery(sql);
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int entityHandle = 0;
        
        
        try
        {
            
            // Loop through the result set
            while (rs.next())
            {
                
                // Read in data from database table
                
                Client thisClient = new Client();
                
                thisClient.clientID = rs.getInt("clientID");
                String first = util.parseNull(rs.getString("first"));
                String last = util.parseNull(rs.getString("last"));
                thisClient.name = (first + " " + last).trim();
                thisClient.address = rs.getString("addr");
                thisClient.phone = util.parseNull(rs.getString("phone"));
                thisClient.instructions = util.parseNull(rs.getString("instructions"));
                thisClient.sugarFree = util.parseNull(rs.getString("sugarFree"));
                
                clientMap.put(thisClient.name, thisClient);
                
                //for testing, we'll read the same file, retrieve client by ID, then add on route, stop
//                int route = rs.getInt("route");
//                String stop = rs.getString("stop");
//                
                
//                
                // #################################################################################
                // #################################################################################
                
//                util.counter(counter);
                counter++;
                if (counter % printDateFeq == 0)
                {
                    Date end = new Date();
                    util.printDateDifference(start, end);
                    start = end;
                }
            }
            rs.close();
            source.closeStatement();
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + entityHandle);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    public static void loadClientInfoFromMSAccessBigIn()
    {
        // Query source database table
        String tableName = "biginContacts";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "]";
        ResultSet rs = source.performQuery(sql);
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int entityHandle = 0;        
        
        try
        {
            
            // Loop through the result set
            while (rs.next())
            {
                
                // Read in data from database table
                
                Client thisClient = new Client();
                
//                thisClient.clientID = rs.getString("Contact ID");
                String first = util.parseNull(rs.getString("First Name"));
                String last = util.parseNull(rs.getString("Last Name"));
                thisClient.name = (first + " " + last).trim();
                thisClient.address = rs.getString("Mailing Street");
                thisClient.phone = util.parseNull(rs.getString("Home Phone"));
                thisClient.instructions = util.parseNull(rs.getString("Delivery Instructions"));
                thisClient.sugarFree = util.parseNull(rs.getString("Special Meal Instrux"));
                thisClient.deliveryDays = util.parseNull(rs.getString("Delivery Days"));
                thisClient.numberOfMeals = rs.getInt("Number of Meals");
                
                clientMap.put(thisClient.name, thisClient);
                
                //for testing, we'll read the same file, retrieve client by ID, then add on route, stop
//                int route = rs.getInt("route");
//                String stop = rs.getString("stop");
//                
                
//                
                // #################################################################################
                // #################################################################################
                
//                util.counter(counter);
                counter++;
                if (counter % printDateFeq == 0)
                {
                    Date end = new Date();
                    util.printDateDifference(start, end);
                    start = end;
                }
            }
            rs.close();
            source.closeStatement();
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + entityHandle);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    
    public static void loadExceptionsFromText() {

        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
        String exceptionFile = "MealExceptions.txt"; //tab-delimited

//        System.out.format("Day number %s = %s\n", theMonday, getDayNumber(theMonday));
        if (getDayNumber(theMonday) != 2) {
            System.out.println("You must enter a Monday");
            System.exit(1);
        }

//        int[][] mealCount = new int[7][10];
//        HashSet<String> exceptionSet = new HashSet();  //clientID~date
//        HashMap<String, Date> extendedSet = new HashMap<>(); //clientID,holdStartDate
//        String nameColName = "name";
//        String addrColName = "address";
//        String mealColName = "meals";
//        String routeColName = "Route";

//        int lineCounter = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath + exceptionFile))) {

            br.readLine(); //header - skip
            String line = br.readLine(); //first data line
            while (line != null && !line.startsWith("!!")) {
//                    System.out.println(line); //header
                String[] fields = line.split("\t");
//                String clientID = fields[0].trim();
                String clientName = fields[1].trim();
                String noDateString = fields[2].trim();
                String resumeDateString = fields.length > 3 ? fields[3].trim() : "";

                Date exceptionDate;
                try {
                    exceptionDate = new Date(noDateString);
                } catch (Exception e) {
                    System.out.println("bad date: " + noDateString);
                    line = br.readLine();
                    continue;
                }
                //multiple days with an end date
                Date resumeDate = null;
                if (!resumeDateString.equals("")) {
                    resumeDate = new Date(resumeDateString);
                }
                String hash = "";
                if (resumeDate == null) {
                    hash = clientName + "~" + df.format(exceptionDate);
                    exceptionSet.add(hash);
                    System.out.println("exception entry: " + hash);
                } else {
                    Date theDate = exceptionDate;
                    while (theDate.before(resumeDate)) {
                        hash = clientName + "~" + df.format(theDate);
//                        System.out.println(hash);
                        exceptionSet.add(hash);
                        System.out.println("exception entry: " + hash);
                        theDate = addDays(theDate, 1);
                    }
                }
//                        hash += " extended"; //only for the print statement 2 lines below
//                    System.out.println(hash);
                line = br.readLine();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

    }
    
    public static void loadClientInfoFromTextAndProcessTotals() {

        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
        String routeFilename = "gpsCoords.txt"; //tab-delimited

//        System.out.format("Day number %s = %s\n", theMonday, getDayNumber(theMonday));

        if (getDayNumber(theMonday) != 2) {
            System.out.println("You must enter a Monday");
            System.exit(1);
        }

//        int[][] mealCount = new int[7][10];
//        HashSet<String> exceptionSet = new HashSet();  //clientID~date
//        HashMap<String, Date> extendedSet = new HashMap<>(); //clientID,holdStartDate
        String nameColName = "name";
        String addrColName = "address";
//        String idColName = "ClientID";
        String mealColName = "meals";
        String routeColName = "Route";
        
        int lineCounter = 0;

        try {

            if (routeFilename.isEmpty()) {
                System.out.println("ERROR - no route fileName given");
                return;
            }
            //read it in
            try (BufferedReader br = new BufferedReader(new FileReader(filePath + routeFilename))) {
                //copy columns A-I from GPS Coordinates
                // paste it into routeStops.txt

                String line = br.readLine();
                lineCounter++;
//                System.out.println(line); //header
                String[] fields = line.split("\t");
//                System.out.println("I see " + fields.length + " fields");

                //find Name column
                int nameIndex = getFieldIndex(fields, nameColName);
                int addrIndex = getFieldIndex(fields, addrColName);
//                int idIndex = getFieldIndex(fields, idColName);
                int mealIndex = getFieldIndex(fields, mealColName);
                int routeIndex = getFieldIndex(fields, routeColName);

                int mondayIndex = mealIndex+1;
                int tuesdayIndex = mondayIndex + 1;
                int wednesdayIndex = mondayIndex + 2;
                int thursdayIndex = mondayIndex + 3;
                int fridayIndex = mondayIndex + 4;
                int saturdayIndex = mondayIndex + 5;

//                System.out.println("ClientID index = " + idIndex);
                System.out.println("Name index = " + nameIndex);
                System.out.println("Monday index = " + mondayIndex);
//                    System.out.println(fields[mondayIndex]);

                //read next line (first data line)
                line = br.readLine();
                lineCounter++;
                fields = line.split("\t");
                int route = 0;
                int prevRoute = route;

                System.out.println("Route\tMon\tTue\tWed\tThu\tFri\tSat\tClientName\tClientAddr\tExceptions");

                while (!fields[routeIndex].equals("99") && !line.startsWith("!!")) {
//                    System.out.println(line);

                    if (fields.length > 0) {
                        String clientName = fields[nameIndex].trim();
                        String address = fields[addrIndex].trim();
//                        String clientID = fields[idIndex].trim();
                        String mealsString = fields[mealIndex].trim();
                        String routeString = fields[routeIndex].trim();
                        
                        try {
                            route = Integer.parseInt(routeString);
                        } catch (Exception e) {
                            System.out.format("bad route: %s on line %s%n", routeString,lineCounter);
                            route = 0;
                        }
                        if (route != prevRoute) {
                            if (route < prevRoute) {
                                System.out.println("ERROR - routes are not sorted properly");
                                return;
                            }
                            System.out.println("===============================");
                            prevRoute = route;
                        }
                        
                        int [] personMeals = new int[7];
                        boolean hasException = false;

                        //add meals to counter - checking for exceptions
                        for (int day = 0; day < 6; day++) {
                            int index = mondayIndex + day;
                            Date thisDate = addDays(theMonday,day);
                            String dayMealsString = fields[index].trim();

                            //*** replace x with number of meals, then adjust for exceptions ***//
                            int dayMeals = 0;
                            if (dayMealsString.equals("x")) {
                                dayMealsString = mealsString;
                            }

                            try {
                                dayMeals = Integer.parseInt(dayMealsString);
                            } catch (Exception ex) {
                                //ignore
                            }

                            //check for exception
                            
                            String hash = clientName + "~" + df.format(thisDate);
//                            System.out.println("searching for: " + hash);
                            if (exceptionSet.contains(hash)) {
                                System.out.println("exception found " + clientName + " - cancel meal for " + df.format(thisDate));
                                hasException = true;
                                dayMeals = 0;
                            }
                            //check for extended hold
                            Date holdDate = extendedSet.get(clientName);
                            if (holdDate != null && !holdDate.after(thisDate)) {
                                System.out.format("extended exception found " + clientName + " - holdDate: %s - cancel meal for %s%n",df.format(holdDate),df.format(thisDate));
                                hasException = true;
                                dayMeals = 0;
                            }
                            
                            mealCount[day][route] += dayMeals;
                            personMeals[day] = dayMeals;
//                                System.out.format("mealcount %s %s = %s\n",day,route,mealCount[day][route]);

                        }
                        
                        //print person line
                        System.out.print(route+"\t");
                        for (int i = 0; i < 6; i++) {
                            System.out.print(personMeals[i] + "\t");
                        }
                        System.out.print(clientName);
                        System.out.print("\t"+address);
                        System.out.println(hasException ? "\tException(s)" : "");

//                            String address = fields[8]; //column I
//                            if (!address.trim().isEmpty()) {
//                                lineCount++;
//                                address = address.replaceAll("[ ,#]", " ");
//                                address = address.replaceAll("\\s+", "%20");
//                                address = address.replaceAll(" ", "%20");
//                                address = address.replaceAll(",", "%20");
//                                address = address.replaceAll("#", "%20");
//                                if (!wayPoints.contains(address))
//                                    wayPoints.add(address);
//                            }
                    }
                    line = br.readLine();
                    lineCounter++;
                    fields = line.split("\t");
                }

                br.close();
            } catch (Exception ex) {
                System.out.println("error on line: " + lineCounter);
//                    System.out.println(ex);
                ex.printStackTrace();
//                if (lineCount <= 0) {
//                    System.out.println("Not enough lines in file");
//                    System.exit(1);
            }

            //print totals
            System.out.println("--------------TOTALS---------------");
            System.out.println("Route\tMon\tTue\tWed\tThu\tFri\tSat");
            int dayTotal[] = {0, 0, 0, 0, 0, 0, 0};
            int grandTotal = 0;
            for (int route = 1; route < 10; route++) {
                System.out.print(route);
                for (int day = 0; day < 6; day++) {
                    System.out.print("\t" + mealCount[day][route]);
                    dayTotal[day] += mealCount[day][route];
                    grandTotal += mealCount[day][route];
                }
                System.out.println("");
            }
            System.out.println("");

            //print grand totals
            System.out.print("TOTAL");
            for (int day = 0; day < 6; day++) {
                System.out.print("\t" + dayTotal[day]);
            }
            System.out.format("\nTotal for week of: %s: %s%n",df.format(theMonday),grandTotal);

        } catch (Exception ex) {
            System.out.println(ex);
            System.exit(0);
//            Logger.getLogger(getmsmapsdirections.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public static void loadClientInfoFromBiginText() {
    
        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
        String routeFilename = "gpsCoords.txt"; //tab-delimited
        String colSeparator = "\t";

//        System.out.format("Day number %s = %s\n", theMonday, getDayNumber(theMonday));

        if (getDayNumber(theMonday) != 2) {
            System.out.println("You must enter a Monday");
            System.exit(1);
        }

        int lineCounter = 0;

        try {

            if (routeFilename.isEmpty()) {
                System.out.println("ERROR - no route fileName given");
                return;
            }
            //read it in
            try (BufferedReader br = new BufferedReader(new FileReader(filePath + routeFilename))) {
                
                String line = br.readLine();
                lineCounter++;
                System.out.println(line); //header
                String[] fields = line.split(colSeparator);
                System.out.println("I see " + fields.length + " fields");

                //get column indexes
                int fnameCol = getFieldIndex(fields, "First Name");
                int lnameCol = getFieldIndex(fields, "Last Name");
                int phoneCol = getFieldIndex(fields, "Home Phone");
                int addrCol = getFieldIndex(fields, "Mailing Street");
                int mealCountCol = getFieldIndex(fields, "Number of Meals");
                int routeCol = getFieldIndex(fields, "Route ID");
                int instrCol = getFieldIndex(fields, "Delivery Instructions");
                int dietCol =  getFieldIndex(fields, "Special Meal Instrux");

                //read next line (first data line)
                line = br.readLine();
                lineCounter++;
                fields = line.split(colSeparator);
                int route = 0;
                int prevRoute = route;

                while (line != null) {
//                    System.out.println(line);

                    if (fields.length > 0) {
                        
                        Client thisClient = new Client();
                
//                        thisClient.clientID = rs.getInt("clientID");
                        thisClient.name = (fields[fnameCol] + " " + fields[lnameCol]).trim();
                        
                        System.out.println(thisClient.name);
                        
                        thisClient.address = fields[addrCol].trim();
                        thisClient.phone = fields[phoneCol].trim();
                        thisClient.instructions = fields[instrCol].trim();
                        thisClient.sugarFree = (fields.length < dietCol) ? "" : fields[dietCol].trim();

                        clientMap.put(thisClient.name, thisClient);
                        
//                        System.out.println(thisClient.name + "\t" + thisClient.sugarFree);

                    }
                    line = br.readLine();
                    lineCounter++;
                    fields = line.split(colSeparator);
                    
                }
                
                br.close();
                
            } catch(Exception ex) {
                ex.printStackTrace();
    
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public static void addWaypointsFromAccess(int route)
    {
        //start with a clean list each time
        wayPoints.clear();
        
        // Query source database table
        String tableName = "gpsCoords";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "] where route = " + route + " order by route, stop";
        ResultSet rs = source.performQuery(sql);

        //always start at MOW
        wayPoints.add("13600%20Tamiami%20Trl%20North%20Port%20FL%2034287");

        try
        {
            // Loop through the result set
            while (rs.next()) {

                String address = rs.getString("address");
                if (!address.trim().isEmpty() && !address.trim().startsWith("13600 Tamiami")) {
                    address = address.replaceAll("[ ,#]", " ");
                    address = address.replaceAll("\\s+", "%20");
//                                address = address.replaceAll(" ", "%20");
//                                address = address.replaceAll(",", "%20");
//                                address = address.replaceAll("#", "%20");
//                                if (!wayPoints.contains(address))
                    wayPoints.add(address);
                }
            }
            rs.close();
            source.closeStatement();
            
            //always end at walmart
            wayPoints.add("17000%20Tamiami%20Trl%20North%20Port%20FL%2034287");

            
        }
        catch (Exception e)
        {
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    public static void addWaypointsFromAccessBigIn(int route)
    {
        //start with a clean list each time
        wayPoints.clear();
        
        // Query source database table
        String tableName = "biginContacts";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "] where [Route Name] = 'Route " + route + "'";
        ResultSet rs = source.performQuery(sql);

        //always start at MOW
        wayPoints.add("13600%20Tamiami%20Trl%20North%20Port%20FL%2034287");

        try
        {
            // Loop through the result set
            while (rs.next()) {

                String street = rs.getString("Mailing Street");
                String address = street + ", North Port FL";
                if (!address.trim().isEmpty() && !address.trim().startsWith("13600 Tamiami")) {
                    address = address.replaceAll("[ ,#]", " ");
                    address = address.replaceAll("\\s+", "%20");
//                                address = address.replaceAll(" ", "%20");
//                                address = address.replaceAll(",", "%20");
//                                address = address.replaceAll("#", "%20");
//                                if (!wayPoints.contains(address))
                    wayPoints.add(address);
                }
            }
            rs.close();
            source.closeStatement();
            
            //always end at walmart
            wayPoints.add("17000%20Tamiami%20Trl%20North%20Port%20FL%2034287");

            
        }
        catch (Exception e)
        {
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        System.out.println(" DONE");
    }
    
    public static Route getRoute(LinkedList wayPoints)
    {
        Route thisRoute = new Route();
        thisRoute.resultList = new LinkedList<>();
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int entityHandle = 0;

        //variables to set before running - prompt boxes would be better
        
//        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
//        String wpFilename = "routeStops.txt";
        
        // Query source database table

        System.out.println("--------------------------------------------------------");

        String key = "AnFNOfn4rd7TqvKYmRMqTAgp-q_FuD1cuUPBzlOV7ODb-hvWCtC6wUL4ozQr-fOU";

        for (Object wpObject : wayPoints) {
            System.out.println(wpObject.toString());
        }

        StringBuilder geoCallString = new StringBuilder();
        geoCallString.append("http://dev.virtualearth.net/REST/V1/Routes?");

        for (int i = 0; i < wayPoints.size(); i++) {
            String wp = (String) wayPoints.get(i);
            if (wp != null) {
                geoCallString.append(i > 0 ? "&" : "");
                geoCallString.append("wp.").append(i).append("=").append(wp);
            }
        }

        if (optimizeOrder) {
            geoCallString.append("&optimizeWaypoints=true");
        }
        geoCallString.append("&distanceUnit=mi");
        geoCallString.append("&key=").append(key);

        System.out.println(geoCallString);

        int compassDirection = 0;

        // read from the URL
        try {
            URL url = new URL(geoCallString.toString());
            String str;
            try (Scanner scan = new Scanner(url.openStream())) {
                str = new String();
                while (scan.hasNext()) {
                    str += scan.nextLine();
                }
            }
            System.out.println(str + "\n\n");

            // build a JSON object
            JSONObject obj = new JSONObject(str);
            if (!obj.getString("statusDescription").equals("OK")) {
                return null;
            }
            // get the first result
            JSONObject resSets = obj.getJSONArray("resourceSets").getJSONObject(0);
            JSONObject res = resSets.getJSONArray("resources").getJSONObject(0);

            thisRoute.travelDistance = res.getDouble("travelDistance");
            thisRoute.travelDuration = res.getDouble("travelDuration");

            if (optimizeOrder) {
                JSONArray wayPointOrder = res.getJSONArray("waypointsOrder");
                System.out.println("\n\nOptimized waypoint order: " + wayPointOrder + "\n\n");
                thisRoute.wayPointOrder = "" + wayPointOrder;
            }
            
            System.out.format("Total route distance %3.2f miles\n",thisRoute.travelDistance);
            System.out.format("Travel duration: %3.2f minutes %n",thisRoute.travelDuration/60);

            //get routeLegs (destinations)
            JSONArray routeLegs = res.getJSONArray("routeLegs");

            int endCompass = 0;
            for (Iterator legIterator = routeLegs.iterator(); legIterator.hasNext();) {
                JSONObject routeLeg = (JSONObject) legIterator.next();

                String startLocation = routeLeg.getJSONObject("startLocation").getString("name");
                String endLocation = routeLeg.getJSONObject("endLocation").getString("name");
//                System.out.println("going from " + startLocation + " to " + endLocation);
                
                StringBuilder directionBlock = new StringBuilder("");
//                directionBlock.append("\ngoing from " + startLocation + " to " + endLocation + "\n");
                
                JSONArray itineraryItems = routeLeg.getJSONArray("itineraryItems");

                    //stops from 1 routeLeg to the next
                // 0 is 'depart' the place you stopped at
                for (Iterator it = itineraryItems.iterator(); it.hasNext();) {

                    JSONObject item = (JSONObject) it.next();
//                        compassDirection 
                    JSONObject instruction = item.getJSONObject("instruction");
                    String directionText = instruction.getString("text");
                    
                    //shorten turn description
                    directionText = directionText.replaceAll("Turn left onto ", "L on ");
                    directionText = directionText.replaceAll("Turn right onto ", "R on ");
                    
                    double distance = item.getFloat("travelDistance");
//                    System.out.print(directionText);
                    if (!directionText.startsWith("Arrive")) {
                        directionBlock.append(directionText + "; ");
                    } else {
                        directionBlock.append("Arrive ***");
                    }
                    
                    if (distance > .5) {
//                        System.out.format(" go %2.2f mi", distance);
                        String x = String.format(" go %2.2f mi", distance);
//                        System.out.print(x);
                        directionBlock.append(x);
                    }
//                    System.out.println("");
                    directionBlock.append(" ");
//                    if (directionText.startsWith("Arrive")) {
//                            endCompass = item.
//                        System.out.println("");
//                        directionBlock.append(" *** ");
//                    }
                }
                thisRoute.resultList.add(directionBlock.toString());
            }
            
            //Printing instructions with index number
            System.out.println("index\tinstructions");
            for (int i = 0; i < thisRoute.resultList.size(); i++) {
                String string = thisRoute.resultList.get(i);
                System.out.println(i+"\t"+string);
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
//                Logger.getLogger(getmsmapsdirections.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println(" DONE");
        return thisRoute;
    }
    
    public static void printRoute(Route thisRoute, int route)
    {
        // Query source database table
        String tableName = "gpsCoords";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "] where route = " + route + " ORDER BY route,stop";
        System.out.println(sql);
        ResultSet rs = source.performQuery(sql);
        
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int clientID = 0;
        int currRoute = 1;
        
        
        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
                
        try
        {
            int monIndex = rs.findColumn("Tues") - 1; //monday column head often is renamed to date
            
            try (FileWriter htmlWriter = new FileWriter(filePath + "routesheet"+ route + ".html")) {
                htmlWriter.append("<HTML DIR=LTR>\n" +
                        "<HEAD>\n" +
                        "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=Windows-1252\">\n" +
                        "</HEAD>\n" +
                        "<BODY>\n");
                
                String styleHeader =
                        "<!-- Start Styles. Move the 'style' tags and everything between them to between the 'head' tags -->\n" +
                        "<style type=\"text/css\">\n" +
                        ".myTable { background-color:white;border-collapse:collapse; }\n" +
                        ".myTable th { background-color:white;color:black;}\n" +
                        ".myTable td, .myTable th { padding:5px;border:1px solid #000; }\n" +
                        "</style>\n" +
                        "<!-- End Styles -->\n";
                
                htmlWriter.append(styleHeader + "\n");
                
                
//        FileWriter errorWriter = new FileWriter(filePath + "_errorlist.txt");
                
                
                /*
                <HTML DIR=LTR>
                <HEAD>
                <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=Windows-1252">
                </HEAD>
                <BODY>
                <!-- Start Styles. Move the 'style' tags and everything between them to between the 'head' tags -->
                <style type="text/css">
                .myTable { background-color:white;border-collapse:collapse; }
                .myTable th { background-color:white;color:black;}
                .myTable td, .myTable th { padding:5px;border:1px solid #000; }
                </style>
                <!-- End Styles -->
                <table class="myTable">
                <TR><B><head></head><h1 align=center>ROUTE 1</h1>
                <th width="150" border="3">REMARKS</th>
                <th width="300">NAME/ADDRESS</th>
                <th width="20">M</th>
                <th width="20">T</th>
                <th width="20">W</th>
                <th width="20">TH</th>
                <th width="20">FR</th>
                <th width="20">SA</th>
                <th width="300">DIRECTIONS/INSTRUCTIONS</th></TR></B>
                */
                //html file header
                
                
                
                String htmlHeader =
                        "<table class=\"myTable\">\n" +
                        "<TR><B><head></head><h1 align=center>ROUTE " + route + " </h1>\n" +
                        "<th width=\"150\" border=\"3\">REMARKS</th>\n" +
                        "<th width=\"250\">NAME/ADDRESS</th>\n" +
                        "<th width=\"20\">M</th>\n" +
                        "<th width=\"20\">T</th>\n" +
                        "<th width=\"20\">W</th>\n" +
                        "<th width=\"20\">TH</th>\n" +
                        "<th width=\"20\">FR</th>\n" +
                        "<th width=\"20\">SA</th>\n" +
                        "<th width=\"350\">DIRECTIONS/INSTRUCTIONS</th></TR></B>";
                htmlWriter.append(htmlHeader);
                
                
                
                // Loop through the result set
                int stopIndex = -1;
                while (rs.next())
                {
                    //** if doing multiple routes - pagebread and new header here
                    
                    //skip the NPMOW record -- assuming we're starting here - maybe later we'll start at multiple locations
                    String name = DBAccessUtil.parseNull(rs.getString("name"));
                    if (name.equals("Meals on Wheels")) {
                        continue;
                    }
                    stopIndex++;
                    
                    String stop = DBAccessUtil.parseNull(rs.getString("stop"));
//                    clientID = rs.getInt("clientID"); //search by name
                    Client thisClient = clientMap.get(name);

                    if (thisClient == null) {
                        System.out.println("client not found: " + name);
                        thisClient = new Client();
                        thisClient.name = name;
                        thisClient.address = util.parseNull(rs.getString("address"));
                        thisClient.sugarFree = "Client not found";
                    }

                    String remarks = thisClient.sugarFree;
                    String phone = thisClient.phone; 
                    String nameAddressBlock = thisClient.name + "<BR>" + thisClient.address + "<BR>" + phone;

//                    String directions = "driving directions here";
                    String directions = thisRoute.resultList.get(stopIndex);
                    
                    //special directions fixes
                    directions = directions.replaceAll("US-41 S / FL-45 / Tamiami Trail", "US41");
                    
                    String instructions = thisClient.instructions;
                    //<h6 style="color:blue;font-size:14px;">
                    String directionInstructionBlock = directions + "&nbsp;&nbsp;<B>" + instructions + "</B>";
                    
                    System.out.format("Index: %s\tDirections: %s\n", stopIndex, directionInstructionBlock);
                    
                    String [] dayString = new String[7];
                    String [] noString = new String[7];
                    
                    

                    dayString[1] = util.parseNull(rs.getString(monIndex));
                    dayString[2]  = util.parseNull(rs.getString("Tues"));
                    dayString[3]  = util.parseNull(rs.getString("Wed"));
                    dayString[4]  = util.parseNull(rs.getString("Thurs"));
                    dayString[5]  = util.parseNull(rs.getString("Fri"));
                    dayString[6]  = util.parseNull(rs.getString("Sat"));
                    
//                    remarks = thisClient.sugarFree.isEmpty() ? "" : "FRI SPECIAL";
                    if (remarks.equalsIgnoreCase("NO")) {
                        remarks = "";
                    }
                    //TODO - no bread, no soup, 
                    //TODO - 2 MEALS
                    //TODO - variable schedule
                    //TODO - new route, gate code, other notes for this week - in gps data
                    
                    
                    htmlWriter.append("<TR>\n" +
                            "<TD DIR=LTR ALIGN=CENTER>"+remarks+"</TD>\n" +
                            "<TD DIR=LTR ALIGN=LEFT style=\"max-width:300px;\">"+nameAddressBlock+"</TD>\n"); 
                    
                    int [] personMeals = new int[7];
                    boolean hasException = false;
        
                    for (int i = 1; i <= 6; i++) {
                        String mealString = dayString[i];
                        noString[i] = (dayString[i].equals("1") || dayString[i].equals("2") || dayString[i].equals("x")) ? "" : "NO";
                        
                        int mealInteger = 0;
                        try {
                            mealInteger = Integer.parseInt(mealString);
                        } catch (Exception ex) {
                            //ignore
                        }
                        //search for exceptions
                        Date thisDate = addDays(theMonday, i-1);
                        String hash = clientID + "~" + df.format(thisDate);
                        //                            System.out.println("searching for: " + hash);
                        if (exceptionSet.contains(hash)) {
                            System.out.println("exception found " + thisClient.name + " - cancel meal for " + df.format(thisDate));
                            noString[i] = "<I>NO</I>";
                            hasException = true;
                            mealInteger = 0;
                        }
                        
                        mealCount[i][route] += mealInteger;
                        personMeals[i] = mealInteger;
                        
                        htmlWriter.append("<TD DIR=LTR ALIGN=CENTER>"+noString[i]+"</TD>\n");
                        
                    }
                    //print person line
                        System.out.print(route+"\t");
                        for (int i = 0; i < 6; i++) {
                            System.out.print(personMeals[i] + "\t");
                        }
                        System.out.print(thisClient.name);
                        System.out.print("\t"+thisClient.address);
                        System.out.println(hasException ? "\tException(s)" : "");

                    
                    htmlWriter.append("<TD DIR=LTR ALIGN=LEFT style=\"max-width:300px;\">"+directionInstructionBlock+"</TD>\n" +
//                        "<TD DIR=LTR ALIGN=LEFT><div class=\"b\">"+directionInstructionBlock+"</div></TD>\n" +
                            "</TR>");
                    
//
                    // #################################################################################
                
//                util.counter(counter);
//                counter++;
//                if (counter % printDateFeq == 0)
//                {
//                    Date end = new Date();
//                    util.printDateDifference(start, end);
//                    start = end;
//                }
                    
                    
                    
                } //end of file
                
                rs.close();
                source.closeStatement();
                
                //footer
                
                htmlWriter.append("</TABLE>\n");
                htmlWriter.append("</BODY>\n");
                htmlWriter.append("</HTML>\n");
                htmlWriter.flush();
            }
            
            
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + clientID);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        
        System.out.println(" DONE");
    }

    public static void printRouteBigIn(Route thisRoute, int route)
    {
        // Query source database table
        String tableName = "biginContacts";
        System.out.println("--------------------------------------------------------");
        
        System.out.println("CONVERTING " + tableName + "...");
        String sql = "SELECT * FROM [" + tableName + "] where [Route Name] = 'Route " + route + "'";
        System.out.println(sql);
        ResultSet rs = source.performQuery(sql);
        
        //*******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED
        int recordCount = source.countRecords(tableName);
        System.out.println("Number of Records: " + recordCount);
        //******************PRINTS OUT HOW MANY RECORDS ARE GOING TO BE CONVERTED        
        
        int counter = 0;
        int printDateFeq = 3000;
        Date start = new Date();
        int clientID = 0;
        int currRoute = 1;
        
        
        String filePath = "C:\\Users\\iluvt\\Google Drive\\Routes\\";
                
        try
        {
//            int monIndex = rs.findColumn("Tues") - 1; //monday column head often is renamed to date
            
            try (FileWriter htmlWriter = new FileWriter(filePath + "routesheet"+ route + ".html")) {
                htmlWriter.append("<HTML DIR=LTR>\n" +
                        "<HEAD>\n" +
                        "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=Windows-1252\">\n" +
                        "</HEAD>\n" +
                        "<BODY>\n");
                
                String styleHeader =
                        "<!-- Start Styles. Move the 'style' tags and everything between them to between the 'head' tags -->\n" +
                        "<style type=\"text/css\">\n" +
                        ".myTable { background-color:white;border-collapse:collapse; }\n" +
                        ".myTable th { background-color:white;color:black;}\n" +
                        ".myTable td, .myTable th { padding:5px;border:1px solid #000; }\n" +
                        "</style>\n" +
                        "<!-- End Styles -->\n";
                
                htmlWriter.append(styleHeader + "\n");
                
                
//        FileWriter errorWriter = new FileWriter(filePath + "_errorlist.txt");
                
                
                /*
                <HTML DIR=LTR>
                <HEAD>
                <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=Windows-1252">
                </HEAD>
                <BODY>
                <!-- Start Styles. Move the 'style' tags and everything between them to between the 'head' tags -->
                <style type="text/css">
                .myTable { background-color:white;border-collapse:collapse; }
                .myTable th { background-color:white;color:black;}
                .myTable td, .myTable th { padding:5px;border:1px solid #000; }
                </style>
                <!-- End Styles -->
                <table class="myTable">
                <TR><B><head></head><h1 align=center>ROUTE 1</h1>
                <th width="150" border="3">REMARKS</th>
                <th width="300">NAME/ADDRESS</th>
                <th width="20">M</th>
                <th width="20">T</th>
                <th width="20">W</th>
                <th width="20">TH</th>
                <th width="20">FR</th>
                <th width="20">SA</th>
                <th width="300">DIRECTIONS/INSTRUCTIONS</th></TR></B>
                */
                //html file header
                
                
                
                String htmlHeader =
                        "<table class=\"myTable\">\n" +
                        "<TR><B><head></head><h1 align=center>ROUTE " + route + " </h1>\n" +
                        "<th width=\"150\" border=\"3\">REMARKS</th>\n" +
                        "<th width=\"250\">NAME/ADDRESS</th>\n" +
                        "<th width=\"20\">M</th>\n" +
                        "<th width=\"20\">T</th>\n" +
                        "<th width=\"20\">W</th>\n" +
                        "<th width=\"20\">TH</th>\n" +
                        "<th width=\"20\">FR</th>\n" +
                        "<th width=\"20\">SA</th>\n" +
                        "<th width=\"350\">DIRECTIONS/INSTRUCTIONS</th></TR></B>";
                htmlWriter.append(htmlHeader);
                
                
                
                // Loop through the result set
                int stopIndex = -1;
                while (rs.next())
                {
                    //** if doing multiple routes - pagebreak and new header here
                    
                    //skip the NPMOW record -- assuming we're starting here - maybe later we'll start at multiple locations
                    String firstname = DBAccessUtil.parseNull(rs.getString("First Name"));
                    String lastname = DBAccessUtil.parseNull(rs.getString("Last Name"));
                    String name = (firstname + " " + lastname).trim();
                    if (name.equals("Meals on Wheels")) {
                        continue;
                    }
                    stopIndex++;
                    
//                    String stop = DBAccessUtil.parseNull(rs.getString("stop"));
//                    clientID = rs.getInt("clientID"); //search by name
                    Client thisClient = clientMap.get(name);

                    if (thisClient == null) {
                        System.out.println("client not found: " + name);
                        thisClient = new Client();
                        thisClient.name = name;
                        thisClient.address = util.parseNull(rs.getString("address"));
                        thisClient.sugarFree = "Client not found";
                    }

                    String remarks = thisClient.sugarFree;
                    String phone = thisClient.phone; 
                    String nameAddressBlock = thisClient.name + "<BR>" + thisClient.address + "<BR>" + phone;
                    String deliveryDays = thisClient.deliveryDays;
                    int numberOfMeals = thisClient.numberOfMeals;

//                    String directions = "driving directions here";
                    String directions = thisRoute.resultList.get(stopIndex);
                    
                    //special directions fixes
                    directions = directions.replaceAll("US-41 S / FL-45 / Tamiami Trail", "US41");
                    
                    String instructions = thisClient.instructions;
                    //<h6 style="color:blue;font-size:14px;">
                    String directionInstructionBlock = directions + "&nbsp;&nbsp;<B>" + instructions + "</B>";
                    
                    System.out.format("Index: %s\tDirections: %s\n", stopIndex, directionInstructionBlock);
                    
                    String [] dayString = new String[7];
                    String [] noString = new String[7];

//                    dayString[1] = util.parseNull(rs.getString(monIndex));
//                    dayString[2]  = util.parseNull(rs.getString("Tues"));
//                    dayString[3]  = util.parseNull(rs.getString("Wed"));
//                    dayString[4]  = util.parseNull(rs.getString("Thurs"));
//                    dayString[5]  = util.parseNull(rs.getString("Fri"));
//                    dayString[6]  = util.parseNull(rs.getString("Sat"));
                    
                    
                    dayString[1] = deliveryDays.contains("Mon") ? ""+numberOfMeals : "";
                    dayString[2] = deliveryDays.contains("Tue") ? ""+numberOfMeals : "";
                    dayString[3] = deliveryDays.contains("Wed") ? ""+numberOfMeals : "";
                    dayString[4] = deliveryDays.contains("Thu") ? ""+numberOfMeals : "";
                    dayString[5] = deliveryDays.contains("Fri") ? ""+numberOfMeals : "";
                    dayString[6] = deliveryDays.contains("Sat") ? ""+numberOfMeals : "";
                    
                    
//                    remarks = thisClient.sugarFree.isEmpty() ? "" : "FRI SPECIAL";
                    if (remarks.equalsIgnoreCase("NO")) {
                        remarks = "";
                    }
                    //TODO - no bread, no soup, 
                    //TODO - 2 MEALS
                    //TODO - variable schedule
                    //TODO - new route, gate code, other notes for this week - in gps data
                    
                    
                    htmlWriter.append("<TR>\n" +
                            "<TD DIR=LTR ALIGN=CENTER>"+remarks+"</TD>\n" +
                            "<TD DIR=LTR ALIGN=LEFT style=\"max-width:300px;\">"+nameAddressBlock+"</TD>\n"); 
                    
                    int [] personMeals = new int[7];
                    boolean hasException = false;
        
                    for (int i = 1; i <= 6; i++) {
                        String mealString = dayString[i];
                        noString[i] = (dayString[i].equals("1") || dayString[i].equals("2") || dayString[i].equals("x")) ? "" : "NO";
                        
                        int mealInteger = 0;
                        try {
                            mealInteger = Integer.parseInt(mealString);
                        } catch (Exception ex) {
                            //ignore
                        }
                        //search for exceptions
                        Date thisDate = addDays(theMonday, i-1);
                        String hash = clientID + "~" + df.format(thisDate);
                        //                            System.out.println("searching for: " + hash);
                        if (exceptionSet.contains(hash)) {
                            System.out.println("exception found " + thisClient.name + " - cancel meal for " + df.format(thisDate));
                            noString[i] = "<I>NO</I>";
                            hasException = true;
                            mealInteger = 0;
                        }
                        
                        mealCount[i][route] += mealInteger;
                        personMeals[i] = mealInteger;
                        
                        htmlWriter.append("<TD DIR=LTR ALIGN=CENTER>"+noString[i]+"</TD>\n");
                        
                    }
                    //print person line
                        System.out.print(route+"\t");
                        for (int i = 0; i < 6; i++) {
                            System.out.print(personMeals[i] + "\t");
                        }
                        System.out.print(thisClient.name);
                        System.out.print("\t"+thisClient.address);
                        System.out.println(hasException ? "\tException(s)" : "");

                    
                    htmlWriter.append("<TD DIR=LTR ALIGN=LEFT style=\"max-width:300px;\">"+directionInstructionBlock+"</TD>\n" +
//                        "<TD DIR=LTR ALIGN=LEFT><div class=\"b\">"+directionInstructionBlock+"</div></TD>\n" +
                            "</TR>");
                    
//
                    // #################################################################################
                
//                util.counter(counter);
//                counter++;
//                if (counter % printDateFeq == 0)
//                {
//                    Date end = new Date();
//                    util.printDateDifference(start, end);
//                    start = end;
//                }
                    
                    
                    
                } //end of file
                
                rs.close();
                source.closeStatement();
                
                //footer
                
                htmlWriter.append("</TABLE>\n");
                htmlWriter.append("</BODY>\n");
                htmlWriter.append("</HTML>\n");
                htmlWriter.flush();
            }
            
            
        }
        catch (Exception e)
        {
            System.out.println("Error occurred on entityHandle " + clientID);
            e.printStackTrace();
//            target.processKnownExceptions(e);
        }
//        target.commit();
        
        System.out.println(" DONE");
    }
    public static void printTotals() {
        //print totals
        System.out.println("--------------TOTALS---------------");
        System.out.println("Route\tMon\tTue\tWed\tThu\tFri\tSat");
        int dayTotal[] = {0, 0, 0, 0, 0, 0, 0};
        int grandTotal = 0;
        for (int rt = 1; rt < 10; rt++) {
            System.out.print(rt);
            for (int day = 1; day <= 6; day++) { //monday is 1
                System.out.print("\t" + mealCount[day][rt]);
                dayTotal[day] += mealCount[day][rt];
                grandTotal += mealCount[day][rt];
            }
            System.out.println("");
        }
        System.out.println("");

        //print grand totals
        System.out.print("TOTAL");
        for (int day = 0; day < 6; day++) {
            System.out.print("\t" + dayTotal[day]);
        }
        System.out.format("\nTotal for week of: %s: %s%n", df.format(theMonday), grandTotal);

    }
    
        
    public static int getDayNumber(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_WEEK);
    }
    
    public static Date addDays (Date date, int x) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, x);  // number of days to add
        date = c.getTime();  // dt is now the new date
        return date;
    }

   
     public static int getFieldIndex(String [] fields, String fieldName) {
        //find Name column
        int fieldIndex = 0;
        while (fieldIndex < fields.length && !fields[fieldIndex].equals(fieldName)) {
            fieldIndex++;
        }
        return fieldIndex;
    }


}

   


class Client {
    String name = "";
    String address = "";
    String phone = "";
    String sugarFree = "";
    String instructions = "";
    String deliveryDays = "";
    int numberOfMeals = 1;
    int clientID = 0;
}

class Route {
    LinkedList<String> resultList;
    String wayPointOrder = "";
    double travelDistance = 0.0;
    double travelDuration = 0.0;

    
}