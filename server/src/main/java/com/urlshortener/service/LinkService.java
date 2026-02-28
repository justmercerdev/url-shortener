package com.urlshortener.service;

import com.urlshortener.dto.CreateLinkRequest;
import com.urlshortener.dto.LinkResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.SlugConflictException;
import com.urlshortener.model.Link;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.LinkRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LinkService {

    private final LinkRepository linkRepository;
    private final ClickEventRepository clickEventRepository;
    private final String baseUrl;

    public LinkService(LinkRepository linkRepository,
                       ClickEventRepository clickEventRepository,
                       @Value("${app.base-url:http://localhost:8080}") String baseUrl) {
        this.linkRepository = linkRepository;
        this.clickEventRepository = clickEventRepository;
        this.baseUrl = baseUrl;
    }

    public LinkResponse createLink(CreateLinkRequest request) {
        // Return the existing record if this URL has already been shortened
        var existing = linkRepository.findByTargetUrl(request.targetUrl());
        if (existing.isPresent()) {
            return LinkResponse.from(existing.get(), baseUrl);
        }

        String slug = resolveSlug(request.slug());

        if (linkRepository.existsBySlug(slug)) {
            throw new SlugConflictException("Slug '" + slug + "' is already taken");
        }

        Link link = new Link();
        link.setSlug(slug);
        link.setTargetUrl(request.targetUrl());
        link = linkRepository.save(link);

        return LinkResponse.from(link, baseUrl);
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> listLinks() {
        return linkRepository.findAllWithClickCounts().stream()
            .map(p -> new LinkResponse(
                p.getId(),
                p.getSlug(),
                p.getTargetUrl(),
                p.getCreatedAt(),
                baseUrl + "/" + p.getSlug(),
                p.getClickCount()
            ))
            .toList();
    }

    @Transactional(readOnly = true)
    public LinkResponse getLink(Long id) {
        Link link = linkRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Link with id " + id + " not found"));
        long clickCount = clickEventRepository.countByLinkId(id);
        return new LinkResponse(
            link.getId(),
            link.getSlug(),
            link.getTargetUrl(),
            link.getCreatedAt(),
            baseUrl + "/" + link.getSlug(),
            clickCount
        );
    }

    public void deleteLink(Long id) {
        if (!linkRepository.existsById(id)) {
            throw new ResourceNotFoundException("Link with id " + id + " not found");
        }
        linkRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Link findBySlug(String slug) {
        return linkRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Slug '" + slug + "' does not exist"));
    }

    private String resolveSlug(String requestedSlug) {
        if (requestedSlug != null && !requestedSlug.isBlank()) {
            return requestedSlug;
        }
        return generateUniqueSlug();
    }

    private String generateUniqueSlug() {
        for (int i = 0; i < 5; i++) {
            String candidate = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
            if (!linkRepository.existsBySlug(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("Failed to generate unique slug after 5 attempts");
    }
}
