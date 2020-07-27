package VideoGameEncyclopedia;

import java.util.ArrayList;

public class Game {

    private int id;
    private String title;
    private String releaseDate;
    private String description;
    private int metacriticScore;
    private String imageLink;
    private String[] genre;
    private String[] platform;
    private String[] store;


    public Game(int id, String title, String description, 
		String releaseDate, int metacriticScore,
		String imageLink, String[] genre,
		String[] platform, String[] store){

        this.id = id;
        this.title = title;
	this.description = description;
        this.releaseDate = releaseDate;
        this.metacriticScore = metacriticScore;
        this.imageLink = imageLink;

        this.genre = new String[genre.length];
        for (int i = 0; i < genre.length; i++){
            this.genre[i] = genre[i];
        }

        this.platform = new String[platform.length];
        for (int i = 0; i < platform.length; i++){
            this.platform[i] = platform[i];
        }

        this.store = new String[store.length];
        for (int i = 0; i < store.length; i++){
            this.store[i] = store[i];
        }
    }
}
