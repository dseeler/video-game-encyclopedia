package VideoGameEncyclopedia;

import com.google.gson.*;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;

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

            // Retrieve Data
            for (int i = 1; i <= 25; i++){
                try {
                    URL gameURL = new URL("https://api.rawg.io/api/games?page_size=40&page=" + i);
                    InputStreamReader reader = new InputStreamReader(gameURL.openStream());
                    JsonParser jp = new JsonParser();
                    JsonElement je = jp.parse(reader);
                    JsonObject root = je.getAsJsonObject();
                    JsonArray results = root.getAsJsonArray("results");

                    for (int j = 0; j < results.size(); j++){
                        System.out.println(results.get(j));
                    }
                }
                catch(Exception e){
                    System.out.println(e.getMessage());
                }
            }

            // Populate tables

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }//end main
}//end JDBCExample

