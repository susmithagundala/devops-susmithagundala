import React from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import CustomerProfile from "./pages/CustomerProfile";
import Login from "./pages/Login";
import Register from "./pages/Register";
import ClaimantDashboard from "./pages/ClaimantDashboard";
import Claims from "./pages/Claims";
import SubmitClaim from "./pages/SubmitClaim";
import EvidenceRequests from "./pages/EvidenceRequests";
import InvestigatorDashboard from "./pages/InvestigatorDashboard";
import Investigations from "./pages/Investigations";
import InvestigationDetail from "./pages/InvestigationDetail";
import { useAuth } from "./context/AuthContext";
function Home() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return (
    <Navigate
      to={user.roles?.includes("INVESTIGATOR") ? "/investigator" : "/dashboard"}
      replace
    />
  );
}
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route element={<ProtectedRoute role="CLAIMANT" />}>
            <Route path="/dashboard" element={<ClaimantDashboard />} />
            <Route path="/claims" element={<Claims />} />
            <Route path="/claims/new" element={<SubmitClaim />} />
            <Route path="/evidence-requests" element={<EvidenceRequests />} />
            <Route path="/profile" element={<CustomerProfile />} />
          </Route>
          <Route element={<ProtectedRoute role="INVESTIGATOR" />}>
            <Route path="/investigator" element={<InvestigatorDashboard />} />
            <Route path="/investigator/cases" element={<Investigations />} />
            <Route
              path="/investigator/cases/:id"
              element={<InvestigationDetail />}
            />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<Home />} />
    </Routes>
  );
}
