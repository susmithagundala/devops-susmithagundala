import React from "react";
export default function StatusBadge({ value }) {
  const v = (value || "UNKNOWN").toString();
  const cls = v.includes("CLEARED")
    ? "success"
    : v === "REJECTED"
      ? "danger"
      : v.includes("EVIDENCE")
        ? "warning"
        : v === "UNDER_REVIEW"
          ? "info"
          : "secondary";
  return (
    <span className={`status-badge ${cls}`}>
      {v
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (c) => c.toUpperCase())}
    </span>
  );
}
