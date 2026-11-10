"use client";

import { Cliente } from "@/app/types/cliente";
import { buscarListaClientes, deletarCliente } from "@/app/services/clienteService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useEffect, useState } from "react";

export default function Clientes(){

    const [clientes,setClientes] = useState<Cliente[]>([]);

    // contador usado para recarregar a lista depois de excluir/alterar
    const [versao,setVersao] = useState(0);
    const carregarDados = () => setVersao(v => v + 1);

    useEffect(()=>{
        const carregar = async ()=>{
            try {
                setClientes(await buscarListaClientes());
            } catch (error) {
                alert(mensagemDeErro(error, "Erro ao carregar dados!"));
            }
        };
        carregar();
    },[versao]);

    const handleDeletar = async (id: number) => {

        const confirmar = confirm("Tem certeza que deseja excluir este cliente?");
        if (!confirmar) return;

        try {
            await deletarCliente(id);
            carregarDados();
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao excluir cliente!"));
        }

    }


    return (
        // Container geral da página. Tailwind: min-h-screen (altura mínima = tela inteira),
        // w-full (largura total), bg-slate-50 (fundo cinza claro), p-6/md:p-8 (espaçamento interno,
        // maior em telas médias+), font-sans (fonte sem serifa).
        <div className="min-h-screen w-full bg-slate-50 p-6 md:p-8 font-sans">
            {/* Cabeçalho: em telas pequenas empilha (flex-col), em sm+ fica lado a lado (sm:flex-row).
                justify-between afasta título e botão; gap-4 dá espaço entre eles; mb-8 margem abaixo. */}
            <div className="w-full flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-8">
                {/* Título da página: texto grande, negrito, cor quase preta */}
                <h1 className="text-2xl md:text-3xl font-bold text-slate-900 tracking-tight">
                    Gestao de clientes
                </h1>
                {/* Link estilizado como botão azul que leva à página de cadastro (/clientes/novo).
                    hover: muda a cor ao passar o mouse; focus: mostra um anel ao navegar pelo teclado. */}
                <Link
                    href="/clientes/novo"
                    className="inline-flex items-center justify-center px-4 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white font-medium text-sm rounded-lg shadow-sm transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:ring-offset-2"
                >
                    Novo Cliente
                </Link>
            </div>

            {/* Área que envolve a tabela */}
            <div className="w-full">
                {/* "Cartão" branco com borda, cantos arredondados e sombra leve.
                    overflow-hidden corta o conteúdo que passar dos cantos arredondados. */}
                <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden w-full">
                    {/* overflow-x-auto cria rolagem horizontal se a tabela não couber (celulares) */}
                    <div className="overflow-x-auto w-full">
                        {/* Tabela HTML. border-collapse junta as bordas das células. */}
                        <table className="w-full text-left border-collapse">
                            {/* Cabeçalho da tabela */}
                            <thead>
                                {/* Linha do cabeçalho com fundo cinza e linha inferior */}
                                <tr className="bg-slate-100/75 border-b border-slate-200">
                                    {/* Cada <th> é o título de uma coluna (texto pequeno, MAIÚSCULO, negrito) */}
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
                                        Telefone
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        E-mail
                                    </th>
                                    <th className="px-6 py-3.5 text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Ações
                                    </th>
                                </tr>
                            </thead>
                            {/* Corpo da tabela. divide-y coloca uma linha separadora entre as linhas. */}
                            <tbody className="divide-y divide-slate-200">
                               {/* map percorre a lista e devolve uma <tr> para cada cliente */}
                               {clientes.map((cliente)=>(
                                // "key" é obrigatória no React em listas: identifica cada item de forma única.
                                // hover: destaca a linha em azul claro ao passar o mouse.
                                <tr key={cliente.id} className="hover:bg-blue-50/50 transition-colors duration-150">
                                    {/* Cada <td> é uma célula; as chaves {} inserem valores JavaScript no HTML */}
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {cliente.id}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {cliente.nome}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {cliente.cpf}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {cliente.telefone}
                                    </td>
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800">
                                        {cliente.email}
                                    </td>
                                    {/* Célula de ações; space-x-2 separa os dois botões horizontalmente */}
                                    <td className="px-6 py-4 text-sm font-medium text-slate-800 space-x-2">
                                        {/* Link para a tela de edição daquele cliente: /clientes/<id>/editar */}
                                        <Link
                                            href={`/clientes/${cliente.id}/editar`}
                                            className="inline-flex items-center justify-center px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-medium rounded-md transition-colors duration-150"
                                        >
                                            Editar
                                        </Link>
                                        {/* Botão vermelho. onClick chama handleDeletar passando o id do cliente.
                                            O "!" após id diz ao TypeScript: "garanto que não é undefined"
                                            (no tipo Cliente o id é opcional, pois ainda não existe ao cadastrar). */}
                                        <button
                                            onClick={() => handleDeletar(cliente.id!)}
                                            className="inline-flex items-center justify-center px-3 py-1.5 bg-red-50 hover:bg-red-100 text-red-600 text-xs font-medium rounded-md transition-colors duration-150"
                                        >
                                            Excluir
                                        </button>
                                    </td>
                                </tr>
                                ))}

                                {/* Renderização condicional: o && só mostra o que vem depois
                                    se a condição à esquerda for verdadeira (lista vazia). */}
                                { clientes.length === 0 &&
                                (
                                    <tr>
                                        {/* colSpan={6} faz a célula ocupar as 6 colunas da tabela, centralizando a mensagem */}
                                        <td colSpan={6} className="px-6 py-12 text-center text-slate-800 italic" >
                                            Nenhum cliente encontrado!
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