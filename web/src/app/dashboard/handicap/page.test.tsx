import { render, screen } from "@testing-library/react";
import Handicap from "./page";
import { expect, test } from "vitest";

test("displays handicap placeholder", async () => {
  render(<Handicap />);

  expect(
    screen.getByText(
      "Handicap page content goes here. You can add your handicap tracking features and display them in this section.",
    ),
  ).toBeInTheDocument();
});
