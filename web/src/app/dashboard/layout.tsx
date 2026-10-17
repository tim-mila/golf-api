import { auth0 } from "@/lib/auth0";
import Navigation from "../components/navigation/nav";

export default async function DashboardLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <div className="flex flex-col min-h-screen">
      <Navigation session={await auth0.getSession()} />
      <main className="flex-1">{children}</main>
    </div>
  );
}
