export default function LinkButton({
  children,
  href,
  onClick,
  className = "p-2 rounded-md border",
}: {
  children: React.ReactNode;
  href: string;
  onClick?: () => void;
  className?: string;
}) {
  return (
    <a href={href} className={className} onClick={onClick}>
      {children}
    </a>
  );
}
