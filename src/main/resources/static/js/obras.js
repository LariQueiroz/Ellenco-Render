/* =====================================================
   ELLENCO - OBRAS (listagem + cadastro/edição)
===================================================== */
(function () {
    const esc = App.esc;
    let obras = [];
    let filtro = "TODAS";
    let editandoId = null;

    const lista = document.getElementById("listaObras");
    const modal = document.getElementById("modalObra");

    /* ---------------- Render ---------------- */
    function badge(o) {
        if (o.status === "FINALIZADA") {
            return '<span class="text-xs px-2 py-0.5 rounded-full bg-green-50 text-green-700 border border-green-200">Finalizada</span>';
        }
        if (o.atrasada) {
            return '<span class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-700 border border-red-200">Atrasada</span>';
        }
        return '<span class="text-xs px-2 py-0.5 rounded-full bg-blue-50 text-blue-700 border border-blue-200">Em andamento</span>';
    }

    function cardObra(o) {
        const capa = o.capaUrl
            ? '<img src="' + esc(o.capaUrl) + '" alt="' + esc(o.nome) + '" class="w-full h-40 object-cover" loading="lazy">'
            : '<div class="w-full h-40 bg-gray-100 flex items-center justify-center text-5xl" aria-hidden="true">🏗️</div>';

        const registros = o.totalRegistros
            ? o.totalRegistros + (o.totalRegistros === 1 ? " registro" : " registros") + " · último em " + App.fmtData(o.ultimoRegistro)
            : "Nenhum registro ainda";

        return '<div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">' +
            capa +
            '<div class="p-5">' +
            '<div class="flex justify-between items-start gap-2">' +
            '<h2 class="font-bold text-gray-800">' + esc(o.nome) + "</h2>" + badge(o) +
            "</div>" +
            '<p class="text-sm text-gray-500 mt-1">' + esc(o.localizacao || "Localização não informada") + "</p>" +
            '<div class="mt-5">' +
            '<div class="flex justify-between text-sm mb-2"><span>Progresso</span><strong>' + o.progresso + "%</strong></div>" +
            '<div class="w-full bg-gray-200 rounded-full h-2"><div class="bg-[#08088F] h-2 rounded-full" style="width: ' + o.progresso + '%"></div></div>' +
            "</div>" +
            '<p class="text-xs text-gray-500 mt-3">' + registros + "</p>" +
            '<button data-abrir="' + o.id + '" class="w-full mt-4 bg-[#08088F] hover:bg-blue-800 text-white py-2.5 rounded-lg transition">Abrir obra</button>' +
            '<button data-editar="' + o.id + '" class="w-full mt-2 text-sm text-gray-500 hover:underline">Editar obra</button>' +
            "</div></div>";
    }

    function render() {
        // contadores nas abas de filtro
        const contagem = {
            TODAS: obras.length,
            EM_ANDAMENTO: obras.filter(function (o) { return o.status !== "FINALIZADA"; }).length,
            FINALIZADA: obras.filter(function (o) { return o.status === "FINALIZADA"; }).length
        };
        document.querySelectorAll("#filtros [data-filtro]").forEach(function (b) {
            const f = b.dataset.filtro;
            const rotulo = { TODAS: "Todas", EM_ANDAMENTO: "Em andamento", FINALIZADA: "Finalizadas" }[f];
            b.textContent = rotulo + " (" + contagem[f] + ")";
            const ativo = f === filtro;
            b.className = "px-4 py-1.5 rounded-full text-sm border " +
                (ativo ? "bg-[#08088F] text-white border-[#08088F]" : "bg-white text-gray-600 hover:bg-gray-100");
        });

        const visiveis = obras.filter(function (o) {
            if (filtro === "TODAS") return true;
            return filtro === "FINALIZADA" ? o.status === "FINALIZADA" : o.status !== "FINALIZADA";
        });

        if (!visiveis.length) {
            lista.innerHTML = '<div class="col-span-full bg-white border border-dashed rounded-xl p-10 text-center text-gray-500">' +
                (obras.length
                    ? "Nenhuma obra neste filtro."
                    : 'Nenhuma obra cadastrada ainda.<br>Clique em <strong>＋ Nova obra</strong> para começar.') +
                "</div>";
            return;
        }
        lista.innerHTML = visiveis.map(cardObra).join("");
    }

    async function carregar() {
        try {
            obras = await App.api("/api/obras");
            render();
        } catch (e) {
            lista.innerHTML = '<p class="col-span-full text-red-600">' + esc(e.message) + "</p>";
        }
    }

    /* ---------------- Modal ---------------- */
    function abrirModal(obra) {
        editandoId = obra ? obra.id : null;
        document.getElementById("tituloModal").textContent = obra ? "Editar obra" : "Nova obra";
        document.getElementById("obraNome").value = obra ? obra.nome : "";
        document.getElementById("obraLocal").value = obra ? (obra.localizacao || "") : "";
        document.getElementById("obraResp").value = obra ? (obra.responsavel || "") : "";
        document.getElementById("obraInicio").value = obra ? (obra.dataInicio || "") : "";
        document.getElementById("obraPrevisao").value = obra ? (obra.previsaoTermino || "") : "";
        document.getElementById("obraDescricao").value = obra ? (obra.descricao || "") : "";
        document.getElementById("btnExcluirObra").classList.toggle("hidden", !obra);
        modal.classList.remove("hidden");
        modal.classList.add("flex");
        document.getElementById("obraNome").focus();
    }

    function fecharModal() {
        modal.classList.add("hidden");
        modal.classList.remove("flex");
        editandoId = null;
    }

    async function salvarObra(event) {
        event.preventDefault();
        const botao = document.getElementById("btnSalvarObra");
        const valor = function (id) { return document.getElementById(id).value.trim(); };

        const corpo = {
            nome: valor("obraNome"),
            localizacao: valor("obraLocal") || null,
            responsavel: valor("obraResp") || null,
            descricao: valor("obraDescricao") || null,
            dataInicio: valor("obraInicio") || null,
            previsaoTermino: valor("obraPrevisao") || null
        };

        botao.disabled = true;
        try {
            if (editandoId) {
                await App.api("/api/obras/" + editandoId, { method: "PUT", body: corpo });
                App.toast("Obra atualizada.", "ok");
            } else {
                await App.api("/api/obras", { method: "POST", body: corpo });
                App.toast("Obra cadastrada.", "ok");
            }
            fecharModal();
            await carregar();
        } catch (e) {
            App.toast(e.message, "erro");
        } finally {
            botao.disabled = false;
        }
    }

    async function excluirObra() {
        const obra = obras.find(function (o) { return o.id === editandoId; });
        if (!obra) return;
        const aviso = "Excluir a obra \"" + obra.nome + "\"?\n\nTodos os registros e fotos dela serão apagados e isso não pode ser desfeito.";
        if (!window.confirm(aviso)) return;
        try {
            await App.api("/api/obras/" + obra.id, { method: "DELETE" });
            App.toast("Obra excluída.", "ok");
            fecharModal();
            await carregar();
        } catch (e) {
            App.toast(e.message, "erro");
        }
    }

    /* ---------------- Eventos ---------------- */
    lista.addEventListener("click", function (e) {
        const abrir = e.target.closest("[data-abrir]");
        if (abrir) { App.abrirObra(abrir.dataset.abrir); return; }
        const editar = e.target.closest("[data-editar]");
        if (editar) {
            const obra = obras.find(function (o) { return String(o.id) === editar.dataset.editar; });
            if (obra) abrirModal(obra);
        }
    });

    document.getElementById("filtros").addEventListener("click", function (e) {
        const b = e.target.closest("[data-filtro]");
        if (b) { filtro = b.dataset.filtro; render(); }
    });

    document.getElementById("btnNovaObra").addEventListener("click", function () { abrirModal(null); });
    document.getElementById("btnCancelarObra").addEventListener("click", fecharModal);
    document.getElementById("btnExcluirObra").addEventListener("click", excluirObra);
    document.getElementById("formObra").addEventListener("submit", salvarObra);
    modal.addEventListener("mousedown", function (e) { if (e.target === modal) fecharModal(); });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape" && !modal.classList.contains("hidden")) fecharModal(); });

    /* ---------------- Início ---------------- */
    App.init("obras").then(carregar).catch(function () { /* redirecionado ao login */ });

    // compatibilidade com chamadas antigas do HTML
    window.abrirObra = App.abrirObra;
})();
