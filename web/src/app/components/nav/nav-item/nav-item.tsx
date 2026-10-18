import Link from "next/link";

export default function NavItem({
  href,
  text,
  active,
}: {
  href: string;
  text: string;
  active?: boolean;
}) {
  return (
    <Link
      className={`border-b-2 px-0.5 py-2 text-[15px] transition-colors ${
        active
          ? "border-accent text-foreground"
          : "border-transparent text-muted hover:text-foreground"
      }`}
      href={href}
    >
      {text}
    </Link>
  );
}
