package ru.practicum.moviehub.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;


public class ErrorResponse {
    private String error;
    private String details;


    public static void sendValidationError(HttpExchange ex, int status, String error, String[] details) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        JsonArray detailsArray = new JsonArray();
        for (String detail : details) {
            detailsArray.add(detail);
        }
        jsonObject.add("details", detailsArray);

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendUnsupportedMediaType(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(415, -1);
    }

    public static void sendBadRequest(HttpExchange ex) throws IOException {
        ex.sendResponseHeaders(400, -1);
    }

    public static void sendNotFoundRequest(HttpExchange ex, int status, String error) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendNotNumberRequest(HttpExchange ex, int status, String error) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("error", error);

        Gson gson = new Gson();
        String response = gson.toJson(jsonObject);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

//    private static void sendJsonError(HttpExchange ex, int status, JsonObject jsonObject) throws IOException {
//        String response = new Gson().toJson(jsonObject);
//        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
//
//        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
//        ex.sendResponseHeaders(status, bytes.length);
//
//        try (OutputStream os = ex.getResponseBody()) {
//            os.write(bytes);
//        }
//    }

    private String getError() {
        return error;
    }

    private void setError(String error) {
        this.error = error;
    }

    private String getDetails() {
        return details;
    }

    private void setDetails(String details) {
        this.details = details;
    }
}