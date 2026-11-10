"use client";

import { Veiculo } from "@/app/types/veiculo";
import { buscarListaVeiculos, deletarVeiculo, registrarRevisao } from "@/app/services/veiculoService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useEffect, useState } from "react";

const rotuloStatus: Record<string, string> = {
    DISPONIVEL: "Disponível",
    ALUGADO: "Alugado",
    EM_MANUTENCAO: "Em Manutenção",
};

const corStatus: Record<string, string> = {
    DISPONIVEL: "bg-green-50 text-green-700",
    ALUGADO: "bg-amber-50 text-amber-700",
    EM_MANUTENCAO: "bg-red-50 text-red-700",
};

export default function Veiculos(){

    const [veiculos,setVeiculos] = useState<Veiculo[]>([]);

    // contador usado para recarregar a lista depois de excluir/alterar
    const [versao,setVersao] = useState(0);
    const carregarDados = () => setVersao(v => v + 1);

    useEffect(()=>{
        const carregar = async ()=>{
            try {
                setVeiculos(await buscarListaVeiculos());
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar dados!"));
            }
        };
        carregar();
    },[versao]);

    const handleDeletar = async (id: number) => {

        const confirmar = confirm("Tem certeza que deseja excluir este veiculo?");
        if (!confirmar) return;

        try {
            await deletarVeiculo(id);
            carregarDados();
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao excluir veiculo!"));
        }

    }

    const handleRevisao = async (veiculo: Veiculo) => {
        if (!confirm("Confirma que a revisão de " + veiculo.marca + " " + veiculo.modelo + " foi feita? A km atual passa a ser a base do próximo alerta.")) return;

        try {
            await registrarRevisao(veiculo.id!);
            carregarDados();
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao registrar revisão!"));
        }
    }

    return (
        <div className="min-h-screen w-full bg-slate-50 p-6 md:p-8 font-sans">
            <div className="w-full flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
                    Gestao de veiculos
                </h1>
                <Link href="/veiculos/novo" className="inline-flex items-center justify-center px-4 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2">
                    Novo Veículo
                </Link>
            </div>

            <div className="w-full">
                <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden w-full">
                    <div className="overflow-x-auto w-full">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-slate-100/75 border-b border-slate-200">
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Código</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Marca</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Modelo</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Placa</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Categoria</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Situação</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Km</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Diária</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Ações</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                                {veiculos.map((veiculo)=>(
                                <tr key={veiculo.id} className="hover:bg-blue-50/50 transition-colors duration-150">
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{veiculo.id}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{veiculo.marca}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{veiculo.modelo}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{veiculo.placa}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{veiculo.categoriaNome ?? "—"}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        <span className={"inline-block px-2 py-1 rounded-md text-xs " + (corStatus[veiculo.status] ?? "")}>
                                            {rotuloStatus[veiculo.status] ?? veiculo.status}
                                        </span>
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {veiculo.quilometragem} km
                                        {veiculo.revisaoPendente && (
                                            <span className="ml-2 inline-block px-2 py-1 rounded-md text-xs bg-red-100 text-red-700"
                                                title={"Passou de " + veiculo.limiteRevisaoKm + " km desde a última revisão (" + veiculo.kmUltimaRevisao + " km)"}>
                                                Revisão pendente
                                            </span>
                                        )}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">R$ {veiculo.valorDiaria}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800 space-x-2">
                                        <Link href={`/veiculos/${veiculo.id}/editar`} className="inline-flex items-center justify-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md transition-colors duration-150">
                                            Editar
                                        </Link>
                                        {veiculo.revisaoPendente && (
                                            <button onClick={() => handleRevisao(veiculo)} className="inline-flex items-center justify-center px-3 py-1.5 bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-medium rounded-md transition-colors duration-150">
                                                Revisão feita
                                            </button>
                                        )}
                                        <button onClick={() => handleDeletar(veiculo.id!)} className="inline-flex items-center justify-center px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-medium rounded-md transition-colors duration-150">
                                            Excluir
                                        </button>
                                    </td>
                                </tr>
                                ))}

                                { veiculos.length === 0 &&
                                (
                                    <tr>
                                        <td colSpan={9} className="px-6 py-12 text-center text-slate-800 italic" >
                                            Nenhum veiculo encontrado!
                                        </td>
                                    </tr>
                                )
                                }
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    )

}
