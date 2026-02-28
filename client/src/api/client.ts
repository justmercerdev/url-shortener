export const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export interface LinkResponse {
  id: number;
  slug: string;
  targetUrl: string;
  createdAt: string;
  shortUrl: string;
  clickCount: number;
}

export interface CreateLinkRequest {
  targetUrl: string;
  slug?: string;
}

export interface DailyClicksResponse {
  day: string;
  count: number;
}

export interface UserAgentClicksResponse {
  browserType: string;
  count: number;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    });
  } catch {
    throw new ApiError('Cannot connect to the server — check that the backend is running.', 0);
  }

  if (!res.ok) {
    let message = `Unexpected error (HTTP ${res.status})`;
    try {
      const err: ErrorResponse = await res.json();
      if (err.message) message = err.message;
    } catch {
      // Backend returned a non-JSON body (e.g. nginx 502 gateway error page)
    }
    throw new ApiError(message, res.status);
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export const api = {
  links: {
    list: () => request<LinkResponse[]>('/api/v1/links'),
    get: (id: number) => request<LinkResponse>(`/api/v1/links/${id}`),
    create: (body: CreateLinkRequest) =>
      request<LinkResponse>('/api/v1/links', {
        method: 'POST',
        body: JSON.stringify(body),
      }),
    delete: (id: number) => request<void>(`/api/v1/links/${id}`, { method: 'DELETE' }),
  },
  analytics: {
    totalClicks: (id: number) =>
      request<{ linkId: number; totalClicks: number }>(`/api/v1/analytics/${id}/clicks`),
    dailyClicks: (id: number) =>
      request<DailyClicksResponse[]>(`/api/v1/analytics/${id}/clicks/daily`),
    userAgentBreakdown: (id: number) =>
      request<UserAgentClicksResponse[]>(`/api/v1/analytics/${id}/clicks/user-agents`),
  },
};
