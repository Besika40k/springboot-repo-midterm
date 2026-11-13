package com.example.library.config;

import com.example.library.entity.Author;
import com.example.library.entity.Genre;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Fills an empty database with sample authors, genres and light novels
 * so the application has something to show on the first start.
 * Titles, authors, chapter counts, ratings and genres are taken from a
 * public novel ranking list; the descriptions are short summaries.
 */
@Configuration
public class DataInitializer {

    private static final Logger LOG =
            LoggerFactory.getLogger(DataInitializer.class);

    private static final NovelStatus ONGOING = NovelStatus.ONGOING;
    private static final NovelStatus COMPLETED = NovelStatus.COMPLETED;

    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final TransactionTemplate transactionTemplate;

    private final Map<String, Genre> genres = new LinkedHashMap<>();
    private final Map<String, Author> authors = new LinkedHashMap<>();

    public DataInitializer(AuthorRepository authorRepository,
            GenreRepository genreRepository,
            TransactionTemplate transactionTemplate) {
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Bean
    public CommandLineRunner seedSampleData() {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (authorRepository.count() > 0) {
                return;
            }
            seedGenres();
            seedAuthors();
            seedNovels();
            // Novels are saved through the cascade on Author.novels.
            authorRepository.saveAll(authors.values());
            LOG.info("Sample data created: {} authors, {} genres",
                    authorRepository.count(), genreRepository.count());
        });
    }

    private void seedGenres() {
        List<Genre> list = List.of(
                new Genre("Action", "Fights, battles and fast-paced scenes"),
                new Genre("Adventure", "Journeys into the unknown"),
                new Genre("Comedy", "Stories meant to make you laugh"),
                new Genre("Drama", "Emotional, character-driven stories"),
                new Genre("Fantasy", "Magic, monsters and other worlds"),
                new Genre("Game", "Game systems, levels and stats"),
                new Genre("Martial Arts", "Fighting techniques and training"),
                new Genre("Mecha", "Giant robots and machines"),
                new Genre("Mystery", "Secrets and puzzles to uncover"),
                new Genre("Psychological", "Focus on the mind of characters"),
                new Genre("Romance", "Love and relationships"),
                new Genre("Sci-fi", "Science, technology and the future"),
                new Genre("Supernatural", "Ghosts, curses and hidden powers"),
                new Genre("Tragedy", "Loss, sacrifice and dark turns"),
                new Genre("Xianxia", "Chinese immortal cultivation"),
                new Genre("Xuanhuan",
                        "Chinese fantasy with mystical powers"));
        for (Genre genre : genreRepository.saveAll(list)) {
            genres.put(genre.getName(), genre);
        }
    }

    private void seedAuthors() {
        addAuthor("Guiltythree", null,
                "Author of the web novel Shadow Slave.");
        addAuthor("Cuttlefish That Loves Diving", "爱潜水的乌贼",
                "Chinese web novelist, author of Lord of the Mysteries.");
        addAuthor("Gu Zhen Ren", "蛊真人",
                "Chinese web novelist, author of Reverend Insanity.");
        addAuthor("Chocolion", "齐佩甲",
                "Chinese web novelist, author of The Legendary Mechanic.");
        addAuthor("TurtleMe", null,
                "Author of The Beginning After The End.");
        addAuthor("nobody103", null,
                "Author of the web serial Mother of Learning.");
        addAuthor("Zogarth", null, "Author of The Primal Hunter.");
        addAuthor("Void Herald", null, "Author of The Perfect Run.");
        addAuthor("RinoZ", null, "Author of Chrysalis.");
        addAuthor("Nagatsuki Tappei", "長月達平",
                "Japanese light novel author, creator of Re:Zero.");
        addAuthor("Er Mu", "二目",
                "Chinese web novelist, author of Release that Witch.");
        addAuthor("Twelve Winged Dark Burning Angel", "十二翼黑暗炽天使",
                "Chinese web novelist, author of Super Gene.");
        addAuthor("The Speaking Pork Trotter", "会说话的肘子",
                "Chinese web novelist, author of The First Order.");
        addAuthor("Yuan Tong", "远瞳",
                "Chinese web novelist, author of Deep Sea Embers.");
        addAuthor("UnstoppableJuggernaut", null,
                "Author of Mark of the Fool.");
        addAuthor("Spaizzer", null, "Author of Tree of Aeons.");
        addAuthor("Warmaisach", null, "Author of Kill the Sun.");
        addAuthor("Zhai Zhu", "宅猪",
                "Chinese web novelist, author of Tales of Herding Gods.");
        addAuthor("Exlor", null, "Author of The Mech Touch.");
    }

    private void seedNovels() {
        addNovel("Guiltythree", "Shadow Slave", ONGOING, 3242, 4.5,
                "A boy from the outskirts is chosen by the Nightmare "
                + "Spell and must survive with a strange shadow power.",
                "Action", "Adventure", "Fantasy", "Romance");
        addNovel("Cuttlefish That Loves Diving", "Lord of the Mysteries",
                COMPLETED, 1432, 4.8,
                "In a world of steam and mysticism, Klein Moretti "
                + "becomes a Beyonder and uncovers ancient secrets.",
                "Fantasy", "Mystery", "Supernatural", "Xuanhuan");
        addNovel("Cuttlefish That Loves Diving",
                "Lord of Mysteries 2: Circle of Inevitability",
                COMPLETED, 1181, 4.0,
                "A sequel set in the same world as Lord of the "
                + "Mysteries, with a new protagonist.",
                "Fantasy", "Mystery");
        addNovel("Cuttlefish That Loves Diving", "Throne of Magical Arcana",
                COMPLETED, 910, 4.5,
                "A physics student wakes up in a world of magic and "
                + "studies arcana like a scientist.",
                "Adventure", "Fantasy", "Sci-fi", "Xuanhuan");
        addNovel("Gu Zhen Ren", "Reverend Insanity", COMPLETED, 2334, 4.6,
                "Fang Yuan, a ruthless demonic cultivator, chases "
                + "eternal life using Gu worms.",
                "Fantasy", "Martial Arts", "Psychological", "Xianxia");
        addNovel("Chocolion", "The Legendary Mechanic", COMPLETED, 1463,
                4.6, "A gamer wakes up inside a game as an NPC mechanic "
                + "and uses what he knows about its future.",
                "Action", "Adventure", "Mecha", "Sci-fi");
        addNovel("TurtleMe", "The Beginning After The End", COMPLETED, 532,
                4.2, "A king is reborn into a world of magic and gets a "
                + "second chance at life.",
                "Fantasy", "Martial Arts");
        addNovel("nobody103", "Mother of Learning", COMPLETED, 112, 4.7,
                "A mage student is trapped in a month-long time loop "
                + "and must find out why.",
                "Fantasy", "Mystery", "Drama", "Psychological");
        addNovel("Zogarth", "The Primal Hunter", ONGOING, 1446, 4.1,
                "An office worker is pulled into a deadly tutorial and "
                + "grows stronger as a hunter and alchemist.",
                "Action", "Adventure", "Fantasy");
        addNovel("Void Herald", "The Perfect Run", COMPLETED, 130, 4.8,
                "A courier who can save and reload time tries to get "
                + "the perfect run in a post-apocalyptic Italy.",
                "Adventure", "Comedy", "Mystery", "Sci-fi");
        addNovel("RinoZ", "Chrysalis", ONGOING, 1902, 4.6,
                "A man is reborn as an ant in a dungeon full of "
                + "monsters and has to evolve to survive.",
                "Adventure", "Fantasy", "Game");
        addNovel("Nagatsuki Tappei", "Re:Zero Kara Hajimeru Isekai Seikatsu",
                ONGOING, 677, 4.8,
                "Subaru is summoned to another world and returns to an "
                + "earlier point every time he dies.",
                "Adventure", "Comedy", "Fantasy", "Psychological");
        addNovel("Er Mu", "Release that Witch", COMPLETED, 1501, 4.2,
                "An engineer wakes up as a medieval prince and starts an "
                + "industrial revolution with the help of witches.",
                "Adventure", "Drama", "Fantasy", "Supernatural");
        addNovel("Twelve Winged Dark Burning Angel", "Super Gene",
                COMPLETED, 3462, 4.0,
                "In the God's Sanctuary, Han Sen hunts creatures to "
                + "evolve his genes.",
                "Action", "Comedy", "Martial Arts", "Sci-fi");
        addNovel("Twelve Winged Dark Burning Angel", "Let Me Game in Peace",
                COMPLETED, 1905, 4.4,
                "Zhou Wen only wants to play his phone game, but it is "
                + "linked to dangerous dimensional zones.",
                "Action", "Fantasy", "Game", "Xuanhuan");
        addNovel("The Speaking Pork Trotter", "The First Order", COMPLETED,
                1260, 4.3, "After a catastrophe, Ren Xiaosu gains a "
                + "mysterious power and survives in the wasteland.",
                "Action", "Comedy", "Sci-fi", "Tragedy");
        addNovel("Yuan Tong", "Deep Sea Embers", COMPLETED, 851, 4.4,
                "A man becomes the captain of a ghost ship sailing an "
                + "endless and deadly sea.",
                "Adventure", "Fantasy", "Mystery", "Supernatural");
        addNovel("UnstoppableJuggernaut", "Mark of the Fool", COMPLETED,
                944, 4.5, "A young man marked by the gods as a fool "
                + "refuses to give up on magic.",
                "Action", "Adventure", "Comedy", "Fantasy");
        addNovel("Spaizzer", "Tree of Aeons", COMPLETED, 398, 4.3,
                "A man is reborn as a tree in a fantasy world and slowly "
                + "grows his own domain.",
                "Adventure", "Fantasy");
        addNovel("Warmaisach", "Kill the Sun", COMPLETED, 992, 4.4,
                "A dark fantasy adventure full of mystery and tragedy.",
                "Action", "Fantasy", "Mystery", "Tragedy");
        addNovel("Zhai Zhu", "Tales of Herding Gods", COMPLETED, 1844, 4.8,
                "Qin Mu, raised by strange elders in the Great Ruins, "
                + "sets out to learn the secrets of the world.",
                "Action", "Comedy", "Martial Arts", "Xuanhuan");
        addNovel("Exlor", "The Mech Touch", ONGOING, 7617, 4.2,
                "Ves Larkinson inherits a small mech workshop and designs "
                + "mechs during an interstellar war.",
                "Mecha", "Sci-fi");
    }

    private void addAuthor(String penName, String originalName,
            String biography) {
        authors.put(penName, new Author(penName, originalName, biography));
    }

    private void addNovel(String penName, String title, NovelStatus status,
            int chapters, double rating, String description,
            String... genreNames) {
        LightNovel novel = new LightNovel(title, status, chapters, rating,
                description);
        authors.get(penName).addNovel(novel);
        for (String name : genreNames) {
            novel.addGenre(genres.get(name));
        }
    }
}
