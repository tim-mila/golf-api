import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import SignOutButton from "./sign-out-button";

test("renders sign out link", () => {
  render(<SignOutButton />);
  const signOutLink = screen.getByRole("link", { name: /sign out/i });
  expect(signOutLink).toBeInTheDocument();
  expect(signOutLink).toHaveAttribute("href", "/auth/logout");
});
