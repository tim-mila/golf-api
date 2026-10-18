import { render, screen } from "@testing-library/react";
import { expect, test, vi } from "vitest";
import UserName from "./user-name";
import { auth0 } from "@/lib/auth0";

type SessionData = Awaited<ReturnType<typeof auth0.getSession>>;

test("displays user name", async () => {
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

  render(<UserName session={mockAuthenticatedSession} />);

  expect(screen.getByText("Jane Doe")).toBeInTheDocument();
});
