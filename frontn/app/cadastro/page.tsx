'use client'

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { cadastrarCliente } from "../services/clienteAreaService";
import { mensagemDeErro } from "../services/api";

const campo = "w-full px-4 py-2.5 bg-[#141312] border border-[#3A3733] rounded-lg text-white placeholder-[#635E57] focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all";
const rotulo = "block text-sm font-medium text-[#C9C4BC]";

export default function Cadastro() {
    const router = useRouter();
    const [enviando, setEnviando] = useState(false);

    const handleCadastro = async (evento: React.FormEvent<HTMLFormElement>) => {
        evento.preventDefault();
        const f = new FormData(evento.currentTarget);
        const texto = (nome: string) => f.get(nome)?.toString() ?? "";

        if (texto("senha") !== texto("confirmar")) {
            alert("As senhas não conferem!");
            return;
        }

        setEnviando(true);
        try {
            await cadastrarCliente({
                nome: texto("nome"),
                cpf: texto("cpf"),
                telefone: texto("telefone"),
                email: texto("email"),
                senha: texto("senha"),
            });
            alert("Cadastro realizado! Entre com o seu e-mail e senha.");
            // mantem o carro escolhido (?alugar=ID) para continuar de onde parou
            router.push("/login" + window.location.search);
        } catch (err) {
            alert(mensagemDeErro(err, "Não foi possível concluir o cadastro. Tente novamente."));
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="min-h-screen w-full flex items-center justify-center bg-[#1A1917] px-4 py-10">
            <div className="w-full max-w-md bg-[#232120] border border-[#3A3733] rounded-2xl shadow-2xl p-8 space-y-6">
                <div className="text-center space-y-2">
                    <h1 className="text-2xl font-bold tracking-tight text-white">Criar conta</h1>
                    <p className="text-sm text-[#9B958B]">Cadastre-se para alugar o seu carro</p>
                </div>
                <form onSubmit={handleCadastro} className="space-y-4">
                    <div className="space-y-2">
                        <label className={rotulo}>Nome completo</label>
                        <input name="nome" required placeholder="Seu nome" className={campo} />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-2">
                            <label className={rotulo}>CPF</label>
                            <input name="cpf" required placeholder="000.000.000-00" className={campo} />
                        </div>
                        <div className="space-y-2">
                            <label className={rotulo}>Telefone</label>
                            <input name="telefone" required placeholder="(00) 00000-0000" className={campo} />
                        </div>
                    </div>
                    <div className="space-y-2">
                        <label className={rotulo}>E-Mail</label>
                        <input name="email" type="email" required placeholder="seu@email.com" className={campo} />
                    </div>
                    <div className="grid grid-cols-2 gap-3">
                        <div className="space-y-2">
                            <label className={rotulo}>Senha</label>
                            <input name="senha" type="password" required minLength={6} placeholder="mín. 6 caracteres" className={campo} />
                        </div>
                        <div className="space-y-2">
                            <label className={rotulo}>Confirmar senha</label>
                            <input name="confirmar" type="password" required minLength={6} placeholder="repita a senha" className={campo} />
                        </div>
                    </div>
                    <button
                        type="submit"
                        disabled={enviando}
                        className="w-full py-2.5 px-4 bg-[#E8590C] hover:bg-[#C24509] disabled:opacity-60 text-white font-medium rounded-lg shadow-lg shadow-orange-950/40 transition-all duration-200"
                    >
                        {enviando ? "Cadastrando..." : "Cadastrar"}
                    </button>
                </form>
                <p className="text-center text-sm text-[#9B958B]">
                    Já tem conta?{" "}
                    <button
                        type="button"
                        onClick={() => router.push("/login" + window.location.search)}
                        className="font-medium text-[#FF7A2E] hover:underline"
                    >
                        Entrar
                    </button>
                </p>
                <Link href="/" className="block text-center text-sm font-medium text-[#9B958B] hover:text-[#E8590C] transition-colors">
                    ← Voltar à tela inicial
                </Link>
            </div>
        </div>
    );
}
