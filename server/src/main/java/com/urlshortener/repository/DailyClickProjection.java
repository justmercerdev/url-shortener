package com.urlshortener.repository;

import java.time.LocalDate;

public interface DailyClickProjection {
    LocalDate getDay();
    Long getCount();
}
