"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { RootState } from "@/app/redux/store";
import { buscarListaUsuarios } from "@/app/services/usuarioService";
import { buscarListaClientes } from "@/app/services/clienteService";
import { buscarListaVeiculos } from "@/app/services/veiculoService";
import { buscarListaLocacoes } from "@/app/services/locacaoService";

interface CardInfo {
  titulo: string;
  descricao: string;
  href: string;
  somenteAdmin?: boolean;
  buscar: () => Promise<unknown[]>;
}

const cards: CardInfo[] = [
  { titulo: "Usuários", descricao: "Atendentes com acesso ao painel", href: "/usuarios", somenteAdmin: true, buscar: buscarListaUsuarios },
  { titulo: "Clientes", descricao: "Cadastro de clientes da locadora", href: "/clientes", buscar: buscarListaClientes },
  { titulo: "Veículos", descricao: "Frota disponível para locação", href: "/veiculos", buscar: () => buscarListaVeiculos() },
  { titulo: "Locações", descricao: "Contratos de aluguel em andamento", href: "/locacoes", buscar: buscarListaLocacoes },
];

export default function Home() {
  const usuario = useSelector((state: RootState) => state.auth.usuario);
  const ehAdmin = usuario?.role === "ROLE_ADMIN";
  const [contagens, setContagens] = useState<Record<string, number | null>>({});

  const cardsVisiveis = cards.filter((card) => !card.somenteAdmin || ehAdmin);

  useEffect(() => {
    cards
      .filter((card) => !card.somenteAdmin || ehAdmin)
      .forEach(async (card) => {
        try {
          const lista = await card.buscar();
          setContagens((prev) => ({ ...prev, [card.titulo]: lista.length }));
        } catch {
          setContagens((prev) => ({ ...prev, [card.titulo]: null }));
        }
      });
  }, [ehAdmin]);

  return (
    <div className="min-h-screen w-full bg-slate-50 p-6 md:p-8 font-sans">
      <div className="max-w-5xl mx-auto space-y-8">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
            Painel RentACar
          </h1>
          <p className="text-sm text-slate-500 mt-1">
            Visão geral do sistema. Escolha uma área para gerenciar.
          </p>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          {cardsVisiveis.map((card) => (
            <Link
              key={card.titulo}
              href={card.href}
              className="group bg-white border border-slate-200 rounded-xl shadow-sm p-6 flex flex-col justify-between hover:border-blue-300 hover:shadow-md transition-all duration-150"
            >
              <div>
                <h2 className="text-lg font-semibold text-slate-900 group-hover:text-blue-700 transition-colors">
                  {card.titulo}
                </h2>
                <p className="text-sm text-slate-500 mt-1">{card.descricao}</p>
              </div>

              <div className="mt-6 flex items-center justify-between">
                <span className="text-3xl font-bold text-slate-800">
                  {contagens[card.titulo] === undefined
                    ? "..."
                    : contagens[card.titulo] === null
                    ? "—"
                    : contagens[card.titulo]}
                </span>
                <span className="text-sm font-medium text-blue-600 group-hover:translate-x-1 transition-transform">
                  Ver tudo →
                </span>
              </div>
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
