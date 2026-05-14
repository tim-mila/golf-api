import { type NextRequest } from 'next/server';
import { getApiClient } from '@/lib/api/server';
import { courseDelete, coursePatch } from '@/lib/api/generated/sdk.gen';
import type { PatchCourseRequest } from '@/lib/api/generated/types.gen';

export async function PATCH(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  let apiClient;
  try {
    apiClient = await getApiClient();
  } catch {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const body: PatchCourseRequest = await req.json();
  const { data, error, response } = await coursePatch({ client: apiClient, path: { id }, body });
  if (!response) return new Response(null, { status: 502 });
  return Response.json(data ?? error, { status: response.status });
}

export async function DELETE(_req: Request, { params }: { params: Promise<{ id: string }> }) {
  let apiClient;
  try {
    apiClient = await getApiClient();
  } catch {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const { response } = await courseDelete({ client: apiClient, path: { id } });
  if (!response) return new Response(null, { status: 502 });
  return new Response(null, { status: response.status });
}
