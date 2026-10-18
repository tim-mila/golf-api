import { render, screen } from "@testing-library/react";
import { expect, test } from "vitest";
import NavItem from "./nav-item";

test("should display the correct link text and href", () => {
  const href = "/test-link";
  const text = "Test Link";

  render(<NavItem href={href} text={text} />);

  const linkElement = screen.getByRole("link", { name: text });
  expect(linkElement).toBeInTheDocument();
  expect(linkElement).toHaveAttribute("href", href);
});
