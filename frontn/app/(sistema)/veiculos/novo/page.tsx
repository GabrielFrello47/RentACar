import VeiculosForm from "../components/VeiculosForm";
import CabecalhoCadastro from "@/app/components/CabecalhoCadastro";

export default function CadastroVeiculo() {

return (
        <CabecalhoCadastro voltarHref="/veiculos" titulo="Novo Veiculo" descricao="Preencha os dados abaixo para cadastrar um novo Veiculo">
            <VeiculosForm/>
        </CabecalhoCadastro>
    )
}