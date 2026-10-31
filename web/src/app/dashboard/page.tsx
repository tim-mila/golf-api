import { auth0 } from "@/lib/auth0";
import { redirect } from "next/navigation";
import { ThemeToggle } from "../components/theme/theme-toggle";

export default async function Dashboard() {
  const session = await auth0.getSession();

  if (!session) {
    redirect("/auth/login?returnTo=/dashboard");
  }

  return (
    <main className="flex flex-col items-center justify-center min-h-screen gap-6 p-8">
      <div className="text-center space-y-2">
        <h1 className="text-4xl font-bold tracking-tight">
          Welcome, {session.user.name}!
        </h1>
        <p>{session.user.email}</p>
        <ThemeToggle />
      </div>
      <a
        href="/auth/logout"
        className="px-6 py-3 rounded-lg font-semibold  transition-colors"
      >
        Sign Out
      </a>
    </main>
  );
}
