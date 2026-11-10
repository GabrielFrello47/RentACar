'use client'

import axios, { isAxiosError } from "axios";
import { lerToken, limparSessao } from "./sessaoCookies";

const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080"
});

// Toda requisicao leva o token que esta no COOKIE (antes o token era descartado no login).
api.interceptors.request.use(
    (config) => {
        const token = lerToken();

        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }

        return config;
    }
);

// Token vencido/invalido (401): limpa os cookies e volta para o login.
api.interceptors.response.use(
    (resposta) => resposta,
    (erro) => {
        if (isAxiosError(erro) && erro.response?.status === 401 && typeof window !== "undefined") {
            limparSessao();
            if (!window.location.pathname.startsWith("/login")) {
                // recarga completa de proposito: zera o estado do Redux junto com os cookies
                // eslint-disable-next-line @next/next/no-location-assign-relative-destination
                window.location.assign("/login");
            }
        }
        return Promise.reject(erro);
    }
);

/** Texto de erro devolvido pela API (ex.: "CPF Invalido!") ou uma mensagem padrao. */
export function mensagemDeErro(erro: unknown, padrao: string): string {
    if (isAxiosError(erro) && typeof erro.response?.data === "string" && erro.response.data.trim() !== "") {
        return erro.response.data;
    }
    return padrao;
}

export default api;
