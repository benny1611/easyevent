import { useEffect, useMemo, useState } from "react";
import {
  Tabs,
  Tab,
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
  getGridStringOperators,
  getGridSingleSelectOperators,
} from "@mui/x-data-grid";

import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import SaveIcon from "@mui/icons-material/Save";
import BlockIcon from "@mui/icons-material/Block";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";
import ChangeUserRequest from "../models/dto/ChangeUserRequest";
import ListUserResponse from "../models/dto/ListUserResponse";
import BanReasonDialog from "../components/BanReasonDialog";

const EditableCellInput = ({
  value,
  onSave,
  disabled,
}: {
  value: string;
  onSave: (val: string) => void;
  disabled?: boolean;
}) => {
  const [localValue, setLocalValue] = useState(value);

  // Sync local state if the external data changes (e.g. after a fetch)
  useEffect(() => {
    setLocalValue(value);
  }, [value]);

  return (
    <TextField
      value={localValue}
      size="small"
      disabled={disabled}
      onChange={(e) => setLocalValue(e.target.value)}
      // Important: only update the heavy global state when the user is done
      onBlur={() => {
        if (localValue !== value) {
          onSave(localValue);
        }
      }}
    />
  );
};

export default function AdminPage() {
  const { token, userId, roles } = useAuth();
  const { translation } = useI18n();

  const currentUser = {
    id: Number(userId),
    role: roles[0],
  };

  const isSuperAdmin = currentUser.role === "ROLE_SUPER_ADMIN";

  // State for the Ban Dialog
  const [banDialogOpen, setBanDialogOpen] = useState(false);
  const [userToBan, setUserToBan] = useState<ListUserResponse | null>(null);

  const [currentTab, setCurrentTab] = useState(0);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setCurrentTab(newValue);
  };

  const [users, setUsers] = useState<ListUserResponse[]>([]);
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
  const canEdit = (target: ListUserResponse) => {
    const isSelf = target.id === currentUser.id;

    if (isSuperAdmin) {
      if (isSelf) return true;
      return target.roles[0] !== "ROLE_SUPER_ADMIN";
    }

    if (currentUser.role === "ROLE_ADMIN") {
      return isSelf || target.roles[0] === "ROLE_USER";
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

    const mapped = data._embedded.listUserResponseList.map(
      (u: any) =>
        new ListUserResponse(
          u.id,
          u.name,
          u.email,
          u.profilePicture,
          u.active,
          u.banned,
          u.roles,
        ),
    );

    setUsers(mapped);
    setRowCount(data.page.totalElements);
    setLoading(false);
  };

  useEffect(() => {
    fetchUsers();
  }, [page, pageSize, sortModel]);

  const handleChange = (
    id: number,
    field: keyof ListUserResponse,
    value: any,
  ) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === id ? { ...u, [field]: value } : u)),
    );
  };

  const handleSave = async (user: ListUserResponse) => {
    try {
      const endpoint = isSuperAdmin
        ? `${ENV.API_BASE_URL}/users/update/admin/${user.id}`
        : `${ENV.API_BASE_URL}/users/update/${user.id}`;

      const formData = new FormData();

      const changeUserRequest: ChangeUserRequest = new ChangeUserRequest(
        user.email,
        user.name,
      );

      formData.append(
        "changeUserRequest",
        new Blob([JSON.stringify(changeUserRequest)], {
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
        message: translation.admin.user_updated,
        severity: "success",
      });

      fetchUsers();
    } catch {
      setSnackbar({
        open: true,
        message: translation.admin.update_failed,
        severity: "error",
      });
    }
  };

  const toggleBan = (user: ListUserResponse) => {
    if (!canEdit(user)) return;
    if (user.banned) {
      // If already banned, unban immediately
      handleChange(user.id, "banned", false);
    } else {
      // If not banned, open dialog to get reason
      setUserToBan(user);
      setBanDialogOpen(true);
    }
  };

  const handleConfirmBan = (reason: string) => {
    if (userToBan) {
      handleChange(userToBan.id, "banned", true);
      // You can now use the 'reason' variable here to send to your API
      console.log("Banning for reason:", reason);

      setBanDialogOpen(false);
      setUserToBan(null);
    }
  };

  const handleRoleChange = (user: ListUserResponse, newRole: string) => {
    if (!canEdit(user)) return;
    handleChange(user.id, "roles", newRole);
  };

  // Columns
  const columns: GridColDef[] = useMemo(
    () => [
      {
        field: translation.admin.profilePicture,
        headerName: "",
        width: 90,
        sortable: false,
        renderCell: (params: { row: any }) => {
          const user = params.row;
          const editable = canEdit(user);

          return (
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center", // Center the avatar in the cell
                height: "100%",
              }}
            >
              <Box sx={{ position: "relative", width: 40, height: 40 }}>
                <Avatar
                  sx={{ width: 40, height: 40 }}
                  src={
                    selectedFiles[user.id]
                      ? URL.createObjectURL(selectedFiles[user.id])
                      : `${ENV.BARE_URL_BASE}${user.profilePicture}`
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
            </Box>
          );
        },
      },
      {
        field: "name",
        headerName: translation.admin.name,
        flex: 1,
        filterOperators: getGridStringOperators().filter(
          (operator) => operator.value !== "isAnyOf",
        ),
        renderCell: (params) => {
          const user = params.row;
          const editable = canEdit(user);

          return (
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                gap: 1,
                height: "100%",
              }}
            >
              <EditableCellInput
                value={user.name}
                disabled={!editable}
                onSave={(newValue) => handleChange(user.id, "name", newValue)}
              />

              <Chip
                label={user.roles[0].replace("ROLE_", "").toLowerCase()}
                size="small"
              />

              {user.id === currentUser.id && (
                <Chip label={translation.admin.you} size="small" />
              )}
            </Box>
          );
        },
      },
      {
        field: "email",
        headerName: translation.admin.email,
        flex: 1,
        filterOperators: getGridStringOperators().filter(
          (operator) => operator.value !== "isAnyOf",
        ),
        renderCell: (params) => {
          const user = params.row;

          return (
            <Box sx={{ display: "flex", alignItems: "center", height: "100%" }}>
              <EditableCellInput
                value={user.email}
                disabled={!isSuperAdmin}
                onSave={(newValue) => handleChange(user.id, "email", newValue)}
              />
            </Box>
          );
        },
      },
      {
        field: translation.admin.active,
        headerName: translation.admin.active,
        width: 100,
        sortable: false,
        filterOperators: getGridStringOperators().filter(
          (operator) => operator.value !== "isAnyOf",
        ),
        renderCell: (params) => {
          const user = params.row;
          return <Checkbox checked={user.active} disabled />;
        },
      },
      {
        field: translation.admin.banned,
        headerName: translation.admin.ban,
        width: 100,
        sortable: false,
        renderCell: (params) => {
          const user = params.row;
          const isSelf = user.id === currentUser.id;

          // If it's the logged-in user, don't show the button at all
          if (isSelf) {
            return null;
          }

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
              field: "role",
              type: "singleSelect" as const,
              valueOptions: ["ROLE_USER", "ROLE_ADMIN", "ROLE_SUPER_ADMIN"],
              valueGetter: (_value: any, row: { roles: string[] }) => {
                return row.roles?.[0] || "";
              },
              filterOperators: getGridSingleSelectOperators().filter(
                (operator) => operator.value !== "isAnyOf",
              ),
              headerName: translation.admin.role_control,
              width: 160,
              sortable: false,
              renderCell: (params: any) => {
                const user = params.row;

                if (user.roles[0] === "ROLE_SUPER_ADMIN") return null;

                return (
                  <Select
                    size="small"
                    value={user.roles[0]}
                    onChange={(e) => handleRoleChange(user, e.target.value)}
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      height: "100%",
                    }}
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
    ],
    [translation, isSuperAdmin, selectedFiles, currentUser.id],
  );

  return (
    <Box sx={{ mt: 4, px: 2, width: "100%" }}>
      <Typography variant="h4" align="center" sx={{ mb: 3 }}>
        {translation.admin.panel}
      </Typography>

      {/* Tab Navigation */}
      <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
        <Tabs
          value={currentTab}
          onChange={handleTabChange}
          aria-label="admin panel tabs"
        >
          <Tab
            label={translation.admin.users}
            id="tab-0"
            aria-controls="tabpanel-0"
          />
          {/*<Tab label="Other Management" id="tab-1" aria-controls="tabpanel-1" />*/}
        </Tabs>
      </Box>

      {/* TAB 0: USERS (Default) */}
      <div role="tabpanel" hidden={currentTab !== 0} id="tabpanel-0">
        {currentTab === 0 && (
          <Box>
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
                border: "none",
                "& .disabled-row": {
                  opacity: 0.5,
                  pointerEvents: "none",
                },
              }}
            />
          </Box>
        )}
      </div>

      {/* TAB 1: PLACEHOLDER */}
      {/*<div role="tabpanel" hidden={currentTab !== 1} id="tabpanel-1">
        {currentTab === 1 && (
          <Box sx={{ p: 3, textAlign: "center" }}>
            <Typography variant="h6" color="text.secondary">
              Future Management Module Goes Here
            </Typography>
          </Box>
        )}
      </div>*/}

      {/* Ban Reason Dialog */}
      <BanReasonDialog
        open={banDialogOpen}
        userName={userToBan?.name}
        onClose={() => setBanDialogOpen(false)}
        onConfirm={handleConfirmBan}
      />

      <Snackbar open={snackbar.open} autoHideDuration={4000}>
        <Alert severity={snackbar.severity} variant="filled">
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
