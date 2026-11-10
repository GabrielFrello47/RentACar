import LocacoesForm from "../components/LocacoesForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function CadastroLocacao() {

return (
        <CabecalhoCadastro voltarHref="/locacoes" titulo="Nova Locacao" descricao="Preencha os dados abaixo para cadastrar uma nova Locacao">
            <LocacoesForm/>
        </CabecalhoCadastro>
    )
}