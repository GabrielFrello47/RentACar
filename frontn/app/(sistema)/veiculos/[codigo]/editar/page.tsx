"use client"

import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { Veiculo } from "@/app/types/veiculo";
import { consultaVeiculoPorId } from "@/app/services/veiculoService";
import VeiculosForm from "../../components/VeiculosForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function EditarVeiculo() {

    const parametro = useParams();
    // a pasta da rota e [codigo] (antes o codigo lia "id" e virava NaN)
    const codigo = Number(parametro.codigo);

    const router = useRouter();
    const [veiculo, setVeiculo] = useState<Veiculo | null>(null);

    useEffect(() => {
        const buscarDados = async () => {
            try {
                setVeiculo(await consultaVeiculoPorId(codigo));
            } catch {
                alert("Erro ao carregar Veículo!");
                router.push("/veiculos");
            }
        }
        buscarDados();
    }, [codigo, router]);

    if (!veiculo) return (<div className="p-8">Carregando Dados ...</div>)

    return (
        <CabecalhoCadastro voltarHref="/veiculos" titulo="Editar Veículo" descricao="Altere os dados abaixo para atualizar o registro">
            <VeiculosForm veiculoExistente={veiculo} />
        </CabecalhoCadastro>
    )
}
