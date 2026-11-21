import { expect, test } from "vitest";
import HandicapIndex from "./handicap-index";
import { render, screen } from "@testing-library/react";

test("handicap index placeholder test", () => {
  render(<HandicapIndex />);

  expect(
    screen.getByText("Handicap index component content goes here"),
  ).toBeInTheDocument();
});
