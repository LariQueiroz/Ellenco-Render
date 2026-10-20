/* Service Worker: cache apenas do "casco" estático (rede primeiro).
   Dados (/api) e fotos nunca são guardados em cache. */
const CACHE = "ellenco-v2";

self.addEventListener("install", function () { self.skipWaiting(); });

self.addEventListener("activate", function (event) {
    event.waitUntil(
        caches.keys()
            .then(function (chaves) {
                return Promise.all(chaves.filter(function (c) { return c !== CACHE; }).map(function (c) { return caches.delete(c); }));
            })
            .then(function () { return self.clients.claim(); })
    );
});

self.addEventListener("fetch", function (event) {
    const req = event.request;
    const url = new URL(req.url);
    if (req.method !== "GET" || url.origin !== self.location.origin || url.pathname.startsWith("/api/")) {
        return;
    }
    event.respondWith(
        fetch(req)
            .then(function (res) {
                if (res.ok) {
                    const copia = res.clone();
                    caches.open(CACHE).then(function (c) { c.put(req, copia); });
                }
                return res;
            })
            .catch(function () { return caches.match(req); })
    );
});
