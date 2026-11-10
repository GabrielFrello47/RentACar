import { Categoria } from "../types/categoria";
import api from "./api";

export async function buscarListaCategorias(): Promise<Categoria[]> {
    return (await api.get<Categoria[]>("/categorias")).data;
}

export async function consultaCategoriaPorId(id: number): Promise<Categoria> {
    return (await api.get<Categoria>("/categorias/" + id)).data;
}

// criar, alterar e excluir exigem perfil ADMIN na API
export async function salvarCategoria(categoria: Categoria): Promise<Categoria> {
    return (await api.post<Categoria>("/categorias", categoria)).data;
}

export async function atualizarCategoria(categoria: Categoria): Promise<void> {
    await api.put("/categorias/" + categoria.id, categoria);
}

export async function deletarCategoria(id: number): Promise<void> {
    await api.delete("/categorias/" + id);
}
