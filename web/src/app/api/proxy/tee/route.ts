import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { teeCreate } from '@/lib/api/generated/sdk.gen';
import type { CreateTeeRequest } from '@/lib/api/generated/types.gen';

export async function POST(req: NextRequest) {
  const client = await getApiClient().catch(() => null);
  if (!client) {
    return new Response(null, { status: 401 });
  }

  const body: CreateTeeRequest = await req.json();
  const { data, error, response } = await teeCreate({ client, body });
  if (!response) {return new Response(null, { status: 502 });}
  return Response.json(data ?? error, { status: response.status });
}
