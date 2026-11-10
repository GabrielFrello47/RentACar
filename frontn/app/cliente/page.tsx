'use client'

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../redux/store";
import { logout } from "../redux/slices/authSlice";
import { mensagemDeErro } from "../services/api";
import {
    alugarVeiculo, buscarMinhasLocacoes, buscarVeiculosPublicos,
    MinhaLocacao, VeiculoPublico,
} from "../services/clienteAreaService";

const moeda = (v: number) => v.toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const dataBr = (iso: string) => iso.split("-").reverse().join("/");
const hoje = () => new Date().toISOString().slice(0, 10);

export default function AreaCliente() {
    const router = useRouter();
    const dispatch = useDispatch();
    const usuario = useSelector((state: RootState) => state.auth.usuario);
    const carregado = useSelector((state: RootState) => state.auth.carregado);

    const [veiculos, setVeiculos] = useState<VeiculoPublico[]>([]);
    const [locacoes, setLocacoes] = useState<MinhaLocacao[]>([]);
    const [escolhido, setEscolhido] = useState<number | null>(null);
    const [inicio, setInicio] = useState(hoje());
    const [fim, setFim] = useState(hoje());
    const [enviando, setEnviando] = useState(false);

    // so cliente logado entra aqui
    useEffect(() => {
        if (!carregado) return;
        if (usuario == null) router.replace("/login");
        else if (usuario.role !== "ROLE_CLIENTE") router.replace("/home");
    }, [carregado, usuario, router]);

    useEffect(() => {
        if (!carregado || usuario?.role !== "ROLE_CLIENTE") return;
        const carregar = async () => {
            try {
                const lista = await buscarVeiculosPublicos();
                setVeiculos(lista);
                const id = Number(new URLSearchParams(window.location.search).get("alugar"));
                if (id && lista.some((v) => v.id === id)) setEscolhido(id);
                setLocacoes(await buscarMinhasLocacoes());
            } catch (erro) {
                alert(mensagemDeErro(erro, "Erro ao carregar os dados!"));
            }
        };
        carregar();
    }, [carregado, usuario]);

    if (!carregado || usuario?.role !== "ROLE_CLIENTE") {
        return <div className="min-h-screen flex items-center justify-center bg-[#1A1917] text-sm text-[#9B958B]">Carregando...</div>;
    }

    const veiculo = veiculos.find((v) => v.id === escolhido) ?? null;
    const dias = Math.max(1, Math.round((new Date(fim).getTime() - new Date(inicio).getTime()) / 86400000));
    const total = veiculo ? veiculo.valorDiaria * dias : 0;

    const confirmar = async () => {
        if (!veiculo) return;
        setEnviando(true);
        try {
            await alugarVeiculo(veiculo.id, inicio, fim);
            alert("Locação solicitada com sucesso! Retire o carro na nossa loja.");
            setEscolhido(null);
            setLocacoes(await buscarMinhasLocacoes());
            setVeiculos(await buscarVeiculosPublicos());
        } catch (erro) {
            alert(mensagemDeErro(erro, "Não foi possível concluir a locação."));
        } finally {
            setEnviando(false);
        }
    };

    const sair = () => {
        dispatch(logout());
        router.push("/");
    };

    const campo = "px-3 py-2 border border-slate-300 rounded-lg text-slate-900 focus:outline-none focus:ring-2 focus:ring-[#E8590C]";

    return (
        <div className="min-h-screen bg-slate-50">
            <header className="bg-[#1A1917] text-white">
                <div className="max-w-4xl mx-auto flex items-center justify-between px-6 h-16">
                    <Link href="/" className="font-extrabold tracking-tight uppercase text-[15px]">RentACar</Link>
                    <div className="flex items-center gap-4 text-sm">
                        <span className="text-[#C9C4BC]">Olá, {usuario.nome}</span>
                        <button onClick={sair} className="px-3 py-1.5 rounded-lg bg-[#2A2725] hover:bg-[#3A3733] transition-colors">Sair</button>
                    </div>
                </div>
            </header>

            <main className="max-w-4xl mx-auto px-6 py-8 space-y-10">
                {veiculo && (
                    <section className="bg-white border border-slate-200 rounded-2xl shadow-md p-6 space-y-4">
                        <h2 className="text-xl font-bold text-slate-900">Alugar {veiculo.marca} {veiculo.modelo}</h2>
                        <p className="text-sm text-slate-500">{veiculo.categoriaNome} · ano {veiculo.ano} · {moeda(veiculo.valorDiaria)} / dia</p>
                        <div className="flex flex-wrap gap-4">
                            <label className="text-sm text-slate-700 space-y-1">
                                <span className="block">Retirada</span>
                                <input type="date" min={hoje()} value={inicio}
                                    onChange={(e) => { setInicio(e.target.value); if (fim < e.target.value) setFim(e.target.value); }}
                                    className={campo} />
                            </label>
                            <label className="text-sm text-slate-700 space-y-1">
                                <span className="block">Devolução</span>
                                <input type="date" min={inicio} value={fim} onChange={(e) => setFim(e.target.value)} className={campo} />
                            </label>
                        </div>
                        <p className="text-lg font-semibold text-slate-900">{dias} {dias === 1 ? "dia" : "dias"} · Total {moeda(total)}</p>
                        <div className="flex gap-3">
                            <button onClick={confirmar} disabled={enviando}
                                className="px-5 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] disabled:opacity-60 text-white text-sm font-medium rounded-lg shadow-sm transition-colors">
                                {enviando ? "Enviando..." : "Confirmar locação"}
                            </button>
                            <button onClick={() => setEscolhido(null)} className="px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors">
                                Cancelar
                            </button>
                        </div>
                    </section>
                )}

                <section>
                    <h2 className="text-xl font-bold text-slate-900 mb-4">Carros disponíveis</h2>
                    {veiculos.length === 0 ? (
                        <p className="text-slate-500 italic">Nenhum veículo disponível no momento.</p>
                    ) : (
                        <div className="grid sm:grid-cols-2 gap-4">
                            {veiculos.map((v) => (
                                <div key={v.id} className="bg-white border border-slate-200 rounded-xl shadow-sm p-5 flex items-center justify-between gap-3">
                                    <div>
                                        <p className="font-semibold text-slate-900">{v.marca} {v.modelo}</p>
                                        <p className="text-xs text-slate-500">{v.categoriaNome} · {v.ano}</p>
                                        <p className="text-sm font-medium text-slate-800 mt-1">{moeda(v.valorDiaria)} / dia</p>
                                    </div>
                                    <button onClick={() => setEscolhido(v.id)}
                                        className="px-4 py-2 bg-[#E8590C] hover:bg-[#C94A08] text-white text-sm font-medium rounded-lg transition-colors">
                                        Alugar
                                    </button>
                                </div>
                            ))}
                        </div>
                    )}
                </section>

                <section>
                    <h2 className="text-xl font-bold text-slate-900 mb-4">Minhas locações</h2>
                    {locacoes.length === 0 ? (
                        <p className="text-slate-500 italic">Você ainda não tem locações.</p>
                    ) : (
                        <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-x-auto">
                            <table className="w-full text-left text-sm">
                                <thead>
                                    <tr className="bg-slate-100/75 text-xs uppercase tracking-wider text-slate-600">
                                        <th className="px-5 py-3">Retirada</th>
                                        <th className="px-5 py-3">Devolução</th>
                                        <th className="px-5 py-3">Total</th>
                                        <th className="px-5 py-3">Situação</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-200">
                                    {locacoes.map((l) => (
                                        <tr key={l.id}>
                                            <td className="px-5 py-3">{dataBr(l.dataInicio)}</td>
                                            <td className="px-5 py-3">{dataBr(l.dataFim)}</td>
                                            <td className="px-5 py-3">{moeda(l.valorTotal)}</td>
                                            <td className="px-5 py-3">{l.status === "FINALIZADA" ? "Finalizada" : "Em andamento"}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </section>
            </main>
        </div>
    );
}
