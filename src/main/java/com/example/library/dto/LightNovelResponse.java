package com.example.library.dto;

import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outgoing light novel data returned by the REST API.
 * Flattens the relationships so the JSON has no circular references.
 */
public record LightNovelResponse(
        Long id,
        String title,
        NovelStatus status,
        Integer chapterCount,
        Double rating,
        String description,
        String coverImage,
        String coverUrl,
        Long authorId,
        String authorName,
        List<String> genres,
        LocalDateTime createdAt) {

    /**
     * Converts a novel entity into a response object.
     */
    public static LightNovelResponse from(LightNovel novel) {
        String coverUrl = null;
        if (novel.getCoverImage() != null) {
            coverUrl = "/uploads/" + novel.getCoverImage();
        }
        List<String> genreNames = novel.getGenres().stream()
                .map(Genre::getName)
                .sorted()
                .toList();
        return new LightNovelResponse(
                novel.getId(),
                novel.getTitle(),
                novel.getStatus(),
                novel.getChapterCount(),
                novel.getRating(),
                novel.getDescription(),
                novel.getCoverImage(),
                coverUrl,
                novel.getAuthor().getId(),
                novel.getAuthor().getPenName(),
                genreNames,
                novel.getCreatedAt());
    }
}
