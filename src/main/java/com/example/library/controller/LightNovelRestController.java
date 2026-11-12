package com.example.library.controller;

import com.example.library.dto.LightNovelDto;
import com.example.library.dto.LightNovelResponse;
import com.example.library.entity.LightNovel;
import com.example.library.entity.NovelStatus;
import com.example.library.service.LightNovelService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

/**
 * JSON REST API for light novels.
 */
@RestController
@RequestMapping("/api/novels")
public class LightNovelRestController {

    private static final Set<String> SORT_FIELDS =
            Set.of("id", "title", "rating", "chapterCount", "status",
                    "createdAt");

    private final LightNovelService novelService;

    public LightNovelRestController(LightNovelService novelService) {
        this.novelService = novelService;
    }

    /**
     * Returns one page of novels, e.g.
     * {@code /api/novels?status=COMPLETED&page=0&size=5&sort=rating,desc}.
     */
    @GetMapping
    public PagedModel<LightNovelResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) NovelStatus status,
            @PageableDefault(size = 10, sort = "rating",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException(
                        "Cannot sort by " + order.getProperty()
                        + ". Allowed fields: " + SORT_FIELDS);
            }
        }
        Page<LightNovelResponse> page = novelService
                .search(keyword, authorId, genreId, status, pageable)
                .map(LightNovelResponse::from);
        return new PagedModel<>(page);
    }

    @GetMapping("/{id}")
    public LightNovelResponse get(@PathVariable Long id) {
        return LightNovelResponse.from(novelService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LightNovelResponse> create(
            @Valid @RequestBody LightNovelDto request) {
        LightNovel saved = novelService.create(request, null);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(LightNovelResponse.from(saved));
    }

    @PutMapping("/{id}")
    public LightNovelResponse update(@PathVariable Long id,
            @Valid @RequestBody LightNovelDto request) {
        return LightNovelResponse.from(
                novelService.update(id, request, null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        novelService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Uploads a cover image as multipart/form-data with a "file" part.
     */
    @PostMapping(value = "/{id}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LightNovelResponse uploadCover(@PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return LightNovelResponse.from(novelService.updateCover(id, file));
    }

    @DeleteMapping("/{id}/cover")
    public ResponseEntity<Void> removeCover(@PathVariable Long id) {
        novelService.removeCover(id);
        return ResponseEntity.noContent().build();
    }
}
