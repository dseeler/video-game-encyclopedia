package VideoGameEncyclopedia;

import java.sql.*;
import java.io.*;
import java.util.*;

public class CreateDatabase {
    public static void main(String[] args) {
        try {
            // Connect to local SQL server using JDBC
            String url = "jdbc:mysql://localhost:3306/?useTimezone=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(url, "root", "Run3scap3"); // Your server password

            // Create connection statement
            Statement stmnt = conn.createStatement();

            // Create video_game_encyclopedia database on local SQL server using script
            File file = new File("src/main/resources/video_game_encyclopedia.sql");
            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                PreparedStatement update = conn.prepareStatement(fileScanner.nextLine());
                update.executeUpdate();
            }
            fileScanner.close();

            // Populate Tables

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }//end main
}//end JDBCExample

