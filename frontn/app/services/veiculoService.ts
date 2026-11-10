import { Veiculo } from "../types/veiculo";
import api from "./api";

// apenasDisponiveis=true devolve so os veiculos que podem ser selecionados para nova locacao
export async function buscarListaVeiculos(apenasDisponiveis = false): Promise<Veiculo[]> {
    return (await api.get<Veiculo[]>("/veiculos", { params: apenasDisponiveis ? { disponiveis: true } : {} })).data;
}

export async function consultaVeiculoPorId(id: number): Promise<Veiculo> {
    return (await api.get<Veiculo>("/veiculos/" + id)).data;
}

// so os campos que a API aceita (o resto, como revisaoPendente, e apenas leitura)
function corpo(v: Veiculo) {
    return {
        marca: v.marca, modelo: v.modelo, placa: v.placa, ano: v.ano, valorDiaria: v.valorDiaria,
        status: v.status, quilometragem: v.quilometragem, kmUltimaRevisao: v.kmUltimaRevisao,
        categoriaId: v.categoriaId,
    };
}

export async function salvarVeiculo(veiculo: Veiculo): Promise<Veiculo> {
    // na criacao a km da ultima revisao e assumida pela API (= km atual)
    const { kmUltimaRevisao, ...novo } = corpo(veiculo);
    void kmUltimaRevisao;
    return (await api.post<Veiculo>("/veiculos", novo)).data;
}

export async function atualizarVeiculo(veiculo: Veiculo): Promise<void> {
    await api.put("/veiculos/" + veiculo.id, corpo(veiculo));
}

export async function registrarRevisao(id: number): Promise<Veiculo> {
    return (await api.patch<Veiculo>("/veiculos/" + id + "/revisao")).data;
}

export async function deletarVeiculo(id: number): Promise<void> {
    await api.delete("/veiculos/" + id);
}
