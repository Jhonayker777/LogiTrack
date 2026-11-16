# LogiTrack · Frontend

1. Arranca la API (`http://localhost:8080`).
2. Sirve esta carpeta **desde un origen que el CORS de la API permita**: puertos 5500, 3000 u 8081.
   - VS Code → extensión *Live Server* (puerto 5500), o
   - `python3 -m http.server 5500` dentro de `frontend/`
3. Abre `http://localhost:5500/index.html`.

No abras los archivos con doble clic (`file://`): el navegador bloqueará las peticiones por CORS.
Para cambiar la URL de la API, edita `API_URL` al inicio de `js/auth.js`.
