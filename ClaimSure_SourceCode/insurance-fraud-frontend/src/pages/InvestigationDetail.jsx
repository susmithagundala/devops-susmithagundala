import React, { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { errorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import { toast } from "react-toastify";

const formatAmount = (value) =>
  value != null ? `₹${Number(value).toLocaleString("en-IN")}` : "—";

const formatDate = (value) =>
  value ? new Date(value).toLocaleDateString("en-IN") : "—";

const formatDateTime = (value) =>
  value ? new Date(value).toLocaleString("en-IN") : "—";

const display = (value) =>
  value !== null && value !== undefined && value !== "" ? value : "—";

// Format match details safely, whether returned as an object or JSON string.
function formatMatchDetails(value) {
  if (value === null || value === undefined || value === "") {
    return "No technical match details available.";
  }

  let details = value;

  if (typeof details === "string") {
    try {
      details = JSON.parse(details);
    } catch {
      return details;
    }
  }

  if (
    typeof details === "object" &&
    !Array.isArray(details) &&
    Object.keys(details).length === 0
  ) {
    return "No technical match details available.";
  }

  try {
    return JSON.stringify(details, null, 2);
  } catch {
    return "Unable to display technical match details.";
  }
}

function DetailField({ label, value }) {
  return (
    <div>
      <small>{label}</small>
      <strong>{display(value)}</strong>
    </div>
  );
}

// Entity match card uses the actual EntityMatchSummary DTO.
function MatchCard({ match }) {
  const confidence =
    match.confidenceScore != null
      ? `${(Number(match.confidenceScore) * 100).toFixed(1)}%`
      : "—";

  const technicalDetails = formatMatchDetails(match.matchDetails);

  return (
    <div className="border rounded-3 p-3 mb-3">
      <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
        <div>
          <h5 className="mb-1">
            Match #{match.id} — {display(match.matchType)}
          </h5>

          <small className="text-muted">
            Confidence: {confidence}
          </small>
        </div>

        <StatusBadge value={match.reviewStatus} />
      </div>

      
    </div>
  );
}

export default function InvestigationDetail() {
  const { id } = useParams();

  const [item, setItem] = useState(null);
  const [notes, setNotes] = useState([]);
  const [requests, setRequests] = useState([]);
  const [matches, setMatches] = useState([]);

  const [note, setNote] = useState("");
  const [description, setDescription] = useState("");
  const [reason, setReason] = useState("");

  const [decision, setDecision] = useState("CLEARED_FOR_PROCESSING");

  const [busy, setBusy] = useState(false);
  const [loading, setLoading] = useState(true);

  const load = useCallback(async () => {
    setLoading(true);

    try {
      const [a, b, c] = await Promise.all([
        api.get(`/investigator/investigations/${id}`),
        api.get(`/investigator/investigations/${id}/notes`),
        api.get(`/investigator/investigations/${id}/evidence-requests`),
      ]);

      setItem(a.data);
      setNotes(b.data || []);
      setRequests(c.data || []);

      const claimId = a.data.claim?.id;

      if (claimId) {
        const matchResponse = await api.get(
          `/investigator/claims/${claimId}/matches`
        );

        setMatches(matchResponse.data || []);
      } else {
        setMatches([]);
      }
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    load();
  }, [load]);

  const act = async (fn, message) => {
    setBusy(true);

    try {
      await fn();
      toast.success(message);
      await load();
      return true;
    } catch (error) {
      toast.error(errorMessage(error));
      return false;
    } finally {
      setBusy(false);
    }
  };

  const submitNote = async (e) => {
    e.preventDefault();

    const value = note.trim();

    if (!value) return;

    const success = await act(
      () =>
        api.post(`/investigator/investigations/${id}/notes`, {
          note: value,
        }),
      "Note added"
    );

    if (success) {
      setNote("");
    }
  };

  const submitRequest = async (e) => {
    e.preventDefault();

    const value = description.trim();

    if (!value) return;

    const success = await act(
      () =>
        api.post(`/investigator/investigations/${id}/evidence-requests`, {
          description: value,
        }),
      "Evidence requested"
    );

    if (success) {
      setDescription("");
    }
  };

  const submitDecision = (e) => {
    e.preventDefault();

    if (decision === "REJECTED" && !reason.trim()) {
      toast.error("A rejection reason is mandatory.");
      return;
    }

    if (
      !window.confirm(
        `Confirm decision: ${decision.replaceAll("_", " ")}?`
      )
    ) {
      return;
    }

    act(
      () =>
        api.post(`/investigator/investigations/${id}/decision`, {
          decision,
          reason: reason.trim(),
        }),
      "Decision recorded"
    );
  };

  if (loading && !item) {
    return (
      <div className="panel loading-state">
        Loading investigation…
      </div>
    );
  }

  if (!item) {
    return (
      <div className="panel">
        <p>Investigation could not be loaded.</p>

        <Link to="/investigator/cases">
          Back to investigations
        </Link>
      </div>
    );
  }

  const claim = item.claim || {};
  const completed = item.status === "COMPLETED";

  const pendingRequests = requests.filter(
    (request) => request.status === "PENDING"
  ).length;

  return (
    <>
      {/* Page heading */}
      <div className="page-heading">
        <div>
          <Link to="/investigator/cases" className="back-link">
            <i className="bi bi-arrow-left" /> Flagged queue & cases
          </Link>

          <span className="eyebrow d-block mt-3">
            CASE INV-{id}
          </span>

          <h1>Investigation details</h1>

          <p>
            Review claim context, investigate fraud indicators, record
            findings and make a screening decision.
          </p>
        </div>

        <StatusBadge value={item.status} />
      </div>

      <div className="detail-grid">
        {/* Claim overview and entity matches */}
        <div className="panel detail-main">
          <h2>Claim overview</h2>

          <div className="detail-fields">
            {[
              ["Claim ID", claim.id],
              ["Claim number", claim.claimNumber],
              ["Incident date", formatDate(claim.incidentDate)],
              ["Location", claim.incidentLocation],
              ["Claimed amount", formatAmount(claim.claimedAmount)],
              [
                "Risk score",
                claim.riskScore != null
                  ? `${claim.riskScore}/100`
                  : "—",
              ],
              ["Risk band", claim.riskBand],
              ["Claim status", claim.status],
              ["Assigned investigator", item.investigator?.email],
            ].map(([key, value]) => (
              <DetailField
                key={key}
                label={key}
                value={value}
              />
            ))}
          </div>

          <hr />

          <h2>Fraud indicators and entity matches</h2>

          <p className="muted small">
            These matches identify potential relationships between claims
            and repair invoice records. They require manual verification
            and do not independently establish fraud.
          </p>

          {matches.length === 0 ? (
            <p className="muted">
              No entity matches are currently associated with this claim.
            </p>
          ) : (
            matches.map((match) => (
              <MatchCard key={match.id} match={match} />
            ))
          )}
        </div>

        {/* Investigation actions */}
        <div className="panel">
          <h2>Investigation actions</h2>

          {completed ? (
            <div className="alert alert-secondary small">
              This investigation is completed. No further actions
              are available.
            </div>
          ) : (
            <>
              {/* Add investigation note */}
              <form onSubmit={submitNote}>
                <label>Add investigation note</label>

                <textarea
                  className="form-control"
                  rows="3"
                  value={note}
                  onChange={(e) => setNote(e.target.value)}
                  required
                  placeholder="Record your observation…"
                />

                <button
                  className="btn btn-outline-primary w-100 mt-2"
                  disabled={busy}
                >
                  Add note
                </button>
              </form>

              <hr />

              {/* Request additional evidence */}
              <form onSubmit={submitRequest}>
                <label>Request additional evidence</label>

                <textarea
                  className="form-control"
                  rows="3"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  required
                  placeholder="Describe the document or information needed…"
                />

                <button
                  className="btn btn-outline-primary w-100 mt-2"
                  disabled={busy}
                >
                  Send evidence request
                </button>
              </form>

              <hr />

              {/* Final decision */}
              <form onSubmit={submitDecision}>
                <label>Final fraud-screening decision</label>

                <select
                  className="form-select mb-2"
                  value={decision}
                  onChange={(e) => setDecision(e.target.value)}
                >
                  <option value="CLEARED_FOR_PROCESSING">
                    Clear for processing
                  </option>

                  <option value="REJECTED">
                    Reject claim
                  </option>
                </select>

                <label>
                  Reason {decision === "REJECTED" ? "*" : "(optional)"}
                </label>

                <textarea
                  className="form-control"
                  rows="2"
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  required={decision === "REJECTED"}
                  placeholder="Explain the decision…"
                />

                {pendingRequests > 0 && (
                  <div className="alert alert-warning small mt-2 mb-2">
                    There are {pendingRequests} pending evidence
                    request(s). Resolve them before recording a decision.
                  </div>
                )}

                <button
                  className="btn btn-primary w-100 mt-2"
                  disabled={busy || pendingRequests > 0}
                >
                  Record decision
                </button>
              </form>
            </>
          )}
        </div>
      </div>

      {/* Investigation notes and evidence requests */}
      <div className="panel mt-4">
        <h2>Investigation notes</h2>

        {notes.length ? (
          notes.map((n) => (
            <div className="timeline-item" key={n.id}>
              <div className="timeline-dot" />

              <div>
                <strong>
                  {n.author?.email || "Investigator"}
                </strong>

                <p>{n.note}</p>

                <small>
                  {formatDateTime(n.createdAt)}
                </small>
              </div>
            </div>
          ))
        ) : (
          <p className="muted">
            No notes have been added.
          </p>
        )}

        <h2 className="mt-4">Evidence requests</h2>

        {requests.length ? (
          requests.map((request) => (
            <div className="request-row" key={request.id}>
              <div>
                <strong>{request.description}</strong>

                <small>
                  Requested {formatDateTime(request.requestedAt)}
                  {request.dueAt
                    ? ` · Due ${formatDate(request.dueAt)}`
                    : ""}
                </small>
              </div>

              <StatusBadge value={request.status} />
            </div>
          ))
        ) : (
          <p className="muted">
            No evidence requests.
          </p>
        )}
      </div>

      <p className="helper-text mt-3">
        Fraud flags and entity matches are review indicators, not proof
        of fraud. A cleared claim has passed fraud screening only; it
        is not a final payout decision.
      </p>
    </>
  );
}
