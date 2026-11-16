import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import api, { errorMessage } from "../services/api";
const today = new Date().toISOString().slice(0, 10);
const emptyProfile = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  dateOfBirth: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
};
export default function SubmitClaim() {
  const [profile, setProfile] = useState(emptyProfile);
  const [policies, setPolicies] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [loadingProfile, setLoadingProfile] = useState(true);
  const [loadingPolicies, setLoadingPolicies] = useState(true);
  const [loadingVehicles, setLoadingVehicles] = useState(false);
  const [form, setForm] = useState({
    policyId: "",
    vehicleId: "",
    incidentDate: "",
    incidentLocation: "",
    incidentDescription: "",
    claimedAmount: "",
  });
  const [busy, setBusy] = useState(false);
  const nav = useNavigate();
  useEffect(() => {
    api
      .get("/customers/me")
      .then(({ data }) => {
        setProfile({
          ...emptyProfile,
          ...data,
        });
      })
      .catch((error) => {
        toast.error(errorMessage(error));
      })
      .finally(() => {
        setLoadingProfile(false);
      });
  }, []);
  useEffect(() => {
    api
      .get("/policies")
      .then(({ data }) => {
        setPolicies(data || []);
      })
      .catch((error) => {
        toast.error(errorMessage(error));
      })
      .finally(() => {
        setLoadingPolicies(false);
      });
  }, []);
  useEffect(() => {
    if (!form.policyId) {
      setVehicles([]);
      return;
    }
    setLoadingVehicles(true);
    api
      .get(`/policies/${form.policyId}/vehicles`)
      .then(({ data }) => {
        setVehicles(data || []);
      })
      .catch((error) => {
        setVehicles([]);
        toast.error(errorMessage(error));
      })
      .finally(() => {
        setLoadingVehicles(false);
      });
  }, [form.policyId]);
  const change = (event) => {
    const { name, value } = event.target;
    setForm((previous) => ({
      ...previous,
      [name]: value,
      ...(name === "policyId"
        ? {
            vehicleId: "",
          }
        : {}),
    }));
  };
  const selectedPolicy = policies.find(
    (policy) => String(policy.id) === String(form.policyId),
  );
  const selectedVehicle = vehicles.find(
    (vehicle) => String(vehicle.id) === String(form.vehicleId),
  );
  const submit = async (event) => {
    event.preventDefault();
    if (!selectedPolicy || !selectedVehicle) {
      toast.error("Please select a valid policy and insured vehicle.");
      return;
    }
    setBusy(true);
    const payload = {
      policyId: Number(form.policyId),
      vehicleId: Number(form.vehicleId),
      incidentDate: form.incidentDate,
      incidentLocation: form.incidentLocation.trim(),
      incidentDescription: form.incidentDescription.trim(),
      claimedAmount: Number(form.claimedAmount),
    };
    try {
      const { data } = await api.post("/claims", payload);
      toast.success(`Claim ${data.claimNumber} submitted successfully.`);
      nav("/claims");
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setBusy(false);
    }
  };
  const fullAddress = [
    profile.addressLine1,
    profile.addressLine2,
    profile.city,
    profile.state,
    profile.postalCode,
    profile.country,
  ]
    .filter(Boolean)
    .join(", ");
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">NEW SUBMISSION</span>
          <h1>Submit an insurance claim</h1>
          <p>
            Verify your registered information, select your insured vehicle, and
            provide the incident details.
          </p>
        </div>
      </div>

      <div className="form-panel">
        {}
        <div className="form-intro">
          <div className="form-icon">
            <i className="bi bi-person-check" />
          </div>
          <div>
            <h2>Registered claimant information</h2>
            <p>
              Retrieved from your account. No need to enter these details again.
            </p>
          </div>
        </div>

        {loadingProfile ? (
          <div className="panel loading-state">
            Loading your registered information...
          </div>
        ) : (
          <div className="border rounded p-3 mb-4 bg-light">
            <div className="row g-3">
              <div className="col-md-6">
                <label>Claimant name</label>
                <input
                  className="form-control"
                  value={`${profile.firstName} ${profile.lastName}`.trim()}
                  readOnly
                />
              </div>

              <div className="col-md-6">
                <label>Email address</label>
                <input
                  className="form-control"
                  value={profile.email}
                  readOnly
                />
              </div>

              <div className="col-md-6">
                <label>Phone number</label>
                <input
                  className="form-control"
                  value={profile.phone || ""}
                  readOnly
                />
              </div>

              <div className="col-md-6">
                <label>Date of birth</label>
                <input
                  className="form-control"
                  value={profile.dateOfBirth || ""}
                  readOnly
                />
              </div>

              <div className="col-12">
                <label>Registered address</label>
                <input className="form-control" value={fullAddress} readOnly />
              </div>
            </div>
          </div>
        )}

        <form onSubmit={submit}>
          {}
          <div className="form-intro">
            <div className="form-icon">
              <i className="bi bi-shield-check" />
            </div>
            <div>
              <h2>Policy and vehicle information</h2>
              <p>
                Select the policy and vehicle for which you are submitting this
                claim.
              </p>
            </div>
          </div>

          <div className="row g-3">
            <div className="col-md-6">
              <label>Insurance policy *</label>

              <select
                className="form-select"
                name="policyId"
                value={form.policyId}
                onChange={change}
                required
                disabled={loadingPolicies}
              >
                <option value="">
                  {loadingPolicies
                    ? "Loading policies..."
                    : "Select your policy"}
                </option>

                {policies.map((policy) => (
                  <option key={policy.id} value={policy.id}>
                    {policy.policyNumber} · {policy.coverageType} ·{" "}
                    {policy.status}
                  </option>
                ))}
              </select>

              {selectedPolicy && (
                <small className="helper-text">
                  Coverage: {selectedPolicy.startDate} to{" "}
                  {selectedPolicy.endDate}
                  {" · "}
                  Limit: ₹
                  {Number(selectedPolicy.coverageLimit).toLocaleString("en-IN")}
                </small>
              )}

              {!loadingPolicies && policies.length === 0 && (
                <small className="text-danger">
                  No policies are currently associated with your account.
                </small>
              )}
            </div>

            <div className="col-md-6">
              <label>Insured vehicle *</label>

              <select
                className="form-select"
                name="vehicleId"
                value={form.vehicleId}
                onChange={change}
                required
                disabled={!form.policyId || loadingVehicles}
              >
                <option value="">
                  {loadingVehicles ? "Loading vehicles..." : "Select a vehicle"}
                </option>

                {vehicles.map((vehicle) => (
                  <option key={vehicle.id} value={vehicle.id}>
                    {vehicle.registrationNumber} · {vehicle.make}{" "}
                    {vehicle.model}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {}
          {selectedVehicle && (
            <div className="border rounded p-3 mt-4 mb-4 bg-light">
              <h3 className="h6 mb-3">
                <i className="bi bi-car-front me-2" />
                Registered vehicle details
              </h3>

              <p className="helper-text mb-3">
                These details are retrieved from your registered vehicle record
                and cannot be edited during claim submission.
              </p>

              <div className="row g-3">
                <div className="col-md-6">
                  <label>Registration number</label>
                  <input
                    className="form-control"
                    value={selectedVehicle.registrationNumber || ""}
                    readOnly
                  />
                </div>

                <div className="col-md-6">
                  <label>VIN</label>
                  <input
                    className="form-control"
                    value={selectedVehicle.vin || ""}
                    readOnly
                  />
                </div>

                <div className="col-md-6">
                  <label>Make and model</label>
                  <input
                    className="form-control"
                    value={`${selectedVehicle.make || ""} ${selectedVehicle.model || ""}`.trim()}
                    readOnly
                  />
                </div>

                <div className="col-md-6">
                  <label>Manufacturing year</label>
                  <input
                    className="form-control"
                    value={selectedVehicle.manufacturingYear || ""}
                    readOnly
                  />
                </div>
              </div>
            </div>
          )}

          {}
          <div className="form-intro mt-4">
            <div className="form-icon">
              <i className="bi bi-file-earmark-text" />
            </div>
            <div>
              <h2>Incident information</h2>
              <p>
                Provide the details of the incident for which you are raising
                the claim.
              </p>
            </div>
          </div>

          <div className="row g-3">
            <div className="col-md-6">
              <label>Incident date *</label>
              <input
                className="form-control"
                type="date"
                name="incidentDate"
                value={form.incidentDate}
                onChange={change}
                min={selectedPolicy?.startDate || undefined}
                max={
                  selectedPolicy?.endDate
                    ? [today, selectedPolicy.endDate].sort()[0]
                    : today
                }
                required
              />
            </div>

            <div className="col-md-6">
              <label>Claimed amount (₹) *</label>
              <input
                className="form-control"
                type="number"
                name="claimedAmount"
                value={form.claimedAmount}
                onChange={change}
                min="0.01"
                step="0.01"
                max={selectedPolicy?.coverageLimit || undefined}
                required
                placeholder="e.g. 45000"
              />
            </div>

            <div className="col-12">
              <label>Incident location *</label>
              <input
                className="form-control"
                name="incidentLocation"
                value={form.incidentLocation}
                onChange={change}
                maxLength="255"
                required
                placeholder="City, road or incident location"
              />
            </div>

            <div className="col-12">
              <label>Describe what happened *</label>
              <textarea
                className="form-control"
                rows="4"
                name="incidentDescription"
                value={form.incidentDescription}
                onChange={change}
                maxLength="4000"
                required
                placeholder="Describe the circumstances and relevant details..."
              />
            </div>
          </div>

          <div className="form-actions">
            <button
              type="button"
              className="btn btn-outline-secondary"
              onClick={() => nav("/claims")}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={
                busy ||
                loadingProfile ||
                loadingPolicies ||
                loadingVehicles ||
                !selectedPolicy ||
                !selectedVehicle
              }
            >
              {busy ? "Submitting..." : "Submit claim"}
              <i className="bi bi-arrow-right ms-2" />
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
