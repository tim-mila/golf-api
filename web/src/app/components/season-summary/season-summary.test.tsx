import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import SeasonSummary from "./season-summary";

test("season summary test", () => {
  render(<SeasonSummary />);
  expect(
    screen.getByText("Season summary component content goes here"),
  ).toBeInTheDocument();
});
