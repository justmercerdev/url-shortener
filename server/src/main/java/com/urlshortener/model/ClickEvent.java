package com.urlshortener.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "click_events")
public class ClickEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "link_id", nullable = false)
    private Link link;

    @Column(name = "clicked_at", nullable = false)
    private OffsetDateTime clickedAt;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (clickedAt == null) {
            clickedAt = OffsetDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Link getLink() { return link; }
    public void setLink(Link link) { this.link = link; }

    public OffsetDateTime getClickedAt() { return clickedAt; }
    public void setClickedAt(OffsetDateTime clickedAt) { this.clickedAt = clickedAt; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
