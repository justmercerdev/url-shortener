package com.urlshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateLinkRequest(
    @NotBlank(message = "targetUrl must not be blank")
    @URL(message = "targetUrl must be a valid URL")
    String targetUrl,

    @Size(max = 20, message = "slug must be 20 characters or fewer")
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$", message = "slug may only contain letters, digits, hyphens, and underscores")
    String slug
) {}
