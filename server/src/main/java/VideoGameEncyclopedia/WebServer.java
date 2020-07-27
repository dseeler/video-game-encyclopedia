package VideoGameEncyclopedia;

import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.*;

public class WebServer {
    public void start(int port, String password) throws Exception{

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

final class HttpRequest implements Runnable{
    final static String CRLF = "\r\n";
    Socket socket;
    String password;

    public HttpRequest(Socket socket, String password) throws Exception{
        this.socket = socket;
        this.password = password;
    }

    public void run() {
        try {
            processRequest();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }

    private void processRequest() throws Exception{
        // Get a reference to the socket's input and output streams.
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Get the request line of the HTTP request message.
        String requestLine = br.readLine();

        // Extract the game title from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        tokens.nextToken(); // skip over the method, which should be "GET"
        String query = tokens.nextToken();
        if (!query.contains("favicon")) {
            //query = convertString(query);
            String category = getCategory(query);
            String item = getItem(query);

            // Query database
            boolean gameExists = false;
            String url = "jdbc:mysql://localhost:3306/?useTimezone=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(url, "root", password);
            Statement stmnt = conn.createStatement();
            stmnt.executeUpdate("show databases;");
            stmnt.executeUpdate("use video_game_encyclopedia;");
            Statement stmnt2 = conn.createStatement();
            String entityBody = "";
            switch (category) {
                case "game":
                ResultSet result = stmnt.executeQuery("select title " +
                        "from Game where title like '%" + item + "%';");
                if (result.next()) {
                    gameExists = true;
                    entityBody = makeJsonGame(item, stmnt, stmnt2, 1);
                }
                break;
                case "genre":
                    result = stmnt.executeQuery("select title from Game g1, Genre g2 " +
                            "where g1.id = g2.gameId AND genre = '" + item + "';");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, stmnt, stmnt2, 2);
                    }
                    break;
                case "year":
                    result = stmnt.executeQuery("select title from Game where releaseDate" +
                            " like '" + item + "%'");
                    if (result.next()) {
                        gameExists = true;
                        entityBody = makeJsonGame(item, stmnt, stmnt2, 3);
                    }
                    break;
            }

            // Construct the response message.
            String statusLine = null;
            String contentTypeLine = null;
            if (gameExists) {
                statusLine = "HTTP/1.1 200 OK" + CRLF;
                contentTypeLine = "Content-type: application/json" + CRLF;
                //Statement stmnt2 = conn.createStatement();
                //entityBody = makeJsonGame(query, stmnt, stmnt2);
            } else {
                statusLine = "HTTP/1.1 404 Not Found" + CRLF;
                contentTypeLine = "Content-type: " + "text/html" + CRLF;
                entityBody = "<HTML>" +
                        "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                        "<BODY>Not Found</BODY></HTML>";
            }

            os.writeBytes(statusLine);
            os.writeBytes(contentTypeLine);
            os.writeBytes(CRLF);
            os.writeBytes(entityBody);

            // Close the streams and socket.
            os.close();
            br.close();
            socket.close();
        }
    }
    private static String convertString(String str){
        String newString = str.substring(1, str.length());
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    private static String getCategory(String str){
        String newString = str.substring(1, str.length());
        newString = newString.substring(0, newString.indexOf('/'));
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    private static String getItem(String str){
        String newString = str.substring(1, str.length());
        int size = newString.length();
        newString = newString.substring(newString.indexOf('/') + 1, size);
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    private static int countLines(String str){
        String[] lines = str.split("\r\n|\r|\n");
        return  lines.length;
    }

    private static String makeJsonGame(String item, Statement stmnt, Statement stmnt2,
                                       int searchType) throws Exception {
        int id = 0;
        int metacriticScore = 0;
        String title = null;
	String description = null;
        String releaseDate = null;
        String imageLink = null;
        ArrayList<String> genresList = new ArrayList<>();
        ArrayList<String> platformsList = new ArrayList<>();
        ArrayList<String> storesList = new ArrayList<>();
        ArrayList<Game> games = new ArrayList<>();

        ResultSet result = null;

        if (searchType == 1){
            result = stmnt.executeQuery("select * " +
                    "from Game where title like '%" + item + "%';");
        }
        else if (searchType == 2){
            result = stmnt.executeQuery("select * from Game g1, Genre g2 " +
                    "where g1.id = g2.gameId AND genre = '" + item + "';");
        }
        else if (searchType == 3){
            result = stmnt.executeQuery("select * " +
                    "from Game where releaseDate like '" + item + "%';");
        }
        while (result.next()) {
            stmnt2.executeUpdate("show databases;");
            stmnt2.executeUpdate("use video_game_encyclopedia;");

            id = Integer.parseInt(result.getString("id"));
            title = result.getString("title");
	    description = result.getString("description").replaceAll("[^\\x00-\\x7F]", " ");
            releaseDate = result.getString("releaseDate");
            metacriticScore = Integer.parseInt(result.getString("metacriticScore"));
            imageLink = result.getString("imageLink");


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

            Game game = new Game(id, title, description, releaseDate, metacriticScore,
				 imageLink, genre, platform, store);
            games.add(game);
        }
        Gson gson = new Gson();
        Game[] gamesArray = new Game[games.size()];
        for (int i = 0; i < games.size(); i++){
            gamesArray[i] = games.get(i);
        }
        return gson.toJson(gamesArray);
    }
}
