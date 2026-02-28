package com.urlshortener.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.model.Link;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();
    }

    @Test
    void createLink_validBody_returns201WithSlug() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "https://example.com");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value(notNullValue()))
            .andExpect(jsonPath("$.targetUrl").value("https://example.com"))
            .andExpect(jsonPath("$.id").value(notNullValue()));
    }

    @Test
    void createLink_customSlug_returnsProvidedSlug() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "https://example.com", "slug", "my-link");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("my-link"));
    }

    @Test
    void createLink_duplicateSlug_returns409() throws Exception {
        Link existing = new Link();
        existing.setSlug("taken-slug");
        existing.setTargetUrl("https://existing.com");
        linkRepository.save(existing);

        Map<String, String> body = Map.of("targetUrl", "https://another.com", "slug", "taken-slug");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createLink_blankTargetUrl_returns400() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createLink_invalidUrl_returns400() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "not-a-url");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createLink_duplicateUrl_returnsExistingLink() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "https://example.com");

        String firstResponse = mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(objectMapper.readTree(firstResponse).get("id").asLong()))
            .andExpect(jsonPath("$.slug").value(objectMapper.readTree(firstResponse).get("slug").asText()))
            .andReturn().getResponse().getContentAsString();

        assertThat(objectMapper.readTree(firstResponse).get("id"))
            .isEqualTo(objectMapper.readTree(secondResponse).get("id"));
    }

    @Test
    void createLink_duplicateUrl_doesNotCreateNewRecord() throws Exception {
        Map<String, String> body = Map.of("targetUrl", "https://example.com");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isCreated());

        assertThat(linkRepository.count()).isEqualTo(1);
    }

    @Test
    void createLink_duplicateUrl_withDifferentSlug_returnsOriginalSlug() throws Exception {
        Map<String, String> first = Map.of("targetUrl", "https://example.com", "slug", "original");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(first)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("original"));

        Map<String, String> second = Map.of("targetUrl", "https://example.com", "slug", "new-slug");

        mockMvc.perform(post("/api/v1/links")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(second)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.slug").value("original"));
    }
}
