package com.example.library.repository;

import com.example.library.dto.GenreStats;
import com.example.library.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data access for {@link Genre} entities.
 */
@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {

    // ---------- Method-name queries ----------

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Genre> findAllByOrderByNameAsc();

    // ---------- Custom queries ----------

    /**
     * Lists every genre with its number of novels and their average rating.
     */
    @Query("SELECT new com.example.library.dto.GenreStats("
            + "g.id, g.name, g.description, COUNT(n), AVG(n.rating)) "
            + "FROM Genre g LEFT JOIN g.novels n "
            + "GROUP BY g.id, g.name, g.description "
            + "ORDER BY g.name")
    List<GenreStats> findAllWithNovelCount();
}
