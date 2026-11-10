import CategoriaForm from "../components/CategoriaForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function CadastroCategoria() {
    return (
        <CabecalhoCadastro voltarHref="/categorias" titulo="Nova Categoria" descricao="Preencha os dados abaixo para cadastrar uma nova Categoria">
            <CategoriaForm />
        </CabecalhoCadastro>
    )
}
