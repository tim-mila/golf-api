import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { teeCreate } from '@/lib/api/generated/sdk.gen';
import type { CreateTeeRequest } from '@/lib/api/generated/types.gen';

export async function POST(req: NextRequest) {
  let apiClient;
  try {
    apiClient = await getApiClient();
  } catch {
    return new Response(null, { status: 401 });
  }

  const body: CreateTeeRequest = await req.json();
  const { data, error, response } = await teeCreate({ client: apiClient, body });
  if (!response) return new Response(null, { status: 502 });
  return Response.json(data ?? error, { status: response.status });
}
