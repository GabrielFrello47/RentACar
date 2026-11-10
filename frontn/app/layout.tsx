import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import StoreProvider from "./redux/StoreProvider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "RentACar",
  description: "RentACar - Gestão de Locadoras de Veículos",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html
      lang="pt-br"
      className="h-full"
    >
      <body className={`${geistSans.variable} ${geistMono.variable} min-h-full flex flex-col`}>
        <StoreProvider>
          {children}
        </StoreProvider>
      </body>
    </html>
  );
}
