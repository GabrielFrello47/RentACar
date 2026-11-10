import UsuarioForm from "../components/UsuarioForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function CadastroUsuario() {

return (
        <CabecalhoCadastro voltarHref="/usuarios" titulo="Novo Usuario" descricao="Preencha os dados abaixo para cadastrar um novo Usuario">
            <UsuarioForm/>
        </CabecalhoCadastro>
    )
}