import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, LinkResponse } from '../api/client';
import LinkTable from '../components/LinkTable';
import './Page.css';

export default function Dashboard() {
  const [links, setLinks] = useState<LinkResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadLinks();
  }, []);

  async function loadLinks() {
    try {
      setLoading(true);
      setError(null);
      const data = await api.links.list();
      setLinks(data);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Failed to load links');
    } finally {
      setLoading(false);
    }
  }

  const handleDelete = (id: number) => {
    setLinks((current) => current.filter((link) => link.id !== id));
  };

  const totalClicks = links.reduce((sum, link) => sum + link.clickCount, 0);

  if (loading) return <div className="page-loading">Loading...</div>;

  if (error)
    return (
      <div className="page">
        <div className="page-header">
          <h1>Dashboard</h1>
          <Link to="/new" className="btn btn-primary">
            + New Link
          </Link>
        </div>
        <div className="error-banner">
          <span>{error}</span>
          <button className="btn btn-outline btn-sm" onClick={() => loadLinks()}>
            Retry
          </button>
        </div>
      </div>
    );

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p className="page-subtitle">
            {links.length} link{links.length !== 1 ? 's' : ''}
            {links.length > 0 &&
              ` · ${totalClicks.toLocaleString()} total click${totalClicks !== 1 ? 's' : ''}`}
          </p>
        </div>
        <Link to="/new" className="btn btn-primary">
          + New Link
        </Link>
      </div>
      <LinkTable links={links} onDelete={handleDelete} />
    </div>
  );
}
