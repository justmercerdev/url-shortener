package com.urlshortener.repository;

import com.urlshortener.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    interface LinkWithCount {
        Long getId();
        String getSlug();
        String getTargetUrl();
        OffsetDateTime getCreatedAt();
        Long getClickCount();
    }

    @Query("""
        SELECT l.id as id, l.slug as slug, l.targetUrl as targetUrl,
               l.createdAt as createdAt, COUNT(c) as clickCount
        FROM Link l LEFT JOIN l.clickEvents c
        GROUP BY l.id, l.slug, l.targetUrl, l.createdAt
        ORDER BY l.createdAt DESC
        """)
    List<LinkWithCount> findAllWithClickCounts();

    Optional<Link> findBySlug(String slug);
    Optional<Link> findByTargetUrl(String targetUrl);
    boolean existsBySlug(String slug);
}
