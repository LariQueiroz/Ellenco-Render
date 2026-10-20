/* =====================================================
   ELLENCO - RELATÓRIO SEMANAL
   Semana de segunda a domingo, navegável, pronta para imprimir.
===================================================== */
(function () {
    const esc = App.esc;
    const el = function (id) { return document.getElementById(id); };

    const DIAS = ["dom", "seg", "ter", "qua", "qui", "sex", "sáb"];

    let obraId = null;
    let inicio = null;   // qualquer data (ISO) dentro da semana desejada
    let dados = null;

    /* ---------------- datas (sem fuso: trabalha só com ISO yyyy-mm-dd) ---------------- */
    function addDias(iso, n) {
        const p = iso.split("-").map(Number);
        const d = new Date(Date.UTC(p[0], p[1] - 1, p[2] + n));
        return d.toISOString().substring(0, 10);
    }
    function diaSemana(iso) {
        const p = iso.split("-").map(Number);
        return DIAS[new Date(Date.UTC(p[0], p[1] - 1, p[2])).getUTCDay()];
    }
    function curto(iso) { return iso.substring(8, 10) + "/" + iso.substring(5, 7); }

    /* ---------------- carregamento ---------------- */
    function atualizarUrl() {
        const q = new URLSearchParams();
        if (obraId) q.set("obra", obraId);
        if (inicio) q.set("inicio", inicio);
        window.history.replaceState(null, "", "relatorio.html?" + q.toString());
    }

    async function buscar() {
        return App.api("/api/obras/" + obraId + "/relatorio-semanal" + (inicio ? "?inicio=" + inicio : ""));
    }

    async function carregar() {
        try {
            let r = await buscar();
            // sem semana escolhida e semana atual vazia: abre na semana do último registro
            if (!inicio && r.totalRegistros === 0 && r.obra.ultimoRegistro) {
                inicio = r.obra.ultimoRegistro;
                r = await buscar();
            }
            dados = r;
            inicio = r.inicio;
            localStorage.setItem("obraSelecionada", String(obraId));
            atualizarUrl();
            render();
        } catch (e) {
            App.toast(e.message, "erro");
            el("resumo").textContent = e.message;
        }
    }

    /* ---------------- render ---------------- */
    function escolherEvolucao(registros) {
        const todas = [];
        registros.forEach(function (r) {
            r.fotos.forEach(function (f) { todas.push({ url: f.url, data: r.data, turno: r.turno }); });
        });
        if (todas.length <= 3) return todas;
        const idx = [0, Math.floor((todas.length - 1) / 2), todas.length - 1];
        return idx.map(function (i) { return todas[i]; });
    }

    function renderResumo(d) {
        const area = el("resumo");
        if (!d.totalRegistros) {
            area.innerHTML = "<p>Não há registros lançados para esta semana.</p>";
            return;
        }
        const itens = d.registros.slice(0, 8).map(function (r) {
            const texto = r.descricao.length > 200 ? r.descricao.substring(0, 200) + "…" : r.descricao;
            return "<li><strong>" + curto(r.data) + "</strong> — " + esc(texto) + "</li>";
        }).join("");
        const extra = d.registros.length > 8 ? '<p class="text-sm text-gray-500 mt-2">+ ' + (d.registros.length - 8) + " registro(s) não listados aqui.</p>" : "";

        area.innerHTML =
            "<p>Na semana de " + App.fmtData(d.inicio) + " a " + App.fmtData(d.fim) + " foram realizados <strong>" +
            d.totalRegistros + "</strong> registro(s) em <strong>" + d.diasTrabalhados + "</strong> dia(s) de trabalho, com <strong>" +
            d.totalFotos + "</strong> foto(s) anexada(s).</p>" +
            '<ul class="list-disc ml-5 mt-3 space-y-1 text-sm">' + itens + "</ul>" + extra;
    }

    function renderRegistros(d) {
        const area = el("registrosSemana");
        if (!d.registros.length) {
            area.innerHTML = '<p class="text-gray-500 col-span-full">Nenhum registro nesta semana.</p>';
            return;
        }
        area.innerHTML = d.registros.map(function (r) {
            const foto = r.fotos.length
                ? '<div class="relative"><img src="' + esc(r.fotos[0].url) + '" alt="Foto do registro" loading="lazy" data-foto="' + esc(r.fotos[0].url) + '" class="w-full h-24 object-cover foto-thumb">' +
                  (r.fotos.length > 1 ? '<span class="absolute bottom-1 right-1 bg-black/60 text-white text-[10px] px-1.5 py-0.5 rounded">📷 ' + r.fotos.length + "</span>" : "") + "</div>"
                : '<div class="w-full h-24 bg-gray-100 flex items-center justify-center text-2xl">📝</div>';
            const mapa = (r.latitude != null && r.longitude != null)
                ? ' · <a href="' + App.linkMapa(r.latitude, r.longitude) + '" target="_blank" rel="noopener" class="text-blue-600 no-print">📍</a>' : "";
            return '<div class="border rounded-lg overflow-hidden break-avoid">' + foto +
                '<div class="p-2 text-xs">' +
                '<p class="font-semibold text-gray-800">' + curto(r.data) + " · " + diaSemana(r.data) + mapa + "</p>" +
                '<p class="text-gray-500">' + esc([r.turno, r.clima].filter(Boolean).join(" · ")) + "</p>" +
                '<p class="text-gray-600 line-clamp-2 mt-1">' + esc(r.descricao) + "</p>" +
                "</div></div>";
        }).join("");
    }

    function renderEvolucao(d) {
        const area = el("evolucao");
        const fotos = escolherEvolucao(d.registros);
        if (!fotos.length) {
            area.innerHTML = '<p class="text-gray-500 col-span-full">Nenhuma foto registrada nesta semana.</p>';
            return;
        }
        area.innerHTML = fotos.map(function (f) {
            return '<figure class="break-avoid"><img src="' + esc(f.url) + '" alt="Evolução da obra" loading="lazy" data-foto="' + esc(f.url) + '" class="w-full h-48 object-cover rounded-lg border foto-thumb">' +
                '<figcaption class="text-xs text-gray-500 mt-1">' + App.fmtData(f.data) + (f.turno ? " · " + esc(f.turno) : "") + "</figcaption></figure>";
        }).join("");
    }

    function render() {
        const d = dados;
        const o = d.obra;

        el("nomeObra").textContent = o.nome + (o.localizacao ? " — " + o.localizacao : "") + (o.status === "FINALIZADA" ? " (obra finalizada)" : "");
        el("periodo").textContent = "Semana de " + App.fmtData(d.inicio) + " a " + App.fmtData(d.fim);

        // progresso
        const progresso = d.progressoAtual;
        el("porcentagem").textContent = progresso != null ? progresso + "%" : "--";
        el("barraProgresso").style.width = (progresso != null ? progresso : 0) + "%";
        el("variacao").textContent = (d.variacaoProgresso != null && d.totalRegistros)
            ? (d.variacaoProgresso >= 0 ? "+" : "") + d.variacaoProgresso + " p.p. em relação à semana anterior"
            : (progresso == null ? "Progresso ainda não informado nos registros." : "");
        el("diasTrabalhados").textContent = d.diasTrabalhados;
        el("quantidadeRegistros").textContent = d.totalRegistros;

        renderResumo(d);
        renderRegistros(d);
        renderEvolucao(d);

        el("responsavel").textContent = o.responsavel || "Responsável pela obra";
        el("dataEmissao").textContent = App.fmtData(App.hojeISO());

        // navegação entre semanas
        const fimAnterior = addDias(d.semanaAnterior, 6);
        el("btnAnterior").disabled = !d.primeiroRegistro || fimAnterior < d.primeiroRegistro;
        el("btnProxima").disabled = !d.proximaSemana;
        [el("btnAnterior"), el("btnProxima")].forEach(function (b) { b.classList.toggle("opacity-40", b.disabled); });
        el("selObra").value = String(o.id);
    }

    /* ---------------- eventos ---------------- */
    el("btnAnterior").addEventListener("click", function () { if (dados) { inicio = dados.semanaAnterior; carregar(); } });
    el("btnProxima").addEventListener("click", function () { if (dados && dados.proximaSemana) { inicio = dados.proximaSemana; carregar(); } });
    el("btnHoje").addEventListener("click", function () { inicio = App.hojeISO(); carregar(); });
    el("selObra").addEventListener("change", function (e) {
        obraId = Number(e.target.value);
        inicio = null;
        carregar();
    });

    document.addEventListener("click", function (e) {
        const foto = e.target.closest("[data-foto]");
        if (foto) App.verFoto(foto.dataset.foto);
    });

    window.voltarRegistro = function () {
        window.location.href = obraId ? "registros.html?obra=" + obraId : "obras.html";
    };

    /* ---------------- início ---------------- */
    (async function iniciar() {
        try { await App.init("relatorio"); } catch (e) { return; }

        let obras;
        try { obras = await App.api("/api/obras"); } catch (e) { App.toast(e.message, "erro"); return; }

        if (!obras.length) {
            el("nomeObra").textContent = "Nenhuma obra cadastrada.";
            el("resumo").innerHTML = '<p>Cadastre uma obra em <a class="text-blue-600 underline" href="obras.html">Obras</a> para gerar relatórios.</p>';
            return;
        }

        el("selObra").innerHTML = obras.map(function (o) {
            return '<option value="' + o.id + '">' + esc(o.nome) + "</option>";
        }).join("");

        const params = new URLSearchParams(window.location.search);
        const pedida = App.obraAtualId();
        obraId = obras.some(function (o) { return o.id === pedida; }) ? pedida : obras[0].id;
        inicio = params.get("inicio");
        await carregar();
    })();
})();
