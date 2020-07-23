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
        query = convertString(query);

        // Query database
        boolean gameExists = false;
        String url = "jdbc:mysql://localhost:3306/?useTimezone=true&serverTimezone=UTC";
        Connection conn = DriverManager.getConnection(url, "root", password);
        Statement stmnt = conn.createStatement();
        stmnt.executeUpdate("show databases;");
        stmnt.executeUpdate("use video_game_encyclopedia;");
        ResultSet result = stmnt.executeQuery("select title " +
                "from Game where title = '" + query + "';");
        if (result.next()){
            gameExists = true;
        }

        // Construct the response message.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (gameExists) {
            statusLine = "HTTP/1.1 200 OK" + CRLF;
            contentTypeLine = "Content-type: application/json" + CRLF;
            entityBody = makeJsonGame(query, stmnt);
        }
        else {
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
    private static String convertString(String str){
        String newString = str.substring(1, str.length());
        newString = newString.replaceAll("_", " ");
        return newString;
    }

    private static String makeJsonGame(String query, Statement stmnt) throws Exception {
        int id = 0;
        int metacriticScore = 0;
        String title = null;
        String releaseDate = null;
        String imageLink = null;
        ArrayList<String> genresList = new ArrayList<>();
        ArrayList<String> platformsList = new ArrayList<>();
        ArrayList<String> storesList = new ArrayList<>();

        ResultSet result = stmnt.executeQuery("select * " +
                "from Game where title = '" + query + "';");

        if (result.next()) {
            id = Integer.parseInt(result.getString("id"));
            title = result.getString("title");
            releaseDate = result.getString("releaseDate");
            metacriticScore = Integer.parseInt(result.getString("metacriticScore"));
            imageLink = result.getString("imageLink");
        }

        result = stmnt.executeQuery("select genre from Genre where gameId = " + id);
        while (result.next()){
            genresList.add(result.getString("genre"));
        }

        result = stmnt.executeQuery("select platform from Platform where gameId = " + id);
        while (result.next()){
            platformsList.add(result.getString("platform"));
        }

        result = stmnt.executeQuery("select store from Store where gameId = " + id);
        while (result.next()){
            storesList.add(result.getString("store"));
        }
        String[] genre = new String[genresList.size()];
        for (int i = 0; i < genresList.size(); i++){
            genre[i] = genresList.get(i);
        }

        String[] platform = new String[platformsList.size()];
        for (int i = 0; i < platformsList.size(); i++){
            platform[i] = platformsList.get(i);
        }

        String[] store = new String[storesList.size()];
        for (int i = 0; i < storesList.size(); i++){
            store[i] = storesList.get(i);
        }

        Game game = new Game(id, title, releaseDate, metacriticScore, imageLink, genre, platform, store);
        Gson gson = new Gson();
        String json = gson.toJson(game);
        return json;
    }
}
