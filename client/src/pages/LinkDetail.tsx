import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import {
  api,
  ApiError,
  LinkResponse,
  DailyClicksResponse,
  UserAgentClicksResponse,
} from '../api/client';
import DailyChart from '../components/DailyChart';
import UserAgentChart from '../components/UserAgentChart';
import './Page.css';

function friendlyLoadError(e: unknown): string {
  if (e instanceof ApiError) {
    if (e.status === 0) return e.message;
    if (e.status === 404) return "This link doesn't exist or may have been deleted.";
    if (e.status >= 500) return 'The server encountered an error — please try again shortly.';
  }
  return e instanceof Error ? e.message : 'Failed to load link';
}

export default function LinkDetail() {
  const { id } = useParams<{ id: string }>();
  const linkId = Number(id);

  const [link, setLink] = useState<LinkResponse | null>(null);
  const [totalClicks, setTotalClicks] = useState<number>(0);
  const [dailyClicks, setDailyClicks] = useState<DailyClicksResponse[]>([]);
  const [userAgentClicks, setUserAgentClicks] = useState<UserAgentClicksResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      if (isNaN(linkId)) {
        setError('Invalid link ID.');
        setLoading(false);
        return;
      }
      try {
        setLoading(true);
        setError(null);
        const [linkData, totalData, dailyData, uaData] = await Promise.all([
          api.links.get(linkId),
          api.analytics.totalClicks(linkId),
          api.analytics.dailyClicks(linkId),
          api.analytics.userAgentBreakdown(linkId),
        ]);
        setLink(linkData);
        setTotalClicks(totalData.totalClicks);
        setDailyClicks(dailyData);
        setUserAgentClicks(uaData);
      } catch (e) {
        setError(friendlyLoadError(e));
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [linkId]);

  if (loading) return <div className="page-loading">Loading...</div>;

  if (error)
    return (
      <div className="page">
        <div className="page-header">
          <div>
            <Link to="/" className="back-link">
              ← Dashboard
            </Link>
            <h1>Link Analytics</h1>
          </div>
        </div>
        <div className="error-banner">{error}</div>
      </div>
    );

  if (!link) return null;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <Link to="/" className="back-link">
            ← Dashboard
          </Link>
          <h1>Link Analytics</h1>
          <p className="page-subtitle">/{link.slug}</p>
        </div>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-highlight">
          <div className="stat-label">Total Clicks</div>
          <div className="stat-value stat-number">{totalClicks.toLocaleString()}</div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Created</div>
          <div className="stat-value">
            {new Date(link.createdAt).toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Short URL</div>
          <div className="stat-value stat-url">
            <a href={link.shortUrl} target="_blank" rel="noopener noreferrer" title={link.shortUrl}>
              {link.shortUrl}
            </a>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-label">Target URL</div>
          <div className="stat-value stat-url">
            <a
              href={link.targetUrl}
              target="_blank"
              rel="noopener noreferrer"
              title={link.targetUrl}
            >
              {link.targetUrl}
            </a>
          </div>
        </div>
      </div>

      <div className="charts-row">
        <div className="card chart-card">
          <h3 className="card-title">Clicks Over Time</h3>
          <DailyChart data={dailyClicks} />
        </div>
        <div className="card chart-card">
          <h3 className="card-title">Browsers</h3>
          <UserAgentChart data={userAgentClicks} />
        </div>
      </div>
    </div>
  );
}
