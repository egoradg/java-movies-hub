package ru.practicum.moviehub.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.practicum.moviehub.model.Movie;

import java.util.*;

public class MoviesStore {
    private Map<Integer, Movie> movies;
    private List<Integer> deletedIds;

    public MoviesStore() {
        movies = new HashMap<>();
        deletedIds = new ArrayList<>();
    }

    public void addMovie(Movie movie) {
        movies.put(movie.getId(), movie);
    }

    public Movie getMovie(int id) {

        return movies.get(id);
    }

    public void clear() {
        movies.clear();
        deletedIds.clear();
    }

    public void deleteMovie(int id) {
        deletedIds.add(id);
        movies.remove(id);
        Collections.sort(deletedIds);
    }

    public int getNextId() {
        if (deletedIds.isEmpty())
            return movies.size();

        int id = deletedIds.getFirst();
        if(deletedIds.size()==1)
            deletedIds.clear();
        else
            deletedIds.remove(id);
        return id;
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        return gson.toJson(movies.values());
    }
}