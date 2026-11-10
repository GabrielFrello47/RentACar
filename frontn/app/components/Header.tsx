"use client";

import { useDispatch, useSelector } from "react-redux";
import { useRouter } from "next/navigation";
import { RootState } from "../redux/store";
import { logout } from "../redux/slices/authSlice";

export default function Header() {
    const usuario = useSelector((state: RootState) => state.auth.usuario);
    const dispatch = useDispatch();
    const router = useRouter();

    // apaga os cookies da sessao e volta para o login
    const sair = () => {
      dispatch(logout());
      router.push("/login");
    };

    return (
      <header className="sticky top-0 z-40 bg-white border-b border-slate-200">
        <div className="max-w-5xl mx-auto flex items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-full bg-blue-50 flex items-center justify-center text-blue-600">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="20"
                height="20"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </div>
            <span className="text-sm font-medium text-slate-800">{usuario?.nome ?? "Usuário"}</span>
          </div>
  
          <button onClick={sair} className="px-4 py-2 text-sm font-medium text-red-600 hover:bg-red-50 rounded-lg transition-colors">
            Sair
          </button>
        </div>
      </header>
    );
  }