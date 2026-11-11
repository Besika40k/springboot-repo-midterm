package com.example.library.dto;

import com.example.library.entity.Author;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Incoming author data from the HTML form, validated with JSR-303.
 */
public class AuthorDto {

    private Long id;

    @NotBlank(message = "Pen name is required")
    @Size(max = 100, message = "Pen name must be at most 100 characters")
    private String penName;

    @Size(max = 100,
            message = "Original name must be at most 100 characters")
    private String originalName;

    @Email(message = "Please enter a valid email address")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(max = 1000, message = "Biography must be at most 1000 characters")
    private String biography;

    public AuthorDto() {
    }

    /**
     * Creates a form object pre-filled with the data of an existing author.
     */
    public static AuthorDto from(Author author) {
        AuthorDto dto = new AuthorDto();
        dto.setId(author.getId());
        dto.setPenName(author.getPenName());
        dto.setOriginalName(author.getOriginalName());
        dto.setEmail(author.getEmail());
        dto.setBiography(author.getBiography());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }
}
