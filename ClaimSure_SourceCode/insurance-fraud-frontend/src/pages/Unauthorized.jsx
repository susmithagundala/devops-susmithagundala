import React from "react";
import { Link } from "react-router-dom";
export default function Unauthorized() {
  return (
    <div className="auth-page">
      <div className="auth-form-wrap">
        <div className="auth-form">
          <h2>Access not available</h2>
          <p className="muted">
            Your account does not have permission to open this page.
          </p>
          <Link className="btn btn-primary" to="/">
            Return to workspace
          </Link>
        </div>
      </div>
    </div>
  );
}
