package com.urlshortener.controller;

import com.urlshortener.dto.ErrorResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.model.Link;
import com.urlshortener.service.ClickRecordingService;
import com.urlshortener.service.LinkService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class RedirectController {

    private final LinkService linkService;
    private final ClickRecordingService clickRecordingService;

    public RedirectController(LinkService linkService,
                              ClickRecordingService clickRecordingService) {
        this.linkService = linkService;
        this.clickRecordingService = clickRecordingService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<?> redirect(
            @PathVariable String slug,
            @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent) {
        try {
            Link link = linkService.findBySlug(slug);
            clickRecordingService.recordClick(link, userAgent);
            return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(link.getTargetUrl()))
                .build();
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, "Not Found", ex.getMessage()));
        }
    }
}
