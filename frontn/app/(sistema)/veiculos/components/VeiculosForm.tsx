"use client";

import { useRouter } from "next/navigation";
import { Veiculo, VeiculoFormProps } from "@/app/types/veiculo";
import { Categoria } from "@/app/types/categoria";
import { atualizarVeiculo, salvarVeiculo } from "@/app/services/veiculoService";
import { buscarListaCategorias } from "@/app/services/categoriaService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useEffect, useState } from "react";

const classeInput = "w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all";

export default function VeiculosForm({ veiculoExistente }: VeiculoFormProps) {

    const router = useRouter();
    const [veiculo, setVeiculo] = useState<Veiculo>(veiculoExistente ?? new Veiculo(null, "", "", "", "", 0));
    const [categorias, setCategorias] = useState<Categoria[]>([]);

    useEffect(() => {
        async function carregarCategorias() {
            try {
                setCategorias(await buscarListaCategorias());
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar categorias!"));
            }
        }
        carregarCategorias();
    }, []);

    const handlerChange = (campo: 'marca' | 'modelo' | 'placa' | 'ano' | 'status', valor: string) => {
        setVeiculo(valorAnterior => ({ ...valorAnterior, [campo]: valor }));
    }

    const handlerChangeNumero = (campo: 'valorDiaria' | 'quilometragem' | 'kmUltimaRevisao', valor: string) => {
        setVeiculo(valorAnterior => ({ ...valorAnterior, [campo]: Number(valor) }));
    }

    const handlerSalvar = async () => {

        try {
            if (veiculoExistente) {
                await atualizarVeiculo(veiculo);
                alert("Veiculo foi atualizado com sucesso");
            } else {
                await salvarVeiculo(veiculo);
                alert("Veiculo foi criado com sucesso");
            }
            router.push("/veiculos");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao salvar veiculo"));
        }

    }

    // Alugado so muda pela devolucao da locacao
    const alugado = veiculoExistente?.status === "ALUGADO";

    return (

        <form action={handlerSalvar} className="bg-white border border-slate-200 rounded-2xl shadow-md p-8 space-y-5">
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Marca</label>
                <input name="marca" type="text" placeholder="Ex: Fiat" required value={veiculo.marca}
                    onChange={(e) => handlerChange('marca', e.target.value)} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Modelo</label>
                <input name="modelo" type="text" placeholder="Ex: Uno" required value={veiculo.modelo}
                    onChange={(e) => handlerChange('modelo', e.target.value)} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Placa</label>
                <input name="placa" type="text" placeholder="ABC1D23" required value={veiculo.placa}
                    onChange={(e) => handlerChange('placa', e.target.value)} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Ano</label>
                <input name="ano" type="text" placeholder="2024" value={veiculo.ano}
                    onChange={(e) => handlerChange('ano', e.target.value)} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Categoria</label>
                <select name="categoria" required value={veiculo.categoriaId ?? ""}
                    onChange={(e) => setVeiculo(v => ({ ...v, categoriaId: e.target.value ? Number(e.target.value) : null }))}
                    className={classeInput}>
                    <option value="">Selecione...</option>
                    {categorias.map((categoria) => (
                        <option key={categoria.id} value={categoria.id ?? ""}>{categoria.nome}</option>
                    ))}
                </select>
                {categorias.length === 0 && (
                    <p className="text-xs text-slate-500">Nenhuma categoria cadastrada. Peça a um administrador para cadastrar pelo aplicativo desktop.</p>
                )}
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Valor da diária (R$)</label>
                <input name="valorDiaria" type="number" step="0.01" min="0" placeholder="150.00" value={veiculo.valorDiaria}
                    onChange={(e) => handlerChangeNumero('valorDiaria', e.target.value)} className={classeInput} />
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Situação</label>
                {alugado ? (
                    <p className="text-sm text-slate-600">Alugado — registre a devolução na tela de locações para liberar.</p>
                ) : (
                    <select name="status" value={veiculo.status} onChange={(e) => handlerChange('status', e.target.value)} className={classeInput}>
                        <option value="DISPONIVEL">Disponível</option>
                        <option value="EM_MANUTENCAO">Em Manutenção</option>
                    </select>
                )}
                <p className="text-xs text-slate-500">Veículos Em Manutenção ou Alugados não podem ser selecionados para novas locações.</p>
            </div>
            <div className="space-y-1.5">
                <label className="block text-sm font-medium text-slate-700">Quilometragem atual (km)</label>
                <input name="quilometragem" type="number" min="0" value={veiculo.quilometragem}
                    onChange={(e) => handlerChangeNumero('quilometragem', e.target.value)} className={classeInput} />
            </div>
            {veiculoExistente && (
                <div className="space-y-1.5">
                    <label className="block text-sm font-medium text-slate-700">Km da última revisão</label>
                    <input name="kmUltimaRevisao" type="number" min="0" value={veiculo.kmUltimaRevisao}
                        onChange={(e) => handlerChangeNumero('kmUltimaRevisao', e.target.value)} className={classeInput} />
                </div>
            )}
            <div className="flex items-center justify-end gap-3 mt-8">
                <Link href="/veiculos" className="px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                    Cancelar
                </Link>
                <button type="submit" className="px-5 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
                    Salvar
                </button>
            </div>
        </form>
    );
}
