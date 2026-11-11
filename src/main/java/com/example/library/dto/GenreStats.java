package com.example.library.dto;

/**
 * Read-only view of a genre with the number of novels in it and their
 * average rating (null when the genre has no rated novels).
 */
public record GenreStats(Long id, String name, String description,
        Long novelCount, Double averageRating) {
}
