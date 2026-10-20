/* =====================================================
   ELLENCO - CONFIGURAÇÕES
===================================================== */
(function () {
    const el = function (id) { return document.getElementById(id); };

    function mensagem(id, texto, tipo) {
        const p = el(id);
        if (!p) return;
        p.textContent = texto;
        p.className = p.className.replace(/\btext-(red|green)-\d+\b/g, "").trim() + (tipo === "ok" ? " text-green-600" : " text-red-600");
    }

    window.voltar = function () { window.location.href = "obras.html"; };

    // modo escuro: aplica na hora
    el("modoEscuro").checked = Tema.ativo();
    el("modoEscuro").addEventListener("change", function (e) { Tema.definir(e.target.checked); });

    window.salvarConfiguracoes = async function () {
        const nome = el("nomeUsuario").value.trim();
        const email = el("emailUsuario").value.trim();
        const cargo = el("cargoUsuario").value;

        if (!nome || !email) {
            mensagem("mensagem", "Informe nome e e-mail.", "erro");
            return;
        }
        try {
            const u = await App.api("/api/usuarios/me", { method: "PUT", body: { nome: nome, email: email, cargo: cargo || null } });
            App.usuario = u;
            Tema.definir(el("modoEscuro").checked);
            mensagem("mensagem", "Alterações salvas com sucesso!", "ok");
            App.toast("Configurações salvas.", "ok");
        } catch (e) {
            mensagem("mensagem", e.message, "erro");
        }
    };

    window.alterarSenha = async function () {
        const nova = el("novaSenha").value;
        const confirmar = el("confirmarSenha").value;

        if (!nova || !confirmar) { mensagem("mensagemSenha", "Preencha os dois campos de senha.", "erro"); return; }
        if (nova.length < 6) { mensagem("mensagemSenha", "A senha deve possuir pelo menos 6 caracteres.", "erro"); return; }
        if (nova !== confirmar) { mensagem("mensagemSenha", "As senhas não são iguais.", "erro"); return; }

        try {
            await App.api("/api/usuarios/me/senha", { method: "PUT", body: { novaSenha: nova } });
            el("novaSenha").value = "";
            el("confirmarSenha").value = "";
            mensagem("mensagemSenha", "Senha alterada com sucesso!", "ok");
        } catch (e) {
            mensagem("mensagemSenha", e.message, "erro");
        }
    };

    App.init("configuracoes").then(function (u) {
        el("nomeUsuario").value = u.nome || "";
        el("emailUsuario").value = u.email || "";
        el("cargoUsuario").value = u.cargo || "";
    }).catch(function () { /* redirecionado ao login */ });
})();
