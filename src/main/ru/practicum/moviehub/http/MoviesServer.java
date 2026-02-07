package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private static final int PORT = 8080;
    private final HttpServer server;

    public MoviesServer() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Добавьте контекст для /movies и укажите созданный хендлер
            server.createContext("/movies", new MoviesHandler());

        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен");
    }
}