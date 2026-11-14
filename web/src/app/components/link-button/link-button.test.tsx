import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import LinkButton from "./link-button";

test("link button test", () => {
  render(<LinkButton href="/target">Test Link</LinkButton>);

  expect(screen.getByRole("link", { name: "Test Link" })).toBeInTheDocument();
});
