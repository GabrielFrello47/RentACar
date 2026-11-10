import Link from "next/link";
import { ReactNode } from "react";

interface CabecalhoCadastroProps {
    voltarHref: string;
    titulo: string;
    descricao: string;
    children: ReactNode;
}

// Moldura das telas de cadastro/edicao: link de voltar, titulo, descricao e o formulario.
export default function CabecalhoCadastro({ voltarHref, titulo, descricao, children }: CabecalhoCadastroProps) {
    return (
        <div className="max-w-2xl mx-auto py-6">
            <Link
                href={voltarHref}
                className="inline-flex items-center gap-1 text-sm font-medium text-slate-500 hover:text-[#E8590C] transition-colors"
            >
                <span aria-hidden>←</span> Voltar para Listagem
            </Link>
            <div className="mt-5 mb-6 flex items-stretch gap-4">
                <div className="w-1.5 rounded-full bg-[#E8590C]" />
                <div>
                    <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">{titulo}</h1>
                    <p className="mt-1 text-sm text-slate-500">{descricao}</p>
                </div>
            </div>
            {children}
        </div>
    );
}
