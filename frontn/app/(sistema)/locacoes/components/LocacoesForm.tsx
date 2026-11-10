"use client";

import { useRouter } from "next/navigation";
import { Locacao, LocacaoFormProps } from "@/app/types/locacao";
import { Cliente } from "@/app/types/cliente";
import { Veiculo } from "@/app/types/veiculo";
import { atualizarLocacao, salvarLocacao } from "@/app/services/locacaoService";
import { buscarListaClientes } from "@/app/services/clienteService";
import { buscarListaVeiculos } from "@/app/services/veiculoService";
import { mensagemDeErro } from "@/app/services/api";
import { hojeISO } from "@/app/utils/data";
import Link from "next/link";
import { useEffect, useState } from "react";

const classeInput = "w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all";

export default function LocacoesForm({ locacaoExistente }: LocacaoFormProps) {

    const router = useRouter();
    const [locacao, setLocacao] = useState<Locacao>(
        locacaoExistente ?? new Locacao(null, hojeISO(), hojeISO(), 0, null, null)
    );
    const [clientes, setClientes] = useState<Cliente[]>([]);
    const [veiculos, setVeiculos] = useState<Veiculo[]>([]);

    useEffect(() => {
        async function carregarListas() {
            try {
                const [resClientes, resVeiculos] = await Promise.all([
                    buscarListaClientes(),
                    buscarListaVeiculos(),
                ]);
                setClientes(resClientes);
                setVeiculos(resVeiculos);
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar clientes/veiculos!"));
            }
        }
        carregarListas();
    }, []);

    // REGRA CENTRAL: so aparecem veiculos Disponiveis (Em Manutencao e Alugados nao podem ser selecionados).
    // Na edicao, o veiculo atual da locacao continua na lista.
    const veiculosSelecionaveis = veiculos.filter(
        (v) => v.status === "DISPONIVEL" || String(v.id) === locacaoExistente?.veiculo
    );

    // locacao ja finalizada nao troca de veiculo
    const veiculoTravado = !!locacaoExistente && locacaoExistente.status !== "EM_ANDAMENTO";

    const handlerSalvar = async () => {

        try {
            if (locacaoExistente) {
                await atualizarLocacao(locacao);
                alert("Locacao foi atualizada com sucesso");
            } else {
                await salvarLocacao(locacao);
                alert("Locacao foi criada com sucesso");
            }
            router.push("/locacoes");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao salvar locacao"));
        }

    }

    return (

        <form action={handlerSalvar} className="bg-white border border-slate-200 rounded-2xl shadow-md p-8 space-y-5">
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Data de início</label>
                <input name="dataInicio" type="date" required value={locacao.dataInicio}
                    onChange={(e) => setLocacao(l => ({ ...l, dataInicio: e.target.value }))} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Data de fim</label>
                {/* (antes este campo mostrava a data de INICIO) */}
                <input name="dataFim" type="date" required min={locacao.dataInicio} value={locacao.dataFim}
                    onChange={(e) => setLocacao(l => ({ ...l, dataFim: e.target.value }))} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Valor total (R$)</label>
                <input name="valorTotal" type="number" step="0.01" min="0" placeholder="600.00" value={locacao.valorTotal}
                    onChange={(e) => setLocacao(l => ({ ...l, valorTotal: Number(e.target.value) }))} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Cliente</label>
                <select name="cliente" required value={locacao.cliente ?? ""}
                    onChange={(e) => setLocacao(l => ({ ...l, cliente: e.target.value || null }))} className={classeInput}>
                    <option value="">Selecione...</option>
                    {clientes.map((cliente) => (
                        <option key={cliente.id} value={cliente.id ?? ""}>{cliente.nome}</option>
                    ))}
                </select>
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Veículo</label>
                <select name="veiculo" required disabled={veiculoTravado} value={locacao.veiculo ?? ""}
                    onChange={(e) => setLocacao(l => ({ ...l, veiculo: e.target.value || null }))} className={classeInput}>
                    <option value="">Selecione...</option>
                    {veiculosSelecionaveis.map((veiculo) => (
                        <option key={veiculo.id} value={veiculo.id ?? ""}>
                            {veiculo.marca} {veiculo.modelo} - {veiculo.placa}
                        </option>
                    ))}
                </select>
                {veiculos.length > 0 && veiculosSelecionaveis.length === 0 && (
                    <p className="text-xs text-red-600">Nenhum veículo disponível: todos estão Alugados ou Em Manutenção.</p>
                )}
                <p className="text-xs text-slate-500">Veículos Em Manutenção ou Alugados não aparecem aqui.</p>
            </div>
            <div className="flex items-center justify-end gap-3 mt-8">
                <Link href="/locacoes" className="px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                    Cancelar
                </Link>
                <button type="submit" className="px-5 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
                    Salvar
                </button>
            </div>
        </form>

    );

}
