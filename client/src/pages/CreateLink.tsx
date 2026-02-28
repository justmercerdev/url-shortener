import { useState, FormEvent } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { api, ApiError, BASE_URL } from '../api/client';
import './CreateLink.css';

const API_HOST = new URL(BASE_URL).host;

function friendlyCreateError(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 0) return e.message;
    if (e.status === 409)
      return 'That slug is already taken — try a different one or leave blank for an auto-generated slug.';
    if (e.status === 400) return `Invalid input: ${e.message}`;
    if (e.status >= 500) return 'The server encountered an error — please try again shortly.';
  }
  return e instanceof Error ? e.message : 'Failed to create link';
}

export default function CreateLink() {
  const navigate = useNavigate();
  const [targetUrl, setTargetUrl] = useState('');
  const [slug, setSlug] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    try {
      setSubmitting(true);
      const link = await api.links.create({
        targetUrl: targetUrl.trim(),
        slug: slug.trim() || undefined,
      });
      navigate(`/links/${link.id}`);
    } catch (e) {
      setError(friendlyCreateError(e));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="create-page">
      <div className="create-card">
        <div className="create-heading">
          <div className="create-icon">→</div>
          <div>
            <h1>Create Short Link</h1>
            <p>Paste a long URL and get a short one back instantly.</p>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="create-form">
          <div className="field">
            <label htmlFor="targetUrl" className="field-label">
              Destination URL <span className="field-required">*</span>
            </label>
            <input
              id="targetUrl"
              type="url"
              className="field-input"
              placeholder="https://example.com/some/very/long/path"
              value={targetUrl}
              onChange={(e) => setTargetUrl(e.target.value)}
              required
              autoFocus
            />
          </div>

          <div className="field">
            <label htmlFor="slug" className="field-label">
              Custom slug
              <span className="field-optional">optional</span>
            </label>
            <div className="slug-row">
              <span className="slug-host">{API_HOST}/</span>
              <input
                id="slug"
                type="text"
                className="field-input slug-field-input"
                placeholder="my-link"
                value={slug}
                onChange={(e) => setSlug(e.target.value)}
                pattern="[a-zA-Z0-9_-]*"
                maxLength={20}
              />
              <span className={`char-count ${slug.length >= 18 ? 'char-count-warn' : ''}`}>
                {slug.length}/20
              </span>
            </div>
            <p className="field-hint">
              Letters, digits, hyphens and underscores only. Leave blank to auto-generate.
            </p>
          </div>

          {error && (
            <div className="create-error">
              <span className="create-error-icon">!</span>
              {error}
            </div>
          )}

          <button type="submit" className="create-submit" disabled={submitting}>
            {submitting ? 'Creating…' : 'Create Short Link'}
          </button>
        </form>

        <div className="create-footer">
          <Link to="/" className="create-cancel">
            ← Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
}
