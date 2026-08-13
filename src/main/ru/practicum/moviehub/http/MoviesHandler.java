package ru.practicum.moviehub.http;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class MoviesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // Напишите реализацию, удовлетворяющую тест
        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            byte[] bytes =
                    "[]".getBytes(StandardCharsets.UTF_8);
            Headers responseHeaders = ex.getResponseHeaders();
            responseHeaders.set("Content-Type", "application/json; charset=UTF-8");
            ex.sendResponseHeaders(200, 0);

            try (OutputStream os = ex.getResponseBody()) {
                os.write(bytes);
            }
        } else ex.sendResponseHeaders(405, -1);
    }
}