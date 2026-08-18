package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

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
    void getMovies() throws Exception {
        // Создайте и запустите MoviesServer
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp1 =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp1.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp1.body().trim();
        //System.out.println(body);
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");

        HttpRequest create1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("qwe", 1900), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("asd", 1911), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("zxc", 1922), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpResponse<String> resp2 =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp2.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue2 =
                resp2.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue2,
                "Content-Type должен содержать формат данных и кодировку");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Movie> movies = gson.fromJson(resp2.body(), new ListOfMoviesTypeToken().getType());

        assertEquals(3, movies.size(), "Ожидается JSON-массив");
        assertEquals(new Movie(0, "qwe", 1900), movies.getFirst(), "Ожидается фильм");
        assertEquals(new Movie(1, "asd", 1911), movies.get(1), "Ожидается фильм");
        assertEquals(new Movie(2, "zxc", 1922), movies.getLast(), "Ожидается фильм");

    }

    @Test
    void postMovie_returnsMovie() throws Exception {
        Movie movie1 = new Movie(0, "star wars", 1888);

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie1.getTitle(), movie1.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp1.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue1 =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue1,
                "Content-Type должен содержать формат данных и кодировку");

        String body1 = resp1.body().trim();
        assertEquals(movie1.toString(), body1,
                "Ожидается фильм в JSON");

        Movie movie2 = new Movie(1, "star wars", LocalDate.now().getYear() + 1);

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie2.getTitle(), movie2.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp2.statusCode(), "POST /movies должен вернуть 201");

        String body2 = resp2.body().trim();
        assertEquals(movie2.toString(), body2,
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
    void postMovieWithBadParams_returnsErrorMassage() throws Exception {
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
        ErrorResponse errorResponse1 = new ErrorResponse("Unprocessable Entity",
                new String[]{"название не должно быть пустым или длиннее 100 символов",
                        "год должен быть между 1888 и " + (LocalDate.now().getYear() + 1)
                });
        assertEquals(
                errorResponse1.toJson(),
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
        ErrorResponse errorResponse2 = new ErrorResponse("Unprocessable Entity",
                new String[]{"год должен быть между 1888 и " + (LocalDate.now().getYear() + 1)});
        assertEquals(
                errorResponse2.toJson(),
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

        assertEquals(422, resp3.statusCode(), "POST /movies должен вернуть 422");

        String body3 = resp3.body().trim();
        ErrorResponse errorResponse3 = new ErrorResponse("Unprocessable Entity",
                new String[]{"название не должно быть пустым или длиннее 100 символов"});
        assertEquals(
                errorResponse3.toJson(),
                body3,
                "Ожидается ошибка для названия"
        );

        Movie movie4 = new Movie(2, "", 1888);

        HttpRequest req4 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson(movie3.getTitle(), movie3.getYear()), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp4 =
                client.send(req4, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp4.statusCode(), "POST /movies должен вернуть 422");

        String body4 = resp4.body().trim();
        assertEquals(
                errorResponse3.toJson(),
                body4,
                "Ожидается ошибка для названия"
        );

        HttpRequest req5 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString("{ \"title\": \"star wars\", \"year\": \"qwe\"}", StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        HttpResponse<String> resp5 =
                client.send(req5, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp2.statusCode(), "POST /movies должен вернуть 422");

        String body5 = resp5.body().trim();
        ErrorResponse errorResponse5 = new ErrorResponse("Unprocessable Entity",
                new String[]{"год должен быть числом"});
        assertEquals(
                errorResponse5.toJson(),
                body5,
                "Ожидается ошибка для года"
        );
    }

    private String getJson(String title, int year) {
        return "{\n" +
                "  \"title\": \"" + title + "\",\n" +
                "  \"year\": " + year + '\n' +
                "}";
    }

    @Test
    void getMovieById() throws Exception {
        HttpRequest create1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("qwe", 1900), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("asd", 1911), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("zxc", 1922), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .GET()
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp1.statusCode(), "GET /movies/{id} должен вернуть 200");

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp1.body().trim();
        assertEquals(new Movie(1, "asd", 1911).toString(), body,
                "Ожидается JSON-массив");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //поиск несуществующего элемента
        assertEquals(404, resp2.statusCode(), "GET /movies/{id} должен вернуть 404");

        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/qwe"))
                .GET()
                .build();

        HttpResponse<String> resp3 =
                client.send(req3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //неверные входные данные
        assertEquals(400, resp3.statusCode(), "GET /movies/{id} должен вернуть 400");
    }

    @Test
    void deleteMovieById() throws Exception {
        HttpRequest create1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("qwe", 1900), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("asd", 1911), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("zxc", 1922), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/1"))
                .DELETE()
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp1.statusCode(), "DELETE /movies/{id} должен вернуть 204");

        String body = resp1.body().trim();
        assertEquals("", body,
                "Ожидается пустая строка");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/10"))
                .DELETE()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        //удаление несуществующего элемента
        assertEquals(404, resp2.statusCode(), "DELETE /movies/{id} должен вернуть 404");
    }

    @Test
    void getMoviesByYear() throws Exception {
        HttpRequest create1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("qwe", 1988), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("asd", 1911), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest create3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(getJson("zxc", 1988), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        client.send(create3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest req1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=1988"))
                .GET()
                .build();

        HttpResponse<String> resp1 =
                client.send(req1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp1.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp1.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Movie> movies = gson.fromJson(resp1.body(), new ListOfMoviesTypeToken().getType());

        assertEquals(new Movie(0, "qwe", 1988), movies.getFirst(), "Ожидается JSON-массив");
        assertEquals(new Movie(2, "zxc", 1988), movies.getLast(), "Ожидается JSON-массив");

        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=1977"))
                .GET()
                .build();

        HttpResponse<String> resp2 =
                client.send(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp1.statusCode(), "GET /movies должен вернуть 200");

        List<Movie> movies2 = gson.fromJson(resp2.body(), new ListOfMoviesTypeToken().getType());
        assertTrue(movies2.isEmpty());
    }

    @Test
    void methodNotAllowed() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString(getJson("qwe", 1988), StandardCharsets.UTF_8))
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        assertEquals(405, resp.statusCode(), "405 Method Not Allowed");
    }
}
