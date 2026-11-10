"use client";

import { useRouter } from "next/navigation";
import { Cliente, ClienteFormProps } from "@/app/types/cliente";
import { atualizarCliente, salvarCliente } from "@/app/services/clienteService";
import { mensagemDeErro } from "@/app/services/api";
import Link from "next/link";
import { useState } from "react";

export default function ClienteForm({ clienteExistente }: ClienteFormProps) {

    const router = useRouter();
    const [cliente, setCliente] = useState<Cliente>(clienteExistente ?? new Cliente(null, "", "", "", ""));

    const handlerChange = (campo: 'nome' | 'cpf' | 'telefone' | 'email', valor: string) => {
        setCliente(valorAnterior => ({ ...valorAnterior, [campo]: valor }));
    }

    const handlerSalvar = async () => {

        try {
            if (clienteExistente) {
                await atualizarCliente(cliente);
                alert("Cliente foi atualizado com sucesso");
            } else {
                await salvarCliente(cliente);
                alert("Cliente foi criado com sucesso");
            }
            router.push("/clientes");
        } catch (error) {
            alert(mensagemDeErro(error, "Erro ao salvar cliente"));
        }

    }

    return (

        // <form> com "action": no React 19/Next.js, passar uma função aqui faz o React chamá-la
        // ao enviar o formulário (clique em Salvar ou Enter). Ela recebe o FormData.
        // Classes Tailwind: fundo branco, borda, cantos arredondados, sombra, espaçamento interno (p-8),
        // espaço vertical entre filhos (space-y-5), largura máxima (max-w-lg) e centralizado (mx-auto).
        <form action={handlerSalvar} className="bg-white border border-slate-200 rounded-2xl shadow-md p-8 space-y-5">
            {/* Div que agrupa todos os campos e os botões */}
            <div>
                {/* Grupo do campo Nome (space-y-1.5 = pequeno espaço entre label e input) */}
                <div className="space-y-1.5">
                    {/* Rótulo do campo. "block" faz o label ocupar a linha toda, ficando acima do input. */}
                    <label className="block text-sm font-medium text-slate-700">
                    Nome completo
                    </label>

                    {/* Campo de texto do nome */}
                    <input
                        // Nome do campo no formulário (também usado no FormData).
                        name="nome"
                        // Tipo texto simples.
                        type="text"
                        // Texto de dica, exibido enquanto o campo está vazio.
                        placeholder="Nome completo"
                        // Campo "controlado": o valor exibido vem sempre do estado do React.
                        value={cliente.nome}
                        // A cada tecla digitada, chama handlerChange informando o campo e o novo texto (e.target.value).
                        onChange={(e) => handlerChange('nome', e.target.value)}
                        // Estilo: largura total, borda, cantos arredondados; ao focar (focus:), mostra um anel azul.
                        className="w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                    />
                </div>
                {/* Grupo do campo CPF (mt-5 = margem acima, para separar do campo anterior) */}
                <div className="space-y-1.5 mt-5">
                    <label className="block text-sm font-medium text-slate-700">
                    CPF
                    </label>

                    <input
                        name="cpf"
                        type="text"
                        // Dica para o usuário digitar apenas números.
                        placeholder="Somente números"
                        // Valor vem do estado.
                        value={cliente.cpf}
                        // Atualiza o campo "cpf" no estado a cada digitação.
                        onChange={(e) => handlerChange('cpf', e.target.value)}
                        className="w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                    />
                </div>
                {/* Grupo do campo Telefone */}
                <div className="space-y-1.5 mt-5">
                    <label className="block text-sm font-medium text-slate-700">
                    Telefone
                    </label>

                    <input
                        name="telefone"
                        type="text"
                        // Exemplo do formato esperado.
                        placeholder="(00) 00000-0000"
                        value={cliente.telefone}
                        onChange={(e) => handlerChange('telefone', e.target.value)}
                        className="w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                    />
                </div>
                {/* Grupo do campo Email */}
                <div className="space-y-1.5 mt-5">
                    <label className="block text-sm font-medium text-slate-700">
                    Email
                    </label>

                    <input
                        name="email"
                        // type="email" faz o navegador validar o formato (precisa ter @) ao enviar.
                        type="email"
                        placeholder="cliente@email.com"
                        value={cliente.email}
                        onChange={(e) => handlerChange('email', e.target.value)}
                        className="w-full px-4 py-2.5 border border-slate-300 rounded-lg text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-[#E8590C] focus:border-transparent transition-all"
                    />
                </div>
                {/* Área dos botões: lado a lado (flex), alinhados à direita (justify-end), com espaço entre eles (gap-3) */}
                <div className="flex items-center justify-end gap-3 mt-8">
                    {/* Link que volta para a listagem sem salvar nada */}
                    <Link
                        href="/clientes"
                        className="px-4 py-2.5 text-sm font-medium text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
                    >
                        Cancelar
                    </Link>
                    {/* type="submit" dispara o envio do formulário, que executa o handlerSalvar (via action do form) */}
                    <button
                        type="submit"
                        className="px-5 py-2.5 bg-[#E8590C] hover:bg-[#C94A08] text-white text-sm font-medium rounded-lg shadow-sm transition-colors"
                    >
                        Salvar
                    </button>
                </div>

            </div>
        </form>

    );

}