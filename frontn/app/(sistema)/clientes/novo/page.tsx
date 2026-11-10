import ClienteForm from "../components/ClientesForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function CadastroCliente() {

return (
        <CabecalhoCadastro voltarHref="/clientes" titulo="Novo Cliente" descricao="Preencha os dados abaixo para cadastrar um novo Cliente">
            <ClienteForm/>
        </CabecalhoCadastro>
    )
}