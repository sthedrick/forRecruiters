package worddocreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class ConvertIntake {
    public static String remarks = "";

    public static void main(String[] args) throws Exception {

        final Date filterDate = new Date("09/17/2021"); //only convert files with update date after this
        String filePath = "C:\\Users\\iluvt\\Google Drive\\clients\\";
//        String filePath = "C:\\Users\\iluvt\\Google Drive\\clients\\formerClients\\";
        
        DateFormat dfShort = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat dfShort2 = new SimpleDateFormat("MM/dd/yy");
        DateFormat dfIntake = new SimpleDateFormat("MMMMM dd, yyyy");
        final Date badDate = new Date("1/1/2099");
        final Date before1900 = new Date("1/1/1900");
        
        FileWriter csvWriter = new FileWriter(filePath + "export.csv");
        FileWriter errorWriter = new FileWriter(filePath + "_errorlist.txt");
        
        //header
        csvWriter.append("intakeDate,constitType,intakeby,first,last,addr,city,state,zip,neighborhood,"
                + "dob,phone,email,"
                + "vet,allergies,sugarFree,startDate,endDate,"
                + "deliverydays,mealCostText,instructions,referralSource,referralPhone,reasonForApp,"
                + "pmtName,pmtAddr,pmtCity,pmtState,pmtZip,pmtPhone,"
                + "kinName,kinAddr,kinCity,kinState,kinZip,kinPhone,"
                + "emergName,emergAddr,emergCsz,emergPhone,"
                + "race,income,remarks,fileModDate,x"
                + "\n");
                
        final File folder = new File(filePath);
        for (final File fileEntry : folder.listFiles()) {
            String fileName = fileEntry.getName();
            Date fileDate = new Date(fileEntry.lastModified());
            if (!fileEntry.isDirectory() && fileDate.after(filterDate) && fileName.endsWith("docx") && !fileName.startsWith("~") && !fileName.startsWith("_intakeFormTemplate")) {
                System.out.println("\n**" + fileName);
                
                

                XWPFDocument docx = new XWPFDocument(new FileInputStream(filePath + fileName));

                //using XWPFWordExtractor Class
                XWPFWordExtractor we = new XWPFWordExtractor(docx);
                String theText = we.getText();
                String[] lines = theText.split("\n");
                int maxIndex = lines.length - 1 ;
                System.out.println("maxIndex: " + maxIndex);
                
//                for (int i = 0; i < lines.length; i++) {
//                    String line = lines[i];
//                    System.out.println(i + ":" + line);
//                }

                

                /*
                 **Abel, Alice.docx
                 MEALS ON WHEELS OF NORTH PORT
                 CLIENT INTERVIEW
                 $4.25/each meal	(off San Marco)							

                 Cancel

                 Reason
                 Date:	October 09, 2019
                 Name:	Alice Abel	DOB:	01/07/1930
                 Address:	4809 XXXX Terrace
                 City:	North Port	State:	FL	ZIP:	34288
                 Telephone:		941-555-5563	Veteran?	NO
                 Email:	Alice.abel@yahoo.com 	Widow of a veteran?  	N/A
                 Start Date:	Monday, October 21, 2019		
                 Remarks:	Just home from rehab	Estimated Ending Date:	unknown
                 Diabetes	YES      NO
                 Food Allergies	   None                                                    MOW does not provide any special meals
                 Delivery Days:	Monday, Wednesday and Friday
                 Instructions for meal delivery	Knock, ring bell, announce MOW, she will come to the door, if not, put food in cooler

                 Referral Source:	Rehab in Port Charlotte	Telephone:	
                 Reason for Application:	
                 */
                
                //import the doc
                
                //**Note - 'counter' should be the line you're on - incremement it as you read it, backup if you dont use it
                
                String header = "";
                remarks = "";
                String mealCost = "";
                
                int counter = 0;
                String thisLine = cleanLine(lines[counter]);
                
                //loop at the top for header
                //might have intake date line at top or bottom, grab notes until then
                while (!thisLine.contains("Date:") && !thisLine.contains("Name:")) {
                    String thisLineUC = thisLine.toUpperCase();
//                    System.out.println(counter + ":" + thisLineUC);
                    if (thisLine.isEmpty()) {
                        //skip
                    } else if (thisLine.contains("@") 
                            || thisLine.startsWith("Number of")
                            || thisLine.contains(" each ")  
                            || thisLine.contains("$")  
                            || thisLineUC.contains("GRATIS")) {
                        mealCost += (mealCost.isEmpty() ? "" : ";") + thisLine;
                    } else if (thisLineUC.startsWith("MEALS ON")) {
                        //do nothing 
                    } else if (thisLineUC.startsWith("CLIENT IN")) {
                        //do nothing 
                    } else if (thisLineUC.equals("NOTES:")) {
                        //do nothing - no notes
                    } else if (thisLineUC.equals("CANCEL")) {
                        //do nothing - no cancel notes
                    } else if (thisLineUC.equals("REASON")) {
                        //do nothing - no reason notes
                    } else {
                        header += (header.isEmpty() ? "" : ";") + thisLine;
                    }
                    thisLine = cleanLine(lines[++counter]);
                }
                
                String intakeBy = "";
                Date intakeDate = badDate;
                String firstName = "";
                String lastName = "";
                Date dob = badDate;
//                String name = "";
                String homeAddr = "";
                String neighborhood = "";
                String city = "";
                String state = "";
                String zip = "";
                String phone = "";
                String veteran = "";
                Date startDate = badDate;
                Date endDate = badDate;
                String endDateString = "";
                String email = "";
                String widow = "";
                String allergiesString = "";
                String diabetesString = "";
                String race = "NO DATA";
                String incomeLevel = "NO DATA";
                String deliveryDays = "";
                
                String instructions = "";
                String referralSource = "";
                String referralPhone = "";
                String reasonForApp = "";
                
                String paymentName = "";
                String paymentAddr = "";
                String pmtcity = "";
                String pmtstate = "";
                String pmtzip = "";
                String paymentPhone = "";
                
                String kinName = "";
                String kinAddr = "";
                String kinCity = "";
                String kinState = "";
                String kinZip = "";
                String kinPhone = "";
                
                String emergName = "";
                String emergPhone = "";
                String emergAddr = "";
                String emergcsz = "";


                for (; counter < lines.length; counter++) {
                    
                    thisLine = cleanLine(lines[counter]);
                    String lineUpper = thisLine.toUpperCase();
                        
//                    System.out.println(counter + ":checking:" + thisLine);
                    
                    //intake Date/Intake By
                    if (thisLine.contains("Intake") || thisLine.startsWith("Date:")) {
                        String intakeDateString = thisLine;
                        intakeDateString = intakeDateString.substring(intakeDateString.indexOf(":") + 1);
                        if (intakeDateString.contains("Intake")) {
                            int intakeByStart = intakeDateString.indexOf("Intake By");
                            intakeBy = intakeDateString.substring(intakeDateString.indexOf(":", intakeByStart) + 1).trim();
                            intakeDateString = intakeDateString.substring(0, intakeByStart - 1);
                        }
                        System.out.println("intakeLine: " + intakeDateString + "-" + intakeBy);
                        try {
                            //DateFormat dfIntake = new SimpleDateFormat("MMMMM dd, yyyy"); //full month spelled out sometimes
                            intakeDate = dfIntake.parse(intakeDateString);
                        } catch (Exception e) {
                            intakeDate = parseDate(intakeDateString);
                            if (intakeDate.equals(badDate)) {
                                remarks += ";bad intake date: " + intakeDateString + "\n";
                            }
                        }
                        //                System.out.println("intake date: " + intakeDate);
                    } else if (thisLine.startsWith("Name:")) {
                        
                        //name - DOB
                        String nameDob = thisLine;
                        int dobLoc = nameDob.indexOf("DOB");
                        String name = nameDob.substring(5, dobLoc).trim();

                        if (name.contains(",")) {
                            //flip
                            String[] nameParts = name.split(",");
                            firstName = nameParts[1].trim();
                            lastName = nameParts[0].trim();
                        } else {
                            String[] names = parseName(name);
                            firstName = names[1];
                            lastName = names[2];
                        }
                        String dobString = nameDob.substring(dobLoc + 4).trim();
                        
                        try {
                            dob = dfShort.parse(dobString);
                        } catch (Exception e) {
                            dob = badDate;
                            remarks += ";Birthdate note: " + dobString;
                        }

                        if (dob.before(before1900)) {
                            dob.setYear(dob.getYear() + 1900);
                        }
                    } else if (thisLine.startsWith("Address:")) {
                        
                        //get the address/neighborhood, csz, phone/vet
                        
                        homeAddr = thisLine;
                        //address/neighborhood
                        //then grab CSZ lines
                        System.out.println("homeaddr: " + homeAddr);
                        homeAddr = homeAddr.substring(homeAddr.indexOf(":")+1).trim();

                        if (homeAddr.contains("Neighbor")) {
                            int nbrhdStart = homeAddr.indexOf("Neighb");
                            neighborhood = homeAddr.substring(nbrhdStart);
                            homeAddr = homeAddr.substring(0, nbrhdStart-1);
                            neighborhood = neighborhood.substring(neighborhood.indexOf(":") +1).trim();
                            System.out.println("addr/neigh: " + homeAddr + "****" + neighborhood);
                        }
                        
                        //grab csz line for home
                        String csz = cleanLine(lines[++counter]); //next line
                        System.out.println("client csz: " + csz);
                        int stateLoc = csz.indexOf("State");
                        int zipLoc = csz.indexOf("ZIP");
                        city = csz.substring(5, stateLoc).trim();
                        state = "FL";
                        zip = csz.substring(zipLoc + 4).trim();
                        
                        String phoneVet = cleanLine(lines[++counter]);
                        System.out.println(counter + ":phonevet:" + phoneVet);
                        if (!phoneVet.contains("Veteran")) {
                            System.out.println("ERROR in phone/veteran line");
                        }
                        int vetLoc = phoneVet.indexOf("Veteran");
                        phone = phoneVet.substring(10, vetLoc).trim();
                        veteran = phoneVet.substring(vetLoc + 8).trim();
                    } else if (thisLine.startsWith("Email:")) {
                        //email/widow
                        if (thisLine.contains("Widow")) { //Widow of a veteran?
                            int loc1 = thisLine.indexOf("Widow");
                            email = thisLine.substring(6, loc1).trim();
                            widow = thisLine.substring(loc1);
                            widow = widow.replace("Widow of a veteran?", "").trim();
                            veteran += ";Widow: " + widow;
                        }
                        else if (thisLine.contains("Spouse")) { //Spouse of a veteran?
                            int loc1 = thisLine.indexOf("Spouse");
                            int loc2 = thisLine.indexOf("?", loc1);
                            email = thisLine.substring(6, loc1).trim();
                            veteran += "Spouse:" + thisLine.substring(loc2).trim();
                        } else {
                            email = thisLine.replace("Email:", "").trim();
                        }
                    } else if (thisLine.contains("Start Date")) {
                        //could be startdate, race, income
                        //could be startdate, delivery days
                        
                        String startDateString = thisLine;
//                        System.out.println("startDateString: " + startDateString);

                        int startDateEnd = thisLine.length();
                        boolean hasRace = thisLine.contains("Ethnicity") || lineUpper.contains("RACE");
                        boolean hasIncome = thisLine.contains("Income");
                        boolean hasWidow = thisLine.contains("Widow");
                        boolean hasDelivery = thisLine.contains("Delivery");
                        
                        int thislineLength = thisLine.length();
                        if (thisLine.contains("Widow")) {
                            widow = thisLine.substring(thisLine.indexOf("Widow") + 6).trim();
                            veteran += (veteran.isEmpty() ? "" : ";") + "Widow: " + widow;
                            startDateEnd = thisLine.indexOf("Widow") -1;
                        }
                        //added race and income to new sheets (start date, race, income)
                        
                        int raceStart = hasRace ? lineUpper.indexOf("RACE") : thislineLength;
                        int incomeStart = hasIncome ? thisLine.indexOf("Income") : thislineLength;
                        int raceEnd = hasIncome ? incomeStart - 1 : thislineLength;
                        int incomeEnd = thislineLength;
                        
//                        System.out.format("racestart: %d raceEnd: %d incomestart: %d incomeEnd: %d%n",raceStart,raceEnd,incomeStart,incomeEnd);

                        if (hasRace) {
                            startDateEnd = raceStart -1 ;
//                            System.out.format("racestart: %d raceEnd: %d incomestart: %d incomeEnd: %d%n",raceStart,raceEnd,incomeStart,incomeEnd);
                            race = thisLine.substring(raceStart, raceEnd);
                            race = race.replace("Race/ Ethnicity: ", "");
                            race = race.replace("Race/Ethnicity:", "");
                            race = race.replace("Race:", "");
                            race = race.replace("RACE ", "");
                            race = race.trim();
                            if (race.isEmpty()) {
                                race = "BLANK";
                            }
                        }
                        if (hasIncome) {
                            incomeLevel = thisLine.substring(incomeStart, incomeEnd);
                            incomeLevel = incomeLevel.substring(incomeLevel.indexOf(":")+1).trim();
                            if (incomeLevel.isEmpty()) {
                                incomeLevel = "BLANK";
                            }
                        }
                        if (hasDelivery) {
                            int delStart = thisLine.indexOf("Delivery");
                            startDateEnd = delStart -1;
                            deliveryDays = thisLine.substring(delStart).replace("Delivery Dates:", "").trim();
                        }

                        startDateString = thisLine.substring(0, startDateEnd);
                        startDateString = startDateString.substring(thisLine.indexOf(":")+1);
                        System.out.println("startDateString: " + startDateString);
                        startDate = parseDate(startDateString);
                    } else if (lineUpper.startsWith("RACE")) { 
                        //doesn't start with startdate
                        //expecting race, income
                        
                        boolean hasRace = true;
                        int raceStart = 0;
                        boolean hasIncome = thisLine.contains("Income");
                        boolean hasWidow = thisLine.contains("Widow");
                        boolean hasDelivery = thisLine.contains("Delivery");
                        
                        int thislineLength = thisLine.length();
                        if (thisLine.contains("Widow")) {
                            widow = thisLine.substring(thisLine.indexOf("Widow") + 6).trim();
                            veteran += (veteran.isEmpty() ? "" : ";") + "Widow: " + widow;
                        }
                        //added race and income to new sheets (start date, race, income)
                        
                        int incomeStart = hasIncome ? thisLine.indexOf("Income") : thislineLength;
                        int raceEnd = hasIncome ? incomeStart - 1 : thislineLength;
                        int incomeEnd = thislineLength;
                        
//                        System.out.format("racestart: %d raceEnd: %d incomestart: %d incomeEnd: %d%n",raceStart,raceEnd,incomeStart,incomeEnd);

                        if (hasRace) {
//                            System.out.format("racestart: %d raceEnd: %d incomestart: %d incomeEnd: %d%n",raceStart,raceEnd,incomeStart,incomeEnd);
                            race = thisLine.substring(raceStart, raceEnd);
                            race = race.replace("Race/ Ethnicity: ", "");
                            race = race.replace("Race/Ethnicity:", "");
                            race = race.replace("Race:", "");
                            race = race.replace("RACE ", "");
                            race = race.trim();
                            if (race.isEmpty()) {
                                race = "BLANK";
                            }
                        }
                        if (hasIncome) {
                            incomeLevel = thisLine.substring(incomeStart, incomeEnd);
                            incomeLevel = incomeLevel.substring(incomeLevel.indexOf(":")+1).trim();
                            if (incomeLevel.isEmpty()) {
                                incomeLevel = "BLANK";
                            }
                        }
                        if (hasDelivery) {
                            int delStart = thisLine.indexOf("Delivery");
                            deliveryDays = thisLine.substring(delStart).replace("Delivery Dates:", "").trim();
                        }

                    } else if (thisLine.startsWith("Sugar Free") || thisLine.startsWith("Diabetes")) {
                        //see iterations
                        //might be sugarfree/allergies on same line (Ruth Arnold)
                        thisLine = thisLine.replace("MOW does not provide any special meals", "").trim();
                        diabetesString = thisLine;
                        if (thisLine.contains("Allergies")) {
                            int loc1 = thisLine.indexOf("Allerg");
                            diabetesString = thisLine.substring(0, loc1-1);
                            allergiesString = thisLine.substring(loc1);
                        }
                        diabetesString = diabetesString.replace("Diabetes ","").trim();
                        diabetesString = diabetesString.replace("Sugar Free Dessert ","").trim();
                        diabetesString = diabetesString.replace("Sugar Free Dessert: ","").trim();
                        diabetesString = diabetesString.replace("YES      NO","VERIFY").trim();
                        allergiesString = allergiesString.replace("Food Allergies: ","").trim();
                        allergiesString = allergiesString.replace("Allergies: ","").trim();
                    } else if (thisLine.startsWith("Food Allergies")) {
                        thisLine = thisLine.replace("MOW does not provide any special meals", "").trim();
                        allergiesString = thisLine.replace("Food Allergies","").trim();
                    } else if (thisLine.startsWith("Delivery")) {
                        deliveryDays = cleanLine(thisLine);
                        deliveryDays = deliveryDays.substring(deliveryDays.indexOf(":") + 1).trim();
                    } else if (thisLine.startsWith("Instructions")) {
                        instructions = thisLine;
                        instructions = instructions.replace("Instructions for meal delivery","").trim();
                        System.out.println("Instructions block: " + instructions);
                        
                        //maybe more lines of instructions
                        thisLine = cleanLine(lines[++counter]);
                        while (!thisLine.isEmpty() && !thisLine.startsWith("Referral") && !thisLine.startsWith("Reason for")) {
                            System.out.println("**more instructions block:" +counter + ":"+ thisLine);
                            instructions += ";" + thisLine;
                            thisLine = cleanLine(lines[++counter]);
                        }
                        counter--;
                    } else if (thisLine.startsWith("Referral Source:")) {
                        String referralSourcePhone = thisLine;
                        while (referralSourcePhone.isEmpty()) {
                            referralSourcePhone = lines[++counter];
                        }   
                        int loc = referralSourcePhone.indexOf(":");
                        int loc2 = referralSourcePhone.indexOf("Tele");
        //                System.out.format("%s|%d|%d\n",referralSourcePhone,loc,loc2);
                        referralSource = referralSourcePhone.substring(loc + 1,loc2).trim();
                        referralPhone = referralSourcePhone.substring(loc2).trim();
                        referralPhone = referralPhone.replace("Telephone:", "").trim();
        //                System.out.println("referralSource: " + referralSource);
        //                System.out.println("referralPhone: " + referralPhone);
                    } else if (thisLine.startsWith("Reason for")) {
                        reasonForApp = cleanLine(thisLine);
                        reasonForApp = reasonForApp.substring(reasonForApp.indexOf(":") + 1).trim();
                    } else if (thisLine.startsWith("Person Respon")) {
                        //payment block -- assumes name, address, csz, phone
//                        System.out.println(counter + ":Payment name:" + thisLine);
                        paymentName = thisLine;
                        paymentName = paymentName.substring(paymentName.indexOf(":")+1).trim();
                        paymentAddr = lines[++counter].replace("Street Address:", "").trim();
                        String paymentcsz = lines[++counter];
//                        System.out.println(counter + ":paymentcsz: " + paymentcsz);
                        int stateLoc = paymentcsz.indexOf("State");
                        int zipLoc = paymentcsz.toUpperCase().indexOf("ZIP");
                        pmtcity = paymentcsz.substring(5, stateLoc).trim();
                        pmtstate = paymentcsz.substring(stateLoc + 6, zipLoc).trim();
                        pmtzip = paymentcsz.substring(zipLoc + 4).trim();
                        paymentPhone = lines[++counter].replace("Telephone Number:", "").trim();
//                        System.out.println(counter + ":paymentPhone:" + paymentPhone);
                    } else if (thisLine.startsWith("Next of")) {
                        //next of kin
                        //might be  name, phone, addr, csz
                        //or        name, addr, phone, csz
                        
                        kinName = thisLine;
                        kinName = kinName.substring(kinName.indexOf(":") + 1).trim();
                        //System.out.println("kinname: " + kinName);

                        thisLine = cleanLine(lines[counter]);
                        while (!thisLine.contains("Emergency")) { //expect kin above emergency
//                            System.out.format("%s kinline: %s%n",counter,thisLine);
                            if (thisLine.contains("Address")) {
                                kinAddr = thisLine;
                                kinAddr = kinAddr.substring(kinAddr.indexOf(":") + 1).trim();
//                                System.out.println("kinaddr: " + kinAddr);
                            } else if (thisLine.contains("City:")) {
                                String kincsz = thisLine;
//                                System.out.println("kincsz: " + kincsz);
                                int stateLoc = kincsz.indexOf("State");
                                int zipLoc = kincsz.indexOf("Zip");
//                                System.out.println("stateLoc: " + stateLoc + " ziploc: " + zipLoc);
                                kinCity = kincsz.substring(5, stateLoc).trim();
                                kinState = kincsz.substring(stateLoc + 6, zipLoc).trim();
                                kinZip = kincsz.substring(zipLoc + 4).trim();
                            } else if (thisLine.contains("phone")) {
                                kinPhone = thisLine;
                                kinPhone = kinPhone.substring(kinPhone.indexOf(":") + 1).trim();
//                                System.out.println("kinphone: " + kinPhone);
                            } else {
                                if (thisLine.isEmpty()) {
                                    kinAddr += ";Note: " + thisLine;
                                } else {
                                    System.out.println("skipping blank line: " + thisLine);
                                }
                            }
                            thisLine = cleanLine(lines[++counter]);
                        }
                        counter--; //back it up
                    } else if (thisLine.startsWith("Emergency")) {
                        //== Emergency contact info - lines could be out of original order ==//

                        //first line is emerg contact name
                        emergName = thisLine;
                        emergName = emergName.substring(emergName.indexOf(":") + 1).trim();

                        int relLoc = emergName.indexOf("Relationship");
                        if (relLoc > 0) {
                            System.out.println("NOTE - relationship included on Emergency contact line");
                        }
                        
                        //emergency block - get 3 lines of info - phone, address, csz (might be out of order)
                        for (int i = 1; i <= 3; i++) { //expect 3 lines of address
                            thisLine = cleanLine(lines[++counter]);
                            if (thisLine.startsWith("Telephone")) {
                                emergPhone = thisLine;
                                emergPhone = emergPhone.substring(emergPhone.indexOf(":") + 1).trim();
//                                System.out.println("emergPhone: " + emergPhone);
                            } else if (thisLine.startsWith("Street")) {
                                emergAddr = thisLine;
                                emergAddr = emergAddr.substring(emergAddr.indexOf(":") + 1).trim();
//                                System.out.println("emergAddr: " + emergAddr);
                            } else if (thisLine.startsWith("City")) {
                                emergcsz = thisLine;
//                                System.out.println(counter + ":emergcsz:" + emergcsz);
                                emergcsz = emergcsz.replace("City:", "");
                                emergcsz = emergcsz.replace("State:", ",");
                                emergcsz = emergcsz.replace("Zip:", "");
                                emergcsz = emergcsz.trim();
                                if (emergcsz.equals(",")) {
                                    emergcsz = "";
                                }
                            } else {
                                System.out.println("extra line in emerg csz: **\n" + thisLine + "**");
                            }
                        }
                    } else {
                        if (thisLine.isEmpty()) {
                            System.out.println("skipping blank line: " + counter);
                        } else {
                            System.out.println(counter + " Unprocessed: " + thisLine);
                        }
                    }
                    
                }
                
                if (!header.isEmpty()) {
                  System.out.println("adding header to remarks: " + header);
                  remarks = header + (remarks.isEmpty() ? "" : "\n") + remarks;
                }
                
                //////////////////////////// cleanups //
                //clean up veteran
                if (veteran.equals("NO;Widow: NO") || veteran.equals("Widow? NO")) {
                    veteran = "NO";
                }
                veteran = veteran.replace("Widow? ", "Widow: ");

                //clean up allergies
                allergiesString = allergiesString.replace("None", "NO");
                allergiesString = allergiesString.replace("NONE", "NO");
                
                deliveryDays = deliveryDays.replace("Delivery Days: ", "");
                
                //write the line
                csvWriter.append(dfShort.format(intakeDate) + ",");
                csvWriter.append("Client,");
                csvWriter.append(intakeBy + ",");
                csvWriter.append("\"" + firstName + "\"" + ",");
                csvWriter.append("\"" + lastName + "\"" + ",");
                csvWriter.append("\"" + homeAddr + "\"" + ",");
                csvWriter.append(city + ",");
                csvWriter.append(state + ",");
                csvWriter.append(zip + ",");
                csvWriter.append("\"" + neighborhood + "\"" + ",");
                csvWriter.append(dfShort.format(dob) + ",");
                
                csvWriter.append("\"" + phone + "\"" + ",");
                csvWriter.append("\"" + email + "\"" + ",");
                csvWriter.append("\"" + veteran + "\"" + ",");
                csvWriter.append("\"" + allergiesString + "\"" + ",");
                csvWriter.append("\"" + diabetesString + "\"" + ",");
                csvWriter.append("\"" + dfShort.format(startDate) + "\"" + ",");
                csvWriter.append("\"" + endDateString + "\"" + ","); //TODO
//                csvWriter.append("\"" + endDate + "\"" + ",");

                 
//                csvWriter.append("\"" + diabetesString + "\"" + ","); //in remarks
                csvWriter.append("\"" + deliveryDays + "\"" + ",");
                csvWriter.append("\"" + mealCost + "\"" + ",");
                csvWriter.append("\"" + instructions + "\"" + ",");
                
                csvWriter.append("\"" + referralSource + "\"" + ",");
                csvWriter.append("\"" + referralPhone + "\"" + ",");
                csvWriter.append("\"" + reasonForApp + "\"" + ",");
                
                csvWriter.append("\"" + paymentName + "\"" + ",");
                csvWriter.append("\"" + paymentAddr + "\"" + ",");
                csvWriter.append("\"" + pmtcity + "\"" + ",");
                csvWriter.append("\"" + pmtstate + "\"" + ",");
                csvWriter.append("\"" + pmtzip + "\"" + ",");
                csvWriter.append("\"" + paymentPhone + "\"" + ",");
                
                
                csvWriter.append("\"" + kinName + "\"" + ",");
                csvWriter.append("\"" + kinAddr + "\"" + ",");
                csvWriter.append("\"" + kinCity + "\"" + ",");
                csvWriter.append("\"" + kinState + "\"" + ",");
                csvWriter.append("\"" + kinZip + "\"" + ",");
                csvWriter.append("\"" + kinPhone + "\"" + ",");
                
                csvWriter.append("\"" + emergName + "\"" + ",");
                csvWriter.append("\"" + emergAddr + "\"" + ",");
                csvWriter.append("\"" + emergcsz + "\"" + ",");
//                csvWriter.append("\"" + emergCity + "\"" + ",");
//                csvWriter.append("\"" + emergState + "\"" + ",");
//                csvWriter.append("\"" + emergZip + "\"" + ",");
                csvWriter.append("\"" + emergPhone + "\"" + ",");
                csvWriter.append("\"" + race + "\"" + ",");
                csvWriter.append("\"" + incomeLevel + "\"" + ",");
                
                remarks = remarks.replaceAll("\n", ";");
                csvWriter.append("\"" + remarks + "\"" + ",");
                csvWriter.append(dfShort.format(fileDate) + ",");

                //================================
                //end of line

                csvWriter.append("x\n");

            }
            
        }
        csvWriter.close();
        errorWriter.close();
        
        System.out.println("TODO be sure to check deliveryDays for extra stuff");
    }
    
    
    
    
    public static String cleanLine(String theLine) {
        String newLine = theLine.trim();
        newLine = newLine.replaceAll("–", "-");
        newLine = newLine.replaceAll("\t", " ");
        newLine = newLine.replaceAll("\n", "; ");
        newLine = newLine.replaceAll("’", "'");
        
        
        return newLine;
        
    }
    public static Date parseDate(String theString) {
        Date defaultDate = new Date("1/1/2099");
        Date theDate;
        try {
            theDate = new Date(theString);
        } catch (Exception e) {
            System.out.println("ERROR cant parse string: " + theString);
            remarks += ";Cant parse date " + theString;
            theDate = defaultDate;
        }
        return theDate;
    }

}
