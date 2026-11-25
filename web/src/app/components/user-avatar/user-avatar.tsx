import { auth0 } from "@/lib/auth0";

export default function UserAvatar({
  session,
}: {
  session?: Awaited<ReturnType<typeof auth0.getSession>>;
}) {
  return (
    <div className="flex items-center gap-2">
      <img
        src={session?.user?.picture ?? ""}
        alt={session?.user?.name ?? ""}
        className="h-8 w-8 rounded-full"
      />
    </div>
  );
}
