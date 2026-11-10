"use client"

import { Usuario } from "@/app/types/usuario";
import { consultaUsuarioPorId } from "@/app/services/usuarioService";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import UsuarioForm from "../../components/UsuarioForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function EditarUsuario() {

    const parametro = useParams();

    const codigo = Number(parametro.codigo);

    const [usuario, setUsuario] = useState<Usuario | null>(null);

    const router = useRouter();

    useEffect(() => {
        const buscarDados = async () => {
        try {
            setUsuario(await consultaUsuarioPorId(codigo));
        } catch {
            alert("Erro ao carregar usuário!");
            router.push("/usuarios");
        }
        }
        buscarDados();
    }, [codigo, router]);

    if (!usuario) return (<div className="p-8">Carregando Dados ...</div>)

    return (
        <CabecalhoCadastro voltarHref="/usuarios" titulo="Editar Usuario" descricao="Altere os dados abaixo para atualizar o Usuario">
            <UsuarioForm usuarioExistente={usuario} />
        </CabecalhoCadastro>
    )
}