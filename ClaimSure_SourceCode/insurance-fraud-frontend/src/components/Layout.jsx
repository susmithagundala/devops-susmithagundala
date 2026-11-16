import React from "react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
export default function Layout() {
  const { user, logout, role } = useAuth();
  const nav = useNavigate();
  const investigator = role === "INVESTIGATOR";
  const links = investigator
    ? [
        ["/investigator", "Overview", "speedometer2"],
        ["/investigator/cases", "Flagged queue & cases", "clipboard2-check"],
      ]
    : [
        ["/dashboard", "Overview", "grid"],
        ["/claims/new", "Submit a claim", "plus-circle"],
        ["/claims", "My claims", "file-earmark-text"],
        ["/evidence-requests", "Evidence requests", "folder-plus"],
        ["/profile", "My Profile", "person-circle"],
      ];
  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-mark">
            <i className="bi bi-shield-check"></i>
          </span>
          <span>
            Claim<span className="brand-light">Sure</span>
            <small>FRAUD TRIAGE & EVIDENCE</small>
          </span>
        </div>
        <div className="nav-caption">WORKSPACE</div>
        {links.map(([to, label, icon]) => (
          <NavLink
            key={to}
            to={to}
            end
            className={({ isActive }) =>
              "side-link " + (isActive ? "active" : "")
            }
          >
            <i className={`bi bi-${icon}`}></i>
            {label}
          </NavLink>
        ))}
        <div className="sidebar-bottom">
          <div className="profile-chip">
            <div className="avatar">
              {user?.email?.[0]?.toUpperCase() || "U"}
            </div>
            <div className="profile-copy">
              <strong>{user?.email}</strong>
              <small>{investigator ? "Investigator" : "Claimant"}</small>
            </div>
          </div>
          <button
            className="logout-btn"
            onClick={() => {
              logout();
              nav("/login");
            }}
          >
            <i className="bi bi-box-arrow-right"></i> Sign out
          </button>
        </div>
      </aside>
      <main className="main-area">
        <header className="topbar">
          <div>
            <span className="eyebrow">INSURANCE CLAIMS PLATFORM</span>
            <div className="topbar-title">
              Good{" "}
              {new Date().getHours() < 12
                ? "morning"
                : new Date().getHours() < 18
                  ? "afternoon"
                  : "evening"}
            </div>
          </div>
          <div className="topbar-right">
            <span className="secure-pill">
              <i className="bi bi-lock-fill"></i> Secure workspace
            </span>
            <div className="avatar">
              {user?.email?.[0]?.toUpperCase() || "U"}
            </div>
          </div>
        </header>
        <section className="page-content">
          <Outlet />
        </section>
      </main>
    </div>
  );
}
