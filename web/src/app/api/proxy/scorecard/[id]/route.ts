import { getApiClient } from '@/lib/api/server';
import { scorecardDelete } from '@/lib/api/generated/sdk.gen';

export async function DELETE(_req: Request, { params }: { params: Promise<{ id: string }> }) {
  let apiClient;
  try {
    apiClient = await getApiClient();
  } catch {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const { response } = await scorecardDelete({ client: apiClient, path: { id } });
  if (!response) return new Response(null, { status: 502 });
  return new Response(null, { status: response.status });
}
