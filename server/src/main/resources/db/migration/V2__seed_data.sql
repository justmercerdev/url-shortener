INSERT INTO links (slug, target_url, created_at) VALUES
  ('gh-home',   'https://github.com',                                                  NOW() - INTERVAL '30 days'),
  ('goog',      'https://google.com',                                                  NOW() - INTERVAL '30 days'),
  ('wiki-main', 'https://wikipedia.org',                                               NOW() - INTERVAL '30 days'),
  ('npm-pkg',   'https://npmjs.com/package/react',                                     NOW() - INTERVAL '30 days'),
  ('docs-page', 'https://docs.spring.io/spring-boot/docs/current/reference/html/',     NOW() - INTERVAL '30 days'),
  ('blog-post', 'https://martinfowler.com/articles/microservices.html',                NOW() - INTERVAL '30 days'),
  ('yt-video',  'https://youtube.com',                                                 NOW() - INTERVAL '30 days');

-- User agents defined once; picked randomly per click row via LATERAL
CREATE TEMP TABLE _agents (ua TEXT);
INSERT INTO _agents (ua) VALUES
  ('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'),
  ('Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'),
  ('Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36'),
  ('Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:121.0) Gecko/20100101 Firefox/121.0'),
  ('Mozilla/5.0 (X11; Linux x86_64; rv:121.0) Gecko/20100101 Firefox/121.0'),
  ('Mozilla/5.0 (Macintosh; Intel Mac OS X 14_2_1) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Safari/605.1.15'),
  ('Mozilla/5.0 (iPhone; CPU iPhone OS 17_2 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2 Mobile/15E148 Safari/604.1'),
  ('Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0'),
  ('Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)');

-- gh-home (id=1): steady baseline ~10/day over 30 days
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 1, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, 10) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- gh-home: traffic spikes on a handful of days
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 1, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM (VALUES (1), (4), (8), (13), (19), (24), (27)) AS spikes(day)
CROSS JOIN generate_series(1, 14) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- goog (id=2): steady ~7/day over 30 days
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 2, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, 7) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- wiki-main (id=3): sporadic — gaps every few days
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 3, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, 5) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent
WHERE day % 3 != 1;

-- npm-pkg (id=4): growing audience — more clicks on recent days
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 4, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, GREATEST(2, 16 - day / 2)) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- docs-page (id=5): consistent reference traffic ~5/day
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 5, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, 5) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- blog-post (id=6): viral spike on publish day then decay
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 6, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, GREATEST(1, 22 - day * 2)) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

-- yt-video (id=7): weekend-heavy pattern
INSERT INTO click_events (link_id, clicked_at, user_agent)
SELECT 7, NOW() - (day || ' days')::INTERVAL - random() * INTERVAL '23 hours', agent.ua
FROM generate_series(0, 29) day
CROSS JOIN generate_series(1, CASE WHEN day % 7 IN (0, 1) THEN 14 ELSE 4 END) _
CROSS JOIN LATERAL (SELECT ua FROM _agents ORDER BY random() LIMIT 1) agent;

DROP TABLE _agents;
