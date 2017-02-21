
package JDBC;

import java.sql.*;

public class P3 {

    public static void main(String[] args) throws Exception {
        try {
            // Load the driver (registers itself)
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception E) {
            System.err.println("Unable to load driver.");
            E.printStackTrace();
        }
        try {
            // Connect to the database
            Connection conn1;
            String dbUrl = "jdbc:mysql://csdb.cs.iastate.edu:3306/db363abullard";
            String user = "dbu363abullard";
            String password = "MxnxEqFo";

            conn1 = DriverManager.getConnection(dbUrl, user, password);
            System.out.println("*** Connected to the database ***");

            //Statement and ResultSet objects
            Statement s1, s2;
            ResultSet rs1, rs2;

            s1 = conn1.createStatement();
            s2 = conn1.createStatement();

            rs1 = s1.executeQuery("SELECT StudentID, Classification, GPA, CreditHours" + " " +
                                "FROM Student");

            while(rs1.next()) {
                String ID = rs1.getString("StudentID").trim();
                float GPA = rs1.getFloat("GPA");
                int credits = rs1.getInt("CreditHours");

                int numClasses = 0;
                float newGPA = credits * GPA;

                //SQL Query in JDBC
                rs2 = s2.executeQuery("SELECT StudentID, Grade" + " " +
                        "FROM Enrollment" + " " +
                        "WHERE StudentID = " + ID);

                while(rs2.next()) {
                    //convert each letter grade to quality points, and multiply by credit hours of class
                    GPA = scale(rs2.getString("Grade").trim());
                    newGPA += (GPA * 3);
                    numClasses++;
                }

                //divide newGPA by the number of credits taken
                credits += numClasses * 3;
                newGPA /= (float) credits;

                //finally round the GPA
                newGPA = (float) (Math.round(newGPA * 100) / 100.0);

                //Update query
                s2.executeUpdate("UPDATE Student" + " " +
                                      "SET GPA = " + newGPA + "," +
                                      "CreditHours = " + credits + "," +
                                      "Classification = '" + updateClassification(credits) + "' " +
                                      "WHERE StudentID = " + ID);
                rs2.close();
            }

            s1.close();
            s2.close();
            rs1.close();
            
            s1 = conn1.createStatement();
            String senior = "'Senior'";
            int numFound = 0; 
            rs1 = s1.executeQuery("SELECT StudentID, GPA, MentorID, Classification" + " " + "FROM Student" + " " + "WHERE Classification = " + senior + " " + "ORDER BY GPA DESC");
            while(numFound != 5){
            	if(!rs1.next()){
            		//Loop ends
            		break;
            	}else{
            		
            	}
            	float GPA = rs1.getFloat("GPA");
            	String Classification = rs1.getString("Classification");
            	System.out.println(Classification + " " + GPA);
            }

            conn1.close();
            
            
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
            System.out.println("SQLState: " + e.getSQLState());
            System.out.println("VendorError: " + e.getErrorCode());
        }
    }

    //helper method, returns classification as a string depending upon current number or credits
    private static String updateClassification(int credits) {
        if(credits >= 0 && credits <=29)
            return "Freshman";
        else if(credits >= 30 && credits <= 59)
            return "Sophomore";
        else if(credits >= 60 && credits <= 89)
            return "Junior";
        else if(credits >= 90)
                return "Senior";
        else
            return null;
    }

    //helper method, returns letter grade as floating point quality points
    private static float scale(String grade) {

        //Scale for letter grade
        switch(grade) {
            case "A":
                return 4.00f;
            case "A-":
                return 3.66f;
            case "B+":
                return 3.33f;
            case "B":
                return 3.00f;
            case "B-":
                return 2.66f;
            case "C+":
                return 2.33f;
            case "C":
                return 2.00f;
            case "C-":
                return 1.66f;
            case "D+":
                return 1.33f;
            case "D":
                return 1.00f;
            case "F":
                return 0.00f;
            default:
                return 0.00f;
        }
    }
}
