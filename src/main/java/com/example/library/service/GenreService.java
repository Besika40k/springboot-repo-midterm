package com.example.library.service;

import com.example.library.dto.GenreDto;
import com.example.library.dto.GenreStats;
import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.GenreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for genres.
 */
@Service
public class GenreService {

    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional(readOnly = true)
    public Page<Genre> findAll(Pageable pageable) {
        return genreRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Genre> findAllSorted() {
        return genreRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public List<GenreStats> findAllWithNovelCount() {
        return genreRepository.findAllWithNovelCount();
    }

    @Transactional(readOnly = true)
    public Genre findById(Long id) {
        return genreRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Genre", id));
    }

    @Transactional(readOnly = true)
    public long count() {
        return genreRepository.count();
    }

    @Transactional
    public Genre create(GenreDto dto) {
        String name = dto.getName().trim();
        if (genreRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("name",
                    "A genre named " + name + " already exists");
        }
        Genre genre = new Genre(name, dto.getDescription());
        return genreRepository.save(genre);
    }

    @Transactional
    public Genre update(Long id, GenreDto dto) {
        Genre genre = findById(id);
        String name = dto.getName().trim();
        if (genreRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("name",
                    "A genre named " + name + " already exists");
        }
        genre.setName(name);
        genre.setDescription(dto.getDescription());
        return genre;
    }

    /**
     * Deletes a genre. Novels in it are kept; only the link between the
     * novels and the genre is removed.
     */
    @Transactional
    public void delete(Long id) {
        Genre genre = findById(id);
        for (LightNovel novel : new ArrayList<>(genre.getNovels())) {
            novel.removeGenre(genre);
        }
        genreRepository.delete(genre);
    }
}
