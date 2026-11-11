package com.example.library.repository;

import com.example.library.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link Author} entities.
 */
@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {

    // ---------- Method-name queries ----------

    boolean existsByPenNameIgnoreCase(String penName);

    boolean existsByPenNameIgnoreCaseAndIdNot(String penName, Long id);

    List<Author> findAllByOrderByPenNameAsc();

    // ---------- Custom queries ----------

    /**
     * Finds authors whose pen name or original name contains the text.
     */
    @Query("SELECT a FROM Author a "
            + "WHERE LOWER(a.penName) LIKE LOWER(CONCAT('%', :name, '%')) "
            + "OR LOWER(a.originalName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Author> searchByName(@Param("name") String name,
            Pageable pageable);

    /**
     * Finds authors that have written at least the given number of novels.
     */
    @Query("SELECT a FROM Author a JOIN a.novels n "
            + "GROUP BY a "
            + "HAVING COUNT(n) >= :minNovels "
            + "ORDER BY COUNT(n) DESC, a.penName")
    List<Author> findAuthorsWithAtLeast(@Param("minNovels") long minNovels);
}
