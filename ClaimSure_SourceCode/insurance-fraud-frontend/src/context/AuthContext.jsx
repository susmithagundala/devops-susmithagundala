import React, { createContext, useContext, useMemo, useState } from "react";
import api from "../services/api";
const AuthContext = createContext(null);
export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("claimsure_user"));
    } catch {
      return null;
    }
  });
  const save = (data) => {
    const u = {
      userId: data.userId,
      email: data.email,
      roles: data.roles || [],
    };
    localStorage.setItem("claimsure_token", data.accessToken);
    localStorage.setItem("claimsure_user", JSON.stringify(u));
    setUser(u);
  };
  const login = async (values) => {
    const { data } = await api.post("/auth/login", values);
    save(data);
    return data;
  };
  const register = async (values) => {
    const { data } = await api.post("/auth/register", values);
    save(data);
    return data;
  };
  const logout = () => {
    localStorage.removeItem("claimsure_token");
    localStorage.removeItem("claimsure_user");
    setUser(null);
  };
  const value = useMemo(
    () => ({
      user,
      login,
      register,
      logout,
      isAuthenticated: !!localStorage.getItem("claimsure_token"),
      role: user?.roles?.[0],
    }),
    [user],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export const useAuth = () => useContext(AuthContext);
