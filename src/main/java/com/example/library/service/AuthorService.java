package com.example.library.service;

import com.example.library.dto.AuthorDto;
import com.example.library.entity.Author;
import com.example.library.entity.LightNovel;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for authors.
 */
@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final FileStorageService fileStorageService;

    public AuthorService(AuthorRepository authorRepository,
            FileStorageService fileStorageService) {
        this.authorRepository = authorRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Returns one page of authors, optionally filtered by a search text.
     */
    @Transactional(readOnly = true)
    public Page<Author> findAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return authorRepository.findAll(pageable);
        }
        return authorRepository.searchByName(keyword.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Author> findAllSorted() {
        return authorRepository.findAllByOrderByPenNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Author> findAuthorsWithAtLeast(long minNovels) {
        return authorRepository.findAuthorsWithAtLeast(minNovels);
    }

    @Transactional(readOnly = true)
    public Author findById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author", id));
    }

    @Transactional(readOnly = true)
    public long count() {
        return authorRepository.count();
    }

    @Transactional
    public Author create(AuthorDto dto) {
        String penName = dto.getPenName().trim();
        if (authorRepository.existsByPenNameIgnoreCase(penName)) {
            throw new DuplicateResourceException("penName",
                    "An author with this pen name already exists");
        }
        Author author = new Author();
        copyFields(dto, author);
        return authorRepository.save(author);
    }

    @Transactional
    public Author update(Long id, AuthorDto dto) {
        Author author = findById(id);
        String penName = dto.getPenName().trim();
        if (authorRepository.existsByPenNameIgnoreCaseAndIdNot(penName, id)) {
            throw new DuplicateResourceException("penName",
                    "An author with this pen name already exists");
        }
        copyFields(dto, author);
        return author;
    }

    /**
     * Deletes an author together with all of their novels (cascade) and
     * removes the novels' cover images from disk after the commit.
     */
    @Transactional
    public void delete(Long id) {
        Author author = findById(id);
        for (LightNovel novel : author.getNovels()) {
            fileStorageService.deleteAfterCommit(novel.getCoverImage());
        }
        authorRepository.delete(author);
    }

    private void copyFields(AuthorDto dto, Author author) {
        author.setPenName(dto.getPenName().trim());
        author.setOriginalName(blankToNull(dto.getOriginalName()));
        author.setEmail(blankToNull(dto.getEmail()));
        author.setBiography(blankToNull(dto.getBiography()));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
