package com.urlshortener.dto;

public record UserAgentClicksResponse(
    String browserType,
    long count
) {}
