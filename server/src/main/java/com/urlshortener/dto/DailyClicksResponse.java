package com.urlshortener.dto;

import java.time.LocalDate;

public record DailyClicksResponse(
    LocalDate day,
    long count
) {}
