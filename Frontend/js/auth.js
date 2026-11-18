(function () {
  if (Session.isValid()) {
    window.location.replace("app.html");
    return;
  }

  document.getElementById("brand-mark").innerHTML = icon("truck", 20);
  document.getElementById("brand-mark-mobile").innerHTML = icon("truck", 20);

  if (new URLSearchParams(location.search).get("expired")) {
    document.getElementById("expired-msg").hidden = false;
  }

  const tabs = [document.getElementById("tab-login"), document.getElementById("tab-register")];
  const panels = [document.getElementById("panel-login"), document.getElementById("panel-register")];

  function selectTab(index) {
    tabs.forEach((t, i) => {
      const active = i === index;
      t.classList.toggle("is-active", active);
      t.setAttribute("aria-selected", String(active));
      t.tabIndex = active ? 0 : -1;
      panels[i].hidden = !active;
    });
  }
  tabs.forEach((t, i) => {
    t.addEventListener("click", () => selectTab(i));
    t.addEventListener("keydown", (e) => {
      if (e.key === "ArrowRight" || e.key === "ArrowLeft") {
        const next = (i + 1) % 2;
        selectTab(next);
        tabs[next].focus();
      }
    });
  });

  const loginForm = document.getElementById("login-form");
  const loginError = document.getElementById("login-error");
  loginForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    loginError.hidden = true;
    const username = loginForm.username.value.trim();
    const password = loginForm.password.value;
    if (!username || !password) {
      loginError.textContent = "Ingresa tu usuario y contraseña.";
      loginError.hidden = false;
      return;
    }
    const btn = loginForm.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Ingresando…";
    try {
      const res = await api("/auth/login", { method: "POST", body: { username, password }, auth: false });
      Session.set(res);
      window.location.replace("app.html");
    } catch (err) {
      loginError.textContent = err.status === 401 ? "Usuario o contraseña incorrectos." : err.message;
      loginError.hidden = false;
      btn.disabled = false;
      btn.textContent = "Ingresar";
    }
  });

  const registerForm = document.getElementById("register-form");
  const registerError = document.getElementById("register-error");
  registerForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    registerError.hidden = true;
    registerForm.querySelectorAll(".field-error").forEach((el) => el.remove());

    const data = {};
    let invalid = false;
    for (const el of registerForm.elements) {
      if (!el.name) continue;
      const v = el.value.trim();
      if (el.required && !v) {
        markFieldError(el, "Este campo es obligatorio");
        invalid = true;
      }
      data[el.name] = v === "" ? null : v;
    }
    if (invalid) return;
    data.ciudadId = Number(data.ciudadId);

    const btn = registerForm.querySelector("button[type=submit]");
    btn.disabled = true;
    btn.textContent = "Enviando…";
    try {
      await api("/auth/register", { method: "POST", body: data, auth: false });
      registerForm.reset();
      toast("Solicitud enviada. Un administrador la revisará pronto.");
      selectTab(0);
    } catch (err) {
      if (err.fieldErrors) {
        for (const [name, msg] of Object.entries(err.fieldErrors)) {
          if (registerForm.elements[name]) markFieldError(registerForm.elements[name], msg);
        }
      }
      registerError.textContent = err.message;
      registerError.hidden = false;
    } finally {
      btn.disabled = false;
      btn.textContent = "Enviar solicitud";
    }
  });
})();
