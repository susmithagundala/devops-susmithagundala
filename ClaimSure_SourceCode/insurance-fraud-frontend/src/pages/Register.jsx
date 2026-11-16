import React from "react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import { useAuth } from "../context/AuthContext";
import { errorMessage } from "../services/api";
export default function Register() {
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    password: "",
  });
  const [busy, setBusy] = useState(false);
  const { register } = useAuth();
  const nav = useNavigate();
  const change = (e) =>
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    try {
      await register(form);
      toast.success("Account created");
      nav("/dashboard");
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="auth-page">
      <div className="auth-visual">
        <div className="auth-brand">
          <i className="bi bi-shield-check" /> ClaimSure
        </div>
        <div className="auth-message">
          <span className="eyebrow light">YOUR CLAIM, YOUR RECORD</span>
          <h1>Keep your claim journey organized.</h1>
          <p>
            Submit your claim and keep important supporting documents in one
            secure place.
          </p>
        </div>
        <div className="visual-foot">
          INSURANCE CLAIM FRAUD TRIAGE & EVIDENCE LEDGER
        </div>
      </div>
      <div className="auth-form-wrap">
        <form className="auth-form" onSubmit={submit}>
          <span className="eyebrow">GET STARTED</span>
          <h2>Create claimant account</h2>
          <p className="muted">All fields marked * are required.</p>
          <div className="row g-3">
            <div className="col-6">
              <label>First name *</label>
              <input
                className="form-control"
                name="firstName"
                value={form.firstName}
                onChange={change}
                required
              />
            </div>
            <div className="col-6">
              <label>Last name *</label>
              <input
                className="form-control"
                name="lastName"
                value={form.lastName}
                onChange={change}
                required
              />
            </div>
            <div className="col-12">
              <label>Email address *</label>
              <input
                className="form-control"
                type="email"
                name="email"
                value={form.email}
                onChange={change}
                required
              />
            </div>
            <div className="col-12">
              <label>Phone number</label>
              <input
                className="form-control"
                name="phone"
                value={form.phone}
                onChange={change}
              />
            </div>
            <div className="col-12">
              <label>Password * (8–72 characters)</label>
              <input
                className="form-control"
                type="password"
                minLength="8"
                maxLength="72"
                name="password"
                value={form.password}
                onChange={change}
                required
              />
            </div>
          </div>
          <button className="btn btn-primary w-100 mt-4" disabled={busy}>
            {busy ? "Creating account…" : "Create account"}{" "}
            <i className="bi bi-arrow-right ms-2" />
          </button>
          <p className="auth-switch">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  );
}
