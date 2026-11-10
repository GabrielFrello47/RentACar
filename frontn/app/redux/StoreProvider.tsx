'use client'

import { useEffect } from "react"
import { Provider } from "react-redux"
import { store } from "./store"
import { restaurar } from "./slices/authSlice"

export default function StoreProvider({ children }: { children: React.ReactNode }) {

  // ao abrir/recarregar a pagina, recupera a sessao que esta nos cookies
  useEffect(() => {
    store.dispatch(restaurar());
  }, []);

  return <Provider store={store}>{children}</Provider>
}
