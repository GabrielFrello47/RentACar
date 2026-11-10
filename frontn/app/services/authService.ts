import { LoginResponse } from "../types/auth";
import api from "./api";

export async function loginBack(email: string, senha: string): Promise<LoginResponse> {
    const loginResposta = await api.post<LoginResponse>("/auth/login", { email: email, senha: senha });
    return loginResposta.data;
}
