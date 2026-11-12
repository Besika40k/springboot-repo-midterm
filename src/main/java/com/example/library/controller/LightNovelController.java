package com.example.library.controller;

import com.example.library.dto.LightNovelDto;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import com.example.library.exception.DuplicateResourceException;
import com.example.library.exception.FileStorageException;
import com.example.library.service.AuthorService;
import com.example.library.service.GenreService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * HTML pages for browsing and managing light novels, including cover
 * uploads.
 */
@Controller
@RequestMapping("/novels")
public class LightNovelController {

    private static final String FORM_VIEW = "novels/form";
    private static final String DEFAULT_SORT = "rating";
    private static final Set<String> SORT_FIELDS =
            Set.of("title", "rating", "chapterCount", "createdAt");
    private static final int MAX_PAGE_SIZE = 48;

    private final LightNovelService novelService;
    private final AuthorService authorService;
    private final GenreService genreService;

    public LightNovelController(LightNovelService novelService,
            AuthorService authorService,
            GenreService genreService) {
        this.novelService = novelService;
        this.authorService = authorService;
        this.genreService = genreService;
    }

    /**
     * Lists novels with filtering, server-side sorting and pagination.
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) NovelStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = "desc") String dir,
            Model model) {
        String sortField = SORT_FIELDS.contains(sort) ? sort : DEFAULT_SORT;
        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(page, 0), pageSize,
                Sort.by(direction, sortField).and(Sort.by("id")));

        Page<LightNovel> novels = novelService.search(keyword, authorId,
                genreId, status, pageable);

        model.addAttribute("novels", novels);
        model.addAttribute("keyword", keyword);
        model.addAttribute("authorId", authorId);
        model.addAttribute("genreId", genreId);
        model.addAttribute("status", status);
        model.addAttribute("size", pageSize);
        model.addAttribute("sort", sortField);
        model.addAttribute("dir", direction.name().toLowerCase());
        model.addAttribute("authors", authorService.findAllSorted());
        model.addAttribute("genres", genreService.findAllSorted());
        model.addAttribute("statuses", NovelStatus.values());
        return "novels/list";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("novel", novelService.findById(id));
        return "novels/details";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(required = false) Long authorId, Model model) {
        LightNovelDto novel = new LightNovelDto();
        novel.setAuthorId(authorId);
        novel.setStatus(NovelStatus.ONGOING);
        model.addAttribute("novel", novel);
        addFormOptions(model, null);
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("novel") LightNovelDto novel,
            BindingResult bindingResult,
            @RequestParam(value = "coverFile", required = false)
            MultipartFile coverFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            addFormOptions(model, null);
            return FORM_VIEW;
        }
        LightNovel saved;
        try {
            saved = novelService.create(novel, coverFile);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            addFormOptions(model, null);
            return FORM_VIEW;
        } catch (FileStorageException e) {
            model.addAttribute("coverError", e.getMessage());
            addFormOptions(model, null);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                saved.getTitle() + " was added");
        return "redirect:/novels/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("novel",
                LightNovelDto.from(novelService.findById(id)));
        addFormOptions(model, id);
        return FORM_VIEW;
    }

    @PutMapping("/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("novel") LightNovelDto novel,
            BindingResult bindingResult,
            @RequestParam(value = "coverFile", required = false)
            MultipartFile coverFile,
            Model model,
            RedirectAttributes redirectAttributes) {
        novel.setId(id);
        if (bindingResult.hasErrors()) {
            addFormOptions(model, id);
            return FORM_VIEW;
        }
        try {
            novelService.update(id, novel, coverFile);
        } catch (DuplicateResourceException e) {
            bindingResult.rejectValue(e.getField(), "duplicate",
                    e.getMessage());
            addFormOptions(model, id);
            return FORM_VIEW;
        } catch (FileStorageException e) {
            model.addAttribute("coverError", e.getMessage());
            addFormOptions(model, id);
            return FORM_VIEW;
        }
        redirectAttributes.addFlashAttribute("successMessage",
                novel.getTitle() + " was updated");
        return "redirect:/novels/" + id;
    }

    /**
     * Uploads a new cover image from the novel details page.
     */
    @PostMapping("/{id}/cover")
    public String uploadCover(@PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        try {
            novelService.updateCover(id, file);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cover image was uploaded");
        } catch (FileStorageException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
        }
        return "redirect:/novels/" + id;
    }

    @DeleteMapping("/{id}/cover")
    public String removeCover(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        novelService.removeCover(id);
        redirectAttributes.addFlashAttribute("successMessage",
                "Cover image was removed");
        return "redirect:/novels/" + id;
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        String title = novelService.findById(id).getTitle();
        novelService.delete(id);
        redirectAttributes.addFlashAttribute("successMessage",
                title + " was deleted");
        return "redirect:/novels";
    }

    /**
     * Adds the lists needed by the novel form (authors, genres, statuses
     * and the current cover when editing).
     */
    private void addFormOptions(Model model, Long novelId) {
        model.addAttribute("authors", authorService.findAllSorted());
        model.addAttribute("genres", genreService.findAllSorted());
        model.addAttribute("statuses", NovelStatus.values());
        if (novelId != null) {
            model.addAttribute("currentCover",
                    novelService.findById(novelId).getCoverImage());
        }
    }
}
