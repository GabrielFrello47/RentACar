import { DevolucaoResponse, Locacao } from "../types/locacao";
import api from "./api";

export async function buscarListaLocacoes(): Promise<Locacao[]> {
    return (await api.get<Locacao[]>("/locacoes")).data;
}

export async function consultaLocacaoPorId(id: number): Promise<Locacao> {
    return (await api.get<Locacao>("/locacoes/" + id)).data;
}

function corpo(l: Locacao) {
    return { dataInicio: l.dataInicio, dataFim: l.dataFim, valorTotal: l.valorTotal, cliente: l.cliente, veiculo: l.veiculo };
}

export async function salvarLocacao(locacao: Locacao): Promise<Locacao> {
    return (await api.post<Locacao>("/locacoes", corpo(locacao))).data;
}

export async function atualizarLocacao(locacao: Locacao): Promise<void> {
    await api.put("/locacoes/" + locacao.id, corpo(locacao));
}

// Devolucao: libera o veiculo e devolve o alerta de revisao quando a km passa do limite
export async function devolverLocacao(id: number, quilometragemFinal: number): Promise<DevolucaoResponse> {
    return (await api.patch<DevolucaoResponse>("/locacoes/" + id + "/devolucao", { quilometragemFinal })).data;
}

export async function deletarLocacao(id: number): Promise<void> {
    await api.delete("/locacoes/" + id);
}
