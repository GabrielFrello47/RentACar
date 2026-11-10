import { AuthState } from "@/app/types/auth";
import { Usuario } from "@/app/types/usuario";
import { createSlice, PayloadAction } from "@reduxjs/toolkit";
import { lerUsuario, lerToken, limparSessao, salvarSessao, sessaoValida } from "@/app/services/sessaoCookies";

// O estado nasce VAZIO (igual no servidor e no navegador) e so depois da tela montar
// o "restaurar" le os cookies. Assim nao ha divergencia de hidratacao do Next.
const initialState: AuthState = {
    usuario: null,
    token: "",
    carregado: false
}

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        restaurar: (state) => {
            if (sessaoValida()) {
                state.token = lerToken();
                state.usuario = lerUsuario();
            } else {
                limparSessao();
                state.token = "";
                state.usuario = null;
            }
            state.carregado = true;
        },
        login: (state, action: PayloadAction<{ token: string, usuario: Usuario }>) => {
            salvarSessao(action.payload.token, action.payload.usuario);

            state.token = action.payload.token;
            state.usuario = action.payload.usuario;
            state.carregado = true;
        },
        logout: (state) => {
            limparSessao();

            state.token = "";
            state.usuario = null;
        }
    }
});

export const { restaurar, login, logout } = authSlice.actions;
export default authSlice.reducer;
