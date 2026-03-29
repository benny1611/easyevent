import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const ProtectedRoute = () => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    if (location) {
      return <Navigate to="/login" replace state={{ from: location }} />;
    } else {
      return <Navigate to="/login" />;
    }
  }

  return <Outlet />;
};

export default ProtectedRoute;
