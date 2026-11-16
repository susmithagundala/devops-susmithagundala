import React from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
export default function ProtectedRoute({ role }) {
  const { user } = useAuth();
  if (!user || !localStorage.getItem("claimsure_token")) {
    return <Navigate to="/login" replace />;
  }
  if (role && !user.roles?.includes(role)) {
    const defaultPage = user.roles?.includes("INVESTIGATOR")
      ? "/investigator"
      : "/dashboard";
    return <Navigate to={defaultPage} replace />;
  }
  return <Outlet />;
}
