'use client'

import { Usuario, UsuarioFormProps } from "@/app/types/usuario";
import { atualizarUsuario, salvarUsuario } from "@/app/services/usuarioService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";

export default function UsuarioForm({usuarioExistente}:UsuarioFormProps) {
    const router = useRouter();

    const [ usuario,setUsuario ] = useState<Usuario>(
        usuarioExistente
            ? new Usuario(usuarioExistente.id, usuarioExistente.nome, usuarioExistente.email, usuarioExistente.status, usuarioExistente.cpf, "")
            : new Usuario(null,"","","ATIVO","","")
    );

    const handlerChange = ( campo: 'nome'|  'email' |'cpf'| 'senha', valor:string) =>{
        setUsuario(valorAnterior => ({ ...valorAnterior, [campo]: valor }))
    }

    const handlerSalvar = async () =>{

        try {
            if(usuarioExistente){
                await atualizarUsuario(usuario);
            }else{
                await salvarUsuario(usuario);
            }
            alert("Usuário foi salvo com sucesso!");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao salvar usuário!"));
            return;
        }

        router.push("/usuarios");
    }

    return (
        <form action={handlerSalvar} className="space-y-6 bg-white border border-slate-200 rounded-2xl shadow-md p-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-2">
                    <label className="block text-sm font-medium text-slate-700">
                        Nome completo:
                    </label>
                    <input 
                    name="nome" 
                    value={usuario.nome}
                    required
                    onChange={(e)=> handlerChange('nome',e.target.value)}
                    placeholder="João da Silva Sauro"
                    className="w-full px-4 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all duration-200 shadow-inner">
                    </input>
                </div>
                <div className="space-y-2">
                    <label className="block text-sm font-medium text-slate-700">
                        CPF:
                    </label>
                    <input 
                    name="CPF" 
                    value={usuario.cpf}
                    required
                    placeholder="000.000.000-00"
                    onChange={(e)=> handlerChange('cpf',e.target.value)}
                    className="w-full px-4 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all duration-200 shadow-inner">
                    </input>
                </div>
                <div className="space-y-2">
                    <label className="block text-sm font-medium text-slate-700">
                        E-mail
                    </label>
                    <input 
                    name="email" 
                    value={usuario.email}
                    required
                    placeholder="EmailDoJoao@SilvaSauro.com.br"
                    onChange={(e)=> handlerChange('email',e.target.value)}
                    className="w-full px-4 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all duration-200 shadow-inner">
                    </input>
                </div>
                <div className="space-y-2">
                    <label className="block text-sm font-medium text-slate-700">
                        Senha:
                    </label>
                    <input 
                    name="Senha" 
                    value={usuario.senha}
                    required={!usuarioExistente}
                    placeholder= {usuarioExistente ? "Deixe em branco para manter a atual" : "Mínimo 6 caracteres"}
                    onChange={(e)=> handlerChange('senha',e.target.value)}
                    type="password" className="w-full px-4 py-2.5 bg-white border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all duration-200 shadow-inner">
                    </input>
                </div>
            </div>

            <div className="flex items-center justify-end space-x-4 pt-4 border-t border-slate-200">
                <Link href="/usuarios" className="px-5 py-2.5 bg-slate-100 hover:bg-slate-200 text-slate-600 font-medium text-sm rounded-xl transition-all duration-200 text-center border border-slate-300"> Cancelar</Link>
                <button type="submit" className="px-6 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-semibold text-sm rounded-xl shadow-md transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-[#E8590C]"> Salvar</button>
            </div>
        </form>
    );
}