/* =====================================================
   ELLENCO - TELAS PÚBLICAS
   Login, cadastro, recuperação e redefinição de senha
===================================================== */

function mensagemTela(el, texto, tipo) {
    if (!el) return;
    el.textContent = texto;
    const base = el.className.replace(/\btext-(red|green|gray)-\d+\b/g, "").trim();
    el.className = base + " " + (tipo === "ok" ? "text-green-600" : "text-red-600");
}

function ligarOlho(botaoId, campoId) {
    const botao = document.getElementById(botaoId);
    if (botao) botao.addEventListener("click", function () { App.mostrarSenha(campoId, botao); });
}
ligarOlho("btnMostrarSenhaLogin", "senha");
ligarOlho("btnMostrarSenhaCadastro", "senhaCadastro");
ligarOlho("btnMostrarConfirmarSenha", "confirmarSenha");
ligarOlho("btnMostrarNovaSenha", "novaSenhaRedefinir");

/* ---------------- LOGIN ---------------- */
const formLogin = document.getElementById("formLogin");
if (formLogin) {
    // se já existe sessão ativa, vai direto para o sistema
    App.api("/api/usuarios/me", { semRedirect: true })
        .then(function () { window.location.replace("obras.html"); })
        .catch(function () { /* sem sessão: permanece no login */ });

    formLogin.addEventListener("submit", async function (event) {
        event.preventDefault();
        const mensagem = document.getElementById("mensagemLogin");
        const botao = formLogin.querySelector("button[type=submit]");
        botao.disabled = true;

        try {
            const usuario = await App.api("/api/usuarios/login", {
                method: "POST",
                semRedirect: true,
                body: {
                    login: document.getElementById("login").value.trim(),
                    senha: document.getElementById("senha").value
                }
            });
            mensagemTela(mensagem, "Bem-vindo(a), " + usuario.nome + "!", "ok");
            setTimeout(function () { window.location.href = "obras.html"; }, 600);
        } catch (erro) {
            mensagemTela(mensagem, erro.message, "erro");
            botao.disabled = false;
        }
    });
}

/* ---------------- CADASTRO ---------------- */
const formCadastro = document.getElementById("formCadastro");
if (formCadastro) {
    formCadastro.addEventListener("submit", async function (event) {
        event.preventDefault();
        const mensagem = document.getElementById("mensagemCadastro");
        const nome = document.getElementById("nome").value.trim();
        const senha = document.getElementById("senhaCadastro").value;

        if (senha !== document.getElementById("confirmarSenha").value) {
            mensagemTela(mensagem, "As senhas não são iguais.", "erro");
            return;
        }

        const botao = formCadastro.querySelector("button[type=submit]");
        botao.disabled = true;
        try {
            await App.api("/api/usuarios/cadastro", {
                method: "POST",
                semRedirect: true,
                body: {
                    nome: nome,
                    email: document.getElementById("emailCadastro").value.trim(),
                    login: document.getElementById("loginCadastro").value.trim(),
                    senha: senha,
                    cargo: document.getElementById("cargoCadastro").value
                }
            });
            mensagemTela(mensagem, "Seja bem-vindo(a), " + nome + "! Redirecionando para o login...", "ok");
            formCadastro.reset();
            setTimeout(function () { window.location.href = "index.html"; }, 1500);
        } catch (erro) {
            mensagemTela(mensagem, erro.message || "Não foi possível realizar o cadastro.", "erro");
            botao.disabled = false;
        }
    });
}

/* ---------------- RECUPERAR SENHA ---------------- */
const formRecuperar = document.getElementById("formRecuperar");
if (formRecuperar) {
    formRecuperar.addEventListener("submit", async function (event) {
        event.preventDefault();
        const mensagem = document.getElementById("mensagemRecuperar");
        const botao = formRecuperar.querySelector("button[type=submit]");
        botao.disabled = true;
        try {
            const r = await App.api("/api/usuarios/esqueci-senha", {
                method: "POST",
                semRedirect: true,
                body: { email: document.getElementById("emailRecuperar").value.trim() }
            });
            mensagemTela(mensagem, r.message, "ok");
        } catch (erro) {
            mensagemTela(mensagem, erro.message, "erro");
        }
        botao.disabled = false;
    });
}

/* ---------------- REDEFINIR SENHA (link do e-mail) ---------------- */
const formRedefinir = document.getElementById("formRedefinir");
if (formRedefinir) {
    const token = new URLSearchParams(window.location.search).get("token");
    const mensagem = document.getElementById("mensagemRedefinir");

    if (!token) {
        mensagemTela(mensagem, "Link inválido. Solicite uma nova recuperação de senha.", "erro");
        formRedefinir.querySelector("button[type=submit]").disabled = true;
    }

    formRedefinir.addEventListener("submit", async function (event) {
        event.preventDefault();
        const nova = document.getElementById("novaSenhaRedefinir").value;
        if (nova !== document.getElementById("confirmarSenhaRedefinir").value) {
            mensagemTela(mensagem, "As senhas não são iguais.", "erro");
            return;
        }
        const botao = formRedefinir.querySelector("button[type=submit]");
        botao.disabled = true;
        try {
            await App.api("/api/usuarios/redefinir-senha", {
                method: "POST",
                semRedirect: true,
                body: { token: token, novaSenha: nova }
            });
            mensagemTela(mensagem, "Senha alterada! Redirecionando para o login...", "ok");
            setTimeout(function () { window.location.href = "index.html"; }, 1800);
        } catch (erro) {
            mensagemTela(mensagem, erro.message, "erro");
            botao.disabled = false;
        }
    });
}
