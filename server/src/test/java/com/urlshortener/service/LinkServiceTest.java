package com.urlshortener.service;

import com.urlshortener.dto.CreateLinkRequest;
import com.urlshortener.dto.LinkResponse;
import com.urlshortener.model.Link;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @InjectMocks
    private LinkService linkService;

    @Test
    void createLink_existingUrl_returnsExistingLinkWithoutSaving() {
        Link existing = new Link();
        existing.setId(42L);
        existing.setSlug("already-exists");
        existing.setTargetUrl("https://example.com");

        when(linkRepository.findByTargetUrl("https://example.com")).thenReturn(Optional.of(existing));

        LinkResponse response = linkService.createLink(new CreateLinkRequest("https://example.com", null));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.slug()).isEqualTo("already-exists");
        verify(linkRepository, never()).save(any());
    }

    @Test
    void createLink_newUrl_savesAndReturnsNewLink() {
        when(linkRepository.findByTargetUrl("https://new.com")).thenReturn(Optional.empty());
        when(linkRepository.existsBySlug(any())).thenReturn(false);

        Link saved = new Link();
        saved.setId(1L);
        saved.setSlug("custom");
        saved.setTargetUrl("https://new.com");
        when(linkRepository.save(any())).thenReturn(saved);

        LinkResponse response = linkService.createLink(new CreateLinkRequest("https://new.com", "custom"));

        assertThat(response.slug()).isEqualTo("custom");
        verify(linkRepository, times(1)).save(any());
    }
}
