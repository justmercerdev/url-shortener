package com.urlshortener.service;

import com.urlshortener.dto.DailyClicksResponse;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.DailyClickProjection;
import com.urlshortener.repository.LinkRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void dailyClicks_acrossThreeDays_returnsCorrectPerDayCounts() {
        Long linkId = 1L;
        LocalDate day1 = LocalDate.of(2024, 1, 1);
        LocalDate day2 = LocalDate.of(2024, 1, 2);
        LocalDate day3 = LocalDate.of(2024, 1, 3);

        when(linkRepository.existsById(linkId)).thenReturn(true);
        when(clickEventRepository.dailyClicksByLinkId(linkId)).thenReturn(List.of(
            projection(day1, 3L),
            projection(day2, 7L),
            projection(day3, 2L)
        ));

        List<DailyClicksResponse> result = analyticsService.getDailyClicks(linkId);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).day()).isEqualTo(day1);
        assertThat(result.get(0).count()).isEqualTo(3L);
        assertThat(result.get(1).day()).isEqualTo(day2);
        assertThat(result.get(1).count()).isEqualTo(7L);
        assertThat(result.get(2).day()).isEqualTo(day3);
        assertThat(result.get(2).count()).isEqualTo(2L);
    }

    @Test
    void dailyClicks_noClicks_returnsEmptyList() {
        Long linkId = 2L;
        when(linkRepository.existsById(linkId)).thenReturn(true);
        when(clickEventRepository.dailyClicksByLinkId(linkId)).thenReturn(List.of());

        List<DailyClicksResponse> result = analyticsService.getDailyClicks(linkId);

        assertThat(result).isEmpty();
    }

    @Test
    void totalClicks_returnsCorrectCount() {
        Long linkId = 1L;
        when(linkRepository.existsById(linkId)).thenReturn(true);
        when(clickEventRepository.countByLinkId(linkId)).thenReturn(42L);

        long total = analyticsService.getTotalClicks(linkId);

        assertThat(total).isEqualTo(42L);
    }

    private DailyClickProjection projection(LocalDate day, Long count) {
        return new DailyClickProjection() {
            @Override public LocalDate getDay() { return day; }
            @Override public Long getCount() { return count; }
        };
    }
}
