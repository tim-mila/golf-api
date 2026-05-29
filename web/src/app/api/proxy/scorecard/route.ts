import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { scorecardCreate } from '@/lib/api/generated/sdk.gen';
import type { ScorecardRequestDto } from '@/lib/api/generated/types.gen';

export async function POST(req: NextRequest) {
  const client = await getApiClient().catch(() => null);
  if (!client) {
    return new Response(null, { status: 401 });
  }

  const body: ScorecardRequestDto = await req.json();
  const { data, error, response } = await scorecardCreate({ client, body });
  if (!response) {return new Response(null, { status: 502 });}
  return Response.json(data ?? error, { status: response.status });
}
