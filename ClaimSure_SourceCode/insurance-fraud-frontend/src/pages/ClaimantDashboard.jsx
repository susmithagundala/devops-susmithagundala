import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errorMessage } from "../services/api";
import StatCard from "../components/StatCard";
import StatusBadge from "../components/StatusBadge";
import EmptyState from "../components/EmptyState";
import { toast } from "react-toastify";
export default function ClaimantDashboard() {
  const [claims, setClaims] = useState([]);
  const [pendingRequests, setPendingRequests] = useState(0);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    Promise.all([api.get("/claims"), api.get("/evidence-requests")])
      .then(([claimResponse, requestResponse]) => {
        setClaims(claimResponse.data || []);
        setPendingRequests((requestResponse.data || []).length);
      })
      .catch((error) => toast.error(errorMessage(error)))
      .finally(() => setLoading(false));
  }, []);
  const count = (status) => claims.filter((c) => c.status === status).length;
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">CLAIMANT WORKSPACE</span>
          <h1>Your claims at a glance</h1>
          <p>Track submissions, fraud screening and evidence requests.</p>
        </div>
        <Link className="btn btn-primary" to="/claims/new">
          <i className="bi bi-plus-lg me-2" />
          Submit a claim
        </Link>
      </div>
      <div className="stats-grid">
        <StatCard
          label="Total claims"
          value={claims.length}
          icon="files"
          tone="blue"
        />
        <StatCard
          label="Under review"
          value={count("UNDER_REVIEW")}
          icon="search"
          tone="orange"
        />
        <StatCard
          label="Evidence requested"
          value={count("ADDITIONAL_EVIDENCE_REQUESTED")}
          icon="folder-plus"
          tone="purple"
        />
        <StatCard
          label="Pending responses"
          value={pendingRequests}
          icon="file-earmark-arrow-up"
          tone="red"
        />
        <StatCard
          label="Cleared for processing"
          value={count("CLEARED_FOR_PROCESSING")}
          icon="check-circle"
          tone="green"
        />
      </div>
      <div className="section-heading">
        <div>
          <h2>Recent claims</h2>
          <p>Your latest claim activity</p>
        </div>
        <Link to="/claims" className="text-link">
          View all <i className="bi bi-arrow-right" />
        </Link>
      </div>
      <div className="panel">
        {loading ? (
          <div className="loading-state">Loading claims…</div>
        ) : claims.length === 0 ? (
          <EmptyState
            icon="file-earmark-plus"
            title="No claims submitted yet"
            text="When you submit your first claim, its status and screening result will appear here."
          />
        ) : (
          <div className="table-responsive">
            <table className="table app-table">
              <thead>
                <tr>
                  <th>Claim number</th>
                  <th>Incident date</th>
                  <th>Amount</th>
                  <th>Risk</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {claims.slice(0, 5).map((c) => (
                  <tr key={c.id}>
                    <td className="fw-semibold">{c.claimNumber}</td>
                    <td>{c.incidentDate}</td>
                    <td>₹{Number(c.claimedAmount).toLocaleString("en-IN")}</td>
                    <td>
                      <span
                        className={`risk-text ${String(c.riskBand).toLowerCase()}`}
                      >
                        {c.riskBand} · {c.riskScore}
                      </span>
                    </td>
                    <td>
                      <StatusBadge value={c.status} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      {pendingRequests > 0 && (
        <div className="alert alert-warning mt-3">
          You have {pendingRequests} pending evidence request(s).{" "}
          <Link to="/evidence-requests">Respond now</Link>.
        </div>
      )}
      <div className="disclaimer">
        <i className="bi bi-shield-exclamation" /> Fraud risk scores are
        screening indicators, not proof of fraud. “Cleared for processing” means
        the claim passed fraud screening; it is not final insurer approval or
        payout confirmation.
      </div>
    </>
  );
}
