import type { Metadata } from "next";
import { Hanken_Grotesk } from "next/font/google";
import { Auth0Provider } from "@auth0/nextjs-auth0/client";
import "./globals.css";
import { ThemeProvider } from "./components/theme/theme-provider";

const hankenGrotesk = Hanken_Grotesk({
  subsets: ["latin"],
  variable: "--font-hanken-grotesk",
  display: "swap",
});

export const metadata: Metadata = {
  title: "Fairway.",
  description: "Track your golf rounds and handicap index",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${hankenGrotesk.variable} h-full antialiased`}
      suppressHydrationWarning
    >
      <body className="min-h-full flex flex-col">
        <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
          <Auth0Provider>{children}</Auth0Provider>
        </ThemeProvider>
      </body>
    </html>
  );
}
