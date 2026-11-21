"use client";

import type { auth0 } from "@/lib/auth0";
import { useState } from "react";
import UserName from "../user/user-name";
import NavItem from "./nav-item/nav-item";
import { usePathname } from "next/navigation";
import LinkButton from "../link-button/link-button";
import { ThemeToggle } from "../theme/theme-toggle";

function MenuIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <line x1="3" y1="6" x2="21" y2="6" />
      <line x1="3" y1="12" x2="21" y2="12" />
      <line x1="3" y1="18" x2="21" y2="18" />
    </svg>
  );
}

function CloseIcon() {
  return (
    <svg
      width="22"
      height="22"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <line x1="18" y1="6" x2="6" y2="18" />
      <line x1="6" y1="6" x2="18" y2="18" />
    </svg>
  );
}

export default function Navigation({
  brand = "Fairway.",
  session,
}: {
  readonly brand?: string;
  readonly session: Awaited<ReturnType<typeof auth0.getSession>>;
}) {
  const [isOpen, setOpen] = useState(false);

  const pathname = usePathname();

  return (
    <header className="w-full sticky top-0 z-50 ">
      <nav className="mx-auto flex max-w-8xl items-center justify-between px-4 py-3 sm:px-6">
        <a href="#" className="text-lg font-semibold">
          {brand}
        </a>

        {/* Desktop Links */}
        <div className="hidden md:flex md:items-center md:gap-8">
          <NavItem
            href="/dashboard"
            text="Dashboard"
            active={pathname === "/dashboard"}
          />
          <NavItem
            href="/dashboard/rounds"
            text="All Rounds"
            active={pathname === "/dashboard/rounds"}
          />
          <NavItem
            href="/dashboard/handicap"
            text="Handicap History"
            active={pathname === "/dashboard/handicap"}
          />
        </div>

        <div className="flex items-center gap-4 shrink-0">
          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            aria-label="Toggle menu"
            aria-expanded={isOpen}
            className="inline-flex cursor-pointer touch-manipulation items-center justify-center rounded-md p-2"
          >
            {isOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
      </nav>

      {/* Desktop submenu */}
      {isOpen && (
        <div className="border-t hidden md:block">
          <div className="flex flex-col gap-1 px-4 py-3 sm:px-6">
            {session && (
              <>
                <UserName session={session} />
                <ThemeToggle />
                {session && (
                  <LinkButton
                    href="/auth/logout"
                    className="hidden rounded-md px-3 py-2 text-sm font-medium md:inline-block"
                  >
                    Sign Out
                  </LinkButton>
                )}
              </>
            )}
          </div>
        </div>
      )}

      {/* Mobile menu */}
      {isOpen && (
        <div className="border-t md:hidden">
          <div className="flex flex-col gap-1 px-4 py-3 sm:px-6">
            {session && (
              <LinkButton
                href="/auth/logout"
                className="rounded-md px-3 py-2 text-sm font-medium"
                onClick={() => setOpen(false)}
              >
                Sign Out
              </LinkButton>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
