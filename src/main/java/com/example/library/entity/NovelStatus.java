package com.example.library.entity;

/**
 * Publication status of a light novel.
 */
public enum NovelStatus {

    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    HIATUS("On hiatus");

    private final String label;

    NovelStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
