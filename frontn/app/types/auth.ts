import { Usuario } from "./usuario"

export interface LoginResponse {
    token: string
}

export interface AuthState {
    usuario: Usuario | null
    token: string
    // true depois que os cookies foram lidos (evita redirecionar para o login antes da hora)
    carregado: boolean
}
