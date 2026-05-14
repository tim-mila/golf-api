import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { scorecardCreate } from '@/lib/api/generated/sdk.gen';
import type { ScorecardRequestDto } from '@/lib/api/generated/types.gen';

export async function POST(req: NextRequest) {
  let apiClient;
  try {
    apiClient = await getApiClient();
  } catch {
    return new Response(null, { status: 401 });
  }

  const body: ScorecardRequestDto = await req.json();
  const { data, error, response } = await scorecardCreate({ client: apiClient, body });
  if (!response) return new Response(null, { status: 502 });
  return Response.json(data ?? error, { status: response.status });
}
