import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const AdminRoute = () => {
  const { roles, isAuthenticated } = useAuth();

  if (!isAuthenticated) {
    if (location) {
        return <Navigate to="/login" replace state={{ from: location }} />;
    } else {
        return <Navigate to="/login"/>;
    }
  }
  if (!roles.includes("ROLE_ADMIN")) {
    return <Navigate to="/"/>;
  }
  return <Outlet />
};

export default AdminRoute;
