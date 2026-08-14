package ru.practicum.moviehub.api;

import java.util.Arrays;

public class ErrorResponse {
    private final String error;
    private final String[] details;

    public ErrorResponse(String error, String[] details) {
        this.error = error;
        this.details = details;
    }

    @Override
    public String toString() {
        return "Ошибка " + error +
                "\nДетали: " + Arrays.toString(details);
    }
}