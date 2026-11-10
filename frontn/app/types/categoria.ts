export class Categoria {

    constructor(

        public id: number | null,
        public nome: string,
        public descricao: string,

    ) { }
}

export interface CategoriaFormProps {
    categoriaExistente?: Categoria
}
