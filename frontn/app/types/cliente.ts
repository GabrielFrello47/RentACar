export class Cliente {

    constructor(

        public id: number | null,
        public nome: string,
        public cpf: string,
        public telefone: string,
        public email: string,

    ) { }
}

export interface ClienteFormProps {
    clienteExistente?: Cliente
}
