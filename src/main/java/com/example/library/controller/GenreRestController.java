package com.example.library.controller;

import com.example.library.dto.GenreDto;
import com.example.library.entity.Genre;
import com.example.library.service.GenreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * JSON REST API for genres.
 */
@RestController
@RequestMapping("/api/genres")
public class GenreRestController {

    private final GenreService genreService;

    public GenreRestController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    public PagedModel<GenreDto> list(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return new PagedModel<>(
                genreService.findAll(pageable).map(GenreDto::from));
    }

    @GetMapping("/{id}")
    public GenreDto get(@PathVariable Long id) {
        return GenreDto.from(genreService.findById(id));
    }

    @PostMapping
    public ResponseEntity<GenreDto> create(
            @Valid @RequestBody GenreDto request) {
        Genre saved = genreService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(GenreDto.from(saved));
    }

    @PutMapping("/{id}")
    public GenreDto update(@PathVariable Long id,
            @Valid @RequestBody GenreDto request) {
        return GenreDto.from(genreService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        genreService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
