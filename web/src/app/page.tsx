import { auth0 } from "@/lib/auth0";
import { redirect } from "next/navigation";
import LinkButton from "./components/link-button/link-button";

export default async function Home() {
  const session = await auth0.getSession();

  if (session) {
    redirect("/dashboard");
  }

  return (
    <main className="flex flex-col items-center justify-center min-h-screen gap-6 p-8">
      <div className="text-center space-y-2">
        <h1 className="text-4xl font-bold tracking-tight">Fairway.</h1>
        <p>Know your number.</p>
      </div>
      <LinkButton href="/auth/login?returnTo=/dashboard">Sign In</LinkButton>
    </main>
  );
}
