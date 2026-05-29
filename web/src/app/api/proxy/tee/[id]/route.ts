import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { teeDelete, teePatch } from '@/lib/api/generated/sdk.gen';
import type { PatchTeeRequest } from '@/lib/api/generated/types.gen';

export async function PATCH(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const client = await getApiClient().catch(() => null);
  if (!client) {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const body: PatchTeeRequest = await req.json();
  const { data, error, response } = await teePatch({ client, path: { id }, body });
  if (!response) {return new Response(null, { status: 502 });}
  return Response.json(data ?? error, { status: response.status });
}

export async function DELETE(_req: Request, { params }: { params: Promise<{ id: string }> }) {
  const client = await getApiClient().catch(() => null);
  if (!client) {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const { response } = await teeDelete({ client, path: { id } });
  if (!response) {return new Response(null, { status: 502 });}
  return new Response(null, { status: response.status });
}
