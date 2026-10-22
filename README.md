# Ellenco — Relatórios Semanais de Obras

Sistema completo: **Spring Boot + MySQL** no back-end e **HTML + Tailwind + CSS + JavaScript (fetch)** no front,
servido pela pasta `src/main/resources/static` do próprio Spring Boot (um único deploy, mesma origem).

## Funcionalidades

| Área | O que faz |
|---|---|
| **Login / cadastro / recuperação** | Sessão por cookie HttpOnly. Senhas com BCrypt. Recuperação por e-mail (link com token de 30 min → `redefinir-senha.html`). |
| **Obras** | Cadastro, edição, exclusão, filtro (todas / em andamento / finalizadas), progresso, status "atrasada", capa = última foto. |
| **Registros (1 obra : N)** | Vários registros por obra até a finalização: data, horário, turno, clima, relato, progresso (%), **fotos** e **localização GPS**. Histórico na própria tela, com exclusão. |
| **Finalização** | "Finalizar obra" bloqueia novos registros; "Reabrir obra" libera de novo. |
| **Relatório semanal** | Semana de segunda a domingo, navegável (◀ ▶), resumo, progresso e variação, registros, evolução fotográfica, impressão/PDF. |
| **Dashboard** | KPIs, obras em andamento (com alertas de "sem registro há 7 dias"), registros por semana, clima, turno, últimos registros e **mapa online** (OpenStreetMap/Leaflet). |
| **Configurações** | Nome, e-mail, cargo, troca de senha, modo escuro. |

## Estrutura

```
src/main/java/com/ellenco/ellenco/
  config/      SecurityConfig (sessão, rotas públicas) · WebConfig
  controller/  Usuario · Obra · Registro · Foto · Dashboard · Health
  service/     regras de negócio (Obra, Registro, Relatorio, Dashboard, Usuario, Email)
  repository/  Spring Data JPA
  model/       Obra, Registro, Foto, FotoConteudo, Usuario
  dto/         records de entrada/saída da API
  exception/   respostas de erro padronizadas {"message": "..."}
src/main/resources/
  application.properties   (tudo configurável por variável de ambiente)
  static/                  FRONT: *.html · css/ · js/ · img/ · service-worker.js
Dockerfile · docker-compose.yml · render.yaml · .env.example
```

## Rodar localmente

**Opção A — Docker (MySQL + app):**
```bash
docker compose up --build
# http://localhost:8080
```

**Opção B — Maven + MySQL próprio:**
```bash
# MySQL rodando em localhost:3306 (o banco "ellenco" é criado automaticamente)
export DB_USERNAME=root DB_PASSWORD=sua_senha
mvn spring-boot:run
```
Variáveis padrão (ver `application.properties`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`, `APP_BASE_URL`,
`RESEND_API_KEY`, `RESEND_FROM_EMAIL`, `COOKIE_SECURE`, `JPA_DDL_AUTO`.

> A localização GPS do navegador só funciona em **HTTPS** ou em `localhost`.

## Deploy no Render

O Render **não oferece MySQL gerenciado**, então use um MySQL externo (ex.: Aiven, TiDB Cloud, Railway, Clever Cloud)
ou um serviço privado com MySQL e disco persistente no próprio Render.

1. Suba o projeto para um repositório Git (GitHub/GitLab).
2. No Render: **New + → Blueprint** (usa o `render.yaml`) ou **New + → Web Service → Docker**.
3. Em **Environment**, informe:
   | Variável | Exemplo |
   |---|---|
   | `DB_URL` | `jdbc:mysql://HOST:3306/ellenco?useSSL=true&serverTimezone=America/Sao_Paulo` |
   | `DB_USERNAME` / `DB_PASSWORD` | credenciais do MySQL |
   | `APP_BASE_URL` | `https://SEU-APP.onrender.com` (usada no link do e-mail de recuperação) |
   | `COOKIE_SECURE` | `true` |
   | `RESEND_API_KEY` / `RESEND_FROM_EMAIL` | chave API do Resend e remetente autorizado (domínio verificado) |
4. Health check: `/api/health` (já no `render.yaml`).
5. As tabelas são criadas/atualizadas automaticamente no primeiro start (`JPA_DDL_AUTO=update`).

**Fotos:** ficam gravadas **no MySQL** (tabela `foto_conteudo`), pois o disco do Render é efêmero.
O navegador reduz cada foto (máx. 1600 px, JPEG) antes de enviar. Se o seu MySQL limitar `max_allowed_packet`
(< 16 MB), mantenha as fotos pequenas — o limite por foto na API é 15 MB.

## API (resumo)

Tudo em `/api`, exceto cadastro/login/recuperação, exige sessão (401 sem login). Erros: `{"message": "..."}`.

| Método | Rota | Descrição |
|---|---|---|
| POST | `/usuarios/cadastro` · `/login` · `/logout` | autenticação |
| POST | `/usuarios/esqueci-senha` · `/redefinir-senha` | recuperação por e-mail |
| GET/PUT | `/usuarios/me` · `/usuarios/me/senha` | perfil e senha |
| GET/POST | `/obras` | listar (`?status=`) / cadastrar |
| GET/PUT/DELETE | `/obras/{id}` | detalhe / editar / excluir |
| POST | `/obras/{id}/finalizar` · `/reabrir` | ciclo de vida da obra |
| GET | `/obras/{id}/registros` · `/registros/obra/{id}` | registros da obra |
| GET | `/obras/{id}/relatorio-semanal?inicio=YYYY-MM-DD` | relatório da semana |
| POST | `/registros` (multipart: `obraId,data,horario,turno,clima,descricao,latitude,longitude,progresso,fotos[]`) | novo registro |
| GET/DELETE | `/registros/{id}` | detalhe / excluir |
| GET | `/fotos/{id}` | imagem |
| GET | `/dashboard` | indicadores |

## Observações importantes

- **Banco antigo:** com `ddl-auto=update` as tabelas existentes recebem as colunas novas; fotos antigas gravadas em
  `uploads/` continuam sendo exibidas. Se preferir começar limpo em desenvolvimento, apague o schema `ellenco`.
- **Cadastro aberto:** qualquer pessoa pode criar conta em "Criar Login" (comportamento original mantido).
  Para uso em produção, considere restringir (convite/aprovação).
- **Tailwind via CDN** (como no projeto original). Funciona bem; para produção "pura" pode-se trocar por build do Tailwind.
- **Segredos:** nada de senha em `application.properties` — use variáveis de ambiente.
