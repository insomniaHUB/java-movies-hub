package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static ru.practicum.moviehub.http.MoviesServer.getMoviesStore;

public class MoviesHandler extends BaseHttpHandler {
    Gson gson = new Gson();
    private static final String pathURL = "/movies";

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        try {
            if (method.equalsIgnoreCase("GET")) {
                handleGetMethod(ex);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePostMethod(ex);
            } else if (method.equalsIgnoreCase("DELETE")) {
                handleDeleteMethod(ex);
            }  else {
                ex.sendResponseHeaders(405, -1);
            }
        } catch (Exception e) {
            System.out.println("Что-то пошло не так.");
        }
    }

    private void handleGetMethod(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals(pathURL)) {
            String query = ex.getRequestURI().getQuery();

            if (query != null && query.startsWith("year=")) {
                try {
                    String yearStr = query.substring("year=".length());
                    int year = Integer.parseInt(yearStr);
                    List<Movie> movieListByYear = getMoviesStore().getMovieListByYear(year);
                    sendJson(ex, 200, gson.toJson(movieListByYear));
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse(400, "Некорректный год");
                    sendNotNumberRequest(ex, errorResponse);
                }
            } else {
                sendJson(ex, 200, gson.toJson(getMoviesStore().getMovieList()));
            }
        } else if (path.startsWith(pathURL + "/")) {
            String idStr = path.substring((pathURL + "/").length());
            try {
                int id = Integer.parseInt(idStr);
                Movie movie = getMoviesStore().getMovie(id);
                if (movie != null) {
                    sendJson(ex, 200, gson.toJson(movie));
                } else {
                    ErrorResponse errorResponse = new ErrorResponse(404, "Фильм не найден");
                    sendNotFoundRequest(ex, errorResponse);
                }
            } catch (NumberFormatException e) {
                ErrorResponse errorResponse = new ErrorResponse(400, "Некорректный ID");
                sendNotNumberRequest(ex, errorResponse);
            }
        } else {
            ErrorResponse errorResponse = new ErrorResponse(404, "Фильм не найден");
            sendNotFoundRequest(ex, errorResponse);
        }
    }

    private void handlePostMethod(HttpExchange ex) throws IOException {
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
                ErrorResponse errorResponse = new ErrorResponse(422, "Ошибка валидации");
                errorResponse.setErrors(List.of("Неправильная длина названия фильма", "Некорректный год выпуска фильма"));
                sendValidationError(ex, errorResponse);
                return;
            } else if (title.length() > 100 || title.isBlank()) {
                ErrorResponse errorResponse = new ErrorResponse(422, "Ошибка валидации");
                errorResponse.setErrors(List.of("Неправильная длина названия фильма"));
                sendValidationError(ex, errorResponse);
                return;
            } else if (year < 1888 || year > 2027) {
                ErrorResponse errorResponse = new ErrorResponse(422, "Ошибка валидации");
                errorResponse.setErrors(List.of("Некорректный год выпуска фильма"));
                sendValidationError(ex, errorResponse);
                return;
            }

            Movie movie = new Movie(title, year);
            getMoviesStore().addMovie(getMoviesStore().getMovieList().size() + 1, movie);
            sendJson(ex, 201, gson.toJson(movie));

        } catch (Exception e) {
            System.out.println("Что-то пошло не так.");
        }
    }

    private void handleDeleteMethod(HttpExchange ex) throws IOException {
        String idStr = ex.getRequestURI().getPath().substring((pathURL + "/").length());
        try {
            int id = Integer.parseInt(idStr);
            Movie movie = getMoviesStore().getMovie(id);
            if (movie != null) {
                getMoviesStore().deleteMovie(id);
                sendNoContent(ex);
            } else {
                ErrorResponse errorResponse = new ErrorResponse(404, "Фильм не найден");
                sendNotFoundRequest(ex, errorResponse);
            }
        } catch (NumberFormatException e) {
            ErrorResponse errorResponse = new ErrorResponse(400, "Некорректный ID");
            sendNotNumberRequest(ex, errorResponse);
        }
    }
}