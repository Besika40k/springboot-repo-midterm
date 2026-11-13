package com.example.library.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Tests the novel pages and the REST API against the sample data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class LightNovelControllerTests {

    private static final byte[] PNG_BYTES = {(byte) 0x89, 'P', 'N', 'G'};

    private final MockMvc mockMvc;

    LightNovelControllerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void novelListPageIsPaginatedAndSorted() throws Exception {
        mockMvc.perform(get("/novels")
                        .param("size", "4")
                        .param("sort", "chapterCount")
                        .param("dir", "desc")
                        .param("status", "ONGOING"))
                .andExpect(status().isOk())
                .andExpect(view().name("novels/list"))
                .andExpect(model().attribute("sort", "chapterCount"))
                .andExpect(model().attribute("size", 4));
    }

    @Test
    void invalidNovelFormShowsValidationErrors() throws Exception {
        mockMvc.perform(multipart("/novels")
                        .param("title", "")
                        .param("chapterCount", "0")
                        .param("rating", "7"))
                .andExpect(status().isOk())
                .andExpect(view().name("novels/form"))
                .andExpect(model().attributeHasFieldErrors("novel",
                        "title", "status", "chapterCount", "rating",
                        "authorId", "genreIds"));
    }

    @Test
    void unknownNovelShowsNotFoundPage() throws Exception {
        mockMvc.perform(get("/novels/99999"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"));
    }

    @Test
    void apiReturnsPagedNovelsFilteredByStatus() throws Exception {
        mockMvc.perform(get("/api/novels")
                        .param("status", "ONGOING")
                        .param("size", "3")
                        .param("sort", "chapterCount,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page.size").value(3))
                .andExpect(jsonPath("$.content[*].status",
                        everyItem(is("ONGOING"))))
                .andExpect(jsonPath("$.content[0].title")
                        .value("The Mech Touch"));
    }

    @Test
    void apiRejectsInvalidNovel() throws Exception {
        mockMvc.perform(post("/api/novels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\", \"rating\": 9}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.title")
                        .value("Title is required"))
                .andExpect(jsonPath("$.errors.rating")
                        .value("Rating must be between 0 and 5"));
    }

    @Test
    void apiRejectsUnknownSortField() throws Exception {
        mockMvc.perform(get("/api/novels").param("sort", "password"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail",
                        containsString("Cannot sort by")));
    }

    @Test
    void apiUploadsAndRemovesCover() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png",
                MediaType.IMAGE_PNG_VALUE, PNG_BYTES);

        mockMvc.perform(multipart("/api/novels/2/cover").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coverImage", notNullValue()))
                .andExpect(jsonPath("$.coverUrl",
                        containsString("/uploads/")));

        mockMvc.perform(delete("/api/novels/2/cover"))
                .andExpect(status().isNoContent());
    }

    @Test
    void apiRejectsNonImageUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt",
                MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/api/novels/2/cover").file(file))
                .andExpect(status().isBadRequest());
    }
}
