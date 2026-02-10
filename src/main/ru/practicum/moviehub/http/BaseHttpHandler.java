package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.practicum.moviehub.api.ErrorResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendValidationError(HttpExchange ex, ErrorResponse errorResponse) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", errorResponse.getMessage());

        JsonArray detailsArray = new JsonArray();
        for (String detail : errorResponse.getErrors()) {
            detailsArray.add(detail);
        }
        jsonObject.add("details", detailsArray);

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(errorResponse.getErrorCode(), bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendUnsupportedMediaType(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(415, -1);
    }

    protected void sendBadRequest(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(400, -1);
    }

    protected void sendNotFoundRequest(HttpExchange ex, ErrorResponse errorResponse) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", errorResponse.getMessage());

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(errorResponse.getErrorCode(), bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNotNumberRequest(HttpExchange ex, ErrorResponse errorResponse) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", errorResponse.getMessage());

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(errorResponse.getErrorCode(), bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

}