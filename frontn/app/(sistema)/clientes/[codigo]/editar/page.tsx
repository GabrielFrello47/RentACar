"use client"

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Cliente } from "@/app/types/cliente";
import { consultaClientePorId } from "@/app/services/clienteService";
import ClienteForm from "../../components/ClientesForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function EditarCliente() {

    const parametro = useParams();
    // a pasta da rota e [codigo] (antes o codigo lia "id" e virava NaN)
    const codigo = Number(parametro.codigo);

    const router = useRouter();
    const [cliente, setCliente] = useState<Cliente | null>(null);

    useEffect(() => {
        const buscarDados = async () => {
            try {
                setCliente(await consultaClientePorId(codigo));
            } catch {
                alert("Erro ao carregar Cliente!");
                router.push("/clientes");
            }
        }
        buscarDados();
    }, [codigo, router]);

    if (!cliente) return (<div className="p-8">Carregando Dados ...</div>)

    return (
        <CabecalhoCadastro voltarHref="/clientes" titulo="Editar Cliente" descricao="Altere os dados abaixo para atualizar o registro">
            <ClienteForm clienteExistente={cliente} />
        </CabecalhoCadastro>
    )
}
