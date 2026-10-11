import { useCallback, useEffect, useMemo, useState } from "react";
import {
  Activity, AlertTriangle, AudioLines, BarChart3, Bell, Check, ChevronDown, Clock3,
  Gauge, LayoutDashboard, LoaderCircle, LogOut, Menu, RefreshCw, Search, Settings,
  Shield, ShieldCheck, Users, X,
} from "lucide-react";
import { api, login, logout } from "./api";

const navigation = [
  { id: "overview", label: "Overview", icon: LayoutDashboard },
  { id: "activity", label: "Live activity", icon: Activity },
  { id: "reports", label: "Reports", icon: BarChart3 },
  { id: "users", label: "User management", icon: Users, admin: true },
  { id: "settings", label: "Safety settings", icon: Settings, admin: true },
];

function formatDate(value) {
  return value ? new Intl.DateTimeFormat(undefined, {
    month: "short", day: "numeric", hour: "numeric", minute: "2-digit",
  }).format(new Date(value)) : "—";
}

function App() {
  const [user, setUser] = useState(null);
  const [checkingSession, setCheckingSession] = useState(true);
  const [page, setPage] = useState("overview");
  const [mobileNavOpen, setMobileNavOpen] = useState(false);
  const [notice, setNotice] = useState(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const notify = useCallback((message, kind = "success") => {
    setNotice({ message, kind });
    window.setTimeout(() => setNotice(null), 4000);
  }, []);

  useEffect(() => {
    api("/api/auth/me")
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setCheckingSession(false));
  }, []);

  useEffect(() => {
    const navigate = (event) => setPage(event.detail);
    const refresh = () => setRefreshKey((key) => key + 1);
    document.addEventListener("campus-navigate", navigate);
    document.addEventListener("campus-data-updated", refresh);
    return () => {
      document.removeEventListener("campus-navigate", navigate);
      document.removeEventListener("campus-data-updated", refresh);
    };
  }, []);

  const signOut = async () => {
    try {
      await logout();
      setUser(null);
      notify("You have signed out.");
    } catch (error) {
      notify(error.message, "error");
    }
  };

  if (checkingSession) {
    return <div className="loading-screen"><LoaderCircle className="spin" /> Restoring your secure session…</div>;
  }
  if (!user) {
    return <LoginScreen onLogin={setUser} notify={notify} />;
  }

  const selectedPage = navigation.find((item) => item.id === page);
  const visibleNavigation = navigation.filter((item) => !item.admin || user.role === "ADMIN");
  return (
    <div className="app-shell">
      <aside className={`sidebar ${mobileNavOpen ? "sidebar-open" : ""}`}>
        <div className="brand">
          <div className="brand-mark"><ShieldCheck size={21} /></div>
          <div><strong>CampusWatch</strong><span>SAFETY OPERATIONS</span></div>
          <button className="icon-button mobile-close" onClick={() => setMobileNavOpen(false)} aria-label="Close menu"><X size={18} /></button>
        </div>
        <div className="nav-caption">WORKSPACE</div>
        <nav className="navigation">
          {visibleNavigation.map(({ id, label, icon: Icon }) => (
            <button key={id} className={`nav-link ${page === id ? "nav-link-active" : ""}`} onClick={() => { setPage(id); setMobileNavOpen(false); }}>
              <Icon size={18} strokeWidth={1.8} /><span>{label}</span>
              {id === "activity" && <span className="live-dot" />}
            </button>
          ))}
        </nav>
        <div className="sidebar-spacer" />
        <div className="sidebar-status"><span className="status-indicator" /> Systems operational</div>
        <div className="profile">
          <div className="avatar">{user.username.slice(0, 1).toUpperCase()}</div>
          <div className="profile-info"><strong>{user.username}</strong><span>{user.role === "ADMIN" ? "Administrator" : "Safety operator"}</span></div>
          <button className="icon-button" onClick={signOut} title="Sign out" aria-label="Sign out"><LogOut size={17} /></button>
        </div>
      </aside>
      {mobileNavOpen && <button className="mobile-backdrop" onClick={() => setMobileNavOpen(false)} aria-label="Close navigation" />}
      <main className="main-area">
        <header className="topbar">
          <button className="icon-button mobile-menu" onClick={() => setMobileNavOpen(true)} aria-label="Open menu"><Menu size={19} /></button>
          <div className="breadcrumb">Workspace <span>/</span> <strong>{selectedPage?.label}</strong></div>
          <div className="topbar-right"><span className="top-date"><Clock3 size={15} />{new Intl.DateTimeFormat(undefined, { weekday: "short", month: "short", day: "numeric" }).format(new Date())}</span><span className="topbar-divider" /><div className="secure-label"><Shield size={15} /> Secure session</div></div>
        </header>
        <div className="content">
          {page === "overview" && <Overview notify={notify} refreshKey={refreshKey} onActivity={() => setPage("activity")} />}
          {page === "activity" && <ActivityPage notify={notify} refreshKey={refreshKey} />}
          {page === "reports" && <ReportsPage notify={notify} refreshKey={refreshKey} />}
          {page === "users" && user.role === "ADMIN" && <UsersPage notify={notify} refreshKey={refreshKey} currentUsername={user.username} />}
          {page === "settings" && user.role === "ADMIN" && <SettingsPage notify={notify} refreshKey={refreshKey} />}
        </div>
        <footer className="page-footer"><span>Campus Safety Monitor</span><span>Operations console <span className="footer-dot">·</span> {new Date().getFullYear()}</span></footer>
      </main>
      <button className="refresh-fab" onClick={() => setRefreshKey((key) => key + 1)} title="Refresh dashboard" aria-label="Refresh dashboard"><RefreshCw size={17} /></button>
      {notice && <div className={`toast toast-${notice.kind}`}><span>{notice.kind === "error" ? <AlertTriangle size={17} /> : <Check size={17} />}</span>{notice.message}<button onClick={() => setNotice(null)} aria-label="Dismiss"><X size={15} /></button></div>}
    </div>
  );
}

