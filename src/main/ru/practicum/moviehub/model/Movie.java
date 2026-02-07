package ru.practicum.moviehub.model;

import java.util.Objects;

public class Movie {
    private final String title;
    private final int year;
    private final int id;

    public Movie(String title, int year, int id) {
        this.title = title;
        this.year = year;
        this.id = id;
    }

    public int getYear() {
        return year;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return year == movie.year && Objects.equals(title, movie.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }
}