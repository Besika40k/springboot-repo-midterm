package com.example.library.dto;

import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * Incoming light novel data, validated with JSR-303.
 * Used by the HTML form (with {@code @ModelAttribute}) and by the REST
 * API (with {@code @RequestBody}).
 */
public class LightNovelDto {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @NotNull(message = "Please choose a status")
    private NovelStatus status;

    @NotNull(message = "Chapter count is required")
    @Min(value = 1, message = "A novel must have at least 1 chapter")
    @Max(value = 20000, message = "Chapter count must be at most 20000")
    private Integer chapterCount;

    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "Please choose an author")
    private Long authorId;

    @NotEmpty(message = "Please choose at least one genre")
    private Set<Long> genreIds = new HashSet<>();

    public LightNovelDto() {
    }

    /**
     * Creates a form object pre-filled with the data of an existing novel.
     */
    public static LightNovelDto from(LightNovel novel) {
        LightNovelDto dto = new LightNovelDto();
        dto.setId(novel.getId());
        dto.setTitle(novel.getTitle());
        dto.setStatus(novel.getStatus());
        dto.setChapterCount(novel.getChapterCount());
        dto.setRating(novel.getRating());
        dto.setDescription(novel.getDescription());
        dto.setAuthorId(novel.getAuthor().getId());
        for (Genre genre : novel.getGenres()) {
            dto.getGenreIds().add(genre.getId());
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NovelStatus getStatus() {
        return status;
    }

    public void setStatus(NovelStatus status) {
        this.status = status;
    }

    public Integer getChapterCount() {
        return chapterCount;
    }

    public void setChapterCount(Integer chapterCount) {
        this.chapterCount = chapterCount;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Set<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(Set<Long> genreIds) {
        this.genreIds = genreIds;
    }
}
