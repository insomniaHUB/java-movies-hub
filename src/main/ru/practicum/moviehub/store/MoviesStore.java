package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private final Map<Integer, Movie> movieList;

    public MoviesStore() {
        movieList = new HashMap();
    }

    public void addMovie(int id, Movie movie) {
        movieList.put(id, movie);
    }

    public Movie getMovie(int id) {
        return movieList.get(id);
    }

    public Map<Integer, Movie> getMovieMap() {
        return movieList;
    }

    public void deleteMovie(int id) throws IOException {
        movieList.remove(id);
    }

    public List<Movie> getMovieListByYear(int year) {
        List<Movie> movieByYear = new ArrayList<>();
        for (Movie movie : movieList.values()) {
            if (movie.getYear() == year) {
                movieByYear.add(movie);
            }
        }
        return movieByYear;
    }

    public List<Movie> getMovieList() {
        List<Movie> allMovies = new ArrayList<>(movieList.values());
        return allMovies;
    }

    public void clearMovieList() {
        movieList.clear();
    }

}