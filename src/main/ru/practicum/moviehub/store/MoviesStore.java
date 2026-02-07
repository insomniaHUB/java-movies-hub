package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MoviesStore {
    private static List<Movie> movieList;

    public MoviesStore() {
        movieList = new ArrayList<>();
    }

    public void addMovie(Movie movie) {
        movieList.add(movie);
    }

    public Movie getMovie(int id) {
        return movieList.stream()
                .filter(movie -> movie.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public void deleteMovie(Movie movie) throws IOException {
        movieList.remove(movie);
    }

    public List<Movie> getMovieListByYear(int year) {
        return movieList.stream()
                .filter(movie -> movie.getYear() == year)
                .collect(Collectors.toList());
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public void clearMovieList() {
        movieList.clear();
    }

}