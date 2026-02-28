CREATE TABLE links (
  id         BIGSERIAL PRIMARY KEY,
  slug       VARCHAR(20) UNIQUE NOT NULL,
  target_url TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_links_slug ON links(slug);

CREATE TABLE click_events (
  id         BIGSERIAL PRIMARY KEY,
  link_id    BIGINT NOT NULL REFERENCES links(id) ON DELETE CASCADE,
  clicked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  user_agent TEXT
);

CREATE INDEX idx_click_events_link_id ON click_events(link_id);
CREATE INDEX idx_click_events_clicked_at ON click_events(clicked_at);

CREATE INDEX idx_links_target_url ON links(target_url);