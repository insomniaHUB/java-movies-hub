package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static final String pathURL = "/movies";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore moviesStore = new MoviesStore();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(moviesStore);
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        moviesStore.clearMovieList();
    }

    @AfterAll
    static void afterAll() {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
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
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_whenNotEmpty_returnsMoviesArray() throws Exception {
        Movie movie = new Movie("Зеленая книга", 2019);
        moviesStore.addMovie(1, movie);
        Gson gson = new Gson();


        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> movieList = gson.fromJson(resp.body(), new ListOfMoviesTypeToken());
        assertEquals(movie, movieList.getFirst());
    }

    @Test
    void postMovies_Success() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", "Зеленая книга");
        jsonObject.addProperty("year", 2019);
        String jsonBody = jsonObject.toString();

        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Movie newMovie = gson.fromJson(resp.body(), Movie.class);
        assertEquals("Зеленая книга", newMovie.getTitle());
        assertEquals(1, moviesStore.getMovieList().size());
    }

    @Test
    void postMovies_EmptyTitleError() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", "");
        jsonObject.addProperty("year", 2019);
        String jsonBody = jsonObject.toString();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());
        JsonObject responseJson = JsonParser.parseString(resp.body()).getAsJsonObject();

        String error = responseJson.get("error").getAsString();
        assertEquals("Ошибка валидации", error);
        JsonArray detailsArray = responseJson.getAsJsonArray("details");

        assertNotNull(detailsArray);

        boolean found = false;
        for (int i = 0; i < detailsArray.size(); i++) {
            String detail = detailsArray.get(i).getAsString();
            if ("Неправильная длина названия фильма".equals(detail)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Должна быть ошибка 'Неправильная длина названия фильма'");
    }

    @Test
    void postMovies_TooLongTitleError() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", "a".repeat(101));
        jsonObject.addProperty("year", 2019);
        String jsonBody = jsonObject.toString();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        JsonObject responseJson = JsonParser.parseString(resp.body()).getAsJsonObject();

        String error = responseJson.get("error").getAsString();
        assertEquals("Ошибка валидации", error);
        JsonArray detailsArray = responseJson.getAsJsonArray("details");

        assertNotNull(detailsArray);

        boolean found = false;
        for (int i = 0; i < detailsArray.size(); i++) {
            String detail = detailsArray.get(i).getAsString();
            if ("Неправильная длина названия фильма".equals(detail)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Должна быть ошибка 'Неправильная длина названия фильма'");
    }

    @Test
    void postMovies_BadYearError() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("title", "Зеленая книга");
        jsonObject.addProperty("year", 1800);
        String jsonBody = jsonObject.toString();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode());

        JsonObject responseJson = JsonParser.parseString(resp.body()).getAsJsonObject();

        String error = responseJson.get("error").getAsString();
        assertEquals("Ошибка валидации", error);
        JsonArray detailsArray = responseJson.getAsJsonArray("details");

        assertNotNull(detailsArray);

        boolean found = false;
        for (int i = 0; i < detailsArray.size(); i++) {
            String detail = detailsArray.get(i).getAsString();
            if ("Некорректный год выпуска фильма".equals(detail)) {
                found = true;
                break;
            }
        }

        assertTrue(found, "Должна быть ошибка 'Некорректный год выпуска фильма'");
    }

    @Test
    void postMovies_UnsupportedMediaTypeError() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @Test
    void getMoviesById_returnsMovie() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);
        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "/2"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies/{id} должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        Movie responseMovie = gson.fromJson(resp.body(), Movie.class);
        assertEquals("Интерстеллар", responseMovie.getTitle());
        assertEquals(2014, responseMovie.getYear());

        int idResponseMovie = -1;

        for (Integer id : moviesStore.getMovieMap().keySet()) {
            if (moviesStore.getMovieMap().get(id).equals(responseMovie)) {
                idResponseMovie = id;
                break;
            }
        }

        assertEquals(2, idResponseMovie);
    }

    @Test
    void getMoviesById_returnsNotFound() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "/3"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject responseJson = JsonParser.parseString(resp.body()).getAsJsonObject();

        String error = responseJson.get("error").getAsString();
        assertEquals("Фильм не найден", error);

    }

    @Test
    void getMoviesById_returnsNotNumber() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "/three"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject responseJson = JsonParser.parseString(resp.body()).getAsJsonObject();

        String error = responseJson.get("error").getAsString();
        assertEquals("Некорректный ID", error);

    }

    @Test
    void deleteMoviesById() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "/2"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, resp.statusCode(), "delete /movies/{id} должен вернуть 204");
        assertEquals(1, moviesStore.getMovieList().size());
    }

    @Test
    void deleteMoviesById_NotFound() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/3"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, resp.statusCode());
        assertEquals(2, moviesStore.getMovieList().size());
    }

    @Test
    void deleteMoviesById_NotNumberId() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "/two"))
                .DELETE()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
        assertEquals(2, moviesStore.getMovieList().size());
    }

    @Test
    void getMoviesByYear_returnsMovieList() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        Movie movieThree = new Movie("Стражи Галактики", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);
        moviesStore.addMovie(3, movieThree);
        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "?year=2014"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year=YYYY должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> responseMovie = gson.fromJson(resp.body(), new ListOfMoviesTypeToken());
        assertEquals(2, responseMovie.size());
        assertEquals(2014, responseMovie.getFirst().getYear());
        assertEquals(2014, responseMovie.getLast().getYear());
    }

    @Test
    void getMoviesByYear_returnsEmptyMovieList() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        Movie movieThree = new Movie("Стражи Галактики", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2,movieTwo);
        moviesStore.addMovie(3, movieThree);
        Gson gson = new Gson();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "?year=2016"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies?year= должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        List<Movie> responseMovie = gson.fromJson(resp.body(), new ListOfMoviesTypeToken());
        assertTrue(responseMovie.isEmpty());
    }

    @Test
    void getMoviesByYear_returnsYearIsNotNumber() throws Exception {
        Movie movieOne = new Movie("Зеленая книга", 2019);
        Movie movieTwo = new Movie("Интерстеллар", 2014);
        Movie movieThree = new Movie("Стражи Галактики", 2014);
        moviesStore.addMovie(1, movieOne);
        moviesStore.addMovie(2, movieTwo);
        moviesStore.addMovie(3, movieThree);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL + "?year=twentysixteen"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, resp.statusCode());
    }

    @Test
    void badRequestMethod() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + pathURL))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode());
    }

}