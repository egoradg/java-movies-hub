package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.moviehub.model.Movie;

import java.util.HashMap;
import java.util.Map;

public class MoviesStore {
    private Map<Integer, Movie> movies;

    public MoviesStore() {
        movies = new HashMap<>();
    }

    public void addMovie(Movie movie) {
        movies.put(movie.getId(), movie);
    }

    public Movie getMovie(int id) {
        return movies.get(id);
    }

    public void clear(){
        movies.clear();
    }

    public void deleteMovie(int id){
        movies.remove(id);
    }

    public int getNextId(){
        return movies.size();
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(movies.values());
    }
}