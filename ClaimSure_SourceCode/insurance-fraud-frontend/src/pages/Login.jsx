import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../context/AuthContext";
import { errorMessage } from "../services/api";
export default function Login() {
  const [form, setForm] = useState({
    email: "",
    password: "",
  });
  const [busy, setBusy] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const change = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      const data = await login(form);
      toast.success("Welcome back!");
      navigate(
        data.roles?.includes("INVESTIGATOR") ? "/investigator" : "/dashboard",
      );
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy(false);
    }
  };
  return (
    <main className="auth-page">
      <section className="auth-visual">
        <div className="auth-brand">
          <span className="brand-icon">
            <i className="bi bi-shield-lock-fill" />
          </span>
          <span>ClaimSure</span>
        </div>

        <div className="auth-message">
          <span className="eyebrow light">INSURANCE CLAIM INTELLIGENCE</span>

          <h1>
            Every claim.
            <br />
            Every detail.
            <br />
            <span>Handled with trust.</span>
          </h1>

          <p>
            A secure workspace for submitting insurance claims, organizing
            evidence and supporting transparent fraud triage.
          </p>

          <div className="visual-points">
            <div>
              <i className="bi bi-shield-check" />
              <span>Secure evidence management</span>
            </div>
            <div>
              <i className="bi bi-search" />
              <span>Intelligent fraud triage</span>
            </div>
            <div>
              <i className="bi bi-clipboard2-check" />
              <span>Transparent claim tracking</span>
            </div>
          </div>
        </div>

        <div className="visual-foot">
          TRUST <span>•</span> TRANSPARENCY <span>•</span> TRACEABILITY
        </div>
      </section>

      <section className="auth-form-wrap">
        <div className="auth-form">
          <div className="mobile-brand">
            <span className="brand-icon">
              <i className="bi bi-shield-lock-fill" />
            </span>
            ClaimSure
          </div>

          <div className="form-heading">
            <span className="eyebrow">WELCOME BACK</span>
            <h2>Sign in to your account</h2>
            <p>Enter your credentials to access your secure workspace.</p>
          </div>

          <form onSubmit={submit}>
            <div className="auth-field">
              <label htmlFor="email">Email address</label>
              <div className="auth-input-wrap">
                <i className="bi bi-envelope" />
                <input
                  id="email"
                  className="form-control"
                  type="email"
                  name="email"
                  value={form.email}
                  onChange={change}
                  placeholder="name@example.com"
                  autoComplete="username"
                  required
                />
              </div>
            </div>

            <div className="auth-field">
              <label htmlFor="password">Password</label>
              <div className="auth-input-wrap">
                <i className="bi bi-lock" />
                <input
                  id="password"
                  className="form-control"
                  type={showPassword ? "text" : "password"}
                  name="password"
                  value={form.password}
                  onChange={change}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  required
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? "Hide password" : "Show password"}
                >
                  <i
                    className={`bi ${showPassword ? "bi-eye-slash" : "bi-eye"}`}
                  />
                </button>
              </div>
            </div>

            <button className="auth-submit" type="submit" disabled={busy}>
              {busy ? "Signing in..." : "Sign in"}
              {!busy && <i className="bi bi-arrow-right" />}
            </button>
          </form>

          <div className="auth-divider">
            <span>NEW TO CLAIMSURE?</span>
          </div>

          <p className="auth-switch">
            Don't have a claimant account?
            <Link to="/register"> Create an account</Link>
          </p>

          <div className="auth-note">
            <i className="bi bi-info-circle-fill" />
            <p>
              Investigator accounts are provisioned by an administrator. Public
              investigator registration is disabled.
            </p>
          </div>

          <div className="auth-copyright">
            © 2026 ClaimSure. Insurance claim fraud triage and evidence ledger.
          </div>
        </div>
      </section>
    </main>
  );
}
