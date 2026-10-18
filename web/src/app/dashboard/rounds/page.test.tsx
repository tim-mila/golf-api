import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import Rounds from "./page";

test("displays rounds placeholder", async () => {
  render(<Rounds />);

  expect(
    screen.getByText(
      "Rounds page content goes here. You can add your rounds tracking features and display them in this section.",
    ),
  ).toBeInTheDocument();
});
