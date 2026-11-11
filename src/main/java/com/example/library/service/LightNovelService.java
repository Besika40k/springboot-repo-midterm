package com.example.library.service;

import com.example.library.dto.LightNovelDto;
import com.example.library.entity.Author;
import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.ResourceNotFoundException;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.GenreRepository;
import com.example.library.repository.LightNovelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for light novels, including their cover images.
 */
@Service
public class LightNovelService {

    private final LightNovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final FileStorageService fileStorageService;

    public LightNovelService(LightNovelRepository novelRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            FileStorageService fileStorageService) {
        this.novelRepository = novelRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Returns one page of novels matching the optional filters.
     */
    @Transactional(readOnly = true)
    public Page<LightNovel> search(String keyword, Long authorId,
            Long genreId, NovelStatus status, Pageable pageable) {
        String text = null;
        if (keyword != null && !keyword.isBlank()) {
            text = keyword.trim();
        }
        return novelRepository.search(text, authorId, genreId, status,
                pageable);
    }

    @Transactional(readOnly = true)
    public LightNovel findById(Long id) {
        return novelRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Light novel", id));
    }

    @Transactional(readOnly = true)
    public List<LightNovel> findRecent() {
        return novelRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<LightNovel> findTopRated() {
        return novelRepository
                .findTop5ByRatingNotNullOrderByRatingDescTitleAsc();
    }

    @Transactional(readOnly = true)
    public List<LightNovel> findByAuthor(Long authorId) {
        return novelRepository.findByAuthorIdOrderByTitleAsc(authorId);
    }

    @Transactional(readOnly = true)
    public long count() {
        return novelRepository.count();
    }

    @Transactional(readOnly = true)
    public long countByStatus(NovelStatus status) {
        return novelRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long totalChapters() {
        return novelRepository.sumChapters();
    }

    /**
     * Creates a novel. The cover image is optional.
     */
    @Transactional
    public LightNovel create(LightNovelDto dto, MultipartFile cover) {
        String title = dto.getTitle().trim();
        if (novelRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateResourceException("title",
                    "A novel with this title already exists");
        }
        LightNovel novel = new LightNovel();
        copyFields(dto, novel);
        novelRepository.save(novel);
        if (cover != null && !cover.isEmpty()) {
            replaceCover(novel, cover);
        }
        return novel;
    }

    /**
     * Updates a novel. A new cover image replaces the old one if given.
     */
    @Transactional
    public LightNovel update(Long id, LightNovelDto dto, MultipartFile cover) {
        LightNovel novel = findById(id);
        String title = dto.getTitle().trim();
        if (novelRepository.existsByTitleIgnoreCaseAndIdNot(title, id)) {
            throw new DuplicateResourceException("title",
                    "A novel with this title already exists");
        }
        copyFields(dto, novel);
        if (cover != null && !cover.isEmpty()) {
            replaceCover(novel, cover);
        }
        return novel;
    }

    /**
     * Uploads a new cover image for an existing novel.
     */
    @Transactional
    public LightNovel updateCover(Long id, MultipartFile cover) {
        LightNovel novel = findById(id);
        replaceCover(novel, cover);
        return novel;
    }

    /**
     * Removes the cover image of a novel (database and disk).
     */
    @Transactional
    public void removeCover(Long id) {
        LightNovel novel = findById(id);
        String oldCover = novel.getCoverImage();
        if (oldCover == null) {
            return;
        }
        novelRepository.clearCoverImage(id);
        fileStorageService.deleteAfterCommit(oldCover);
    }

    @Transactional
    public void delete(Long id) {
        LightNovel novel = findById(id);
        String cover = novel.getCoverImage();
        for (Genre genre : new ArrayList<>(novel.getGenres())) {
            novel.removeGenre(genre);
        }
        novel.getAuthor().getNovels().remove(novel);
        novelRepository.delete(novel);
        fileStorageService.deleteAfterCommit(cover);
    }

    private void replaceCover(LightNovel novel, MultipartFile cover) {
        String oldCover = novel.getCoverImage();
        String newCover = fileStorageService.store(cover);
        fileStorageService.deleteOnRollback(newCover);
        novel.setCoverImage(newCover);
        fileStorageService.deleteAfterCommit(oldCover);
    }

    private void copyFields(LightNovelDto dto, LightNovel novel) {
        novel.setTitle(dto.getTitle().trim());
        novel.setStatus(dto.getStatus());
        novel.setChapterCount(dto.getChapterCount());
        novel.setRating(dto.getRating());
        novel.setDescription(dto.getDescription());

        Author author = authorRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Author", dto.getAuthorId()));
        Author oldAuthor = novel.getAuthor();
        if (oldAuthor == null || !oldAuthor.getId().equals(author.getId())) {
            if (oldAuthor != null) {
                oldAuthor.getNovels().remove(novel);
            }
            author.addNovel(novel);
        }

        List<Genre> genres = genreRepository.findAllById(dto.getGenreIds());
        if (genres.size() != dto.getGenreIds().size()) {
            throw new ResourceNotFoundException(
                    "One or more selected genres do not exist");
        }
        for (Genre genre : new ArrayList<>(novel.getGenres())) {
            novel.removeGenre(genre);
        }
        for (Genre genre : genres) {
            novel.addGenre(genre);
        }
    }
}
