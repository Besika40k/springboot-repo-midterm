package com.example.library.controller;

import com.example.library.dto.AuthorDto;
import com.example.library.entity.Author;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.service.AuthorService;
import com.example.library.service.LightNovelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * HTML pages for managing authors.
 */
@Controller
@RequestMapping("/authors")
public class AuthorController {

    private static final String FORM_VIEW = "authors/form";
    private static final String DEFAULT_SORT = "penName";
    private static final Set<String> SORT_FIELDS =
            Set.of("penName", "originalName");
    private static final int MAX_PAGE_SIZE = 50;

    private final AuthorService authorService;
    private final LightNovelService novelService;

    public AuthorController(AuthorService authorService,
            LightNovelService novelService) {
        this.authorService = authorService;
        this.novelService = novelService;
    }

    /**
     * Lists authors with server-side search, sorting and pagination.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model) {
        String sortField = SORT_FIELDS.contains(sort) ? sort : DEFAULT_SORT;
        Sort.Direction direction = "desc".equalsIgnoreCase(dir)
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize,
                Sort.by(direction, sortField).and(Sort.by("id")));

        Page<Author> authors = authorService.findAll(keyword, pageable);

        model.addAttribute("authors", authors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", pageSize);
        model.addAttribute("sort", sortField);
        model.addAttribute("dir", direction.name().toLowerCase());
        return "authors/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("author", authorService.findById(id));
        model.addAttribute("novels", novelService.findByAuthor(id));
        return "authors/details";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("author", new AuthorDto());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("author") AuthorDto author,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }
        Author saved;
        try {
            saved = authorService.create(author);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Author " + saved.getPenName() + " was created");
        return "redirect:/authors/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("author",
                AuthorDto.from(authorService.findById(id)));
        return FORM_VIEW;
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("author") AuthorDto author,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        author.setId(id);
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }
        Author saved;
        try {
            saved = authorService.update(id, author);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Author " + saved.getPenName() + " was updated");
        return "redirect:/authors/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        String name = authorService.findById(id).getPenName();
        authorService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Author " + name + " and their novels were deleted");
        return "redirect:/authors";
    }
}
