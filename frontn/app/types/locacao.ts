export class Locacao {

    constructor(

        public id: number | null,
        // datas no formato yyyy-MM-dd (o mesmo do <input type="date"> e do LocalDate do back-end)
        public dataInicio: string,
        public dataFim: string,
        public valorTotal: number,
        public cliente: string | null,
        public veiculo: string | null,
        // EM_ANDAMENTO | FINALIZADA (definido pela API)
        public status?: string | null,
        public dataDevolucao?: string | null,
        public quilometragemDevolucao?: number | null,

    ) { }

}

export interface LocacaoFormProps {
    locacaoExistente?: Locacao
}

export interface DevolucaoResponse {
    locacaoId: number
    veiculoId: number
    quilometragemAtual: number
    kmDesdeUltimaRevisao: number
    limiteRevisaoKm: number
    alertaRevisao: boolean
    mensagem: string
}
