package com.urlshortener.service;

import com.urlshortener.model.ClickEvent;
import com.urlshortener.model.Link;
import com.urlshortener.repository.ClickEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickRecordingService {

    private static final Logger log = LoggerFactory.getLogger(ClickRecordingService.class);

    private final ClickEventRepository clickEventRepository;

    public ClickRecordingService(ClickEventRepository clickEventRepository) {
        this.clickEventRepository = clickEventRepository;
    }

    @Async
    @Transactional
    public void recordClick(Link link, String userAgent) {
        try {
            ClickEvent event = new ClickEvent();
            event.setLink(link);
            event.setUserAgent(userAgent);
            clickEventRepository.save(event);
        } catch (Exception ex) {
            log.error("Failed to record click for link id={}", link.getId(), ex);
        }
    }
}
