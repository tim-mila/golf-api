export default function Button({
  children,
  props,
}: {
  children?: React.ReactNode;
  props?: React.ButtonHTMLAttributes<HTMLButtonElement>;
}) {
  return (
    <button className="self-start p-2 rounded-md border" {...props}>
      {children}
    </button>
  );
}
