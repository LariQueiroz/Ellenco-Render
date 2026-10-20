/* Tema claro/escuro: carregar no <head> para evitar "piscar" ao abrir a página. */
(function () {
    try {
        if (localStorage.getItem("modoEscuro") === "true") {
            document.documentElement.classList.add("dark");
        }
    } catch (e) { /* storage indisponível */ }
})();

window.Tema = {
    definir: function (escuro) {
        document.documentElement.classList.toggle("dark", !!escuro);
        try { localStorage.setItem("modoEscuro", escuro ? "true" : "false"); } catch (e) { }
    },
    ativo: function () {
        return document.documentElement.classList.contains("dark");
    }
};
