package VideoGameEncyclopedia;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.net.*;
import com.google.gson.*;

public class CreateDatabase {
    public static void main(String[] args) throws Exception{
        try {
            // Prompt user for local SQL server password
            Scanner input = new Scanner(System.in);
            System.out.print("Enter your local MySQL server password: ");
            String password = input.nextLine();

            // Connect to local SQL server using JDBC
            System.out.println("Connecting to local SQL server using JDBC...");
            String url = "jdbc:mysql://localhost:3306/?useTimezone=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(url, "root", password); // Your server password

            // Create connection statement
            Statement stmnt = conn.createStatement();

            // Create video_game_encyclopedia database on local SQL server using script
            System.out.println("Creating video_game_encyclopedia database...");
            stmnt.executeUpdate("DROP SCHEMA IF EXISTS video_game_encyclopedia");
            File file = new File("src/main/resources/video_game_encyclopedia.sql");
            Scanner fileScanner = new Scanner(file);
            while (fileScanner.hasNextLine()) {
                PreparedStatement update = conn.prepareStatement(fileScanner.nextLine());
                update.executeUpdate();
            }
            fileScanner.close();

            // Retrieve Data
            System.out.println("Retrieving games using RAWG.io API...");
            ArrayList<JsonObject> games = new ArrayList<>();
            int count = 100;
            for (int i = 1; i <= 50; i++) {
                URL gameURL = new URL("https://api.rawg.io/api/games?page_size=40&page=" + i);
                InputStreamReader reader = new InputStreamReader(gameURL.openStream());
                JsonParser jp = new JsonParser();
                JsonElement je = jp.parse(reader);
                JsonObject root = je.getAsJsonObject();
                JsonArray results = root.getAsJsonArray("results");
                for (int j = 0; j < results.size(); j++) {
                    //games.add(results.get(j));
                    games.add(results.get(j).getAsJsonObject());
                    if (games.size() >= count) {
                        System.out.println(count + " games retrieved.");
                        count += 100;
                    }
                }
            }

            // Populate tables
            populateTables(games, stmnt);

            // Run server
            System.out.println("Running server...");
            WebServer server = new WebServer();
            server.start(5000, password);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void populateTables(ArrayList<JsonObject> list, Statement stmnt) {
        try {
            // Clear database
            stmnt.executeUpdate("SET SQL_SAFE_UPDATES = 0;");
            stmnt.executeUpdate("DELETE FROM Genre;");
            stmnt.executeUpdate("DELETE FROM Platform;");
            stmnt.executeUpdate("DELETE FROM Store;");
            stmnt.executeUpdate("DELETE FROM Game;");

            System.out.println("Populating local SQL database...");

            int count = 100;
            int inserted = 0;
            // Iterate through every game retrieved
            for (int i = 0; i < list.size(); i++) {
                try {
                    // Populate Game table
                    stmnt.executeUpdate("INSERT INTO Game (id, title, releaseDate, metacriticScore, imageLink)" +
                            " VALUES (" + Integer.parseInt(list.get(i).getAsJsonObject().get("id").toString())
                            + ", '" + trimStr(list.get(i).getAsJsonObject().get("name").toString())
                            + "', '" + trimStr(list.get(i).getAsJsonObject().get("released").toString())
                            + "', " + Integer.parseInt(list.get(i).getAsJsonObject().get("metacritic").toString())
                            + ", '" + trimStr(list.get(i).getAsJsonObject().get("background_image").toString()) + "');");

                    // Populate Genre Table
                    for (int j = 0; j < list.get(i).getAsJsonArray("genres").size(); j++) {
                        stmnt.executeUpdate("INSERT INTO Genre (gameId, genre) VALUES (" +
                                        Integer.parseInt(list.get(i).getAsJsonObject().get("id").toString()) +
                                ", '" + trimStr(list.get(i).getAsJsonArray("genres").get(j).getAsJsonObject()
                                        .get("name").toString()) + "')");
                    }

                    // Populate Platform Table
                    for (int j = 0; j < list.get(i).getAsJsonArray("platforms").size(); j++) {
                        stmnt.executeUpdate("INSERT INTO Platform (gameId, platform) VALUES (" +
                                Integer.parseInt(list.get(i).getAsJsonObject().get("id").toString()) +
                                ", '" + trimStr(list.get(i).getAsJsonArray("platforms").get(j).getAsJsonObject()
                                .get("platform").getAsJsonObject().get("name").toString()) + "')");
                    }

                    // Populate Store Table
                    for (int j = 0; j < list.get(i).getAsJsonArray("stores").size(); j++) {
                        stmnt.executeUpdate("INSERT INTO Store (gameId, store) VALUES (" +
                                Integer.parseInt(list.get(i).getAsJsonObject().get("id").toString()) +
                                ", '" + trimStr(list.get(i).getAsJsonArray("stores").get(j).getAsJsonObject()
                                .get("store").getAsJsonObject().get("name").toString()) + "')");
                    }
                    inserted++;
                    if (inserted >= count){
                        System.out.println(inserted + " games inserted.");
                        count += 100;
                    }

                    // Skip to next game if a null value is present
                } catch (Exception e){
                    if (i < list.size()) {
                        i++;
                    }
                    else {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    // Used to trim quotation marks from Strings retrieved from data
    public static String trimStr(String str) {
        return str.substring(1, str.length() - 1);
    }
}