function LoginScreen({ onLogin, notify }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    try {
      onLogin(await login(username, password));
    } catch (error) {
      notify(error.message, "error");
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="login-page">
      <div className="login-decoration decoration-one" /><div className="login-decoration decoration-two" />
      <div className="login-panel">
        <div className="login-brand"><div className="brand-mark"><ShieldCheck size={22} /></div><span>CAMPUSWATCH</span></div>
        <div className="login-heading"><div className="eyebrow">CAMPUS SAFETY OPERATIONS</div><h1>Good to see you.</h1><p>Sign in to access your campus safety workspace.</p></div>
        <form onSubmit={submit} className="login-form">
          <label>Username<input autoComplete="username" value={username} onChange={(event) => setUsername(event.target.value)} placeholder="Enter your username" required autoFocus /></label>
          <label>Password<input type="password" autoComplete="current-password" value={password} onChange={(event) => setPassword(event.target.value)} placeholder="Enter your password" required /></label>
          <button className="button button-primary login-submit" disabled={busy}>{busy ? <LoaderCircle className="spin" size={17} /> : <ShieldCheck size={17} />} Secure sign in</button>
        </form>
        <div className="login-foot"><Shield size={14} /> Protected by secure sessions and role-based access</div>
      </div>
      <div className="login-copyright">CAMPUS SAFETY MONITOR <span>·</span> OPERATIONS CONSOLE</div>
    </div>
  );
}

function PageHeading({ eyebrow, title, description, action }) {
  return <div className="page-heading"><div><div className="eyebrow">{eyebrow}</div><h1>{title}</h1><p>{description}</p></div>{action}</div>;
}

function useDashboardData(refreshKey) {
  const [data, setData] = useState({ summary: null, speed: [], acoustic: [], loading: true, error: null });
  const reload = useCallback(async () => {
    setData((current) => ({ ...current, loading: true, error: null }));
    try {
      const [summary, speed, acoustic] = await Promise.all([
        api("/api/reports/summary"),
        api("/api/reports/speed-violations"),
        api("/api/reports/acoustic-readings"),
      ]);
      setData({ summary, speed, acoustic, loading: false, error: null });
    } catch (error) {
      setData((current) => ({ ...current, loading: false, error: error.message }));
    }
  }, []);
  useEffect(() => {
    reload();
    const interval = window.setInterval(reload, 30_000);
    return () => window.clearInterval(interval);
  }, [reload, refreshKey]);
  return { ...data, reload };
}

