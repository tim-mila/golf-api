import { auth0 } from "@/lib/auth0";
import { redirect } from "next/navigation";

export default async function Home() {
  const session = await auth0.getSession();

  if (session) {
    redirect("/dashboard");
  }

  return (
    <main className="flex flex-col items-center justify-center min-h-screen gap-6 p-8">
      <div className="text-center space-y-2">
        <h1 className="text-4xl font-bold tracking-tight">Golf Tracker</h1>
        <p className="text-gray-500">Track your rounds and monitor your handicap index.</p>
      </div>
      <a
        href="/auth/login?returnTo=/dashboard"
        className="px-6 py-3 rounded-lg bg-green-700 text-white font-semibold hover:bg-green-800 transition-colors"
      >
        Sign In
      </a>
    </main>
  );
}
