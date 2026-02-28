package com.urlshortener.repository;

import com.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    @Query("SELECT COUNT(c) FROM ClickEvent c WHERE c.link.id = :linkId")
    long countByLinkId(@Param("linkId") Long linkId);

    @Query("""
        SELECT CAST(c.clickedAt AS LocalDate) as day, COUNT(c) as count
        FROM ClickEvent c
        WHERE c.link.id = :linkId
        GROUP BY CAST(c.clickedAt AS LocalDate)
        ORDER BY day
        """)
    List<DailyClickProjection> dailyClicksByLinkId(@Param("linkId") Long linkId);

    @Query("""
        SELECT
          CASE
            WHEN c.userAgent LIKE '%Googlebot%' THEN 'Bot'
            WHEN c.userAgent LIKE '%Edg/%'      THEN 'Edge'
            WHEN c.userAgent LIKE '%Firefox%'   THEN 'Firefox'
            WHEN c.userAgent LIKE '%Chrome%'    THEN 'Chrome'
            WHEN c.userAgent LIKE '%Safari%'    THEN 'Safari'
            WHEN c.userAgent IS NULL            THEN 'Unknown'
            ELSE 'Other'
          END as browserType,
          COUNT(c) as count
        FROM ClickEvent c
        WHERE c.link.id = :linkId
        GROUP BY
          CASE
            WHEN c.userAgent LIKE '%Googlebot%' THEN 'Bot'
            WHEN c.userAgent LIKE '%Edg/%'      THEN 'Edge'
            WHEN c.userAgent LIKE '%Firefox%'   THEN 'Firefox'
            WHEN c.userAgent LIKE '%Chrome%'    THEN 'Chrome'
            WHEN c.userAgent LIKE '%Safari%'    THEN 'Safari'
            WHEN c.userAgent IS NULL            THEN 'Unknown'
            ELSE 'Other'
          END
        ORDER BY count DESC
        """)
    List<UserAgentProjection> userAgentBreakdownByLinkId(@Param("linkId") Long linkId);
}
