import Cookies from "js-cookie";
import { Usuario } from "../types/usuario";

// Nomes dos cookies da sessao (mesmo padrao do projeto do professor: js-cookie).
export const COOKIE_TOKEN = "token";
export const COOKIE_USUARIO = "usuario";

/** Le o "exp" (segundos) do JWT para o cookie vencer junto com o token. */
export function expiracaoDoToken(token: string): Date | undefined {
    try {
        const payload = token.split(".")[1];
        const base64 = payload.replace(/-/g, "+").replace(/_/g, "/");
        const json = JSON.parse(atob(base64.padEnd(Math.ceil(base64.length / 4) * 4, "=")));
        return typeof json.exp === "number" ? new Date(json.exp * 1000) : undefined;
    } catch {
        return undefined;
    }
}

function opcoes(token: string): Cookies.CookieAttributes {
    return {
        // sem "exp" valido vira cookie de sessao (some ao fechar o navegador)
        expires: expiracaoDoToken(token),
        // so manda o cookie por HTTPS quando o site esta em HTTPS (em localhost/HTTP um cookie "secure" nao e gravado em todos os navegadores)
        secure: typeof window !== "undefined" && window.location.protocol === "https:",
        // o cookie nao acompanha requisicoes vindas de outros sites
        sameSite: "strict",
        path: "/",
    };
}

export function lerToken(): string {
    return Cookies.get(COOKIE_TOKEN) ?? "";
}

export function lerUsuario(): Usuario | null {
    const bruto = Cookies.get(COOKIE_USUARIO);
    if (!bruto) return null;
    try {
        return JSON.parse(bruto) as Usuario;
    } catch {
        return null;
    }
}

export function salvarToken(token: string): void {
    Cookies.set(COOKIE_TOKEN, token, opcoes(token));
}

export function salvarSessao(token: string, usuario: Usuario): void {
    salvarToken(token);
    // No cookie vai so o necessario para a tela: nada de CPF nem senha.
    const resumo = { id: usuario.id, nome: usuario.nome, email: usuario.email, status: usuario.status, role: usuario.role };
    Cookies.set(COOKIE_USUARIO, JSON.stringify(resumo), opcoes(token));
}

export function limparSessao(): void {
    Cookies.remove(COOKIE_TOKEN, { path: "/" });
    Cookies.remove(COOKIE_USUARIO, { path: "/" });
}

/** O cookie do token ainda existe e nao venceu? */
export function sessaoValida(): boolean {
    const token = lerToken();
    if (!token) return false;
    const exp = expiracaoDoToken(token);
    return !exp || exp.getTime() > Date.now();
}
