import { Cliente } from "../types/cliente";
import api from "./api";

export async function buscarListaClientes(): Promise<Cliente[]> {
    return (await api.get<Cliente[]>("/clientes")).data;
}

export async function consultaClientePorId(id: number): Promise<Cliente> {
    return (await api.get<Cliente>("/clientes/" + id)).data;
}

export async function salvarCliente(cliente: Cliente): Promise<Cliente> {
    return (await api.post<Cliente>("/clientes", cliente)).data;
}

export async function atualizarCliente(cliente: Cliente): Promise<void> {
    await api.put("/clientes/" + cliente.id, cliente);
}

export async function deletarCliente(id: number): Promise<void> {
    await api.delete("/clientes/" + id);
}
