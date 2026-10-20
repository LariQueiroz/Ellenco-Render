/* =====================================================
   ELLENCO - DASHBOARD
===================================================== */
(function () {
    const esc = App.esc;
    const el = function (id) { return document.getElementById(id); };

    function kpi(icone, valor, rotulo, destaque) {
        return '<div class="bg-white rounded-xl shadow-sm border p-5">' +
            '<p class="text-sm text-gray-500">' + icone + " " + rotulo + "</p>" +
            '<p class="text-3xl font-bold mt-2 ' + (destaque ? "text-[#08088F]" : "text-gray-800") + '">' + valor + "</p></div>";
    }

    function renderKpis(t) {
        el("kpis").innerHTML =
            kpi("🏗️", t.emAndamento, "Obras em andamento", true) +
            kpi("✅", t.finalizadas, "Obras finalizadas") +
            kpi("📝", t.registrosSemana, "Registros nesta semana", true) +
            kpi("📷", t.fotos, "Fotos registradas");
    }

    function renderAlerta(obras) {
        const pendentes = obras.filter(function (o) { return o.semRegistroRecente; });
        const caixa = el("alerta");
        if (!pendentes.length) { caixa.classList.add("hidden"); return; }
        caixa.classList.remove("hidden");
        caixa.innerHTML = "<strong>Atenção:</strong> " + pendentes.length + (pendentes.length === 1 ? " obra está" : " obras estão") +
            " sem registro nos últimos 7 dias — " +
            pendentes.map(function (o) {
                return '<a href="#" data-obra="' + o.id + '" class="underline font-medium">' + esc(o.nome) + "</a>";
            }).join(", ") + ".";
    }

    function renderObras(obras) {
        if (!obras.length) {
            el("obrasAndamento").innerHTML = '<p class="text-gray-500 text-sm">Nenhuma obra em andamento. <a href="obras.html" class="text-blue-600 underline">Cadastrar obra</a></p>';
            return;
        }
        el("obrasAndamento").innerHTML = obras.map(function (o) {
            const flags = [];
            if (o.atrasada) flags.push('<span class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-700 border border-red-200">Atrasada</span>');
            if (o.semRegistroRecente) flags.push('<span class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-800 border border-amber-200">Sem registro recente</span>');
            const ultimo = o.ultimoRegistro ? "Último registro: " + App.fmtData(o.ultimoRegistro) : "Nenhum registro ainda";
            return '<div>' +
                '<div class="flex justify-between items-start gap-3 mb-2">' +
                '<div><a href="#" data-obra="' + o.id + '" class="font-semibold text-gray-800 hover:underline">' + esc(o.nome) + "</a>" +
                '<p class="text-xs text-gray-500">' + esc(o.localizacao || "") + (o.localizacao ? " · " : "") + ultimo + "</p></div>" +
                '<div class="flex gap-1 flex-wrap justify-end">' + flags.join("") + "<strong>" + o.progresso + "%</strong></div></div>" +
                '<div class="w-full bg-gray-200 rounded-full h-2"><div class="bg-[#08088F] h-2 rounded-full" style="width:' + o.progresso + '%"></div></div>' +
                "</div>";
        }).join("");
    }

    function renderSemanas(semanas) {
        const max = Math.max.apply(null, semanas.map(function (s) { return s.total; }).concat([1]));
        el("graficoSemanas").innerHTML = semanas.map(function (s) {
            const altura = Math.max(4, Math.round((s.total / max) * 100));
            return '<div class="flex-1 flex flex-col items-center justify-end h-full">' +
                '<span class="text-xs text-gray-600 mb-1">' + s.total + "</span>" +
                '<div class="w-full bg-[#08088F] rounded-t" style="height:' + altura + '%" title="Semana de ' + App.fmtData(s.inicio) + '"></div>' +
                '<span class="text-[10px] text-gray-500 mt-1">' + s.inicio.substring(8, 10) + "/" + s.inicio.substring(5, 7) + "</span></div>";
        }).join("");
    }

    function barras(id, lista) {
        const area = el(id);
        if (!lista.length) { area.innerHTML = '<p class="text-gray-500 text-sm">Sem dados ainda.</p>'; return; }
        const total = lista.reduce(function (a, c) { return a + c.total; }, 0);
        area.innerHTML = lista.map(function (c) {
            const pct = Math.round((c.total / total) * 100);
            return "<div>" +
                '<div class="flex justify-between text-sm mb-1"><span>' + esc(c.nome) + "</span><span class=\"text-gray-500\">" + c.total + " (" + pct + "%)</span></div>" +
                '<div class="w-full bg-gray-200 rounded-full h-2"><div class="bg-[#5D5DF5] h-2 rounded-full" style="width:' + pct + '%"></div></div></div>';
        }).join("");
    }

    function renderUltimos(registros) {
        if (!registros.length) {
            el("ultimos").innerHTML = '<p class="text-gray-500 text-sm">Nenhum registro ainda.</p>';
            return;
        }
        el("ultimos").innerHTML = registros.map(function (r) {
            const foto = r.fotos.length
                ? '<img src="' + esc(r.fotos[0].url) + '" alt="" loading="lazy" class="w-14 h-14 rounded-lg object-cover border shrink-0">'
                : '<div class="w-14 h-14 rounded-lg bg-gray-100 flex items-center justify-center shrink-0">📝</div>';
            return '<a href="#" data-obra="' + r.obraId + '" class="flex gap-3 items-center">' + foto +
                '<div class="min-w-0"><p class="text-sm font-semibold text-gray-800 truncate">' + esc(r.obraNome) + "</p>" +
                '<p class="text-xs text-gray-500">' + App.fmtData(r.data) + (r.turno ? " · " + esc(r.turno) : "") + "</p>" +
                '<p class="text-xs text-gray-600 truncate">' + esc(r.descricao) + "</p></div></a>";
        }).join("");
    }

    function renderMapa(pontos) {
        if (!pontos.length || typeof L === "undefined") {
            el("mapa").classList.add("hidden");
            el("mapaVazio").classList.remove("hidden");
            if (typeof L === "undefined" && pontos.length) {
                el("mapaVazio").textContent = "Não foi possível carregar o mapa (verifique a conexão com a internet).";
            }
            return;
        }
        const mapa = L.map("mapa").setView([pontos[0].latitude, pontos[0].longitude], 12);
        L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
            maxZoom: 19,
            attribution: "&copy; OpenStreetMap"
        }).addTo(mapa);

        const limites = [];
        pontos.forEach(function (p) {
            limites.push([p.latitude, p.longitude]);
            L.marker([p.latitude, p.longitude]).addTo(mapa)
                .bindPopup("<strong>" + esc(p.obraNome) + "</strong><br>" + App.fmtData(p.data) +
                    '<br><a href="' + App.linkMapa(p.latitude, p.longitude) + '" target="_blank" rel="noopener">Abrir no Google Maps</a>');
        });
        if (limites.length > 1) mapa.fitBounds(limites, { padding: [40, 40] });
    }

    document.addEventListener("click", function (e) {
        const link = e.target.closest("[data-obra]");
        if (link) { e.preventDefault(); App.abrirObra(link.dataset.obra); }
    });

    App.init("dashboard")
        .then(function () { return App.api("/api/dashboard"); })
        .then(function (d) {
            renderKpis(d.totais);
            renderAlerta(d.obrasEmAndamento);
            renderObras(d.obrasEmAndamento);
            renderSemanas(d.registrosPorSemana);
            barras("graficoClima", d.clima);
            barras("graficoTurno", d.turno);
            renderUltimos(d.ultimosRegistros);
            renderMapa(d.pontos);
        })
        .catch(function (e) {
            if (e && e.message && !/Sessão expirada/.test(e.message)) App.toast(e.message, "erro");
        });
})();
