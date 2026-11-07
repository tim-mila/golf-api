import { render } from "@testing-library/react";
import { expect, test } from "vitest";
import Button from "./button";

test("button test", () => {
  render(<Button children={"Hello World"} onClick={() => {}} />);

  expect(document.querySelector("button")?.textContent).toBe("Hello World");
});
