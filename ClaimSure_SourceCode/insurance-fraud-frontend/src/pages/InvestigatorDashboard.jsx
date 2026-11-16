import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { errorMessage } from "../services/api";
import StatCard from "../components/StatCard";
import { toast } from "react-toastify";
export default function InvestigatorDashboard() {
  const [data, setData] = useState(null);
  const [flaggedCount, setFlaggedCount] = useState(0);
  useEffect(() => {
    Promise.all([
      api.get("/dashboard/investigator"),
      api.get("/investigator/claims/flagged"),
    ])
      .then(([dashboard, queue]) => {
        setData(dashboard.data);
        setFlaggedCount((queue.data || []).length);
      })
      .catch((error) => toast.error(errorMessage(error)));
  }, []);
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">INVESTIGATOR WORKSPACE</span>
          <h1>Investigation overview</h1>
          <p>
            Monitor flagged claims, assigned investigations and review workload.
          </p>
        </div>
        <Link to="/investigator/cases" className="btn btn-primary">
          Open review workspace <i className="bi bi-arrow-right ms-2" />
        </Link>
      </div>
      <div className="stats-grid">
        <StatCard
          label="Flagged claims awaiting review"
          value={flaggedCount}
          icon="shield-exclamation"
          tone="red"
        />
        <StatCard
          label="My investigations"
          value={data?.totalInvestigations}
          icon="clipboard2-check"
          tone="blue"
        />
        <StatCard
          label="Open investigations"
          value={data?.openInvestigations}
          icon="search"
          tone="orange"
        />
        <StatCard
          label="Waiting for evidence"
          value={data?.waitingForEvidence}
          icon="folder-plus"
          tone="purple"
        />
        <StatCard
          label="High-risk claims"
          value={data?.highRiskClaims}
          icon="exclamation-triangle"
          tone="red"
        />
        <StatCard
          label="Claims under review"
          value={data?.claimsUnderReview}
          icon="hourglass-split"
          tone="blue"
        />
      </div>
      <div className="panel dashboard-callout">
        <div>
          <h2>Review flagged claims</h2>
          <p className="muted mb-0">
            Open the queue to inspect rule indicators and take ownership of a
            claim investigation.
          </p>
        </div>
        <Link to="/investigator/cases" className="btn btn-primary">
          View flagged queue
        </Link>
      </div>
      <div className="disclaimer">
        <i className="bi bi-info-circle" /> Fraud risk scores and rule flags
        support triage; they are not proof of fraud. “Cleared for processing” is
        not final insurer approval or payout confirmation.
      </div>
    </>
  );
}
