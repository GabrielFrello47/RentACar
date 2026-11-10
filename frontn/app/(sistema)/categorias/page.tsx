"use client";

import { Categoria } from "@/app/types/categoria";
import { buscarListaCategorias, deletarCategoria } from "@/app/services/categoriaService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useEffect, useState } from "react";

const th = "px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider";
const td = "px-6 py-4 text-sm font-medium text-slate-800";

export default function Categorias() {

    const [categorias, setCategorias] = useState<Categoria[]>([]);
    const [versao, setVersao] = useState(0);

    useEffect(() => {
        const carregar = async () => {
            try {
                setCategorias(await buscarListaCategorias());
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar dados!"));
            }
        };
        carregar();
    }, [versao]);

    const handleDeletar = async (id: number) => {
        if (!confirm("Tem certeza que deseja excluir esta categoria?")) return;
        try {
            await deletarCategoria(id);
            setVersao(v => v + 1);
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao excluir categoria!"));
        }
    }

    return (
        <div className="min-h-screen w-full bg-slate-50 p-6 md:p-8 font-sans">
            <div className="w-full flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">Gestao de categorias</h1>
                <Link
                    href="/categorias/novo"
                    className="inline-flex items-center justify-center px-4 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2"
                >
                    Nova Categoria
                </Link>
            </div>
            <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden w-full">
                <div className="overflow-x-auto w-full">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-100/75 border-b border-slate-200">
                                <th className={th}>Código</th>
                                <th className={th}>Nome</th>
                                <th className={th}>Descrição</th>
                                <th className={th}>Ações</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-200">
                            {categorias.map((categoria) => (
                                <tr key={categoria.id} className="hover:bg-blue-50/50 transition-colors duration-150">
                                    <td className={td}>{categoria.id}</td>
                                    <td className={td}>{categoria.nome}</td>
                                    <td className={td}>{categoria.descricao}</td>
                                    <td className={td + " space-x-2"}>
                                        <Link
                                            href={`/categorias/${categoria.id}/editar`}
                                            className="inline-flex items-center justify-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md transition-colors duration-150"
                                        >
                                            Editar
                                        </Link>
                                        <button
                                            onClick={() => handleDeletar(categoria.id!)}
                                            className="inline-flex items-center justify-center px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-medium rounded-md transition-colors duration-150"
                                        >
                                            Excluir
                                        </button>
                                    </td>
                                </tr>
                            ))}
                            {categorias.length === 0 && (
                                <tr>
                                    <td colSpan={4} className="px-6 py-12 text-center text-slate-800 italic">
                                        Nenhuma categoria encontrada!
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    )
}
