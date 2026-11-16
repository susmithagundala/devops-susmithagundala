import React from "react";
export default function EmptyState({
  icon = "inbox",
  title = "Nothing here yet",
  text,
}) {
  return (
    <div className="empty-state">
      <i className={`bi bi-${icon}`}></i>
      <h3>{title}</h3>
      <p>{text}</p>
    </div>
  );
}
