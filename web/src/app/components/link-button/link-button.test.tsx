import { render } from "@testing-library/react";
import { expect, test } from "vitest";
import LinkButton from "./link-button";

test("link button test", () => {
  render(<LinkButton href="/target">Test Link</LinkButton>);

  expect(document.querySelector("a")?.textContent).toBe("Test Link");
});
