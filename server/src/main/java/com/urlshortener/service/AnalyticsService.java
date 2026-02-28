package com.urlshortener.service;

import com.urlshortener.dto.DailyClicksResponse;
import com.urlshortener.dto.UserAgentClicksResponse;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.ClickEventRepository;
import com.urlshortener.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final ClickEventRepository clickEventRepository;
    private final LinkRepository linkRepository;

    public AnalyticsService(ClickEventRepository clickEventRepository,
                            LinkRepository linkRepository) {
        this.clickEventRepository = clickEventRepository;
        this.linkRepository = linkRepository;
    }

    public long getTotalClicks(Long linkId) {
        requireLinkExists(linkId);
        return clickEventRepository.countByLinkId(linkId);
    }

    public List<DailyClicksResponse> getDailyClicks(Long linkId) {
        requireLinkExists(linkId);
        return clickEventRepository.dailyClicksByLinkId(linkId).stream()
            .map(p -> new DailyClicksResponse(p.getDay(), p.getCount()))
            .toList();
    }

    public List<UserAgentClicksResponse> getUserAgentBreakdown(Long linkId) {
        requireLinkExists(linkId);
        return clickEventRepository.userAgentBreakdownByLinkId(linkId).stream()
            .map(p -> new UserAgentClicksResponse(p.getBrowserType(), p.getCount()))
            .toList();
    }

    private void requireLinkExists(Long linkId) {
        if (!linkRepository.existsById(linkId)) {
            throw new ResourceNotFoundException("Link with id " + linkId + " not found");
        }
    }
}
