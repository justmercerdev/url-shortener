package com.urlshortener.dto;

import com.urlshortener.model.Link;

import java.time.OffsetDateTime;

public record LinkResponse(
    Long id,
    String slug,
    String targetUrl,
    OffsetDateTime createdAt,
    String shortUrl,
    long clickCount
) {
    public static LinkResponse from(Link link, String baseUrl) {
        return new LinkResponse(
            link.getId(),
            link.getSlug(),
            link.getTargetUrl(),
            link.getCreatedAt(),
            baseUrl + "/" + link.getSlug(),
            0L
        );
    }
}
