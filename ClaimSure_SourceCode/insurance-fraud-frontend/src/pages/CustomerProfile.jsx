import React, { useEffect, useState } from "react";
import api, { errorMessage } from "../services/api";
import { toast } from "react-toastify";
const emptyProfile = {
  customerId: "",
  email: "",
  firstName: "",
  lastName: "",
  dateOfBirth: "",
  phone: "",
  addressLine1: "",
  addressLine2: "",
  city: "",
  state: "",
  postalCode: "",
  country: "",
};
export default function CustomerProfile() {
  const [profile, setProfile] = useState(emptyProfile);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editing, setEditing] = useState(false);
  useEffect(() => {
    loadProfile();
  }, []);
  const loadProfile = async () => {
    try {
      const response = await api.get("/customers/me");
      setProfile({
        ...emptyProfile,
        ...response.data,
        dateOfBirth: response.data.dateOfBirth || "",
      });
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setLoading(false);
    }
  };
  const handleChange = (event) => {
    const { name, value } = event.target;
    setProfile((previous) => ({
      ...previous,
      [name]: value,
    }));
  };
  const handleSave = async (event) => {
    event.preventDefault();
    setSaving(true);
    const payload = {
      firstName: profile.firstName,
      lastName: profile.lastName,
      dateOfBirth: profile.dateOfBirth || null,
      phone: profile.phone,
      addressLine1: profile.addressLine1,
      addressLine2: profile.addressLine2,
      city: profile.city,
      state: profile.state,
      postalCode: profile.postalCode,
      country: profile.country,
    };
    try {
      const response = await api.put("/customers/me", payload);
      setProfile({
        ...emptyProfile,
        ...response.data,
        dateOfBirth: response.data.dateOfBirth || "",
      });
      setEditing(false);
      toast.success("Profile updated successfully");
    } catch (error) {
      toast.error(errorMessage(error));
    } finally {
      setSaving(false);
    }
  };
  if (loading) {
    return <div className="panel loading-state">Loading profile...</div>;
  }
  const initials =
    `${profile.firstName?.[0] || ""}${profile.lastName?.[0] || ""}`.toUpperCase();
  return (
    <>
      <div className="page-heading">
        <div>
          <span className="eyebrow">ACCOUNT SETTINGS</span>
          <h1>My Profile</h1>
          <p>View and manage your personal information.</p>
        </div>

        {!editing && (
          <button className="btn btn-primary" onClick={() => setEditing(true)}>
            <i className="bi bi-pencil me-2"></i>
            Edit profile
          </button>
        )}
      </div>

      <div className="panel profile-panel">
        <div className="profile-header">
          <div className="profile-large-avatar">{initials || "U"}</div>

          <div>
            <h2>
              {profile.firstName} {profile.lastName}
            </h2>
            <p>{profile.email}</p>
            <span className="status-badge info">CLAIMANT</span>
          </div>
        </div>

        <hr />

        <form onSubmit={handleSave}>
          <h2 className="profile-section-title">Personal information</h2>

          <div className="row g-3">
            <div className="col-md-6">
              <label>First name *</label>
              <input
                className="form-control"
                name="firstName"
                value={profile.firstName}
                onChange={handleChange}
                disabled={!editing}
                required
                maxLength={100}
              />
            </div>

            <div className="col-md-6">
              <label>Last name *</label>
              <input
                className="form-control"
                name="lastName"
                value={profile.lastName}
                onChange={handleChange}
                disabled={!editing}
                required
                maxLength={100}
              />
            </div>

            <div className="col-md-6">
              <label>Email address</label>
              <input
                className="form-control"
                type="email"
                value={profile.email}
                disabled
              />
              <small className="helper-text">
                Email cannot be changed here.
              </small>
            </div>

            <div className="col-md-6">
              <label>Date of birth</label>
              <input
                className="form-control"
                type="date"
                name="dateOfBirth"
                value={profile.dateOfBirth}
                onChange={handleChange}
                disabled={!editing}
                max={new Date().toISOString().slice(0, 10)}
              />
            </div>

            <div className="col-md-6">
              <label>Phone number</label>
              <input
                className="form-control"
                name="phone"
                value={profile.phone || ""}
                onChange={handleChange}
                disabled={!editing}
                maxLength={20}
              />
            </div>
          </div>

          <hr className="my-4" />

          <h2 className="profile-section-title">Address information</h2>

          <div className="row g-3">
            <div className="col-12">
              <label>Address line 1</label>
              <input
                className="form-control"
                name="addressLine1"
                value={profile.addressLine1 || ""}
                onChange={handleChange}
                disabled={!editing}
              />
            </div>

            <div className="col-12">
              <label>Address line 2</label>
              <input
                className="form-control"
                name="addressLine2"
                value={profile.addressLine2 || ""}
                onChange={handleChange}
                disabled={!editing}
              />
            </div>

            <div className="col-md-6">
              <label>City</label>
              <input
                className="form-control"
                name="city"
                value={profile.city || ""}
                onChange={handleChange}
                disabled={!editing}
              />
            </div>

            <div className="col-md-6">
              <label>State</label>
              <input
                className="form-control"
                name="state"
                value={profile.state || ""}
                onChange={handleChange}
                disabled={!editing}
              />
            </div>

            <div className="col-md-6">
              <label>Postal code</label>
              <input
                className="form-control"
                name="postalCode"
                value={profile.postalCode || ""}
                onChange={handleChange}
                disabled={!editing}
                maxLength={15}
              />
            </div>

            <div className="col-md-6">
              <label>Country</label>
              <input
                className="form-control"
                name="country"
                value={profile.country || ""}
                onChange={handleChange}
                disabled={!editing}
              />
            </div>
          </div>

          {editing && (
            <div className="form-actions">
              <button
                type="button"
                className="btn btn-outline-secondary"
                onClick={() => {
                  setEditing(false);
                  loadProfile();
                }}
                disabled={saving}
              >
                Cancel
              </button>

              <button
                type="submit"
                className="btn btn-primary"
                disabled={saving}
              >
                {saving ? "Saving..." : "Save changes"}
              </button>
            </div>
          )}
        </form>
      </div>
    </>
  );
}
