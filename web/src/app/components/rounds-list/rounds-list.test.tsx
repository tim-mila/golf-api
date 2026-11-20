import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import RoundsList from "./rounds-list";

test("rounds list test", () => {
  render(<RoundsList />);
  expect(
    screen.getByText("Rounds list component content goes here"),
  ).toBeInTheDocument();
});
