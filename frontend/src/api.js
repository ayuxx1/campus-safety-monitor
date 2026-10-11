let csrfToken;

async function refreshCsrfToken() {
  const response = await fetch("/api/auth/csrf", { credentials: "include" });
  if (!response.ok) {
    throw new Error("Could not initialize a secure session.");
  }
  csrfToken = (await response.json()).token;
}

export async function api(path, options = {}) {
  const method = (options.method ?? "GET").toUpperCase();
  const headers = new Headers(options.headers ?? {});
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    if (!csrfToken) await refreshCsrfToken();
    headers.set("X-XSRF-TOKEN", csrfToken);
  }

  const response = await fetch(path, {
    ...options,
    method,
    headers,
    credentials: "include",
  });
  if (response.status === 204) return null;

  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    const message = payload?.detail ?? payload?.message ?? payload?.error
      ?? `Request failed (${response.status})`;
    throw new Error(message);
  }
  return payload;
}

export async function login(username, password) {
  await refreshCsrfToken();
  const user = await api("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
  await refreshCsrfToken();
  return user;
}

export async function logout() {
  await api("/api/auth/logout", { method: "POST" });
  csrfToken = undefined;
}
