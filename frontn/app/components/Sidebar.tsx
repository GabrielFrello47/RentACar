"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useSelector } from "react-redux";
import { RootState } from "../redux/store";

const links = [
  { label: "Home", href: "/home" },
  { label: "Usuários", href: "/usuarios", somenteAdmin: true },
  { label: "Clientes", href: "/clientes" },
  { label: "Categorias", href: "/categorias", somenteAdmin: true },
  { label: "Veículos", href: "/veiculos" },
  { label: "Locações", href: "/locacoes" },
] as { label: string; href: string; somenteAdmin?: boolean }[];

export default function Sidebar() {
  const pathname = usePathname();
  const usuario = useSelector((state: RootState) => state.auth.usuario);
  const ehAdmin = usuario?.role === "ROLE_ADMIN";
  // atendentes comuns nao tem acesso a tela de usuarios (a API devolve 403)
  const linksVisiveis = links.filter((link) => !link.somenteAdmin || ehAdmin);

  return (
    <aside className="w-64 shrink-0 bg-[#1A1917] text-white flex flex-col min-h-screen">
      <div className="flex items-center gap-3 px-6 h-20 border-b border-[#3A3733]">
        <svg width="26" height="26" viewBox="0 0 40 40" fill="none">
          <path
            d="M4 28 L18 8 L24 8 L14 24 L26 24 L36 8"
            stroke="#FF7A2E"
            strokeWidth="4"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
        <span className="font-extrabold tracking-tight uppercase text-[15px]">RentACar</span>
      </div>

      <nav className="flex-1 py-6 px-3 space-y-1">
        {linksVisiveis.map((link) => {
          const active = pathname === link.href;
          return (
            <Link
              key={link.href}
              href={link.href}
              className={`block px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                active
                  ? "bg-[#E8590C] text-white"
                  : "text-[#C9C4BC] hover:bg-[#2A2725] hover:text-white"
              }`}
            >
              {link.label}
            </Link>
          );
        })}
      </nav>

      <div className="px-6 py-5 border-t border-[#3A3733] text-xs text-[#9B958B]">
        © 2026 RentACar
      </div>
    </aside>
  );
}