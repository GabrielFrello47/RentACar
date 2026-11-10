"use client";

import { useRouter } from "next/navigation";
import { Categoria, CategoriaFormProps } from "@/app/types/categoria";
import { atualizarCategoria, salvarCategoria } from "@/app/services/categoriaService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useState } from "react";

const estiloInput = "w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all";

export default function CategoriaForm({ categoriaExistente }: CategoriaFormProps) {

    const router = useRouter();
    const [categoria, setCategoria] = useState<Categoria>(categoriaExistente ?? new Categoria(null, "", ""));

    const handlerChange = (campo: 'nome' | 'descricao', valor: string) => {
        setCategoria(valorAnterior => ({ ...valorAnterior, [campo]: valor }));
    }

    const handlerSalvar = async () => {
        try {
            if (categoriaExistente) {
                await atualizarCategoria(categoria);
                alert("Categoria foi atualizada com sucesso");
            } else {
                await salvarCategoria(categoria);
                alert("Categoria foi criada com sucesso");
            }
            router.push("/categorias");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao salvar categoria"));
        }
    }

    return (
        <form action={handlerSalvar} className="bg-white border border-slate-200 rounded-2xl shadow-md p-8 space-y-5">
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Nome</label>
                <input
                    name="nome"
                    type="text"
                    required
                    placeholder="Ex.: Econômico, SUV, Utilitário"
                    value={categoria.nome}
                    onChange={(e) => handlerChange('nome', e.target.value)}
                    className={estiloInput}
                />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Descrição</label>
                <input
                    name="descricao"
                    type="text"
                    placeholder="Descrição (opcional)"
                    value={categoria.descricao ?? ""}
                    onChange={(e) => handlerChange('descricao', e.target.value)}
                    className={estiloInput}
                />
            </div>
            <div className="flex items-center justify-end gap-3 mt-8">
                <Link href="/categorias" className="px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                    Cancelar
                </Link>
                <button type="submit" className="px-5 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
                    Salvar
                </button>
            </div>
        </form>
    );
}
