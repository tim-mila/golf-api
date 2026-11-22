import { auth0 } from "@/lib/auth0";
import { redirect } from "next/navigation";
import HandicapIndex from "../components/handicap-index/handicap-index";
import SeasonSummary from "../components/season-summary/season-summary";
import RoundsList from "../components/rounds-list/rounds-list";

export default async function Dashboard() {
  const session = await auth0.getSession();

  if (!session) {
    redirect("/auth/login?returnTo=/dashboard");
  }

  return (
    <main className="grid grid-cols-1 md:grid-cols-[2fr_3fr] gap-6 p-8">
      <div className="dashboard-card">
        <HandicapIndex />
      </div>
      <div className="dashboard-card">
        <SeasonSummary />
      </div>
      <div className="dashboard-card md:col-span-2">
        <RoundsList />
      </div>
    </main>
  );
}
