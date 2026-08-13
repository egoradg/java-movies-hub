package ru.practicum.moviehub.http;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MoviesApiTest {

    @BeforeAll
    static void beforeAll() {

    }

    @BeforeEach
    void beforeEach() {

    }

    @AfterAll
    static void afterAll() {

    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        // Создайте HTTP-клиент,
        // укажите таймаут соединения (connectTimeout), равный 2 секундам
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();

        // создайте объект GET-запроса на эндпоинт /movies
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/movie"))
                .GET()
                .header("Content-Type", "application/json; charset=UTF-8")
                .build();

        // Обработчик тела запроса
        HttpResponse.BodyHandler<String> responseBodyHandler =
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8);
        // Отправьте запрос
        HttpResponse<String> resp = client.send(req, responseBodyHandler);

        // Допишите проверку кода ответа
        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        // Допишите проверку заголовка Content-Type
        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        // проверка, что был возвращён массив
        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }


}