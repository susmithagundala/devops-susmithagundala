import React, { useState } from "react";
import { toast } from "react-toastify";
import api, { errorMessage } from "../services/api";

const TYPES = [
  ["OTHER", "Other"],
  ["REPAIR_INVOICE", "Repair invoice"],
  ["ACCIDENT_PHOTO", "Accident photo"],
  ["POLICE_REPORT", "Police report"],
  ["VEHICLE_DOCUMENT", "Vehicle document"],
  ["DRIVING_LICENCE", "Driving licence"],
  ["MEDICAL_REPORT", "Medical report"],
];

const EMPTY_INVOICE = {
  invoiceNumber: "",
  repairerName: "",
  repairerGstin: "",
  repairerPhone: "",
  invoiceDate: "",
  invoiceAmount: "",
};

export default function EvidenceUploadForm({
  endpoint,
  onUploaded,
  compact = false,
}) {
  const [file, setFile] = useState(null);
  const [evidenceType, setEvidenceType] = useState("OTHER");
  const [description, setDescription] = useState("");
  const [invoice, setInvoice] = useState(EMPTY_INVOICE);
  const [busy, setBusy] = useState(false);

  const isRepairInvoice = evidenceType === "REPAIR_INVOICE";

  const changeInvoice = (e) => {
    const { name, value } = e.target;

    setInvoice((previous) => ({
      ...previous,
      [name]: value,
    }));
  };

  const submit = async (e) => {
    e.preventDefault();

    if (!isRepairInvoice && !file) {
      toast.error("Choose an evidence file first.");
      return;
    }

    if (file && file.size > 10 * 1024 * 1024) {
      toast.error("Maximum file size is 10 MB.");
      return;
    }

    const body = new FormData();

    if (file) {
      body.append("file", file);
    }

    body.append("evidenceType", evidenceType);

    if (description.trim()) {
      body.append("description", description.trim());
    }

    if (isRepairInvoice) {
      body.append("invoiceNumber", invoice.invoiceNumber.trim());
      body.append("repairerName", invoice.repairerName.trim());

      if (invoice.repairerGstin.trim()) {
        body.append("repairerGstin", invoice.repairerGstin.trim());
      }

      if (invoice.repairerPhone.trim()) {
        body.append("repairerPhone", invoice.repairerPhone.trim());
      }

      body.append("invoiceDate", invoice.invoiceDate);
      body.append("invoiceAmount", invoice.invoiceAmount);
    }

    setBusy(true);

    try {
      await api.post(endpoint, body);

      toast.success(
        isRepairInvoice && !file
          ? "Repair invoice details saved successfully."
          : "Evidence uploaded successfully."
      );

      setFile(null);
      setDescription("");
      setEvidenceType("OTHER");
      setInvoice({ ...EMPTY_INVOICE });

      e.target.reset();

      onUploaded?.();
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form
      onSubmit={submit}
      className={
        compact
          ? "evidence-form compact-evidence-form"
          : "evidence-form"
      }
    >
      <div className="row g-2 align-items-end">

        <div className="col-md-4">
          <label>Evidence type</label>

          <select
            className="form-select form-select-sm"
            value={evidenceType}
            onChange={(e) => setEvidenceType(e.target.value)}
          >
            {TYPES.map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </select>
        </div>

        <div className="col-md-5">
          <label>
            Choose file
            {isRepairInvoice
              ? " (optional for repair invoices)"
              : " (required)"}
          </label>

          <input
            className="form-control form-control-sm"
            type="file"
            accept=".pdf,.jpg,.jpeg,.png,.webp,application/pdf,image/jpeg,image/png,image/webp"
            required={!isRepairInvoice}
            onChange={(e) => {
              setFile(e.target.files?.[0] || null);
            }}
          />

          <small className="text-muted">
            PDF, JPG, PNG or WebP. Maximum 10 MB.
          </small>
        </div>

        <div className="col-md-3">
          <button
            type="submit"
            className="btn btn-primary btn-sm w-100"
            disabled={busy}
          >
            {busy ? "Saving…" : "Submit evidence"}
          </button>
        </div>

        <div className="col-12">
          <label>Description</label>

          <input
            className="form-control form-control-sm"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            maxLength={1000}
            placeholder="Briefly describe this evidence"
          />
        </div>
      </div>

      {isRepairInvoice && (
        <div className="invoice-fields mt-3">

          <div className="small fw-bold mb-2">
            Repair invoice details (required)
          </div>

          <div className="alert alert-info py-2 small">
            You can save repair invoice details without attaching
            an invoice photo or PDF. Attach a file if one is available.
          </div>

          <div className="row g-2">

            <div className="col-md-4">
              <label>Invoice number *</label>

              <input
                className="form-control form-control-sm"
                name="invoiceNumber"
                value={invoice.invoiceNumber}
                onChange={changeInvoice}
                required
                maxLength={100}
              />
            </div>

            <div className="col-md-4">
              <label>Repairer name *</label>

              <input
                className="form-control form-control-sm"
                name="repairerName"
                value={invoice.repairerName}
                onChange={changeInvoice}
                required
                maxLength={255}
              />
            </div>

            <div className="col-md-4">
              <label>Repairer GSTIN</label>

              <input
                className="form-control form-control-sm"
                name="repairerGstin"
                value={invoice.repairerGstin}
                onChange={changeInvoice}
                maxLength={15}
                placeholder="Optional"
              />
            </div>

            <div className="col-md-4">
              <label>Repairer phone</label>

              <input
                className="form-control form-control-sm"
                name="repairerPhone"
                value={invoice.repairerPhone}
                onChange={changeInvoice}
                placeholder="Optional"
              />
            </div>

            <div className="col-md-4">
              <label>Invoice date *</label>

              <input
                className="form-control form-control-sm"
                type="date"
                name="invoiceDate"
                value={invoice.invoiceDate}
                onChange={changeInvoice}
                max={new Date().toISOString().slice(0, 10)}
                required
              />
            </div>

            <div className="col-md-4">
              <label>Invoice amount (₹) *</label>

              <input
                className="form-control form-control-sm"
                type="number"
                name="invoiceAmount"
                value={invoice.invoiceAmount}
                onChange={changeInvoice}
                min="0.01"
                step="0.01"
                required
              />
            </div>

          </div>
        </div>
      )}
    </form>
  );
}
