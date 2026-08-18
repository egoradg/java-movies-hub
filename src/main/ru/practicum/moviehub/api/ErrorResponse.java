package ru.practicum.moviehub.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ErrorResponse {
    private final String error;
    private final String[] details;

    public ErrorResponse(String error, String[] details) {
        this.error = error;
        this.details = details;
    }

    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(this);
    }
}