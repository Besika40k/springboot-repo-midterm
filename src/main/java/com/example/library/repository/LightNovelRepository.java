package com.example.library.repository;

import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link LightNovel} entities.
 */
@Repository
public interface LightNovelRepository extends JpaRepository<LightNovel, Long> {

    // ---------- Method-name queries ----------

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    long countByStatus(NovelStatus status);

    List<LightNovel> findTop5ByOrderByCreatedAtDesc();

    List<LightNovel> findTop5ByRatingNotNullOrderByRatingDescTitleAsc();

    List<LightNovel> findByAuthorIdOrderByTitleAsc(Long authorId);

    // ---------- Custom queries ----------

    /**
     * Searches novels by a title keyword, author, genre and status.
     * Every filter is optional: a null value means "do not filter".
     * Paging and sorting come from the given {@link Pageable}.
     */
    @Query(value = "SELECT DISTINCT n FROM LightNovel n "
            + "LEFT JOIN n.genres g "
            + "WHERE (:keyword IS NULL "
            + "OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:authorId IS NULL OR n.author.id = :authorId) "
            + "AND (:genreId IS NULL OR g.id = :genreId) "
            + "AND (:status IS NULL OR n.status = :status)",
            countQuery = "SELECT COUNT(DISTINCT n) FROM LightNovel n "
            + "LEFT JOIN n.genres g "
            + "WHERE (:keyword IS NULL "
            + "OR LOWER(n.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) "
            + "AND (:authorId IS NULL OR n.author.id = :authorId) "
            + "AND (:genreId IS NULL OR g.id = :genreId) "
            + "AND (:status IS NULL OR n.status = :status)")
    Page<LightNovel> search(@Param("keyword") String keyword,
            @Param("authorId") Long authorId,
            @Param("genreId") Long genreId,
            @Param("status") NovelStatus status,
            Pageable pageable);

    /**
     * Removes the cover image reference of a novel with a bulk update.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE LightNovel n SET n.coverImage = NULL WHERE n.id = :id")
    int clearCoverImage(@Param("id") Long id);

    /**
     * Adds up the chapters of every novel (native SQL).
     */
    @Query(value = "SELECT COALESCE(SUM(chapter_count), 0) "
            + "FROM light_novels", nativeQuery = true)
    long sumChapters();
}
