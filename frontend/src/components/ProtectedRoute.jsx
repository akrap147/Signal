import { useNavigate } from "react-router-dom";
import { useEffect } from "react";
import useAuthStore from "../stores/useAuthStore";

export default function ProtectedRoute({ children }) {
  const { accessToken } = useAuthStore();
  const navigate = useNavigate();

  useEffect(() => {
    if (!accessToken) {
      navigate("/login");
    }
  }, [accessToken, navigate]);

  if (!accessToken) return null;

  return children;
}
