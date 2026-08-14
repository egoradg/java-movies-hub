package ru.practicum.moviehub.http;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    private final MoviesStore store;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Напишите реализацию, удовлетворяющую тест
        String method = ex.getRequestMethod();
        switch (method) {
            case "GET": {
                String[] path = ex.getRequestURI().getPath().split("/");
                if (path[1].equals("movies")) {
                    if (path.length == 2) {
                        sendJson(ex, 200, store.toString());
                    } else if (path.length == 3) {
                        try {
                            int id = Integer.parseInt(path[2]);
                        } catch (NumberFormatException e) {
                            sendJson(ex, 400, "Некорректный ID");
                        }
                    }
                }
                return;
            }
            case "POST": {
                postMethod(ex);
                return;
            }
            default:
                System.out.println(method);
                ex.sendResponseHeaders(405, -1);
        }
    }

    private void postMethod(HttpExchange ex) throws IOException {
        List<String> details = new ArrayList<>();

        if (ex.getRequestHeaders().containsKey("Content-Type")) {
            if (!ex.getRequestHeaders().get("Content-Type").getFirst().equals(CT_JSON)) {
                ErrorResponse errorResponse = new ErrorResponse("Unsupported Media Type", new String[]{"\"Content-Type\" must be \"" + CT_JSON + '\"'});
                sendJson(ex, 415, errorResponse.toString());
                return;
            }
        }

        String requestBody = new String(ex.getRequestBody().readAllBytes());
        //System.out.println(requestBody);
        JsonElement jsonElement = JsonParser.parseString(requestBody);

        if (!jsonElement.isJsonObject()) {
            ex.sendResponseHeaders(400, -1);
            System.out.println("не json");
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String title = jsonObject.get("title").getAsString();
        if (title.length() > 100)
            details.add("название не должно быть пустым или длиннее 100 символов");

        int year = jsonObject.get("year").getAsInt();
        if (year < 1888 || year > LocalDate.now().getYear()) {
            details.add("год должен быть между 1888 и " + LocalDate.now().getYear());
        }

        if (!details.isEmpty()) {
            ErrorResponse errorResponse = new ErrorResponse("Unprocessable Entity", details.toArray(new String[0]));
            sendJson(ex, 422, errorResponse.toString());
            return;
        }

        Movie movie = new Movie(
                store.getNextId(),
                title,
                jsonObject.get("year").getAsInt()
        );

        store.addMovie(movie);
        sendJson(ex, 201, movie.toString());
    }

    private void getById(HttpExchange ex){

    }
}