function Overview({ notify, refreshKey, onActivity }) {
  const { summary, speed, acoustic, loading, error, reload } = useDashboardData(refreshKey);
  const recentEvents = useMemo(() => [
    ...speed.map((event) => ({ id: `speed-${event.id}`, type: "speed", title: "Speed violation", detail: event.vehicleNumber, value: `${Number(event.measuredSpeedKmh).toFixed(1)} km/h`, at: event.occurredAt })),
    ...acoustic.filter((event) => event.sustainedViolation).map((event) => ({ id: `noise-${event.id}`, type: "noise", title: "Sustained noise alert", detail: event.zoneId, value: `${Number(event.decibelLevel).toFixed(0)} dB`, at: event.recordedAt })),
  ].sort((a, b) => new Date(b.at) - new Date(a.at)).slice(0, 5), [speed, acoustic]);
  const chart = useMemo(() => acoustic.slice(0, 24).reverse().map((reading) => ({
    time: new Date(reading.recordedAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
    decibels: Number(reading.decibelLevel),
  })), [acoustic]);
  return (
    <>
      <PageHeading eyebrow="CAMPUS OVERVIEW" title="Safety at a glance" description="A real-time pulse on vehicle movement and campus sound levels." action={<button className="button button-secondary" onClick={reload}><RefreshCw size={16} />Refresh</button>} />
      {error && <ErrorBanner message={error} />}
      <div className="metric-grid">
        <MetricCard icon={Gauge} label="Speed violations" value={summary?.speedViolationCount ?? "—"} note="Recorded at checkpoints" tone="blue" />
        <MetricCard icon={AudioLines} label="Acoustic readings" value={summary?.acousticReadingCount ?? "—"} note="Sensor readings received" tone="violet" />
        <MetricCard icon={Bell} label="Sustained noise alerts" value={summary?.sustainedNoiseViolationCount ?? "—"} note="Continuous threshold events" tone="amber" />
        <MetricCard icon={ShieldCheck} label="Safety systems" value={error ? "Degraded" : loading ? "Checking" : "Online"} note={error ? "Review service connectivity" : "Monitoring services active"} tone={error ? "amber" : "green"} status />
      </div>
      <div className="dashboard-grid">
        <section className="panel chart-panel">
          <div className="panel-heading"><div><h2>Campus sound activity</h2><p>Most recent sensor readings</p></div><span className="panel-tag"><span className="status-indicator" /> LIVE</span></div>
          {loading && !chart.length ? <PanelLoading /> : chart.length ? <div className="chart-wrap"><SoundChart data={chart} /></div> : <EmptyState icon={AudioLines} title="Waiting for sensor data" text="New acoustic readings will appear here." />}
          <div className="chart-foot"><span><i className="legend-dot" /> Sound level</span><span>Threshold alerts are highlighted in activity</span></div>
        </section>
        <section className="panel activity-panel">
          <div className="panel-heading"><div><h2>Recent alerts</h2><p>Latest safety incidents</p></div><button className="text-button" onClick={onActivity}>View all <span>→</span></button></div>
          {loading && !recentEvents.length ? <PanelLoading /> : recentEvents.length ? <div className="event-list">{recentEvents.map((event) => <EventRow key={event.id} event={event} />)}</div> : <EmptyState icon={ShieldCheck} title="All clear" text="No safety alerts have been recorded." />}
        </section>
      </div>
      <div className="section-label"><span>QUICK ACTIONS</span><div /></div>
      <div className="quick-grid">
        <QuickAction icon={Gauge} title="Record checkpoint" text="Log a vehicle passing through a gate." onClick={() => document.getElementById("quick-transit")?.scrollIntoView({ behavior: "smooth" })} />
        <QuickAction icon={AudioLines} title="Log sound reading" text="Submit an acoustic sensor sample." onClick={() => document.getElementById("quick-acoustic")?.scrollIntoView({ behavior: "smooth" })} />
        <QuickAction icon={BarChart3} title="Review reports" text="Explore incidents and export records." onClick={() => document.dispatchEvent(new CustomEvent("campus-navigate", { detail: "reports" }))} />
      </div>
      <div className="forms-grid">
        <TransitForm notify={notify} id="quick-transit" />
        <AcousticForm notify={notify} id="quick-acoustic" />
      </div>
    </>
  );
}

function MetricCard({ icon: Icon, label, value, note, tone, status }) {
  return <div className="metric-card"><div className="metric-top"><span className={`metric-icon icon-${tone}`}><Icon size={19} /></span>{status && <span className="metric-live"><i /> LIVE</span>}</div><div className="metric-value">{value}</div><div className="metric-label">{label}</div><div className="metric-note">{note}</div></div>;
}

function SoundChart({ data }) {
  const left = 38;
  const right = 590;
  const top = 10;
  const bottom = 169;
  const points = data.map((entry, index) => ({
    ...entry,
    x: data.length === 1 ? (left + right) / 2 : left + (index / (data.length - 1)) * (right - left),
    y: bottom - (Math.max(0, Math.min(200, entry.decibels)) / 200) * (bottom - top),
  }));
  const line = points.map((point, index) => `${index === 0 ? "M" : "L"}${point.x},${point.y}`).join(" ");
  const area = `${line} L${points.at(-1).x},${bottom} L${points[0].x},${bottom} Z`;
  const labelIndexes = [...new Set([0, Math.floor((points.length - 1) / 2), points.length - 1])];
  return (
    <svg className="sound-chart" viewBox="0 0 600 205" preserveAspectRatio="none" role="img" aria-label="Recent sound levels in decibels">
      <defs><linearGradient id="soundFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#3978f6" stopOpacity=".18" /><stop offset="100%" stopColor="#3978f6" stopOpacity=".01" /></linearGradient></defs>
      {[0, 50, 100, 150, 200].map((value) => {
        const y = bottom - (value / 200) * (bottom - top);
        return <g key={value}><line x1={left} y1={y} x2={right} y2={y} stroke="#edf0f5" /><text x={left - 8} y={y + 3} textAnchor="end" fill="#8b95a5" fontSize="9">{value}</text></g>;
      })}
      <path d={area} fill="url(#soundFill)" />
      <path d={line} fill="none" stroke="#3978f6" strokeWidth="2.5" vectorEffect="non-scaling-stroke" />
      {points.map((point, index) => <circle key={`${point.time}-${index}`} cx={point.x} cy={point.y} r="2.8" fill="#3978f6"><title>{`${point.time}: ${point.decibels} dB`}</title></circle>)}
      {labelIndexes.map((index) => <text key={`${points[index].time}-${index}`} x={points[index].x} y="197" textAnchor={index === 0 ? "start" : index === points.length - 1 ? "end" : "middle"} fill="#8b95a5" fontSize="9">{points[index].time}</text>)}
    </svg>
  );
}

function QuickAction({ icon: Icon, title, text, onClick }) {
  return <button className="quick-action" onClick={onClick}><span className="quick-icon"><Icon size={19} /></span><span className="quick-copy"><strong>{title}</strong><small>{text}</small></span><span className="quick-arrow">→</span></button>;
}

function TransitForm({ notify, id }) {
  const [vehicle, setVehicle] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const submit = async (mode) => {
    if (!vehicle.trim()) return;
    setBusy(true);
    setResult(null);
    try {
      const endpoint = mode === "entry" ? "/api/transit/entry" : `/api/transit/${encodeURIComponent(vehicle.trim())}/exit`;
      const response = await api(endpoint, { method: "POST", ...(mode === "entry" ? { body: JSON.stringify({ vehicleNumber: vehicle.trim() }) } : {}) });
      if (mode === "entry") {
        setResult({ safe: true, text: `${response.vehicleNumber} checkpoint entry recorded.` });
      } else {
        setResult({ safe: !response.violated, text: `${response.vehicleNumber} · ${Number(response.speedKmh).toFixed(1)} km/h · ${response.message}` });
      }
      setVehicle("");
      notify(mode === "entry" ? "Checkpoint entry recorded." : response.message, mode === "exit" && response.violated ? "warning" : "success");
      document.dispatchEvent(new Event("campus-data-updated"));
    } catch (error) {
      setResult({ safe: false, text: error.message });
      notify(error.message, "error");
    } finally {
      setBusy(false);
    }
  };
  return <section className="panel form-panel" id={id}><div className="form-title"><span className="form-icon blue-bg"><Gauge size={19} /></span><div><h2>Checkpoint transit</h2><p>Record entry or calculate vehicle speed.</p></div></div><label className="field-label">Vehicle registration<input value={vehicle} onChange={(event) => setVehicle(event.target.value)} placeholder="e.g. CAMPUS-101" onKeyDown={(event) => { if (event.key === "Enter") submit("entry"); }} /></label><div className="button-row"><button className="button button-secondary grow" disabled={busy || !vehicle.trim()} onClick={() => submit("entry")}>Record entry</button><button className="button button-primary grow" disabled={busy || !vehicle.trim()} onClick={() => submit("exit")}>{busy ? <LoaderCircle className="spin" size={16} /> : null}Record exit</button></div>{result && <div className={`inline-result ${result.safe ? "result-safe" : "result-alert"}`}>{result.safe ? <Check size={15} /> : <AlertTriangle size={15} />}{result.text}</div>}</section>;
}

function AcousticForm({ notify, id }) {
  const [zone, setZone] = useState("");
  const [decibels, setDecibels] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const submit = async (event) => {
    event.preventDefault();
    setBusy(true);
    setResult(null);
    try {
      const response = await api("/api/acoustics/readings", {
        method: "POST",
        body: JSON.stringify({ zoneId: zone.trim(), decibelLevel: Number(decibels) }),
      });
      setResult(response);
      setZone("");
      setDecibels("");
      notify(response.sustainedViolation ? "Sustained noise alert recorded." : "Acoustic reading recorded.",
        response.sustainedViolation ? "warning" : "success");
      document.dispatchEvent(new Event("campus-data-updated"));
    } catch (error) {
      notify(error.message, "error");
    } finally {
      setBusy(false);
    }
  };
  return <section className="panel form-panel" id={id}><div className="form-title"><span className="form-icon violet-bg"><AudioLines size={19} /></span><div><h2>Acoustic reading</h2><p>Submit a campus sound sensor sample.</p></div></div><form onSubmit={submit}><div className="two-fields"><label className="field-label">Campus zone<input value={zone} onChange={(event) => setZone(event.target.value)} placeholder="e.g. LIBRARY-WEST" required /></label><label className="field-label">Sound level<input type="number" min="0" max="200" step="0.1" value={decibels} onChange={(event) => setDecibels(event.target.value)} placeholder="dB" required /></label></div><button className="button button-primary full-width" disabled={busy}>{busy ? <LoaderCircle className="spin" size={16} /> : <AudioLines size={16} />}Submit reading</button></form>{result && <div className={`inline-result ${result.sustainedViolation ? "result-alert" : result.excessive ? "result-warning" : "result-safe"}`}>{result.sustainedViolation ? <AlertTriangle size={15} /> : <Check size={15} />}{result.zoneId}: {Number(result.decibelLevel).toFixed(1)} dB{result.sustainedViolation ? " · sustained alert" : result.excessive ? " · above threshold" : " · within limit"}</div>}</section>;
}

function ActivityPage({ notify, refreshKey }) {
  const { speed, acoustic, loading, error, reload } = useDashboardData(refreshKey);
  const [filter, setFilter] = useState("all");
  const events = useMemo(() => [
    ...speed.map((event) => ({ id: `speed-${event.id}`, type: "speed", title: "Speed violation", detail: event.vehicleNumber, value: `${Number(event.measuredSpeedKmh).toFixed(1)} km/h`, at: event.occurredAt, meta: `Limit ${event.speedLimitKmh} km/h` })),
    ...acoustic.map((event) => ({ id: `noise-${event.id}`, type: event.sustainedViolation ? "noise" : "reading", title: event.sustainedViolation ? "Sustained noise alert" : event.excessive ? "High noise reading" : "Acoustic sensor reading", detail: event.zoneId, value: `${Number(event.decibelLevel).toFixed(1)} dB`, at: event.recordedAt, meta: event.sustainedViolation ? `${Number(event.sustainedDurationSeconds).toFixed(1)} sec sustained` : "Sensor sample" })),
  ].sort((a, b) => new Date(b.at) - new Date(a.at)), [speed, acoustic]);
  const visible = filter === "all" ? events : events.filter((event) => filter === "noise" ? event.type !== "speed" : event.type === "speed");
  return <><PageHeading eyebrow="INCIDENT MONITORING" title="Live activity" description="Review checkpoint violations and acoustic sensor activity." action={<button className="button button-secondary" onClick={reload}><RefreshCw size={16} />Refresh</button>} />{error && <ErrorBanner message={error} />}<div className="panel table-panel"><div className="table-toolbar"><div className="filter-tabs">{[["all", "All activity"], ["speed", "Speed"], ["noise", "Acoustics"]].map(([id, label]) => <button key={id} className={filter === id ? "filter-active" : ""} onClick={() => setFilter(id)}>{label}<span>{id === "all" ? events.length : id === "speed" ? speed.length : acoustic.length}</span></button>)}</div><span className="table-note">Showing the latest {Math.min(visible.length, 500)} records</span></div><ActivityTable events={visible} loading={loading} /></div></>;
}

function ActivityTable({ events, loading }) {
  if (loading && !events.length) return <PanelLoading />;
  if (!events.length) return <EmptyState icon={Search} title="No matching activity" text="Try another filter or record a new sensor event." />;
  return <div className="table-scroll"><table><thead><tr><th>EVENT</th><th>LOCATION / VEHICLE</th><th>MEASUREMENT</th><th>DATE & TIME</th><th>DETAIL</th></tr></thead><tbody>{events.slice(0, 500).map((event) => <tr key={event.id}><td><span className={`event-pill pill-${event.type}`}>{event.type === "speed" ? <Gauge size={14} /> : <AudioLines size={14} />}{event.title}</span></td><td className="strong-cell">{event.detail}</td><td className="measure-cell">{event.value}</td><td>{formatDate(event.at)}</td><td className="muted-cell">{event.meta ?? "—"}</td></tr>)}</tbody></table></div>;
}

function ReportsPage({ notify, refreshKey }) {
  const { summary, speed, acoustic, loading, error, reload } = useDashboardData(refreshKey);
  const [reportType, setReportType] = useState("speed");
  const exportReport = () => {
    const records = reportType === "speed" ? speed : acoustic;
    if (!records.length) {
      notify("There are no records to export.", "warning");
      return;
    }
    const columns = reportType === "speed"
      ? ["id", "vehicleNumber", "measuredSpeedKmh", "speedLimitKmh", "occurredAt"]
      : ["id", "zoneId", "decibelLevel", "excessive", "sustainedViolation", "sustainedDurationSeconds", "recordedAt"];
    const csv = [columns.join(","), ...records.map((record) => columns.map((key) => csvValue(record[key])).join(","))].join("\r\n");
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const link = document.createElement("a");
    link.href = url;
    link.download = `campus-${reportType}-report-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(url);
    notify(`${records.length} records exported.`);
  };
  return <><PageHeading eyebrow="SAFETY ANALYTICS" title="Reports & insights" description="Review recorded incidents and export operational data." action={<button className="button button-primary" onClick={exportReport}><BarChart3 size={16} />Export CSV</button>} />{error && <ErrorBanner message={error} />}<div className="metric-grid report-metrics"><MetricCard icon={Gauge} label="Total speed violations" value={summary?.speedViolationCount ?? "—"} note="All recorded checkpoints" tone="blue" /><MetricCard icon={AudioLines} label="Acoustic samples" value={summary?.acousticReadingCount ?? "—"} note="All submitted readings" tone="violet" /><MetricCard icon={AlertTriangle} label="Sustained noise alerts" value={summary?.sustainedNoiseViolationCount ?? "—"} note="Distinct alert samples" tone="amber" /></div><section className="panel table-panel reports-table"><div className="table-toolbar"><div><h2>Incident records</h2><p>Up to 500 of the latest records are available per report.</p></div><div className="select-wrap"><label htmlFor="report-type">Report type</label><select id="report-type" value={reportType} onChange={(event) => setReportType(event.target.value)}><option value="speed">Speed violations</option><option value="acoustic">Acoustic readings</option></select><ChevronDown size={15} /></div></div><ActivityTable events={reportType === "speed" ? speed.map((event) => ({ id: `speed-${event.id}`, type: "speed", title: "Speed violation", detail: event.vehicleNumber, value: `${Number(event.measuredSpeedKmh).toFixed(1)} km/h`, at: event.occurredAt, meta: `Limit ${event.speedLimitKmh} km/h` })) : acoustic.map((event) => ({ id: `acoustic-${event.id}`, type: event.sustainedViolation ? "noise" : "reading", title: event.sustainedViolation ? "Sustained noise alert" : event.excessive ? "High noise reading" : "Acoustic sensor reading", detail: event.zoneId, value: `${Number(event.decibelLevel).toFixed(1)} dB`, at: event.recordedAt, meta: event.sustainedViolation ? `${Number(event.sustainedDurationSeconds).toFixed(1)} sec sustained` : "Sensor sample" }))} loading={loading} /></section><div className="export-note"><Shield size={15} />Report exports include the 500 most recent records returned by the API.</div></>;
}

function csvValue(value) {
  const text = value == null ? "" : String(value);
  const safeText = /^[=+\-@]/.test(text) ? `'${text}` : text;
  return `"${safeText.replaceAll('"', '""')}"`;
}

function UsersPage({ notify, refreshKey, currentUsername }) {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [createOpen, setCreateOpen] = useState(false);
  const [form, setForm] = useState({ username: "", password: "", role: "OPERATOR" });
  const loadUsers = useCallback(async () => {
    setLoading(true);
    try { setUsers(await api("/api/admin/users")); }
    catch (error) { notify(error.message, "error"); }
    finally { setLoading(false); }
  }, [notify]);
  useEffect(() => { loadUsers(); }, [loadUsers, refreshKey]);
  const createUser = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await api("/api/admin/users", { method: "POST", body: JSON.stringify(form) });
      setForm({ username: "", password: "", role: "OPERATOR" });
      setCreateOpen(false);
      await loadUsers();
      notify("Account created successfully.");
    } catch (error) { notify(error.message, "error"); }
    finally { setSaving(false); }
  };
  const updateUser = async (account, change) => {
    try {
      const updated = await api(`/api/admin/users/${account.id}`, {
        method: "PUT",
        body: JSON.stringify({ role: change.role ?? account.role, enabled: change.enabled ?? account.enabled }),
      });
      setUsers((current) => current.map((item) => item.id === updated.id ? updated : item));
      notify("Account updated.");
    } catch (error) { notify(error.message, "error"); }
  };
  const removeUser = async (account) => {
    if (!window.confirm(`Delete the account "${account.username}"? This cannot be undone.`)) return;
    try {
      await api(`/api/admin/users/${account.id}`, { method: "DELETE" });
      await loadUsers();
      notify("Account deleted.");
    } catch (error) { notify(error.message, "error"); }
  };
  return <><PageHeading eyebrow="ACCESS CONTROL" title="User management" description="Create and manage accounts that access the safety console." action={<button className="button button-primary" onClick={() => setCreateOpen((open) => !open)}><Users size={16} />{createOpen ? "Close form" : "Add user"}</button>} />{createOpen && <form className="panel create-user-form" onSubmit={createUser}><div className="form-title"><span className="form-icon blue-bg"><Users size={19} /></span><div><h2>Create an account</h2><p>New passwords must be at least 12 characters.</p></div></div><div className="create-user-fields"><label className="field-label">Username<input value={form.username} onChange={(event) => setForm({ ...form, username: event.target.value })} pattern="[a-zA-Z0-9._-]+" maxLength="80" required placeholder="e.g. safety.operator" /></label><label className="field-label">Temporary password<input type="password" value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} minLength="12" maxLength="100" required placeholder="At least 12 characters" /></label><label className="field-label">Role<select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}><option value="OPERATOR">Safety operator</option><option value="ADMIN">Administrator</option></select></label><button className="button button-primary create-submit" disabled={saving}>{saving ? <LoaderCircle className="spin" size={16} /> : null}Create account</button></div></form>}<div className="panel table-panel users-table"><div className="table-toolbar"><div><h2>Console accounts</h2><p>{users.length} {users.length === 1 ? "account" : "accounts"} with access to the operations console.</p></div><button className="icon-button" onClick={loadUsers} title="Refresh users"><RefreshCw size={16} /></button></div>{loading ? <PanelLoading /> : <div className="table-scroll"><table><thead><tr><th>ACCOUNT</th><th>ROLE</th><th>STATUS</th><th>ACCESS</th><th /></tr></thead><tbody>{users.map((account) => <tr key={account.id}><td><div className="user-cell"><span className="avatar small-avatar">{account.username.slice(0, 1).toUpperCase()}</span><span className="strong-cell">{account.username}{account.username === currentUsername && <small className="you-label">YOU</small>}</span></div></td><td><select className="inline-select" value={account.role} onChange={(event) => updateUser(account, { role: event.target.value })}><option value="OPERATOR">Operator</option><option value="ADMIN">Admin</option></select></td><td><span className={`status-badge ${account.enabled ? "enabled" : "disabled"}`}><i />{account.enabled ? "Active" : "Disabled"}</span></td><td><button className={`text-button access-toggle ${account.enabled ? "danger-text" : ""}`} disabled={account.username === currentUsername} onClick={() => updateUser(account, { enabled: !account.enabled })}>{account.enabled ? "Disable" : "Enable"}</button></td><td><button className="icon-button delete-button" disabled={account.username === currentUsername} onClick={() => removeUser(account)} title="Delete account" aria-label={`Delete ${account.username}`}><X size={17} /></button></td></tr>)}</tbody></table></div>}</div><div className="permission-note"><Shield size={16} /><span><strong>Administrators</strong> can manage accounts and safety settings. <strong>Operators</strong> can record events and view reports.</span></div></>;
}

