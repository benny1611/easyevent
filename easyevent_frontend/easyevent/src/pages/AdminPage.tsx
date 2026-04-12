import { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Avatar,
  Checkbox,
  Chip,
  Snackbar,
  Alert,
  IconButton,
  TextField,
  Select,
  MenuItem,
} from "@mui/material";
import {
  DataGrid,
  type GridColDef,
  type GridSortModel,
} from "@mui/x-data-grid";

import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import SaveIcon from "@mui/icons-material/Save";
import BlockIcon from "@mui/icons-material/Block";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  language: string;
  active: boolean;
  banned: boolean;
  profilePicture?: string;
}

export default function AdminPage() {
  const { token, userId, roles } = useAuth();
  const { translation } = useI18n();

  const currentUser = {
    id: Number(userId),
    role: roles[0],
  };

  const isSuperAdmin = currentUser.role === "ROLE_SUPER_ADMIN";

  const [users, setUsers] = useState<User[]>([]);
  const [selectedFiles, setSelectedFiles] = useState<Record<number, File>>({});
  const [loading, setLoading] = useState(false);

  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [rowCount, setRowCount] = useState(0);

  const [sortModel, setSortModel] = useState<GridSortModel>([
    { field: "name", sort: "asc" },
  ]);

  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  // FIXED permission logic
  const canEdit = (target: User) => {
    const isSelf = target.id === currentUser.id;

    if (isSuperAdmin) {
      if (isSelf) return true;
      return target.role !== "ROLE_SUPER_ADMIN";
    }

    if (currentUser.role === "ROLE_ADMIN") {
      return isSelf || target.role === "ROLE_USER";
    }

    return false;
  };

  // Fetch
  const fetchUsers = async () => {
    setLoading(true);

    const sort = sortModel[0];
    const sortQuery = sort ? `&sort=${sort.field},${sort.sort}` : "";

    const url = `${ENV.API_BASE_URL}/users/all?page=${page}&size=${pageSize}${sortQuery}`;

    const res = await fetch(url, {
      headers: { Authorization: `Bearer ${token}` },
    });

    const data = await res.json();

    const mapped = data._embedded.userDTOList.map((u: any) => ({
      id: u.id,
      name: u.name,
      email: u.email,
      language: u.language,
      active: u.active,
      banned: u.banned ?? false,
      role: u.roles?.includes("ROLE_SUPER_ADMIN")
        ? "ROLE_SUPER_ADMIN"
        : u.roles?.includes("ROLE_ADMIN")
          ? "ROLE_ADMIN"
          : "ROLE_USER",
      profilePicture: u.profilePicture
        ? `${ENV.BARE_URL_BASE}${u.profilePicture}`
        : undefined,
    }));

    setUsers(mapped);
    setRowCount(data.page.totalElements);
    setLoading(false);
  };

  useEffect(() => {
    fetchUsers();
  }, [page, pageSize, sortModel]);

  const handleChange = (id: number, field: keyof User, value: any) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === id ? { ...u, [field]: value } : u)),
    );
  };

  const handleSave = async (user: User) => {
    try {
      const endpoint = isSuperAdmin
        ? `${ENV.API_BASE_URL}/users/update/admin/${user.id}`
        : `${ENV.API_BASE_URL}/users/update/${user.id}`;

      const formData = new FormData();

      const userDTO = {
        id: user.id,
        email: user.email,
        name: user.name,
        language: user.language,
        active: user.active,
        roles: [user.role],
        newPassword: null,
        oldPassword: null,
        profilePicture: user.profilePicture ?? null,
        token: null,
        isAdmin: user.role === "ROLE_ADMIN",
        isLocalPasswordSet: true,
      };

      formData.append(
        "userDTO",
        new Blob([JSON.stringify(userDTO)], {
          type: "application/json",
        }),
      );

      if (selectedFiles[user.id]) {
        formData.append("profilePicture", selectedFiles[user.id]);
      }

      await fetch(endpoint, {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      setSnackbar({
        open: true,
        message: "User updated",
        severity: "success",
      });

      fetchUsers();
    } catch {
      setSnackbar({
        open: true,
        message: "Update failed",
        severity: "error",
      });
    }
  };

  const toggleBan = (user: User) => {
    if (!canEdit(user)) return;
    handleChange(user.id, "banned", !user.banned);
  };

  const handleRoleChange = (user: User, newRole: string) => {
    if (!canEdit(user)) return;
    handleChange(user.id, "role", newRole);
  };

  // Columns
  const columns: GridColDef[] = [
    {
      field: translation.admin.profilePicture,
      headerName: "",
      width: 90,
      sortable: false,
      renderCell: (params) => {
        const user = params.row;
        const editable = canEdit(user);

        return (
          <Box sx={{ position: "relative", width: 40, height: 40 }}>
            <Avatar
              src={
                selectedFiles[user.id]
                  ? URL.createObjectURL(selectedFiles[user.id])
                  : user.profilePicture
              }
            />

            {editable && (
              <Box
                component="label"
                sx={{
                  position: "absolute",
                  inset: 0,
                  borderRadius: "50%",
                  cursor: "pointer",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  bgcolor: "transparent", // Use "transparent" for clarity
                  transition: "background-color 0.2s", // Optional: makes the fade smoother
                  "&:hover": {
                    bgcolor: "rgba(0,0,0,0.4)",
                  },
                  // Target the icon when this Box is hovered
                  "&:hover .upload-icon": {
                    opacity: 1,
                  },
                }}
              >
                <input
                  hidden
                  type="file"
                  accept="image/*"
                  onChange={(e) => {
                    const file = e.target.files?.[0];
                    if (file) {
                      setSelectedFiles((prev) => ({
                        ...prev,
                        [user.id]: file,
                      }));
                    }
                  }}
                />
                <CloudUploadIcon
                  className="upload-icon" // Add a class to target it
                  sx={{
                    color: "white",
                    opacity: 0, // Hidden by default
                    transition: "opacity 0.2s", // Smooth fade in
                  }}
                />
              </Box>
            )}
          </Box>
        );
      },
    },
    {
      field: translation.admin.name,
      headerName: translation.admin.name,
      flex: 1,
      renderCell: (params) => {
        const user = params.row;
        const editable = canEdit(user);

        return (
          <Box display="flex" gap={1} alignItems="center">
            <TextField
              value={user.name}
              size="small"
              disabled={!editable}
              onChange={(e) => handleChange(user.id, "name", e.target.value)}
            />

            <Chip
              label={user.role.replace("ROLE_", "").toLowerCase()}
              size="small"
            />

            {user.id === currentUser.id && <Chip label={translation.admin.you} size="small" />}
          </Box>
        );
      },
    },
    {
      field: translation.admin.email,
      headerName: translation.admin.email,
      flex: 1,
      renderCell: (params) => {
        const user = params.row;

        return (
          <TextField
            value={user.email}
            size="small"
            disabled={!isSuperAdmin}
            onChange={(e) => handleChange(user.id, "email", e.target.value)}
          />
        );
      },
    },
    {
      field: translation.admin.active,
      headerName: translation.admin.active,
      width: 100,
      sortable: false,
      renderCell: (params) => <Checkbox checked={params.value} disabled />,
    },
    {
      field: translation.admin.banned,
      headerName: translation.admin.ban,
      width: 100,
      sortable: false,
      renderCell: (params) => {
        const user = params.row;

        return (
          <IconButton
            disabled={!canEdit(user)}
            onClick={() => toggleBan(user)}
            color={user.banned ? "success" : "error"}
          >
            {user.banned ? <CheckCircleIcon /> : <BlockIcon />}
          </IconButton>
        );
      },
    },

    // ONLY FOR SUPER ADMIN
    ...(isSuperAdmin
      ? [
          {
            field: translation.admin.role_control,
            headerName: translation.admin.role_control,
            width: 160,
            sortable: false,
            renderCell: (params: any) => {
              const user = params.row;

              if (user.role === "ROLE_SUPER_ADMIN") return null;

              return (
                <Select
                  size="small"
                  value={user.role}
                  onChange={(e) => handleRoleChange(user, e.target.value)}
                >
                  <MenuItem value="ROLE_USER">user</MenuItem>
                  <MenuItem value="ROLE_ADMIN">admin</MenuItem>
                </Select>
              );
            },
          },
        ]
      : []),

    {
      field: translation.admin.actions,
      headerName: "",
      width: 80,
      sortable: false,
      renderCell: (params) => {
        const user = params.row;

        return (
          <IconButton
            disabled={!canEdit(user)}
            onClick={() => handleSave(user)}
            color="primary"
          >
            <SaveIcon />
          </IconButton>
        );
      },
    },
  ];

  return (
    <Box sx={{ mt: 4, px: 2 }}>
      <Typography variant="h4" align="center" sx={{ mb: 3 }}>
        Admin Panel
      </Typography>

      <DataGrid
        rows={users}
        columns={columns}
        loading={loading}
        autoHeight
        pagination
        paginationMode="server"
        sortingMode="server"
        rowCount={rowCount}
        pageSizeOptions={[10, 20, 50]}
        paginationModel={{ page, pageSize }}
        onPaginationModelChange={(model) => {
          setPage(model.page);
          setPageSize(model.pageSize);
        }}
        onSortModelChange={(model) => setSortModel(model)}
        getRowClassName={(params) =>
          !canEdit(params.row) ? "disabled-row" : ""
        }
        sx={{
          "& .disabled-row": {
            opacity: 0.5,
            pointerEvents: "none",
          },
        }}
      />

      <Snackbar open={snackbar.open} autoHideDuration={4000}>
        <Alert severity={snackbar.severity} variant="filled">
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
