import { auth0 } from "@/lib/auth0";
import { redirect } from "next/navigation";

export default async function Dashboard() {
  const session = await auth0.getSession();

  if (!session) {
    redirect("/auth/login?returnTo=/dashboard");
  }

  return (
    <main className="flex flex-col items-center justify-center min-h-screen gap-6 p-8">
      <div className="text-center space-y-2">
        <h1 className="text-4xl font-bold tracking-tight">Welcome, {session.user.name}!</h1>
        <p className="text-gray-500">{session.user.email}</p>
      </div>
      <a
        href="/auth/logout"
        className="px-6 py-3 rounded-lg bg-gray-200 text-gray-800 font-semibold hover:bg-gray-300 transition-colors"
      >
        Sign Out
      </a>
    </main>
  );
}
