"use client"

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Categoria } from "@/app/types/categoria";
import { consultaCategoriaPorId } from "@/app/services/categoriaService";
import CategoriaForm from "../../components/CategoriaForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function EditarCategoria() {

    const parametro = useParams();
    const codigo = Number(parametro.codigo);

    const router = useRouter();
    const [categoria, setCategoria] = useState<Categoria | null>(null);

    useEffect(() => {
        const buscarDados = async () => {
            try {
                setCategoria(await consultaCategoriaPorId(codigo));
            } catch {
                alert("Erro ao carregar Categoria!");
                router.push("/categorias");
            }
        }
        buscarDados();
    }, [codigo, router]);

    if (!categoria) return (<div className="p-8">Carregando Dados ...</div>)

    return (
        <CabecalhoCadastro voltarHref="/categorias" titulo="Editar Categoria" descricao="Altere os dados abaixo para atualizar o registro">
            <CategoriaForm categoriaExistente={categoria} />
        </CabecalhoCadastro>
    )
}
