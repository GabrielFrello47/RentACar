"use client";

import { Locacao } from "@/app/types/locacao";
import { Cliente } from "@/app/types/cliente";
import { Veiculo } from "@/app/types/veiculo";
import { buscarListaLocacoes, deletarLocacao, devolverLocacao } from "@/app/services/locacaoService";
import { buscarListaClientes } from "@/app/services/clienteService";
import { buscarListaVeiculos } from "@/app/services/veiculoService";
import { mensagemDeErro } from "@/app/services/api";
import { formatarData } from "@/app/utils/data";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function Locacoes(){

    const [locacoes,setLocacoes] = useState<Locacao[]>([]);
    const [clientes,setClientes] = useState<Cliente[]>([]);
    const [veiculos,setVeiculos] = useState<Veiculo[]>([]);

    // contador usado para recarregar a lista depois de excluir/devolver
    const [versao,setVersao] = useState(0);
    const carregarDados = () => setVersao(v => v + 1);

    useEffect(()=>{
        const carregar = async ()=>{
            try {
                const [listaLocacoes, listaClientes, listaVeiculos] = await Promise.all([
                    buscarListaLocacoes(), buscarListaClientes(), buscarListaVeiculos(),
                ]);
                setLocacoes(listaLocacoes);
                setClientes(listaClientes);
                setVeiculos(listaVeiculos);
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar dados!"));
            }
        };
        carregar();
    },[versao]);

    // a locacao guarda so o id; aqui mostramos o nome
    const nomeCliente = (id: string | null) => clientes.find(c => String(c.id) === id)?.nome ?? id ?? "";
    const nomeVeiculo = (id: string | null) => {
        const v = veiculos.find(x => String(x.id) === id);
        return v ? v.marca + " " + v.modelo + " (" + v.placa + ")" : id ?? "";
    };

    const handleDeletar = async (id: number) => {

        const confirmar = confirm("Tem certeza que deseja excluir esta locacao?");
        if (!confirmar) return;

        try {
            await deletarLocacao(id);
            carregarDados();
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao excluir locacao!"));
        }

    }

    // Devolucao: libera o veiculo e, se passou do limite de km, a API devolve o ALERTA DE REVISAO
    const handleDevolver = async (locacao: Locacao) => {
        const entrada = prompt("Quilometragem final do veículo (km):");
        if (entrada === null) return;

        const km = Number(entrada);
        if (entrada.trim() === "" || !Number.isInteger(km) || km < 0) {
            alert("Informe uma quilometragem válida (número inteiro).");
            return;
        }

        try {
            const resposta = await devolverLocacao(locacao.id!, km);
            alert((resposta.alertaRevisao ? "⚠ " : "") + resposta.mensagem);
            carregarDados();
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao registrar devolução!"));
        }
    }

    return (
        <div className="min-h-screen w-full bg-slate-50 p-6 md:p-8 font-sans">
            <div className="w-full flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
                    Gestao de locacoes
                </h1>
                <Link href="/locacoes/novo" className="inline-flex items-center justify-center px-4 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2">
                    Nova Locação
                </Link>
            </div>

            <div className="w-full">
                <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden w-full">
                    <div className="overflow-x-auto w-full">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-slate-100/75 border-b border-slate-200">
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Código</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Data início</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Data fim</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Valor total</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Cliente</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Veículo</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Situação</th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">Ações</th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                                {locacoes.map((locacao)=>(
                                <tr key={locacao.id} className="hover:bg-blue-50/50 transition-colors duration-150">
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{locacao.id}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{formatarData(locacao.dataInicio)}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{formatarData(locacao.dataFim)}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">R$ {locacao.valorTotal}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{nomeCliente(locacao.cliente)}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">{nomeVeiculo(locacao.veiculo)}</td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {locacao.status === "EM_ANDAMENTO" ? "Em andamento"
                                            : locacao.status === "FINALIZADA" ? "Finalizada (" + formatarData(locacao.dataDevolucao) + ")"
                                            : "—"}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800 space-x-2">
                                        <Link href={`/locacoes/${locacao.id}/editar`} className="inline-flex items-center justify-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md transition-colors duration-150">
                                            Editar
                                        </Link>
                                        {locacao.status === "EM_ANDAMENTO" && (
                                            <button onClick={() => handleDevolver(locacao)} className="inline-flex items-center justify-center px-3 py-1.5 bg-blue-50 hover:bg-blue-100 text-blue-700 text-xs font-medium rounded-md transition-colors duration-150">
                                                Devolver
                                            </button>
                                        )}
                                        <button onClick={() => handleDeletar(locacao.id!)} className="inline-flex items-center justify-center px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-medium rounded-md transition-colors duration-150">
                                            Excluir
                                        </button>
                                    </td>
                                </tr>
                                ))}

                                { locacoes.length === 0 &&
                                (
                                    <tr>
                                        <td colSpan={8} className="px-6 py-12 text-center text-slate-800 italic" >
                                            Nenhuma locacao encontrada!
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
