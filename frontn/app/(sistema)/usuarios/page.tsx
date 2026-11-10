"use client"
import { Usuario } from "@/app/types/usuario";
import { atualizarStatusUsuario, buscarListaUsuarios, deletarUsuario } from "@/app/services/usuarioService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useEffect, useState } from "react";


export default function Usuarios(){

    const [usuarios,setUsuarios] = useState<Usuario[]>([]);

    // contador usado para recarregar a lista depois de excluir/alterar
    const [versao,setVersao] = useState(0);
    const carregarDados = () => setVersao(v => v + 1);

    useEffect(()=>{
        const carregar = async ()=>{
            try {
                setUsuarios(await buscarListaUsuarios());
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar dados!"));
            }
        };
        carregar();
    },[versao]);

    const handleDeletarUsuario = async(usuario:Usuario) =>{

        if (!confirm("Tem certeza que deseja excluir este usuário?")) return;

        try {
            await deletarUsuario(usuario.id || 0);
            alert("Excluido com sucesso!");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao excluir usuário!"));
            return;
        }

        carregarDados();
    }

    const handleAlterarStatusUsuario = async(usuario:Usuario) =>{

        try {
            await atualizarStatusUsuario(usuario);
            alert("Atualizado status com sucesso!");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao atualizar status!"));
            return;
        }

        carregarDados();
    }

    return (
        <div className="w-full bg-slate-50 p-6 md:p-8 font-sans">
            <div className="w-full flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
                    Gestao de usuarios
                </h1>
                <Link 
                    href="/usuarios/novo"
                    className="inline-flex items-center justify-center px-4 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2"
                >
                    Novo Usuário
                </Link>
            </div>
    
            <div className="w-full">
                <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden w-full">
                    <div className="overflow-x-auto w-full">
                        <table className="w-full text-left border-collapse">
                            <thead>
                                <tr className="bg-slate-100/75 border-b border-slate-200">
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Código
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Nome
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        CPF
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        E-mail
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Status
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Ações
                                    </th>
                                </tr>
                            </thead>
                            <tbody className="divide-y divide-slate-200">
                               {usuarios.map((usuario)=>(     
                                <tr key={usuario.id} className="hover:bg-blue-50/50 transition-colors duration-150">
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {usuario.id}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {usuario.nome}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {usuario.cpf}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {usuario.email}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {usuario.status}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        <div className="flex items-center gap-2">
                                            <Link
                                                href={`/usuarios/${usuario.id}/editar`}
                                                className="inline-flex items-center justify-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md transition-colors duration-150"
                                            >
                                                Editar
                                            </Link>
                                            <button
                                                onClick={()=> handleDeletarUsuario(usuario)}
                                                className="inline-flex items-center justify-center px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 hover:text-red-700 text-xs font-medium rounded-md transition-colors duration-150"
                                            >
                                                Deletar
                                            </button>
                                            <button
                                                onClick={()=> handleAlterarStatusUsuario(usuario)}
                                                className={`inline-flex items-center justify-center px-3 py-1.5 text-xs font-medium rounded-md transition-colors duration-150 ${
                                                    usuario.status === 'BLOQUEADO'
                                                    ? 'bg-orange-50 hover:bg-orange-100 text-orange-600 hover:text-orange-700'
                                                    : 'bg-green-50 hover:bg-green-100 text-green-600 hover:text-green-700'
                                                }`}
                                            >
                                                {usuario.status}
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                                ))}

                                { usuarios.length === 0 &&
                                (
                                    <tr>
                                        <td colSpan={6} className="px-6 py-12 text-center text-slate-800 italic" >
                                            Nenhum usuario encontrado!
                                        </td>
                                    </tr>
                                )
                                }



                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    )

}