import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { Pie } from 'react-chartjs-2';
import { UserAgentClicksResponse } from '../api/client';

ChartJS.register(ArcElement, Tooltip, Legend);

const BROWSER_COLORS: Record<string, string> = {
  Chrome: '#6366f1',
  Firefox: '#f59e0b',
  Safari: '#10b981',
  Edge: '#3b82f6',
  Bot: '#9ca3af',
  Other: '#8b5cf6',
  Unknown: '#d1d5db',
};

const FALLBACK_COLOR = '#e5e7eb';

interface Props {
  data: UserAgentClicksResponse[];
}

export default function UserAgentChart({ data }: Props) {
  if (data.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#94a3b8', padding: '2rem 0' }}>No click data yet.</p>
    );
  }

  const chartData = {
    labels: data.map((d) => d.browserType),
    datasets: [
      {
        data: data.map((d) => d.count),
        backgroundColor: data.map((d) => BROWSER_COLORS[d.browserType] ?? FALLBACK_COLOR),
        borderColor: '#ffffff',
        borderWidth: 2,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'bottom' as const,
        labels: { padding: 16, font: { size: 13 } },
      },
      tooltip: {
        callbacks: {
          label: (ctx: { label: string; parsed: number; dataset: { data: number[] } }) => {
            const total = ctx.dataset.data.reduce((a, b) => a + b, 0);
            const pct = ((ctx.parsed / total) * 100).toFixed(1);
            return ` ${ctx.label}: ${ctx.parsed.toLocaleString()} (${pct}%)`;
          },
        },
      },
    },
  };

  return (
    <div style={{ position: 'relative', height: '320px' }}>
      <Pie data={chartData} options={options} />
    </div>
  );
}
