package com.example.library.dto;

import com.example.library.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming and outgoing genre data, validated with JSR-303.
 * Used both by the HTML form and by the REST API.
 */
public class GenreDto {

    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50,
            message = "Name must be between 2 and 50 characters")
    private String name;

    @Size(max = 255, message = "Description must be at most 255 characters")
    private String description;

    public GenreDto() {
    }

    public GenreDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Creates a DTO from an existing genre.
     */
    public static GenreDto from(Genre genre) {
        return new GenreDto(genre.getId(), genre.getName(),
                genre.getDescription());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
