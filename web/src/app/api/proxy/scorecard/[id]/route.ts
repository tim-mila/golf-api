import { getApiClient } from '@/lib/api/server';
import { scorecardDelete } from '@/lib/api/generated/sdk.gen';

export async function DELETE(_req: Request, { params }: { params: Promise<{ id: string }> }) {
  const client = await getApiClient().catch(() => null);
  if (!client) {
    return new Response(null, { status: 401 });
  }

  const { id } = await params;
  const { response } = await scorecardDelete({ client, path: { id } });
  if (!response) {return new Response(null, { status: 502 });}
  return new Response(null, { status: response.status });
}
