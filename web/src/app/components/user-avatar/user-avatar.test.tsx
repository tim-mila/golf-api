import { render, screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";
import UserAvatar from "./user-avatar";
import { auth0 } from "@/lib/auth0";

type SessionData = Awaited<ReturnType<typeof auth0.getSession>>;

test("displays user avatar", async () => {
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

  render(<UserAvatar session={mockAuthenticatedSession} />);

  expect(screen.getByAltText("Jane Doe")).toBeInTheDocument();
});
