package com.urlshortener.controller;

import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Link;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.LinkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.awaitility.Awaitility;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RedirectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private ClickEventRepository clickEventRepository;

    private Link savedLink;

    @BeforeEach
    void setUp() {
        clickEventRepository.deleteAll();
        linkRepository.deleteAll();

        Link link = new Link();
        link.setSlug("test-slug");
        link.setTargetUrl("https://example.com");
        savedLink = linkRepository.save(link);
    }

    @Test
    void redirect_knownSlug_returns302WithLocationHeader() throws Exception {
        mockMvc.perform(get("/test-slug"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "https://example.com"));
    }

    @Test
    void redirect_unknownSlug_returns404WithErrorBody() throws Exception {
        mockMvc.perform(get("/no-such-slug"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void redirect_knownSlug_persistsClickEvent() throws Exception {
        mockMvc.perform(get("/test-slug")
                .header("User-Agent", "TestAgent/1.0"))
            .andExpect(status().isFound());

        Awaitility.await()
            .atMost(2, TimeUnit.SECONDS)
            .pollInterval(50, TimeUnit.MILLISECONDS)
            .until(() -> clickEventRepository.count() == 1);

        List<ClickEvent> events = clickEventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLink().getId()).isEqualTo(savedLink.getId());
        assertThat(events.get(0).getUserAgent()).isEqualTo("TestAgent/1.0");
    }
}