const settingFields = [
  { key: "speedLimitKmh", label: "Speed limit", unit: "km/h", min: 1, max: 200, step: 1, help: "Vehicles above this speed create a violation." },
  { key: "gateDistanceMeters", label: "Checkpoint distance", unit: "meters", min: 1, max: 10000, step: 10, help: "Distance between the two vehicle checkpoints." },
  { key: "transitTtlSeconds", label: "Transit entry expiry", unit: "seconds", min: 60, max: 86400, step: 60, help: "Time before an unfinished checkpoint entry expires." },
  { key: "noiseThresholdDb", label: "Noise threshold", unit: "dB", min: 20, max: 200, step: 1, help: "Readings above this level count as excessive." },
  { key: "noiseSustainedDurationSeconds", label: "Sustained noise duration", unit: "seconds", min: 1, max: 3600, step: 1, help: "Continuous high noise before an alert is created." },
  { key: "noiseMaxSampleGapSeconds", label: "Maximum sample gap", unit: "seconds", min: 1, max: 60, step: 1, help: "A longer gap starts a new noise episode." },
];

function SettingsPage({ notify, refreshKey }) {
  const [settings, setSettings] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  useEffect(() => {
    setLoading(true);
    api("/api/admin/settings")
      .then(setSettings)
      .catch((error) => notify(error.message, "error"))
      .finally(() => setLoading(false));
  }, [notify, refreshKey]);
  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const updated = Object.fromEntries(Object.entries(settings).map(([key, value]) => [key, Number(value)]));
      setSettings(await api("/api/admin/settings", { method: "PUT", body: JSON.stringify(updated) }));
      notify("Safety settings saved and are now active.");
    } catch (error) { notify(error.message, "error"); }
    finally { setSaving(false); }
  };
  if (loading || !settings) return <><PageHeading eyebrow="CAMPUS CONFIGURATION" title="Safety settings" description="Adjust thresholds used to evaluate incoming campus events." /><div className="panel settings-panel"><PanelLoading /></div></>;
  return <><PageHeading eyebrow="CAMPUS CONFIGURATION" title="Safety settings" description="Adjust thresholds used to evaluate incoming campus events." /><form className="panel settings-panel" onSubmit={save}><div className="panel-heading settings-heading"><div><h2>Monitoring thresholds</h2><p>Changes are saved in the database and applied immediately.</p></div><span className="panel-tag"><ShieldCheck size={14} /> ADMIN ONLY</span></div><div className="settings-grid">{settingFields.map((field) => <label className="setting-field" key={field.key}><span className="setting-label">{field.label}<span>{field.unit}</span></span><input type="number" min={field.min} max={field.max} step={field.step} value={settings[field.key]} onChange={(event) => setSettings({ ...settings, [field.key]: event.target.value })} required /><small>{field.help}</small></label>)}</div><div className="settings-footer"><span><Shield size={15} />Only administrators can change these values.</span><button className="button button-primary" disabled={saving}>{saving ? <LoaderCircle className="spin" size={16} /> : <Check size={16} />}Save changes</button></div></form><div className="permission-note"><AlertTriangle size={16} /><span>New settings take effect immediately. Existing records are not recalculated when thresholds change.</span></div></>;
}

function EventRow({ event }) {
  return <div className="event-row"><span className={`event-symbol ${event.type === "speed" ? "symbol-speed" : "symbol-noise"}`}>{event.type === "speed" ? <Gauge size={17} /> : <AudioLines size={17} />}</span><span className="event-copy"><strong>{event.title}</strong><small>{event.detail}</small></span><span className="event-value">{event.value}</span><span className="event-time">{formatDate(event.at)}</span></div>;
}

function PanelLoading() {
  return <div className="panel-loading"><LoaderCircle className="spin" size={20} />Loading records…</div>;
}

function EmptyState({ icon: Icon, title, text }) {
  return <div className="empty-state"><span><Icon size={21} /></span><strong>{title}</strong><p>{text}</p></div>;
}

function ErrorBanner({ message }) {
  return <div className="error-banner"><AlertTriangle size={16} />{message}<span>Check that the API and data services are running.</span></div>;
}

export default App;
