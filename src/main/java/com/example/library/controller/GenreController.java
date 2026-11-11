package com.example.library.controller;

import com.example.library.dto.GenreDto;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.service.GenreService;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * HTML pages for managing genres.
 */
@Controller
@RequestMapping("/genres")
public class GenreController {

    private static final String FORM_VIEW = "genres/form";

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("genres", genreService.findAllWithNovelCount());
        return "genres/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("genre", new GenreDto());
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("genre") GenreDto genre,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            genreService.create(genre);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Genre " + genre.getName() + " was created");
        return "redirect:/genres";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("genre", GenreDto.from(genreService.findById(id)));
        return FORM_VIEW;
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("genre") GenreDto genre,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        genre.setId(id);
        if (bindingResult.hasErrors()) {
            return FORM_VIEW;
        }
        try {
            genreService.update(id, genre);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                "Genre " + genre.getName() + " was updated");
        return "redirect:/genres";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        String name = genreService.findById(id).getName();
        genreService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Genre " + name + " was deleted");
        return "redirect:/genres";
    }
}
