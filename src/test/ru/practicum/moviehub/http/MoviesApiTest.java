package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static MoviesServer server;
    private static HttpClient client;

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), 8080);
        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        server.clearStore();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        // Создайте и запустите MoviesServer
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        //System.out.println(body);
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void postMovie_returnsMovie() throws Exception {
        Movie movie1 = new Movie(0, "star wars", 1888);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie1.getTitle(), movie1.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertEquals(movie1.toString(), body,
                "Ожидается фильм в JSON");
    }

    @Test
    void postMovieWithBadContentType() throws Exception {
        Movie movie1 = new Movie(0, "star wars", 1888);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie1.getTitle(), movie1.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/xml")
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "POST /movies должен вернуть 415");
    }

    @Test
    void postMovieWithBadParams_returnsMovie() throws Exception {
        Movie movie1 = new Movie(0, "*".repeat(101), 1887);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie1.getTitle(), movie1.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp1.statusCode(), "POST /movies должен вернуть 422");

        String body1 = resp1.body().trim();
        assertEquals(
                "Ошибка Unprocessable Entity\n"
                        + "Детали: [название не должно быть пустым или длиннее 100 символов, год должен быть между 1888 и " + LocalDate.now().getYear() + "]",
                body1,
                "Ожидается ошибка для 2 параметров"
        );


        Movie movie2 = new Movie(1, "*", 2121);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie2.getTitle(), movie2.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp2.statusCode(), "POST /movies должен вернуть 422");

        String body2 = resp2.body().trim();
        assertEquals(
                "Ошибка Unprocessable Entity\n"
                        + "Детали: [год должен быть между 1888 и " + LocalDate.now().getYear() + "]",
                body2,
                "Ожидается ошибка для года"
        );

        Movie movie3 = new Movie(2, "*".repeat(101), 1888);

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie3.getTitle(), movie3.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp3 =
                client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp1.statusCode(), "POST /movies должен вернуть 422");

        String body3 = resp3.body().trim();
        assertEquals(
                "Ошибка Unprocessable Entity\n"
                        + "Детали: [название не должно быть пустым или длиннее 100 символов]",
                body3,
                "Ожидается ошибка для названия"
        );
    }

    private String getJson(String title, int year) {
        return "{\n" +
                "  \"title\": \"" + title + "\",\n" +
                "  \"year\": " + year + '\n' +
                "}";
    }
}
