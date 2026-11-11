package com.example.library.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A light novel author, identified by their pen name.
 * One author has many novels (one-to-many).
 */
@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pen_name", nullable = false, unique = true, length = 100)
    private String penName;

    /** Name in the original language, for example 爱潜水的乌贼. */
    @Column(name = "original_name", length = 100)
    private String originalName;

    @Column(length = 100)
    private String email;

    @Column(length = 1000)
    private String biography;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    @OrderBy("title ASC")
    private List<LightNovel> novels = new ArrayList<>();

    public Author() {
    }

    public Author(String penName, String originalName, String biography) {
        this.penName = penName;
        this.originalName = originalName;
        this.biography = biography;
    }

    /**
     * Adds a novel and keeps both sides of the relationship in sync.
     */
    public void addNovel(LightNovel novel) {
        novels.add(novel);
        novel.setAuthor(this);
    }

    /**
     * Removes a novel and keeps both sides of the relationship in sync.
     */
    public void removeNovel(LightNovel novel) {
        novels.remove(novel);
        novel.setAuthor(null);
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

    public List<LightNovel> getNovels() {
        return novels;
    }
}
