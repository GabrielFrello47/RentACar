'use client'

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useDispatch } from "react-redux";
import { login } from "../redux/slices/authSlice";
import { loginBack } from "../services/authService";
import { buscarUsuarioLogado } from "../services/usuarioService";
import { salvarToken, limparSessao } from "../services/sessaoCookies";
import { mensagemDeErro } from "../services/api";

export default function Login(){
    const router = useRouter();
    const dispatch = useDispatch();

    // onSubmit (em vez de form action) para NAO limpar o e-mail quando a senha estiver errada
    const handleLogin = async (evento: React.FormEvent<HTMLFormElement>) => {
        evento.preventDefault();
        const formData = new FormData(evento.currentTarget);
        const emailTela = formData.get("email")?.toString() ?? "";
        const senhaTela = formData.get("senha")?.toString() ?? "";

        try {
            const loginResposta = await loginBack(emailTela, senhaTela);

            // o token vai para o COOKIE antes de pedir os dados do usuario (o interceptor do axios le o cookie)
            salvarToken(loginResposta.token);

            const usuarioLogado = await buscarUsuarioLogado();

            dispatch(login({ token: loginResposta.token, usuario: { ...usuarioLogado } }));
            // cada perfil vai para a sua area: cliente (vindo do botao "Alugar" da landing page) ou atendente/admin
            if (usuarioLogado.role === "ROLE_CLIENTE") {
                const veiculo = new URLSearchParams(window.location.search).get("alugar");
                router.push(veiculo ? `/cliente?alugar=${encodeURIComponent(veiculo)}` : "/cliente");
            } else {
                router.push("/home");
            }
        } catch (err) {
            limparSessao();
            console.error(err);
            alert(mensagemDeErro(err, "Login ou senha inválidos"));
        }
    }

    return(
        <div className="min-h-screen w-full flex items-center justify-center bg-[#1A1917] px-4">
            <div className="w-full max-w-md bg-[#232120] border border-[#3A3733] rounded-2xl shadow-2xl p-8 space-y-6">
                <div className="text-center space-y-2">
                    <h1 className="text-2xl font-bold tracking-tight text-white">
                        Entrar no sistema
                    </h1>
                    <p className="text-sm text-[#9B958B]">Insira suas credenciais para acessar o painel</p>
                </div>
                <form onSubmit={handleLogin} className="space-y-4" >
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-[#C9C4BC]">
                            E-Mail
                        </label>
                        <input
                            name="email"
                            type="email"
                            required
                            placeholder="seu@email.com"
                            className="w-full px-4 py-2.5 bg-[#141312] border border-[#3A3733] rounded-lg text-white placeholder-[#635E57] focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                        />
                    </div>
                    <div className="space-y-2">
                        <label className="block text-sm font-medium text-[#C9C4BC]">
                            Senha
                        </label>
                        <input
                            name="senha"
                            type="password"
                            required
                            placeholder="••••••••"
                            className="w-full px-4 py-2.5 bg-[#141312] border border-[#3A3733] rounded-lg text-white placeholder-[#635E57] focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                        />
                    </div>
                    <button 
                        type="submit"
                        className="w-full py-2.5 px-4 bg-[#E8590C] hover:bg-[#C24509] text-white font-medium rounded-lg shadow-lg shadow-orange-950/40 transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2 focus:ring-offset-[#232120]"
                    >
                        Entrar
                    </button>
                </form>
                <p className="text-center text-sm text-[#9B958B]">
                    Ainda não tem conta?{" "}
                    <button
                        type="button"
                        onClick={() => router.push("/cadastro" + window.location.search)}
                        className="font-medium text-[#FF7A2E] hover:underline"
                    >
                        Cadastre-se
                    </button>
                </p>
                <Link
                    href="/"
                    className="block text-center text-sm font-medium text-[#9B958B] hover:text-[#E8590C] transition-colors"
                >
                    ← Voltar à tela inicial
                </Link>
            </div>
        </div>
    );
}