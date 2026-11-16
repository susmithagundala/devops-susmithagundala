import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import api, { errorMessage } from "../services/api";
import EmptyState from "../components/EmptyState";
import StatusBadge from "../components/StatusBadge";
export default function Investigations() {
  const [items, setItems] = useState([]);
  const [flagged, setFlagged] = useState([]);
  const [loading, setLoading] = useState(true);
  const [busyClaim, setBusyClaim] = useState(null);
  const nav = useNavigate();
  const load = async () => {
    setLoading(true);
    try {
      const [assigned, queue] = await Promise.all([
        api.get("/investigator/investigations"),
        api.get("/investigator/claims/flagged"),
      ]);
      setItems(assigned.data || []);
      setFlagged(queue.data || []);
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    load();
  }, []);
  const openInvestigation = async (claimId) => {
    setBusyClaim(claimId);
    try {
      const { data } = await api.post(`/investigator/claims/${claimId}/open`);
      toast.success("Investigation opened and assigned to you.");
      await load();
      nav(`/investigator/cases/${data.id}`);
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusyClaim(null);
    }
  };
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">INVESTIGATOR WORKSPACE</span>
          <h1>Flagged claims & investigations</h1>
          <p>
            Review claims awaiting investigation and manage cases assigned to
            you.
          </p>
        </div>
        <button className="btn btn-outline-secondary" onClick={load}>
          <i className="bi bi-arrow-clockwise me-2" />
          Refresh
        </button>
      </div>

      <div className="section-heading">
        <div>
          <h2>Flagged claims queue</h2>
          <p>
            Claims with risk flags that have not yet been opened as
            investigations.
          </p>
        </div>
        <span className="status-badge warning">
          {flagged.length} awaiting review
        </span>
      </div>
      <div className="panel">
        {loading ? (
          <div className="loading-state">Loading queue…</div>
        ) : flagged.length === 0 ? (
          <EmptyState
            icon="shield-check"
            title="No unassigned flagged claims"
            text="New claims that require investigation will appear here."
          />
        ) : (
          <div className="table-responsive">
            <table className="table app-table">
              <thead>
                <tr>
                  <th>Claim</th>
                  <th>Incident date</th>
                  <th>Amount</th>
                  <th>Risk</th>
                  <th>Fraud indicators</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {flagged.map((row) => {
                  const c = row.claim || {};
                  return (
                    <tr key={c.id}>
                      <td>
                        <strong>{c.claimNumber}</strong>
                        <small className="d-block text-muted">ID: {c.id}</small>
                      </td>
                      <td>{c.incidentDate}</td>
                      <td>
                        ₹{Number(c.claimedAmount).toLocaleString("en-IN")}
                      </td>
                      <td>
                        <span
                          className={`risk-text ${String(c.riskBand).toLowerCase()}`}
                        >
                          {c.riskBand} · {c.riskScore}/100
                        </span>
                      </td>
                      <td>
                        <div className="flag-list">
                          {(row.flags || []).map((f) => (
                            <span className="flag-chip" key={f.id}>
                              {f.ruleCode} (+{f.scoreContribution})
                            </span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <button
                          className="btn btn-primary btn-sm"
                          disabled={busyClaim === c.id}
                          onClick={() => openInvestigation(c.id)}
                        >
                          {busyClaim === c.id ? "Opening…" : "Open case"}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <div className="section-heading mt-4">
        <div>
          <h2>My investigations</h2>
          <p>Cases currently assigned to your investigator account.</p>
        </div>
      </div>
      <div className="panel">
        {loading ? (
          <div className="loading-state">Loading investigations…</div>
        ) : items.length === 0 ? (
          <EmptyState
            title="No assigned investigations"
            text="Open a flagged claim above to begin an investigation."
          />
        ) : (
          <div className="table-responsive">
            <table className="table app-table">
              <thead>
                <tr>
                  <th>Investigation</th>
                  <th>Claim number</th>
                  <th>Risk</th>
                  <th>Status</th>
                  <th>Started</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {items.map((i) => (
                  <tr key={i.id}>
                    <td className="fw-semibold">INV-{i.id}</td>
                    <td>
                      {i.claim?.claimNumber || `Claim ${i.claim?.id ?? "—"}`}
                    </td>
                    <td>
                      <span
                        className={`risk-text ${String(i.claim?.riskBand).toLowerCase()}`}
                      >
                        {i.claim?.riskBand || "—"} · {i.claim?.riskScore ?? "—"}
                      </span>
                    </td>
                    <td>
                      <StatusBadge value={i.status} />
                    </td>
                    <td>
                      {i.startedAt
                        ? new Date(i.startedAt).toLocaleString()
                        : "—"}
                    </td>
                    <td>
                      <Link
                        className="text-link"
                        to={`/investigator/cases/${i.id}`}
                      >
                        Review <i className="bi bi-arrow-right" />
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <p className="helper-text mt-3">
        Risk scores and flags are indicators for human review, not proof of
        fraud.
      </p>
    </>
  );
}
