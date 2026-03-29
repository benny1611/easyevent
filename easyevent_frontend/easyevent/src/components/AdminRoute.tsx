import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const AdminRoute = () => {
  const { roles, isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (roles.includes("ROLE_ADMIN") || roles.includes("ROLE_SUPER_ADMIN")) {
    return <Outlet />;
  }

  return <Navigate to="/" replace />;
};

export default AdminRoute;
