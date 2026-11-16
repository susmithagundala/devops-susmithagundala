import React, { useEffect, useState } from "react";
import api, { errorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import EmptyState from "../components/EmptyState";
import EvidenceUploadForm from "../components/EvidenceUploadForm";
import { toast } from "react-toastify";
export default function EvidenceRequests() {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(null);
  const load = async () => {
    setLoading(true);
    try {
      const { data } = await api.get("/evidence-requests");
      setRequests(data || []);
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    load();
  }, []);
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">CLAIMANT WORKSPACE</span>
          <h1>Evidence requests</h1>
          <p>Upload documents requested by the investigator for your claim.</p>
        </div>
        <button className="btn btn-outline-secondary" onClick={load}>
          <i className="bi bi-arrow-clockwise me-2" />
          Refresh
        </button>
      </div>
      <div className="panel">
        {loading ? (
          <div className="loading-state">Loading evidence requests…</div>
        ) : requests.length === 0 ? (
          <EmptyState
            icon="folder-check"
            title="No pending evidence requests"
            text="When an investigator requests additional documents, the request will appear here."
          />
        ) : (
          <div className="table-responsive">
            <table className="table app-table">
              <thead>
                <tr>
                  <th>Claim</th>
                  <th>Requested evidence</th>
                  <th>Requested on</th>
                  <th>Due date</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {requests.map((r) => (
                  <React.Fragment key={r.id}>
                    <tr>
                      <td>
                        <strong>{r.claimNumber}</strong>
                        <small className="d-block text-muted">
                          Claim ID: {r.claimId}
                        </small>
                      </td>
                      <td>{r.description}</td>
                      <td>
                        {r.requestedAt
                          ? new Date(r.requestedAt).toLocaleDateString()
                          : "—"}
                      </td>
                      <td>
                        {r.dueAt ? new Date(r.dueAt).toLocaleDateString() : "—"}
                      </td>
                      <td>
                        <StatusBadge value={r.status} />
                      </td>
                      <td>
                        {r.status === "PENDING" ? (
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() =>
                              setExpanded(expanded === r.id ? null : r.id)
                            }
                          >
                            {expanded === r.id ? "Close" : "Upload response"}
                          </button>
                        ) : (
                          "—"
                        )}
                      </td>
                    </tr>
                    {expanded === r.id && (
                      <tr>
                        <td colSpan="6">
                          <EvidenceUploadForm
                            endpoint={`/evidence-requests/${r.id}/evidence`}
                            onUploaded={load}
                          />
                        </td>
                      </tr>
                    )}
                  </React.Fragment>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <div className="disclaimer">
        <i className="bi bi-info-circle" /> For repair invoices, select “Repair
        invoice” and complete the invoice details. Supported files: PDF, JPEG,
        PNG and WebP; maximum 10 MB.
      </div>
    </>
  );
}
