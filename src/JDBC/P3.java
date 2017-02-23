
package JDBC;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;

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
            Statement s1, s2,s3;
            //need this dumb thing so I don't get concurrent modification 
            ArrayList<PreparedStatement> toUpdate = new ArrayList<>();
            ResultSet rs1, rs2,rs3;

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
                String tempID = "'" + ID + "'";
                tempID = tempID.trim();

                //SQL Query in JDBC
                rs2 = s2.executeQuery("SELECT Grade, StudentID" + " " + "FROM Enrollment" + " " + "WHERE StudentID = " + tempID);
                
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
                

                PreparedStatement preparedStatement;
                //Update query
                preparedStatement = conn1.prepareStatement("UPDATE Student" + " " +
                        "SET GPA = ? , CreditHours = ? , Classification = ?" + " " + 
                        "WHERE StudentID = ?");
                preparedStatement.setFloat(1, newGPA);
                preparedStatement.setInt(2,credits);
                preparedStatement.setString(3, updateClassification(credits));
                preparedStatement.setString(4, ID);     
                toUpdate.add(preparedStatement);


                rs2.close();
            }
            
            s1.close();
            s2.close();
            rs1.close();
            for(PreparedStatement st: toUpdate){
            	st.executeUpdate();
            }
                
            s1 = conn1.createStatement();
            s2 = conn1.createStatement();
            s3 = conn1.createStatement();
            String senior = "'Senior'";
            int numFound = 0; 
            float previousGPA = 0;
            rs1 = s1.executeQuery("SELECT StudentID, GPA, MentorID, Classification" + " " + "FROM Student" + " " + "WHERE Classification = " + senior + " " + "ORDER BY GPA DESC");
		    PrintWriter writer = new PrintWriter("C:/Users/mitch/local_repo/CS363/src/P3Output.txt", "UTF-8");
            while(numFound != 5){
            	if(!rs1.next()){
            		//Loop end
            		break;
            	}else{
            		int ID = rs1.getInt("StudentID");
            		int mentorID = rs1.getInt("MentorID");
            		rs2 = s2.executeQuery("SELECT Name,ID" + " " + "FROM Person" + " " + "WHERE ID = " + ID);
            		rs3 = s3.executeQuery("SELECT Name,ID" + " " + "FROM Person" + " " + "WHERE ID = " + mentorID);
            		if(rs1.getFloat("GPA") == previousGPA && numFound ==  4){
            			//do not increment numFound because the GPA is not unique
            		}else{
            			numFound++;
            		}
            		previousGPA = rs1.getFloat("GPA");
            		
            		String name =  "";
            		String mentorName = "";
            		if (!rs2.next()){
            			  //No user found.
            		}else {
            			  rs2.first();
            			  name = rs2.getString("Name");
            			}
            		
            		if (!rs3.next()){
            			//No User Found
            		}else{
            			rs3.first();
            			mentorName = rs3.getString("Name");	
            		}
            		
            		writer.println("Name: " + name + ", MentorName: " + mentorName + ", GPA: " + previousGPA);
            		
            		rs2.close();
            		rs3.close();
            	}
            }
            
            rs1.close();
            s1.close();
            s2.close();
            s3.close();
            writer.close();
           
            

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
