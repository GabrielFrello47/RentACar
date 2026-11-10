'use client'

import Footer from "../components/Footer";
import Header from "../components/Header";
import Sidebar from "../components/Sidebar";
import { useSelector } from "react-redux";
import { RootState } from "../redux/store";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function SistemaLayout({ children }: { children: React.ReactNode }) {

    const usuario = useSelector((state: RootState) => state.auth.usuario);
    const carregado = useSelector((state: RootState) => state.auth.carregado);

    const router = useRouter();

    // sem usuario logado (cookie ausente ou vencido) volta para o login
    useEffect(() => {
        if (carregado && usuario == null) {
            router.replace("/login");
        } else if (usuario?.role === "ROLE_CLIENTE") {
            // cliente tem a propria area; o painel do atendente nao e para ele
            router.replace("/cliente");
        }
    }, [carregado, usuario, router]);

    // enquanto os cookies sao lidos (ou durante o redirecionamento) nao mostra a area restrita
    if (!carregado || usuario == null || usuario.role === "ROLE_CLIENTE") {
        return (
            <div className="min-h-screen flex items-center justify-center bg-blue-50 text-sm text-slate-500">
                Carregando...
            </div>
        );
    }

    return (
        <div className="flex min-h-screen bg-blue-50">
            <Sidebar />
            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                <Header />
                <main className="flex-1 overflow-y-auto bg-blue-50 p-6 text-blue-900">
                    {children}
                </main>
                <Footer />
            </div>
        </div>);
}
