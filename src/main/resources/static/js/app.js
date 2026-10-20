/* =====================================================
   ELLENCO - NÚCLEO DO FRONT
   API (fetch com sessão), layout/menu, toasts, helpers.
===================================================== */
const App = (function () {

    const MENU = [
        { id: "dashboard", href: "dashboard.html", icone: "📊", rotulo: "Dashboard" },
        { id: "obras", href: "obras.html", icone: "🏗️", rotulo: "Obras" },
        { id: "relatorio", href: "relatorio.html", icone: "📄", rotulo: "Relatórios" },
        { id: "configuracoes", href: "configuracoes.html", icone: "⚙️", rotulo: "Configurações" }
    ];

    const app = { usuario: null };

    /* ---------------- API ---------------- */
    app.api = async function (caminho, opcoes) {
        opcoes = opcoes || {};
        const init = { method: opcoes.method || "GET", credentials: "same-origin", headers: {} };

        if (opcoes.form) {
            init.body = opcoes.form;                       // multipart (fotos)
        } else if (opcoes.body !== undefined) {
            init.headers["Content-Type"] = "application/json";
            init.body = JSON.stringify(opcoes.body);
        }

        let resposta;
        try {
            resposta = await fetch(caminho, init);
        } catch (e) {
            throw new Error("Sem conexão com o servidor. Verifique sua internet e tente novamente.");
        }

        if (resposta.status === 401 && !opcoes.semRedirect) {
            window.location.replace("index.html");
            throw new Error("Sessão expirada. Faça login novamente.");
        }
        if (resposta.status === 204) return null;

        const tipo = resposta.headers.get("content-type") || "";
        const dados = tipo.includes("application/json") ? await resposta.json().catch(function () { return null; }) : null;

        if (!resposta.ok) {
            throw new Error((dados && dados.message) || ("Erro " + resposta.status + " ao falar com o servidor."));
        }
        return dados;
    };

    /* ---------------- Helpers ---------------- */
    app.esc = function (valor) {
        return String(valor == null ? "" : valor)
            .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;").replace(/'/g, "&#39;");
    };

    app.fmtData = function (iso) {
        if (!iso) return "--";
        const p = String(iso).substring(0, 10).split("-");
        return p[2] + "/" + p[1] + "/" + p[0];
    };

    app.fmtHora = function (hora) {
        return hora ? String(hora).substring(0, 5) : "";
    };

    app.hojeISO = function () {
        const d = new Date();
        const m = String(d.getMonth() + 1).padStart(2, "0");
        const dia = String(d.getDate()).padStart(2, "0");
        return d.getFullYear() + "-" + m + "-" + dia;
    };

    app.linkMapa = function (lat, lng) {
        return "https://www.google.com/maps?q=" + encodeURIComponent(lat + "," + lng);
    };

    app.obraAtualId = function () {
        const url = new URLSearchParams(window.location.search).get("obra");
        const id = url || localStorage.getItem("obraSelecionada");
        return id ? Number(id) : null;
    };

    app.abrirObra = function (id) {
        localStorage.setItem("obraSelecionada", String(id));
        window.location.href = "registros.html?obra=" + id;
    };

    app.mostrarSenha = function (campoId, botao) {
        const campo = document.getElementById(campoId);
        if (!campo) return;
        campo.type = campo.type === "password" ? "text" : "password";
        if (botao) botao.setAttribute("aria-pressed", campo.type === "text");
    };

    /* ---------------- Toast ---------------- */
    app.toast = function (mensagem, tipo) {
        let area = document.getElementById("toasts");
        if (!area) {
            area = document.createElement("div");
            area.id = "toasts";
            document.body.appendChild(area);
        }
        const el = document.createElement("div");
        el.className = "toast toast-" + (tipo || "info");
        el.textContent = mensagem;
        area.appendChild(el);
        setTimeout(function () { el.remove(); }, tipo === "erro" ? 6000 : 3500);
    };

    /* ---------------- Lightbox de fotos ---------------- */
    app.verFoto = function (url) {
        const fundo = document.createElement("div");
        fundo.className = "fixed inset-0 z-[90] bg-black/85 flex items-center justify-center p-4 cursor-zoom-out";
        fundo.innerHTML = '<img src="' + app.esc(url) + '" alt="Foto da obra" class="max-w-full max-h-full rounded-lg shadow-2xl">' +
            '<button class="absolute top-4 right-5 text-white text-3xl" aria-label="Fechar">×</button>';
        const fechar = function () { fundo.remove(); document.removeEventListener("keydown", tecla); };
        const tecla = function (e) { if (e.key === "Escape") fechar(); };
        fundo.addEventListener("click", fechar);
        document.addEventListener("keydown", tecla);
        document.body.appendChild(fundo);
    };

    /* ---------------- Sessão ---------------- */
    app.sair = async function () {
        try { await app.api("/api/usuarios/logout", { method: "POST", semRedirect: true }); } catch (e) { }
        localStorage.removeItem("obraSelecionada");
        window.location.href = "index.html";
    };

    /* ---------------- Layout (menu lateral) ---------------- */
    function montarLayout(paginaAtiva) {
        const aside = document.getElementById("sidebar");
        if (!aside) return;

        aside.className = "w-56 shrink-0 bg-[#08088F] text-white flex flex-col fixed md:sticky md:top-0 md:h-screen inset-y-0 left-0 z-40 " +
            "-translate-x-full md:translate-x-0 transition-transform duration-200";

        const itens = MENU.map(function (m) {
            const ativo = m.id === paginaAtiva ? "bg-[#5D5DF5]" : "hover:bg-blue-800 transition";
            return '<a href="' + m.href + '" class="flex items-center gap-3 px-6 py-3 ' + ativo + '">' +
                m.icone + "<span>" + m.rotulo + "</span></a>";
        }).join("");

        aside.innerHTML =
            '<div class="p-5 border-b border-blue-800">' +
            '<img src="img/logo-ellenco.png" alt="Ellenco" class="w-32 mx-auto bg-white rounded-lg p-2">' +
            "</div>" +
            '<nav class="flex-1 py-6">' + itens + "</nav>" +
            '<div class="p-5 border-t border-blue-800">' +
            '<button type="button" id="btnSair" class="flex items-center gap-3 hover:text-gray-300">🚪 <span>Sair</span></button>' +
            "</div>";

        document.getElementById("btnSair").addEventListener("click", app.sair);

        // Barra superior + menu deslizante no celular
        const principal = document.querySelector("main");
        if (principal) {
            const barra = document.createElement("div");
            barra.id = "topbarMobile";
            barra.className = "md:hidden flex items-center justify-between mb-4";
            barra.innerHTML = '<button type="button" id="btnMenu" class="text-2xl leading-none px-1" aria-label="Abrir menu">☰</button>' +
                '<img src="img/logo-ellenco.png" alt="Ellenco" class="h-8 bg-white rounded p-1">';
            principal.prepend(barra);

            const overlay = document.createElement("div");
            overlay.id = "overlayMenu";
            overlay.className = "fixed inset-0 bg-black/40 z-30 hidden";
            document.body.appendChild(overlay);

            const alternar = function (abrir) {
                aside.classList.toggle("-translate-x-full", !abrir);
                overlay.classList.toggle("hidden", !abrir);
            };
            document.getElementById("btnMenu").addEventListener("click", function () { alternar(true); });
            overlay.addEventListener("click", function () { alternar(false); });
        }
    }

    function preencherUsuario(u) {
        document.querySelectorAll("[data-user-nome]").forEach(function (el) { el.textContent = u.nome; });
        document.querySelectorAll("[data-user-cargo]").forEach(function (el) { el.textContent = u.cargo || ""; });
    }

    /** Páginas protegidas: monta o menu e confirma o login antes de carregar os dados. */
    app.init = async function (paginaAtiva) {
        montarLayout(paginaAtiva);
        app.usuario = await app.api("/api/usuarios/me");
        preencherUsuario(app.usuario);
        return app.usuario;
    };

    return app;
})();

/* Funções chamadas direto pelo HTML (onclick) */
function sair() { App.sair(); }
function mostrarSenha(id, botao) { App.mostrarSenha(id, botao); }
