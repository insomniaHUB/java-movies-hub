package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ru.practicum.moviehub.api.ErrorResponse.*;

public class MoviesHandler extends BaseHttpHandler {
    MoviesStore moviesStore = new MoviesStore();
    Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (method.equalsIgnoreCase("GET")) {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/movies")) {
                String query = ex.getRequestURI().getQuery();

                if (query != null && query.startsWith("year=")) {
                    try {
                        String yearStr = query.substring("year=".length());
                        int year = Integer.parseInt(yearStr);
                        List<Movie> movieListByYear = moviesStore.getMovieListByYear(year);
                        sendJson(ex, 200, gson.toJson(movieListByYear));
                    } catch (NumberFormatException e) {
                        sendNotNumberRequest(ex, 400, "Некорректный год");
                    }
                } else {
                    sendJson(ex, 200, gson.toJson(moviesStore.getMovieList()));
                }
            } else if (path.startsWith("/movies/")) {
                String idStr = path.substring("/movies/".length());
                try {
                    int id = Integer.parseInt(idStr);
                    Movie movie = moviesStore.getMovie(id);
                    if (movie != null) {
                        sendJson(ex, 200, gson.toJson(movie));
                    } else {
                        sendNotFoundRequest(ex, 404, "Фильм не найден");
                    }
                } catch (NumberFormatException e) {
                    sendNotNumberRequest(ex, 400, "Некорректный ID");
                }
            } else {
                sendNotFoundRequest(ex, 404, "Фильм не найден");
            }

        } else if (method.equalsIgnoreCase("POST")) {
            String contentType = ex.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.contains("application/json")) {
                sendUnsupportedMediaType(ex);
                return;
            }
            InputStream inputStream = ex.getRequestBody();
            String body = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            if (body == null || body.trim().isEmpty()) {
                sendBadRequest(ex);
                return;
            }

            try {
                JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();


                String title = jsonObject.get("title").getAsString();
                int year = jsonObject.get("year").getAsInt();

                if ((title.length() > 100 || title.isBlank()) && (year < 1888 || year > 2027)) {
                    String[] errors = {"Неправильная длина названия фильма", "Некорректный год выпуска фильма"};
                    sendValidationError(ex, 422, "Ошибка валидации", errors);
                    return;
                } else if (title.length() > 100 || title.isBlank()) {
                    String[] errors = {"Неправильная длина названия фильма"};
                    sendValidationError(ex, 422, "Ошибка валидации", errors);
                    return;
                } else if (year < 1888 || year > 2027) {
                    String[] errors = {"Некорректный год выпуска фильма"};
                    sendValidationError(ex, 422, "Ошибка валидации", errors);
                    return;
                }

                Movie movie = new Movie(title, year, moviesStore.getMovieList().size() + 1);
                moviesStore.addMovie(movie);
                sendJson(ex, 201, gson.toJson(movie));

            } catch (Exception e) {
                System.out.println("Что-то пошло не так.");
            }

        } else if (method.equalsIgnoreCase("DELETE")) {
            String idStr = ex.getRequestURI().getPath().substring("/movies/".length());
            try {
                int id = Integer.parseInt(idStr);
                Movie movie = moviesStore.getMovie(id);
                if (movie != null) {
                    moviesStore.deleteMovie(movie);
                    sendNoContent(ex);
                } else {
                    sendNotFoundRequest(ex, 404, "Фильм не найден");
                }
            } catch (NumberFormatException e) {
                sendNotNumberRequest(ex, 400, "Некорректный ID");
            }
        } else {
            ex.sendResponseHeaders(405, -1);
        }
    }
}