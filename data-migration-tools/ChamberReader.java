package chamberreader;

import java.io.*;

/**
 *
 * @author iluvt
 */
public class ChamberReader {

    /**
     * @param args the command line arguments
     * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
//        convertOrgRecs();
        convertContactRecs();
        
    }
    
    public static void convertOrgRecs() {
        try {
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\iluvt\\Dropbox\\code\\chamberReader\\chamberReps2023.txt"));
            FileWriter writer = new FileWriter("C:\\Users\\iluvt\\Dropbox\\code\\chamberReader\\chamberExport.csv");
        
            System.out.println(ChamberReader.class.getName());
        
            StringBuilder sb = new StringBuilder();
            String line3back = "";
            String line2back = "";
            String line1back = "";
            
            //prime first lines
            String orgName = br.readLine();
            String address = br.readLine();
            String csz = br.readLine();
            String otherStuff = "";
            int lineCount = 3;
            
            
            String line = br.readLine();
            
            //rest of file
            while (line != null) {
                
                
                if (line.toUpperCase().contains(", FL")) {
                    //write/print previous info
                    orgName = line2back;
                    address = line1back;
                    csz = line;
                    
                    if (orgName.substring(0, 2).matches("[0-9][0-9]")) {
                        orgName = line3back;
                        address = line2back;
                        //add2 = line1back;
                        csz = line;
                    }
                    
//                    //fix 2line addr
                    if (orgName.startsWith("("))
                    {
                        orgName = line1back;
                        address = "";
                        //add2 = line1back;
                        csz = line;
                    }
                    
                    //parse csz
                    int loc = csz.indexOf(",");
                    String city = csz.substring(0,loc).trim();
                    String state = csz.substring(loc+1,loc+4).trim();
                    String zip = csz.substring(loc+4).trim();
   
                    writer.append("\"" + orgName + "\",\"" + address + "\",\"" + city + "\",\"" + state + "\",\"" + zip + "\"" + "\n");
                    
//                    System.out.println("Name: " + orgName + " addr: " + address + " csz: " + csz + " Stuff: " + otherStuff);
                    
                    
                    otherStuff = "";                    
                    
                } else {
                    otherStuff += line + "~";
                }

                    
                    
//                System.out.println(line);
//                sb.append(line);
//                sb.append(System.lineSeparator());
                line3back = line2back;
                line2back = line1back;
                line1back = line;
                line = br.readLine();
                lineCount++;
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            System.out.print("Exception: ");
            e.printStackTrace();
        } 

    }
    
    public static void convertContactRecs() {
        try {
            //read in lines, search for representative, then process the lines before as contact and lines after as org
            
            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\iluvt\\Dropbox\\code\\chamberReader\\chamberReps2023.txt"));
            FileWriter writer = new FileWriter("C:\\Users\\iluvt\\Dropbox\\code\\chamberReader\\chamberReps.csv");
//            BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\iluvt\\Dropbox\\npmow\\fundraising\\chamberReps.txt"));
            StringBuilder sb = new StringBuilder();
            String nextLine = br.readLine();
            String lines = "";
            
            while (nextLine != null) 
            {
                while (!nextLine.startsWith("Contact Info")) {
                    if (!nextLine.isEmpty()) {
                        lines += nextLine + "\n";
                    }
                    nextLine = br.readLine();
                }
                lines += nextLine + "\n";

                //split the contact info

                String [] lineArray = lines.split("\n");
//                System.out.format("this array has %s lines%n", lineArray.length);
                System.out.println(lines);
                
                int i=0;
                String contactName = lineArray[i];
                String contactTitle = lineArray[++i];
                String address = lineArray[++i];
                String csz;
                String contactInfo;
                
                if (address.toUpperCase().contains(", FL")) {
                    csz = address;
                    address = "";
                    contactInfo = lineArray[++i];
                } else if (address.contains("Contact")) {
                    contactInfo = address;
                    address = "";
                    csz = "";
                } else {
                    csz = lineArray[++i];
                    if (!csz.toUpperCase().contains(", FL")) {
                        address += "\n" + csz;
                        csz = lineArray[++i];
                    } 
                    contactInfo = lineArray[++i];
                }
                String city = "";
                String state = "";
                String zip = "";
                String email = "";
                String orgName = "";
                String orgAddr = "";
                String orgcsz = "";
                String orgCity = "";
                String orgState = "";
                String orgZip = "";
                
                //parse csz
                int loc = csz.indexOf(",");
                if (loc < 1) {
                    System.out.println("bad csz - contactName: " + contactName + " csz: " + csz);
                } else {
                    city = csz.substring(0, loc).trim();
                    state = csz.substring(loc + 1, loc + 4).trim();
                    zip = csz.substring(loc + 4).trim();
                }
                
                //clean title
                if (contactTitle.startsWith("(")) {
                    contactTitle = contactTitle.substring(1);
                    contactTitle = contactTitle.replace(")", "");
                } else if (!contactTitle.isEmpty() && address.isEmpty()) {
                    address = contactTitle;
                    contactTitle = "";
                }

                //read until you get to the org csv
                lines = "";
                do {
                    lines += br.readLine() + "\n";
                    
                } while (!lines.toUpperCase().contains(", FL")); 
                
                lineArray = lines.split("\n");
                for (i = 0; i < lineArray.length; i++) {
                    String thisline = lineArray[i];
                    if (thisline.contains("@")) {
                        email = thisline;
                    }
                    else if (thisline.contains("Representative")) {
                        orgName = lineArray[++i];
//                        System.out.print(i+ " orgName: " + orgName);
                        
                        orgAddr = lineArray[++i];
                        if (orgAddr.toUpperCase().contains(", FL")) {
                            //no street address
                            orgcsz = orgAddr;
                            orgAddr = "";
                        } else {
                            orgcsz = lineArray[++i];
                            if (!orgcsz.toUpperCase().contains(", FL")) {
                                //assume 4-line orgAddr
                                orgAddr += "\n" + orgcsz;
                                orgcsz = lineArray[++i];
                            }
                        }
                        break;
                    } else {
                        System.out.println("skipping line: " + thisline);
                    }
                }
                //parse csz
                loc = orgcsz.indexOf(",");
                if (loc > 1) {
                    orgCity = orgcsz.substring(0,loc).trim();
                    orgState = orgcsz.substring(loc+1,loc+4).trim();
                    orgZip = orgcsz.substring(loc+4).trim();
                } else {
                    System.out.println("bad orgcsz - orgName: " + orgName + " csz: " + orgcsz);
                }
                
                

//                System.out.format("name: %s, title: %s, address: %s, csz: %s\ncontactInfo: %s, email: %s, orgName: %s, orgAddr: %s, orgCsz: %s%n", 
//                        contactName, contactTitle, address, csz, contactInfo, email, orgName, orgAddr, orgcsz);
                writer.append("\"" + contactName + "\",\"" + contactTitle + "\",\"" + address + "\",\"" + city + "\",\"" + state + "\",\"" + zip  + "\",\""
                        + contactInfo + "\",\"" + email + "\",\"" + orgName + "\",\"" + orgAddr + "\",\"" + orgCity + "\",\"" + orgState + "\",\"" + orgZip
                        + "\"" + "\n");
    //                    
    ////                    System.out.println("Name: " + contactName + " addr: " + address + " csz: " + csz + " Stuff: " + otherStuff);
    //                    
    //                    
    //                    otherStuff = "";                    
    //                    
                
                lines = "";
                nextLine = br.readLine();
                
                if (nextLine == null) {
                    break;
                }
                if (nextLine.isEmpty()) {
                    nextLine = br.readLine();
                }
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            System.out.print("Exception: ");
            e.printStackTrace();
        } 

    }
    
}
