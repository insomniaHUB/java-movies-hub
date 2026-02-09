package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private static MoviesStore moviesStore;

    public MoviesServer(MoviesStore moviesStore) {
        this.moviesStore = moviesStore;
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.createContext("/movies", new MoviesHandler());
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public static MoviesStore getMoviesStore() {
        return moviesStore;
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