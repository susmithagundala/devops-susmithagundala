import React from "react";
export default function StatCard({ label, value, icon, tone = "blue", hint }) {
  return (
    <div className="stat-card">
      <div className={`stat-icon ${tone}`}>
        <i className={`bi bi-${icon}`} />
      </div>
      <div>
        <div className="stat-label">{label}</div>
        <div className="stat-value">{value ?? "—"}</div>
        {hint && <div className="stat-hint">{hint}</div>}
      </div>
    </div>
  );
}
