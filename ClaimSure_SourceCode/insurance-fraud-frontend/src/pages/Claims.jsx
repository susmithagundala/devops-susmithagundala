import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { toast } from "react-toastify";
import api, { errorMessage } from "../services/api";
import StatusBadge from "../components/StatusBadge";
import EmptyState from "../components/EmptyState";
import EvidenceUploadForm from "../components/EvidenceUploadForm";
const formatSize = (bytes) => {
  if (bytes == null) return "—";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`;
  }
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
};
const formatDate = (value) => {
  if (!value) return "—";
  return new Date(value).toLocaleString("en-IN");
};
export default function Claims() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState("ALL");
  const [expanded, setExpanded] = useState(null);
  const [detailsByClaim, setDetailsByClaim] = useState({});
  const [evidenceByClaim, setEvidenceByClaim] = useState({});
  const [contentLoading, setContentLoading] = useState(false);
  const [preview, setPreview] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const clearPreview = () => {
    if (preview?.url) {
      URL.revokeObjectURL(preview.url);
    }
    setPreview(null);
  };
  useEffect(() => {
    return () => {
      if (preview?.url) {
        URL.revokeObjectURL(preview.url);
      }
    };
  }, [preview]);
  const load = async () => {
    setLoading(true);
    try {
      const { data } = await api.get("/claims");
      setItems(data || []);
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => {
    load();
  }, []);
  const loadClaimContent = async (claimId) => {
    setContentLoading(true);
    try {
      const [claimResponse, evidenceResponse] = await Promise.all([
        api.get(`/claims/${claimId}`),
        api.get(`/claims/${claimId}/evidence`),
      ]);
      setDetailsByClaim((previous) => ({
        ...previous,
        [claimId]: claimResponse.data,
      }));
      setEvidenceByClaim((previous) => ({
        ...previous,
        [claimId]: evidenceResponse.data || [],
      }));
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setContentLoading(false);
    }
  };
  const toggleClaim = async (claimId) => {
    if (expanded === claimId) {
      setExpanded(null);
      clearPreview();
      return;
    }
    setExpanded(claimId);
    clearPreview();
    await loadClaimContent(claimId);
  };
  const refreshClaimContent = async (claimId) => {
    await load();
    await loadClaimContent(claimId);
  };
  const previewEvidence = async (evidence) => {
    clearPreview();
    setPreviewLoading(true);
    try {
      const { data } = await api.get(`/evidence/${evidence.id}/download`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(data);
      setPreview({
        id: evidence.id,
        url,
        contentType: evidence.contentType,
        fileName: evidence.fileName,
      });
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setPreviewLoading(false);
    }
  };
  const downloadEvidence = async (evidence) => {
    try {
      const { data } = await api.get(`/evidence/${evidence.id}/download`, {
        responseType: "blob",
      });
      const url = URL.createObjectURL(data);
      const link = document.createElement("a");
      link.href = url;
      link.download = evidence.fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (error) {
      toast.error(errorMessage(error));
    }
  };
  const shown =
    filter === "ALL" ? items : items.filter((item) => item.status === filter);
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">CLAIMANT WORKSPACE</span>

          <h1>My claims</h1>

          <p>
            Review your submitted claims, view claim details, and manage
            supporting documents.
          </p>
        </div>

        <Link className="btn btn-primary" to="/claims/new">
          <i className="bi bi-plus-lg me-2" />
          New claim
        </Link>
      </div>

      <div className="filter-row">
        <span>Filter by status</span>

        <select
          className="form-select"
          value={filter}
          onChange={(event) => setFilter(event.target.value)}
        >
          <option value="ALL">All claims</option>

          {[
            "SUBMITTED",
            "UNDER_REVIEW",
            "ADDITIONAL_EVIDENCE_REQUESTED",
            "CLEARED_FOR_PROCESSING",
            "REJECTED",
          ].map((status) => (
            <option key={status} value={status}>
              {status.replaceAll("_", " ")}
            </option>
          ))}
        </select>

        <button className="btn btn-outline-secondary btn-sm" onClick={load}>
          <i className="bi bi-arrow-clockwise me-1" />
          Refresh
        </button>
      </div>

      <div className="panel">
        {loading ? (
          <div className="loading-state">Loading claims…</div>
        ) : shown.length === 0 ? (
          <EmptyState
            title="No matching claims"
            text="Try another status filter or submit a new claim."
          />
        ) : (
          <div className="table-responsive">
            <table className="table app-table">
              <thead>
                <tr>
                  <th>Claim number</th>
                  <th>Incident date</th>
                  <th>Location</th>
                  <th>Claimed amount</th>
                  <th>Risk</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>

              {}
              <tbody>
                {shown.map((claim) => {
                  const isExpanded = expanded === claim.id;
                  const evidence = evidenceByClaim[claim.id] || [];
                  return (
                    <React.Fragment key={claim.id}>
                      <tr>
                        <td className="fw-semibold">{claim.claimNumber}</td>

                        <td>{claim.incidentDate}</td>

                        <td>{claim.incidentLocation}</td>

                        <td>
                          ₹{Number(claim.claimedAmount).toLocaleString("en-IN")}
                        </td>

                        <td>
                          <span
                            className={`risk-text ${String(claim.riskBand).toLowerCase()}`}
                          >
                            {claim.riskBand} · {claim.riskScore}/100
                          </span>
                        </td>

                        <td>
                          <StatusBadge value={claim.status} />
                        </td>

                        <td>
                          <button
                            className="btn btn-outline-primary btn-sm"
                            onClick={() => toggleClaim(claim.id)}
                          >
                            <i
                              className={`bi ${isExpanded ? "bi-chevron-up" : "bi-eye"} me-1`}
                            />
                            {isExpanded ? "Close" : "View details"}
                          </button>
                        </td>
                      </tr>

                      {}
                      {isExpanded && (
                        <tr>
                          <td colSpan="7">
                            <div className="p-3 border rounded bg-light">
                              {contentLoading ? (
                                <div className="loading-state">
                                  Loading claim details and evidence…
                                </div>
                              ) : (
                                <>
                                  {}
                                  <div className="d-flex justify-content-between align-items-center mb-3">
                                    <h5 className="mb-0">Claim details</h5>

                                    <span className="text-muted small">
                                      Claim ID: {claim.id}
                                    </span>
                                  </div>

                                  {detailsByClaim[claim.id] && (
                                    <div className="row g-3 mb-4">
                                      <div className="col-md-6">
                                        <label>Claim number</label>

                                        <div className="fw-semibold">
                                          {detailsByClaim[claim.id].claimNumber}
                                        </div>
                                      </div>

                                      <div className="col-md-6">
                                        <label>Claim status</label>

                                        <div>
                                          <StatusBadge
                                            value={
                                              detailsByClaim[claim.id].status
                                            }
                                          />
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Policy ID</label>

                                        <div>
                                          {detailsByClaim[claim.id].policyId}
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Vehicle ID</label>

                                        <div>
                                          {detailsByClaim[claim.id].vehicleId}
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Submitted at</label>

                                        <div>
                                          {formatDate(
                                            detailsByClaim[claim.id]
                                              .submittedAt,
                                          )}
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Incident date</label>

                                        <div>
                                          {
                                            detailsByClaim[claim.id]
                                              .incidentDate
                                          }
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Claimed amount</label>

                                        <div>
                                          ₹
                                          {Number(
                                            detailsByClaim[claim.id]
                                              .claimedAmount,
                                          ).toLocaleString("en-IN")}
                                        </div>
                                      </div>

                                      <div className="col-md-4">
                                        <label>Risk assessment</label>

                                        <div>
                                          {detailsByClaim[claim.id].riskBand}
                                          {" · "}
                                          {detailsByClaim[claim.id].riskScore}
                                          /100
                                        </div>
                                      </div>

                                      <div className="col-12">
                                        <label>Incident location</label>

                                        <div>
                                          {
                                            detailsByClaim[claim.id]
                                              .incidentLocation
                                          }
                                        </div>
                                      </div>

                                      <div className="col-12">
                                        <label>Incident description</label>

                                        <div className="text-break">
                                          {
                                            detailsByClaim[claim.id]
                                              .incidentDescription
                                          }
                                        </div>
                                      </div>
                                    </div>
                                  )}

                                  <hr />

                                  {}
                                  <div className="d-flex justify-content-between align-items-center mb-3">
                                    <h5 className="mb-0">
                                      Uploaded evidence ({evidence.length})
                                    </h5>
                                  </div>

                                  {evidence.length === 0 ? (
                                    <p className="text-muted">
                                      No evidence has been uploaded for this
                                      claim yet.
                                    </p>
                                  ) : (
                                    <div className="table-responsive mb-3">
                                      <table className="table table-sm align-middle">
                                        <thead>
                                          <tr>
                                            <th>File name</th>
                                            <th>Evidence type</th>
                                            <th>File size</th>
                                            <th>Uploaded</th>
                                            <th>Actions</th>
                                          </tr>
                                        </thead>

                                        {}
                                        <tbody>
                                          {evidence.map((item) => {
                                            const hasFile =
                                              Boolean(item.fileName) &&
                                              Boolean(item.contentType) &&
                                              item.fileSize != null;
                                            return (
                                              <tr key={item.id}>
                                                <td className="fw-semibold">
                                                  {item.fileName ||
                                                    "Metadata-only invoice"}
                                                </td>

                                                <td>
                                                  {item.evidenceType?.replaceAll(
                                                    "_",
                                                    " ",
                                                  )}
                                                </td>

                                                <td>
                                                  {hasFile
                                                    ? formatSize(item.fileSize)
                                                    : "—"}
                                                </td>

                                                <td>
                                                  {formatDate(item.uploadedAt)}
                                                </td>

                                                <td>
                                                  {hasFile ? (
                                                    <div className="d-flex flex-wrap gap-2">
                                                      <button
                                                        className="btn btn-outline-primary btn-sm"
                                                        onClick={() =>
                                                          previewEvidence(item)
                                                        }
                                                        disabled={
                                                          previewLoading
                                                        }
                                                      >
                                                        <i className="bi bi-eye me-1" />
                                                        Preview
                                                      </button>

                                                      <button
                                                        className="btn btn-outline-secondary btn-sm"
                                                        onClick={() =>
                                                          downloadEvidence(item)
                                                        }
                                                      >
                                                        <i className="bi bi-download me-1" />
                                                        Download
                                                      </button>
                                                    </div>
                                                  ) : (
                                                    <span className="text-muted small">
                                                      <i className="bi bi-file-earmark-text me-1" />
                                                      Attachment not provided
                                                    </span>
                                                  )}
                                                </td>
                                              </tr>
                                            );
                                          })}
                                        </tbody>
                                      </table>
                                    </div>
                                  )}

                                  {}
                                  {preview && (
                                    <div className="border rounded bg-white p-3 mb-3">
                                      <div className="d-flex justify-content-between align-items-center mb-3">
                                        <strong>{preview.fileName}</strong>

                                        <button
                                          className="btn btn-outline-secondary btn-sm"
                                          onClick={clearPreview}
                                        >
                                          Close preview
                                        </button>
                                      </div>

                                      {preview.contentType?.startsWith(
                                        "image/",
                                      ) ? (
                                        <img
                                          src={preview.url}
                                          alt={preview.fileName}
                                          className="img-fluid rounded"
                                          style={{
                                            maxHeight: "500px",
                                            objectFit: "contain",
                                          }}
                                        />
                                      ) : preview.contentType ===
                                        "application/pdf" ? (
                                        <iframe
                                          src={preview.url}
                                          title={preview.fileName}
                                          width="100%"
                                          height="600"
                                          className="border rounded"
                                        />
                                      ) : (
                                        <p className="text-muted">
                                          Preview is not available for this file
                                          type. Please download it.
                                        </p>
                                      )}
                                    </div>
                                  )}

                                  <hr />

                                  {}
                                  <h5 className="mb-3">
                                    Upload additional evidence
                                  </h5>

                                  <EvidenceUploadForm
                                    endpoint={`/claims/${claim.id}/evidence`}
                                    onUploaded={() =>
                                      refreshClaimContent(claim.id)
                                    }
                                  />
                                </>
                              )}
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <p className="helper-text mt-3">
        Risk scores are screening indicators, not proof of fraud. “Cleared for
        processing” means the claim passed fraud screening, not final insurer
        approval or payout.
      </p>
    </>
  );
}
