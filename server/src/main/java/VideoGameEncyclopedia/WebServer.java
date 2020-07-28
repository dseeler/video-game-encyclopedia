package VideoGameEncyclopedia;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

import com.google.gson.Gson;

/**
 * Runs the backend web server that the frontend will use to communicate with the database
 */
public class WebServer {
    public void start(int port, String password) throws Exception {

        // Establish listen socket
        ServerSocket serverSocket = new ServerSocket(port);

        // Process requests in an infinite loop
        while (true) {
            Socket connectionSocket = serverSocket.accept();
            HttpRequest request = new HttpRequest(connectionSocket, password);
            Thread thread = new Thread(request);
            thread.start();
        }
    }
}

/**
 * Handles HTTP requests
 */
final class HttpRequest implements Runnable {

    final static String CRLF = "\r\n";
    Socket socket;
    String password;

    /**
     * HttpRequest Constructor
     * @param socket connection socket
     * @param password local MySQL server password
     * @throws Exception
     */
    public HttpRequest(Socket socket, String password) throws Exception {
        this.socket = socket;
        this.password = password;
    }

    /** Process requests */
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Processes an HTTP request
     */
    private void processRequest() throws Exception {
        // Get a reference to the socket's input and output streams.
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();

        // Extract the game title from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String query = tokens.nextToken();
        if (!query.contains("favicon")) { // favicons are automatically picked up by socket
            //query = convertString(query);
            String category = getCategory(query); // the type of query (e.g. genre, year)
            String item = getItem(query); // the item being searched (e.g. Minecraft, 2016)

            // Establish connection to local MySQL server
            String url = "jdbc:mysql://localhost:3306/?useTimezone=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(url, "root", password);
            Statement stmnt = conn.createStatement();

            // Select video_game_encyclopedia database
            stmnt.executeUpdate("show databases;");
            stmnt.executeUpdate("use video_game_encyclopedia;");
            boolean gameExists = false;
            String entityBody = "";

            // Execute different queries based on category
            switch (category) {
                // Game query
                case "game":
                    ResultSet result = stmnt.executeQuery("select title " +
                            "from Game where title like '%" + item + "%';");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, conn, 1);
                    }
                    break;
                // Genre query
                case "genre":
                    result = stmnt.executeQuery("select title from Game g1, Genre g2 " +
                            "where g1.id = g2.gameId AND genre = '" + item + "';");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, conn, 2);
                    }
                    break;
                // Year query
                case "year":
                    result = stmnt.executeQuery("select title from Game where releaseDate" +
                            " like '" + item + "%'");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, conn, 3);
                    }
                    break;
                // Genre and year query
                case "genre&year":
                    String genre = item.substring(0, item.indexOf(' '));
                    String year = item.substring(item.indexOf(' ') + 1, item.length());
                    result = stmnt.executeQuery("select * from Genre JOIN Game ON genre.gameId = Game.id " +
                            "where Game.releaseDate like '" + year + "%' AND Genre.genre = '" + genre + "';");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, conn, 4);
                    }
                    break;
                // Bucket action
                case "bucket":
                    // Return games saved in bucket
                    if (item.equals("getgames")) {
                        result = stmnt.executeQuery("select title from Game g, Bucket b where " +
                                "g.id = b.gameId");
                        if (result.next()) {
                            gameExists = true;
                            entityBody = makeJsonGame(item, conn, 5);
                        }
                    }
                    // Add game to bucket
                    else {
                        stmnt.executeUpdate("SET FOREIGN_KEY_CHECKS=0;");
                        stmnt.executeUpdate("INSERT INTO Bucket (userId, gameId) VALUES (" +
                                123 + ", " + Integer.parseInt(item) + ")");
                        gameExists = true;
                        entityBody = "Game added to Bucket";
                    }
                    break;
            }

            // Construct the response message.
            String statusLine = null;
            String contentTypeLine = null;
            String cors = null;
            if (gameExists) {
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-type: application/json" + CRLF;
                cors = "Access-Control-Allow-Origin: *" + CRLF;
            } else {
                statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                cors = "Access-Control-Allow-Origin: *" + CRLF;
                entityBody = "No results found";
            }

            // Write to client
            os.writeBytes(statusLine);
            os.writeBytes(contentTypeLine);
            os.writeBytes(cors);
            os.writeBytes(CRLF);
            os.writeBytes(entityBody);

            // Close the streams and socket.
            os.close();
            br.close();
            socket.close();
        }
    }

    /**
     * Parses a query to get the category
     * @param str query
     * @return the category
     */
    private static String getCategory(String str) {
        String newString = str.substring(1, str.length());
        newString = newString.substring(0, newString.indexOf('/'));
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    /**
     * Parses a query to get the item
     * @param str
     * @return the item
     */
    private static String getItem(String str) {
        String newString = str.substring(1, str.length());
        int size = newString.length();
        newString = newString.substring(newString.indexOf('/') + 1, size);
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    /**
     * Create a Games array containing the games meeting the query and serializes it to json
     * @param item the item to be searched
     * @param conn connection to database
     * @param queryType int value resembling the type of query
     * @return A serialized Json Strig of a Game array
     * @throws Exception
     */
    private static String makeJsonGame(String item, Connection conn, int queryType) throws Exception {
        // Declare and initialize all Game attributes
        int id = 0;
        int metacriticScore = 0;
        String title = null;
        String description = null;
        String releaseDate = null;
        String imageLink = null;
        String clipLink = null;
        ArrayList<String> genresList = new ArrayList<>();
        ArrayList<String> platformsList = new ArrayList<>();
        ArrayList<String> storesList = new ArrayList<>();
        ArrayList<Game> games = new ArrayList<>();

        // Database querying variables
        ResultSet result = null;
        Statement stmnt = conn.createStatement();
        Statement stmnt2 = conn.createStatement();

        // Game query
        if (queryType == 1) {
            result = stmnt.executeQuery("select * " +
                    "from Game where title like '%" + item + "%';");
        }
        // Genre query
        else if (queryType == 2) {
            result = stmnt.executeQuery("select * from Game g1, Genre g2 " +
                    "where g1.id = g2.gameId AND genre = '" + item + "';");
        }
        // Year query
        else if (queryType == 3) {
            result = stmnt.executeQuery("select * " +
                    "from Game where releaseDate like '" + item + "%';");
        }
        // Genre and year query
        else if (queryType == 4) {
            String genre = item.substring(0, item.indexOf(' '));
            String year = item.substring(item.indexOf(' ') + 1, item.length());
            result = stmnt.executeQuery("select * from Genre JOIN Game ON genre.gameId = Game.id " +
                    "where Game.releaseDate like '" + year + "%' AND Genre.genre = '" + genre + "';");
        }
        // Bucket query
        else if (queryType == 5) {
            result = stmnt.executeQuery("select * from Game g, Bucket b where " +
                    "g.id = b.gameId");
        }
        // Retrieve games
        while (result.next()) {
            stmnt2.executeUpdate("show databases;");
            stmnt2.executeUpdate("use video_game_encyclopedia;");

            id = Integer.parseInt(result.getString("id"));
            title = result.getString("title").replaceAll("[^\\x00-\\x7F]", " ");
            description = result.getString("description").replaceAll("[^\\x00-\\x7F]", " ");
            releaseDate = result.getString("releaseDate");
            metacriticScore = Integer.parseInt(result.getString("metacriticScore"));
            imageLink = result.getString("imageLink");
            clipLink = result.getString("clipLink");

            ResultSet result2 = stmnt2.executeQuery("select genre from Genre where gameId = " + id);
            while (result2.next()) {
                genresList.add(result2.getString("genre"));
            }

            result2 = stmnt2.executeQuery("select platform from Platform where gameId = " + id);
            while (result2.next()) {
                platformsList.add(result2.getString("platform"));
            }

            result2 = stmnt2.executeQuery("select store from Store where gameId = " + id);
            while (result2.next()) {
                storesList.add(result2.getString("store"));
            }
            String[] genre = new String[genresList.size()];
            for (int i = 0; i < genresList.size(); i++) {
                genre[i] = genresList.get(i);
            }

            String[] platform = new String[platformsList.size()];
            for (int i = 0; i < platformsList.size(); i++) {
                platform[i] = platformsList.get(i);
            }

            String[] store = new String[storesList.size()];
            for (int i = 0; i < storesList.size(); i++) {
                store[i] = storesList.get(i);
            }

            // Create and add games to games array
            Game game = new Game(id, title, description, releaseDate, metacriticScore,
                    imageLink, clipLink, genre, platform, store);

            games.add(game);
        }
        // Convert games array to json string
        Gson gson = new Gson();
        Game[] gamesArray = new Game[games.size()];
        for (int i = 0; i < games.size(); i++) {
            gamesArray[i] = games.get(i);
        }
        // Return json string
        return gson.toJson(gamesArray);
    }
}
