import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, test } from "vitest";
import UserMenu from "./user-menu";
import { auth0 } from "@/lib/auth0";

type SessionData = Awaited<ReturnType<typeof auth0.getSession>>;

const mockAuthenticatedSession: SessionData = {
  user: {
    sub: "auth0|1234567890",
    name: "Jane Doe",
    email: "jane.doe@example.com",
  },
  idToken: "mock-id-token",
  accessToken: "mock-access-token",
  accessTokenScope: "read:profile",
  accessTokenExpiresAt: 1900000000,
  tokenType: "Bearer",
  tokenSet: {} as NonNullable<SessionData>["tokenSet"],
  internal: {
    sid: "",
    createdAt: 0,
    mcd: undefined,
    sessionExpiresAt: undefined,
  },
};

describe("renders user menu", async () => {
  test("displays user menu", async () => {
    render(<UserMenu session={mockAuthenticatedSession} />);

    expect(screen.getByAltText("Jane Doe")).toBeInTheDocument();
  });

  test("displays sign out button", async () => {
    render(<UserMenu session={mockAuthenticatedSession} />);

    const userMenuButton = screen.getByAltText("Jane Doe");
    await userEvent.click(userMenuButton);

    const themeToggleButton = screen.getByRole("button", {
      name: /theme-toggle/i,
    });
    expect(themeToggleButton).toBeInTheDocument();

    const signOutButton = screen.getByRole("link", { name: /sign out/i });
    expect(signOutButton).toBeInTheDocument();
    expect(signOutButton).toHaveAttribute("href", "/auth/logout");
  });
});
