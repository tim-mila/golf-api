import 'server-only';
import { createClient, createConfig } from '@/lib/api/generated/client';
import { auth0 } from '@/lib/auth0';

export async function getApiClient() {
  const { token } = await auth0.getAccessToken();
  return createClient(createConfig({
    baseUrl: process.env.API_BASE_URL!,
    auth: token,
  }));
}
