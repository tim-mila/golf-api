import { Popover, PopoverButton, PopoverPanel } from "@headlessui/react";
import UserAvatar from "../../user-avatar/user-avatar";
import { auth0 } from "@/lib/auth0";
import SignOutButton from "../../sign-out-button/sign-out-button";
import { ThemeToggle } from "../../theme/theme-toggle";

export default function UserMenu({
  session,
}: {
  readonly session: Awaited<ReturnType<typeof auth0.getSession>>;
}) {
  return (
    <Popover className="relative">
      <PopoverButton>
        <UserAvatar session={session} />
      </PopoverButton>
      <PopoverPanel anchor="bottom" className="flex flex-col">
        <ThemeToggle />
        <SignOutButton />
      </PopoverPanel>
    </Popover>
  );
}
