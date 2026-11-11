package com.example.library.controller;

import com.example.library.entity.NovelStatus;
import com.example.library.service.AuthorService;
import com.example.library.service.GenreService;
import com.example.library.service.LightNovelService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Shows the dashboard with library statistics.
 */
@Controller
public class HomeController {

    private static final long PROLIFIC_AUTHOR_MIN_NOVELS = 2;

    private final LightNovelService novelService;
    private final AuthorService authorService;
    private final GenreService genreService;

    public HomeController(LightNovelService novelService,
            AuthorService authorService,
            GenreService genreService) {
        this.novelService = novelService;
        this.authorService = authorService;
        this.genreService = genreService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("novelCount", novelService.count());
        model.addAttribute("ongoingCount",
                novelService.countByStatus(NovelStatus.ONGOING));
        model.addAttribute("completedCount",
                novelService.countByStatus(NovelStatus.COMPLETED));
        model.addAttribute("authorCount", authorService.count());
        model.addAttribute("genreCount", genreService.count());
        model.addAttribute("chapterCount", novelService.totalChapters());
        model.addAttribute("topRated", novelService.findTopRated());
        model.addAttribute("recentNovels", novelService.findRecent());
        model.addAttribute("genreStats", genreService.findAllWithNovelCount());
        model.addAttribute("topAuthors", authorService
                .findAuthorsWithAtLeast(PROLIFIC_AUTHOR_MIN_NOVELS));
        return "index";
    }
}
