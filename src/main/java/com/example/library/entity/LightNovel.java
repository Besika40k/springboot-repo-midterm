package com.example.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A light novel (or web novel) in the library.
 * Many novels belong to one author (many-to-one) and a novel can have
 * many genres while a genre holds many novels (many-to-many).
 */
@Entity
@Table(name = "light_novels")
public class LightNovel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NovelStatus status;

    @Column(name = "chapter_count", nullable = false)
    private Integer chapterCount;

    /** Average reader rating from 0.0 to 5.0. */
    private Double rating;

    @Column(length = 2000)
    private String description;

    /** File name of the uploaded cover image inside the upload folder. */
    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToMany
    @JoinTable(name = "novel_genres",
            joinColumns = @JoinColumn(name = "novel_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new HashSet<>();

    public LightNovel() {
    }

    public LightNovel(String title, NovelStatus status, Integer chapterCount,
            Double rating, String description) {
        this.title = title;
        this.status = status;
        this.chapterCount = chapterCount;
        this.rating = rating;
        this.description = description;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    /**
     * Adds a genre and keeps both sides of the relationship in sync.
     */
    public void addGenre(Genre genre) {
        genres.add(genre);
        genre.getNovels().add(this);
    }

    /**
     * Removes a genre and keeps both sides of the relationship in sync.
     */
    public void removeGenre(Genre genre) {
        genres.remove(genre);
        genre.getNovels().remove(this);
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

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public Set<Genre> getGenres() {
        return genres;
    }
}
