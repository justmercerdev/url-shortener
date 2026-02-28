import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import CreateLink from './pages/CreateLink';
import LinkDetail from './pages/LinkDetail';
import './App.css';

export default function App() {
  return (
    <BrowserRouter>
      <nav className="navbar">
        <Link to="/" className="brand">
          URL Shortener
        </Link>
      </nav>
      <main className="container">
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/new" element={<CreateLink />} />
          <Route path="/links/:id" element={<LinkDetail />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}
