import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Bar } from 'react-chartjs-2';
import { DailyClicksResponse } from '../api/client';

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend);

interface Props {
  data: DailyClicksResponse[];
}

export default function DailyChart({ data }: Props) {
  const labels = data.map((d) => {
    const date = new Date(d.day + 'T00:00:00Z');
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  });

  const chartData = {
    labels,
    datasets: [
      {
        label: 'Clicks',
        data: data.map((d) => d.count),
        backgroundColor: 'rgba(79, 70, 229, 0.7)',
        borderColor: 'rgba(79, 70, 229, 1)',
        borderWidth: 1,
        borderRadius: 4,
        maxBarThickness: 48,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: { display: false },
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: { stepSize: 1 },
        grid: { color: '#f1f5f9' },
      },
      x: {
        grid: { display: false },
      },
    },
  };

  if (data.length === 0) {
    return (
      <p style={{ textAlign: 'center', color: '#94a3b8', padding: '2rem 0' }}>No click data yet.</p>
    );
  }

  return (
    <div style={{ position: 'relative', height: '320px' }}>
      <Bar data={chartData} options={{ ...options, maintainAspectRatio: false }} />
    </div>
  );
}
