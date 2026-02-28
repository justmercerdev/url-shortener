import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api, LinkResponse } from '../api/client';
import './LinkTable.css';

interface Props {
  links: LinkResponse[];
  onDelete: (id: number) => void;
}

export default function LinkTable({ links, onDelete }: Props) {
  const [copied, setCopied] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<number | null>(null);

  const copyToClipboard = async (text: string, id: number) => {
    await navigator.clipboard.writeText(text);
    setCopied(id);
    setTimeout(() => setCopied(null), 1500);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('Delete this link?')) return;
    setDeleteError(null);
    setDeleting(id);
    try {
      await api.links.delete(id);
      onDelete(id);
    } catch (e) {
      setDeleteError(e instanceof Error ? e.message : 'Failed to delete link — please try again.');
    } finally {
      setDeleting(null);
    }
  };

  if (links.length === 0) {
    return (
      <div className="empty-state">
        <div className="empty-icon">→</div>
        <p className="empty-title">No links yet</p>
        <p className="empty-desc">Shorten your first URL to get started.</p>
        <Link to="/new" className="btn btn-primary">
          Create Short Link
        </Link>
      </div>
    );
  }

  return (
    <>
      {deleteError && (
        <div className="error-banner">
          <span>{deleteError}</span>
          <button className="btn btn-outline btn-sm" onClick={() => setDeleteError(null)}>
            Dismiss
          </button>
        </div>
      )}
      <div className="table-wrapper">
        <table className="link-table">
          <thead>
            <tr>
              <th>Slug</th>
              <th>Target URL</th>
              <th>Clicks</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {links.map((link) => (
              <tr key={link.id}>
                <td>
                  <code className="slug" title={link.slug}>
                    {link.slug}
                  </code>
                </td>
                <td>
                  <a
                    href={link.targetUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="target-url"
                    title={link.targetUrl}
                  >
                    {link.targetUrl}
                  </a>
                </td>
                <td className="clicks-cell">{link.clickCount.toLocaleString()}</td>
                <td className="date-cell">{new Date(link.createdAt).toLocaleDateString()}</td>
                <td className="actions-cell">
                  <button
                    className="btn btn-sm btn-outline"
                    onClick={() => copyToClipboard(link.shortUrl, link.id)}
                    title="Copy short URL"
                  >
                    {copied === link.id ? 'Copied!' : 'Copy'}
                  </button>
                  <Link to={`/links/${link.id}`} className="btn btn-sm btn-primary">
                    Analytics
                  </Link>
                  <button
                    className="btn btn-sm btn-danger"
                    onClick={() => handleDelete(link.id)}
                    disabled={deleting === link.id}
                    title="Delete link"
                  >
                    {deleting === link.id ? 'Deleting...' : 'Delete'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
