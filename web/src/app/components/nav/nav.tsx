"use client";

import { auth0 } from "@/lib/auth0";
import { useState } from "react";
import UserName from "../user/user-name";
import NavItem from "./nav-item/nav-item";
import { usePathname } from "next/navigation";

/**
 * Navigation
 * A responsive top nav bar with:
 *  - Desktop horizontal menu, including one dropdown submenu
 *  - Mobile hamburger menu that slides down into a stacked list
 *  - Active-link highlighting
 *  - Zero dependencies beyond React (icons are inline SVG)
 *
 * Usage:
 *   <Navigation
 *     brand="Acme"
 *   />
 */

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
  const [mobileOpen, setMobileOpen] = useState(false);

  const pathname = usePathname();

  return (
    <header className="w-full sticky top-0 z-50">
      <nav className="mx-auto flex max-w-8xl items-center justify-between px-4 py-3 sm:px-6">
        <a href="#" className="text-lg font-semibold">
          {brand}
        </a>

        <div className="hidden md:flex mx-auto max-w-[1120px] flex-wrap items-center justify-between gap-20 px-8">
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
          <UserName session={session} />
          {session && (
            <a
              href="/auth/logout"
              className="hidden rounded-md px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100 hover:text-slate-900 md:inline-block"
            >
              Sign Out
            </a>
          )}
        </div>

        {/* Mobile toggle */}
        <button
          className="inline-flex items-center justify-center rounded-md p-2 text-slate-600 hover:bg-slate-100 md:hidden"
          onClick={() => setMobileOpen((o) => !o)}
          aria-label="Toggle menu"
          aria-expanded={mobileOpen}
        >
          {mobileOpen ? <CloseIcon /> : <MenuIcon />}
        </button>
      </nav>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="border-t border-slate-200 md:hidden">
          <div className="flex flex-col gap-1 px-4 py-3 sm:px-6">
            {session && (
              <a
                href="/auth/logout"
                className="rounded-md px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
                onClick={() => setMobileOpen(false)}
              >
                Sign Out
              </a>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
