/* =====================================================
   ELLENCO - REGISTRO DIÁRIO DA OBRA
   Formulário, fotos, localização online (GPS), histórico
   e finalização da obra.
===================================================== */
(function () {
    const esc = App.esc;
    const MAX_FOTOS = 20;

    let obra = null;
    let fotos = [];          // { file, url }
    let coordenadas = null;  // { lat, lng, precisao }
    let salvando = false;

    const el = function (id) { return document.getElementById(id); };

    /* =====================================================
       OBRA
    ===================================================== */
    function mostrarObra() {
        el("nomeObra").textContent = obra.nome;

        const partes = [];
        if (obra.localizacao) partes.push(obra.localizacao);
        if (obra.responsavel) partes.push("Resp.: " + obra.responsavel);
        partes.push("Progresso atual: " + obra.progresso + "%");
        el("localObra").textContent = partes.join(" · ");

        const finalizada = obra.status === "FINALIZADA";
        el("cardForm").classList.toggle("hidden", finalizada);
        el("bannerFinalizada").classList.toggle("hidden", !finalizada);
        if (finalizada) {
            el("dataFinalizacao").textContent = obra.dataFinalizacao ? "em " + App.fmtData(obra.dataFinalizacao) : "";
        }
        el("progresso").placeholder = "Atual: " + obra.progresso;
    }

    async function carregarObra() {
        obra = await App.api("/api/obras/" + obra.id);
        mostrarObra();
    }

    function valoresPadrao() {
        const agora = new Date();
        el("data").value = App.hojeISO();
        el("data").max = App.hojeISO();
        el("horario").value = String(agora.getHours()).padStart(2, "0") + ":" + String(agora.getMinutes()).padStart(2, "0");
    }

    /* =====================================================
       FOTOS
    ===================================================== */
    // Reduz fotos de celular (vários MB) antes de enviar: mais rápido e cabe melhor no banco.
    async function otimizarImagem(arquivo) {
        const LADO_MAX = 1600;
        if (!arquivo.type.startsWith("image/") || arquivo.type === "image/gif") return arquivo;
        try {
            const bmp = await createImageBitmap(arquivo, { imageOrientation: "from-image" });
            const escala = Math.min(1, LADO_MAX / Math.max(bmp.width, bmp.height));
            const canvas = document.createElement("canvas");
            canvas.width = Math.round(bmp.width * escala);
            canvas.height = Math.round(bmp.height * escala);
            canvas.getContext("2d").drawImage(bmp, 0, 0, canvas.width, canvas.height);
            if (bmp.close) bmp.close();
            const blob = await new Promise(function (ok) { canvas.toBlob(ok, "image/jpeg", 0.82); });
            if (!blob || (escala === 1 && blob.size >= arquivo.size)) return arquivo;
            const nome = (arquivo.name || "foto").replace(/\.[^.]+$/, "") + ".jpg";
            return new File([blob], nome, { type: "image/jpeg", lastModified: Date.now() });
        } catch (e) {
            return arquivo; // formato não decodificável no navegador (ex.: HEIC): envia original
        }
    }

    function renderFotos() {
        const grade = el("previewFotos");
        grade.innerHTML = fotos.map(function (f, i) {
            return '<div class="relative">' +
                '<img src="' + f.url + '" alt="Foto ' + (i + 1) + '" class="w-full h-28 object-cover rounded-lg border foto-thumb" data-ver="' + i + '">' +
                '<button type="button" data-remover="' + i + '" aria-label="Remover foto" ' +
                'class="absolute top-1 right-1 bg-black/60 text-white rounded-full w-6 h-6 text-xs leading-6 text-center">✕</button>' +
                "</div>";
        }).join("");
        el("infoFotos").textContent = fotos.length
            ? fotos.length + (fotos.length === 1 ? " foto selecionada" : " fotos selecionadas") + " (máx. " + MAX_FOTOS + ")"
            : "";
    }

    function limparFotos() {
        fotos.forEach(function (f) { URL.revokeObjectURL(f.url); });
        fotos = [];
        renderFotos();
    }

    el("fotos").addEventListener("change", async function (e) {
        const escolhidas = Array.from(e.target.files || []);
        e.target.value = ""; // permite escolher a mesma foto de novo e ir somando fotos
        for (const arquivo of escolhidas) {
            if (fotos.length >= MAX_FOTOS) {
                App.toast("Limite de " + MAX_FOTOS + " fotos por registro.", "erro");
                break;
            }
            if (!arquivo.type.startsWith("image/")) {
                App.toast(arquivo.name + " não é uma imagem.", "erro");
                continue;
            }
            const otimizada = await otimizarImagem(arquivo);
            fotos.push({ file: otimizada, url: URL.createObjectURL(otimizada) });
            renderFotos();
        }
    });

    el("previewFotos").addEventListener("click", function (e) {
        const rem = e.target.closest("[data-remover]");
        if (rem) {
            const i = Number(rem.dataset.remover);
            URL.revokeObjectURL(fotos[i].url);
            fotos.splice(i, 1);
            renderFotos();
            return;
        }
        const ver = e.target.closest("[data-ver]");
        if (ver) App.verFoto(fotos[Number(ver.dataset.ver)].url);
    });

    /* =====================================================
       LOCALIZAÇÃO ONLINE (GPS do aparelho)
    ===================================================== */
    window.obterLocalizacao = function () {
        const status = el("statusLocalizacao");
        const campo = el("localizacao");
        const botao = el("btnLocalizacao");

        if (!("geolocation" in navigator)) {
            status.textContent = "Este navegador não suporta geolocalização.";
            return;
        }
        if (!window.isSecureContext) {
            status.textContent = "A localização só funciona em conexão segura (HTTPS).";
            return;
        }

        status.textContent = "Obtendo localização...";
        botao.disabled = true;

        navigator.geolocation.getCurrentPosition(
            function (pos) {
                const lat = pos.coords.latitude;
                const lng = pos.coords.longitude;
                coordenadas = { lat: lat, lng: lng, precisao: Math.round(pos.coords.accuracy) };
                campo.value = "Latitude: " + lat.toFixed(6) + " | Longitude: " + lng.toFixed(6);
                status.innerHTML = "Localização obtida com sucesso (precisão de ±" + coordenadas.precisao + " m). " +
                    '<a href="' + App.linkMapa(lat, lng) + '" target="_blank" rel="noopener" class="text-blue-600 hover:underline">Ver no mapa</a>';
                botao.disabled = false;
            },
            function (erro) {
                const mensagens = {
                    1: "Permissão negada. Libere o acesso à localização no navegador.",
                    2: "Não foi possível determinar sua posição. Verifique o GPS/sinal.",
                    3: "Tempo esgotado ao obter a localização. Tente novamente."
                };
                status.textContent = mensagens[erro.code] || "Erro ao obter localização.";
                botao.disabled = false;
            },
            { enableHighAccuracy: true, timeout: 20000, maximumAge: 0 }
        );
    };

    /* =====================================================
       SALVAR REGISTRO
    ===================================================== */
    window.salvarRegistro = async function () {
        if (salvando) return;

        const data = el("data").value;
        const horario = el("horario").value;
        const turno = el("turno").value;
        const clima = el("clima").value;
        const descricao = el("descricao").value.trim();
        const progresso = el("progresso").value.trim();

        if (!data || !turno || !clima || !descricao) {
            App.toast("Preencha data, turno, clima e o relato do dia.", "erro");
            return;
        }
        if (progresso !== "" && (Number(progresso) < 0 || Number(progresso) > 100 || !Number.isInteger(Number(progresso)))) {
            App.toast("O progresso deve ser um número inteiro entre 0 e 100.", "erro");
            return;
        }

        const form = new FormData();
        form.append("obraId", obra.id);
        form.append("data", data);
        if (horario) form.append("horario", horario);
        form.append("turno", turno);
        form.append("clima", clima);
        form.append("descricao", descricao);
        if (progresso !== "") form.append("progresso", progresso);
        if (coordenadas) {
            form.append("latitude", coordenadas.lat);
            form.append("longitude", coordenadas.lng);
        }
        fotos.forEach(function (f) { form.append("fotos", f.file, f.file.name); });

        const botao = el("btnSalvar");
        const textoOriginal = botao.textContent;
        salvando = true;
        botao.disabled = true;
        botao.textContent = "Salvando...";

        try {
            await App.api("/api/registros", { method: "POST", form: form });
            App.toast("Registro salvo com sucesso!", "ok");

            // limpa apenas o que é específico do registro
            el("descricao").value = "";
            el("progresso").value = "";
            el("localizacao").value = "";
            el("statusLocalizacao").textContent = "";
            coordenadas = null;
            limparFotos();
            valoresPadrao();

            await Promise.all([carregarObra(), carregarHistorico()]);
        } catch (e) {
            App.toast(e.message, "erro");
        } finally {
            salvando = false;
            botao.disabled = false;
            botao.textContent = textoOriginal;
        }
    };

    /* =====================================================
       HISTÓRICO (1 obra : N registros)
    ===================================================== */
    function cardRegistro(r) {
        const meta = [r.turno, r.clima];
        if (r.progresso != null) meta.push("Progresso " + r.progresso + "%");
        if (r.autor) meta.push("por " + r.autor);

        const fotosHtml = r.fotos.length
            ? '<div class="grid grid-cols-3 md:grid-cols-6 gap-2 mt-3">' + r.fotos.map(function (f) {
                return '<img src="' + esc(f.url) + '" alt="Foto do registro" loading="lazy" data-foto="' + esc(f.url) + '" ' +
                    'class="w-full h-20 object-cover rounded-lg border foto-thumb">';
            }).join("") + "</div>"
            : "";

        const mapa = (r.latitude != null && r.longitude != null)
            ? '<a href="' + App.linkMapa(r.latitude, r.longitude) + '" target="_blank" rel="noopener" class="text-sm text-blue-600 hover:underline">📍 Ver localização no mapa</a>'
            : "";

        return '<div class="border rounded-xl p-4">' +
            '<div class="flex justify-between items-start gap-3">' +
            "<div>" +
            '<p class="font-semibold text-gray-800">' + App.fmtData(r.data) + (r.horario ? " · " + App.fmtHora(r.horario) : "") + "</p>" +
            '<p class="text-xs text-gray-500 mt-0.5">' + esc(meta.filter(Boolean).join(" · ")) + "</p>" +
            "</div>" +
            '<button type="button" data-excluir="' + r.id + '" class="text-red-600 text-sm hover:underline">Excluir</button>' +
            "</div>" +
            '<p class="text-sm text-gray-700 mt-3 whitespace-pre-line">' + esc(r.descricao) + "</p>" +
            fotosHtml +
            (mapa ? '<div class="mt-3">' + mapa + "</div>" : "") +
            "</div>";
    }

    async function carregarHistorico() {
        const area = el("historico");
        try {
            const registros = await App.api("/api/registros/obra/" + obra.id);
            el("totalRegistros").textContent = registros.length + (registros.length === 1 ? " registro" : " registros");
            area.innerHTML = registros.length
                ? registros.map(cardRegistro).join("")
                : '<p class="text-gray-500 text-sm">Nenhum registro lançado para esta obra ainda.</p>';
        } catch (e) {
            area.innerHTML = '<p class="text-red-600 text-sm">' + esc(e.message) + "</p>";
        }
    }

    el("historico").addEventListener("click", async function (e) {
        const foto = e.target.closest("[data-foto]");
        if (foto) { App.verFoto(foto.dataset.foto); return; }

        const excluir = e.target.closest("[data-excluir]");
        if (excluir) {
            if (!window.confirm("Excluir este registro e as fotos dele? Isso não pode ser desfeito.")) return;
            try {
                await App.api("/api/registros/" + excluir.dataset.excluir, { method: "DELETE" });
                App.toast("Registro excluído.", "ok");
                await Promise.all([carregarObra(), carregarHistorico()]);
            } catch (err) {
                App.toast(err.message, "erro");
            }
        }
    });

    /* =====================================================
       AÇÕES DA TELA
    ===================================================== */
    window.voltarObras = function () { window.location.href = "obras.html"; };

    window.elaborarRelatorio = function () {
        if (!obra.totalRegistros) {
            App.toast("Salve pelo menos um registro antes de elaborar o relatório.", "erro");
            return;
        }
        // abre na semana do último registro lançado
        window.location.href = "relatorio.html?obra=" + obra.id + "&inicio=" + obra.ultimoRegistro;
    };

    window.finalizarObra = async function () {
        const aviso = "Finalizar a obra \"" + obra.nome + "\"?\n\nDepois disso não será possível lançar novos registros (você poderá reabrir a obra se precisar).";
        if (!window.confirm(aviso)) return;
        try {
            await App.api("/api/obras/" + obra.id + "/finalizar", { method: "POST" });
            App.toast("Obra finalizada.", "ok");
            await carregarObra();
        } catch (e) {
            App.toast(e.message, "erro");
        }
    };

    window.reabrirObra = async function () {
        if (!window.confirm("Reabrir a obra para lançar novos registros?")) return;
        try {
            await App.api("/api/obras/" + obra.id + "/reabrir", { method: "POST" });
            App.toast("Obra reaberta.", "ok");
            await carregarObra();
        } catch (e) {
            App.toast(e.message, "erro");
        }
    };

    /* =====================================================
       INÍCIO
    ===================================================== */
    (async function iniciar() {
        try {
            await App.init("obras");
        } catch (e) { return; }

        const id = App.obraAtualId();
        if (!id) {
            App.toast("Selecione uma obra primeiro.", "erro");
            setTimeout(function () { window.location.href = "obras.html"; }, 1200);
            return;
        }
        localStorage.setItem("obraSelecionada", String(id));

        try {
            obra = { id: id };
            await carregarObra();
        } catch (e) {
            App.toast(e.message, "erro");
            localStorage.removeItem("obraSelecionada");
            setTimeout(function () { window.location.href = "obras.html"; }, 1500);
            return;
        }
        valoresPadrao();
        await carregarHistorico();
    })();
})();
