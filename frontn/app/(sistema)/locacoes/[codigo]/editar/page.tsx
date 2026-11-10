"use client"

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Locacao } from "@/app/types/locacao";
import { consultaLocacaoPorId } from "@/app/services/locacaoService";
import LocacoesForm from "../../components/LocacoesForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function EditarLocacao() {

    const parametro = useParams();
    // a pasta da rota e [codigo] (antes o codigo lia "id" e virava NaN)
    const codigo = Number(parametro.codigo);

    const router = useRouter();
    const [locacao, setLocacao] = useState<Locacao | null>(null);

    useEffect(() => {
        const buscarDados = async () => {
            try {
                setLocacao(await consultaLocacaoPorId(codigo));
            } catch {
                alert("Erro ao carregar Locação!");
                router.push("/locacoes");
            }
        }
        buscarDados();
    }, [codigo, router]);

    if (!locacao) return (<div className="p-8">Carregando Dados ...</div>)

    return (
        <CabecalhoCadastro voltarHref="/locacoes" titulo="Editar Locação" descricao="Altere os dados abaixo para atualizar o registro">
            <LocacoesForm locacaoExistente={locacao} />
        </CabecalhoCadastro>
    )
}
