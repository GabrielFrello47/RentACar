/** Data de hoje no formato yyyy-MM-dd, no fuso do usuario (toISOString usa UTC e pode virar o dia). */
export function hojeISO(): string {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

/** "2026-11-10" -> "10/11/2026" (sem passar por Date, para nao mudar o dia por causa do fuso). */
export function formatarData(iso: string | null | undefined): string {
    if (!iso) return "";
    const [ano, mes, dia] = iso.substring(0, 10).split("-");
    return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso;
}
