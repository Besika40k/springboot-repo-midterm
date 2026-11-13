package com.example.library.repository;

import com.example.library.dto.GenreStats;
import com.example.library.entity.Author;
import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestConstructor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LightNovelRepositoryTests {

    private final LightNovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;

    private Author cuttlefish;
    private Genre fantasy;
    private LightNovel lordOfMysteries;

    LightNovelRepositoryTests(LightNovelRepository novelRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository) {
        this.novelRepository = novelRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
    }

    @BeforeEach
    void setUp() {
        fantasy = genreRepository.save(new Genre("Fantasy", null));
        Genre mystery = genreRepository.save(new Genre("Mystery", null));

        cuttlefish = new Author("Cuttlefish That Loves Diving", null, null);
        lordOfMysteries = new LightNovel("Lord of the Mysteries",
                NovelStatus.COMPLETED, 1432, 4.8, null);
        cuttlefish.addNovel(lordOfMysteries);
        lordOfMysteries.addGenre(fantasy);
        lordOfMysteries.addGenre(mystery);
        LightNovel throne = new LightNovel("Throne of Magical Arcana",
                NovelStatus.COMPLETED, 910, 4.5, null);
        cuttlefish.addNovel(throne);
        throne.addGenre(fantasy);

        Author guiltythree = new Author("Guiltythree", null, null);
        LightNovel shadowSlave = new LightNovel("Shadow Slave",
                NovelStatus.ONGOING, 3242, 4.5, null);
        guiltythree.addNovel(shadowSlave);
        shadowSlave.addGenre(fantasy);

        authorRepository.saveAll(List.of(cuttlefish, guiltythree));
    }

    @Test
    void searchFiltersByKeywordIgnoringCase() {
        Page<LightNovel> page = novelRepository.search("SHADOW", null, null,
                null, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(LightNovel::getTitle)
                .containsExactly("Shadow Slave");
    }

    @Test
    void searchFiltersByAuthorAndGenreWithPagingAndSorting() {
        Page<LightNovel> page = novelRepository.search(null,
                cuttlefish.getId(), fantasy.getId(), null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "rating")));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).extracting(LightNovel::getTitle)
                .containsExactly("Lord of the Mysteries");
    }

    @Test
    void searchFiltersByStatus() {
        Page<LightNovel> page = novelRepository.search(null, null, null,
                NovelStatus.ONGOING, PageRequest.of(0, 10));

        assertThat(page.getContent()).extracting(LightNovel::getTitle)
                .containsExactly("Shadow Slave");
    }

    @Test
    void searchWithoutFiltersReturnsEveryNovelOnce() {
        Page<LightNovel> page = novelRepository.search(null, null, null,
                null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void existsByTitleIgnoreCaseAndIdNotIgnoresTheSameNovel() {
        assertThat(novelRepository.existsByTitleIgnoreCaseAndIdNot(
                "lord of the mysteries", lordOfMysteries.getId())).isFalse();
        assertThat(novelRepository.existsByTitleIgnoreCaseAndIdNot(
                "lord of the mysteries", -1L)).isTrue();
    }

    @Test
    void countsNovelsByStatusAndSumsChapters() {
        assertThat(novelRepository.countByStatus(NovelStatus.COMPLETED))
                .isEqualTo(2);
        assertThat(novelRepository.sumChapters())
                .isEqualTo(1432 + 910 + 3242);
    }

    @Test
    void genreStatsCountNovelsAndAverageRating() {
        List<GenreStats> stats = genreRepository.findAllWithNovelCount();

        assertThat(stats).extracting(GenreStats::name)
                .containsExactly("Fantasy", "Mystery");
        assertThat(stats).extracting(GenreStats::novelCount)
                .containsExactly(3L, 1L);
        assertThat(stats.get(1).averageRating()).isEqualTo(4.8);
    }

    @Test
    void findsAuthorsWithAtLeastTwoNovels() {
        assertThat(authorRepository.findAuthorsWithAtLeast(2))
                .extracting(Author::getPenName)
                .containsExactly("Cuttlefish That Loves Diving");
    }

    @Test
    void clearCoverImageRemovesTheFileName() {
        lordOfMysteries.setCoverImage("cover.png");
        novelRepository.saveAndFlush(lordOfMysteries);

        int updated = novelRepository.clearCoverImage(lordOfMysteries.getId());

        assertThat(updated).isEqualTo(1);
        assertThat(novelRepository.findById(lordOfMysteries.getId()))
                .get().extracting(LightNovel::getCoverImage).isNull();
    }
}
