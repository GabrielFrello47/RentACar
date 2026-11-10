import api from "./api";

export interface VeiculoPublico {
    id: number;
    marca: string;
    modelo: string;
    ano: string;
    valorDiaria: number;
    categoriaNome: string | null;
    categoriaDescricao: string | null;
}

export interface MinhaLocacao {
    id: number;
    dataInicio: string;
    dataFim: string;
    valorTotal: number;
    veiculo: string | null;
    status: string | null;
}

export interface CadastroCliente {
    nome: string;
    cpf: string;
    telefone: string;
    email: string;
    senha: string;
}

export async function cadastrarCliente(dados: CadastroCliente): Promise<void> {
    await api.post("/auth/cadastro", dados);
}

export async function buscarVeiculosPublicos(): Promise<VeiculoPublico[]> {
    return (await api.get<VeiculoPublico[]>("/publico/veiculos")).data;
}

export async function buscarMinhasLocacoes(): Promise<MinhaLocacao[]> {
    return (await api.get<MinhaLocacao[]>("/minhas-locacoes")).data;
}

export async function alugarVeiculo(veiculoId: number, dataInicio: string, dataFim: string): Promise<MinhaLocacao> {
    return (await api.post<MinhaLocacao>("/minhas-locacoes", { veiculoId, dataInicio, dataFim })).data;
}
