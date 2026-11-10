export class Veiculo {

    constructor(

        public id: number | null,
        public marca: string,
        public modelo: string,
        public placa: string,
        public ano: string,
        public valorDiaria: number,
        // DISPONIVEL | ALUGADO | EM_MANUTENCAO
        public status: string = "DISPONIVEL",
        public quilometragem: number = 0,
        public kmUltimaRevisao: number = 0,
        public categoriaId: number | null = null,
        // abaixo: so leitura (calculados pela API)
        public categoriaNome?: string | null,
        public revisaoPendente?: boolean,
        public limiteRevisaoKm?: number,

    ) { }

}

export interface VeiculoFormProps {
    veiculoExistente?: Veiculo
}
