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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from "@mui/material";
import {
  DataGrid,
  type GridColDef,
  type GridSortModel,
  getGridStringOperators,
} from "@mui/x-data-grid";

import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import SaveIcon from "@mui/icons-material/Save";
import DeleteIcon from "@mui/icons-material/Delete";
import BlockIcon from "@mui/icons-material/Block";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";

import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";
import { useI18n } from "../i18n/i18nContext";
import ChangeUserRequest from "../models/dto/ChangeUserRequest";
import ListUserResponse from "../models/dto/ListUserResponse";
import BanReasonDialog from "../components/BanReasonDialog";
import BanRequest from "../models/dto/BanRequest";
import ChangeRolesRequest from "../models/dto/ChangeRolesRequest";

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
    setLoading(true);
    try {
      const profileEndpoint = isSuperAdmin
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

      const profilePromise = fetch(profileEndpoint, {
        method: "PUT",
        headers: { Authorization: `Bearer ${token}` },
        body: formData,
      });

      let rolePromise = Promise.resolve(null as any);

      // Now allowing both Super Admins AND Admins to send role updates
      // as long as the target is not a Super Admin (which we handled in UI anyway)
      if (user.roles[0] !== "ROLE_SUPER_ADMIN") {
        const roleEndpoint = `${ENV.API_BASE_URL}/users/update/roles/${user.id}`;
        const roleRequestBody = new ChangeRolesRequest(user.roles);

        rolePromise = fetch(roleEndpoint, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(roleRequestBody),
        });
      }

      const [profileRes, roleRes] = await Promise.all([
        profilePromise,
        rolePromise,
      ]);

      if (!profileRes.ok || (roleRes && !roleRes.ok)) {
        throw new Error("Update failed");
      }

      setSnackbar({
        open: true,
        message: translation.admin.user_updated,
        severity: "success",
      });

      // Clear the selected file for this user after successful upload
      if (selectedFiles[user.id]) {
        const newFiles = { ...selectedFiles };
        delete newFiles[user.id];
        setSelectedFiles(newFiles);
      }

      fetchUsers();
    } catch (error) {
      setSnackbar({
        open: true,
        message: translation.admin.update_failed,
        severity: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  const toggleBan = async (user: ListUserResponse) => {
    if (!canEdit(user)) return;
    if (user.banned) {
      // If already banned, unban immediately
      try {
        const url = `${ENV.API_BASE_URL}/users/unban/${user.id}`;
        const response = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        });
        if (!response.ok) {
          setSnackbar({
            open: true,
            message: translation.admin.update_failed,
            severity: "error",
          });
        } else {
          setSnackbar({
            open: true,
            message: translation.admin.user_updated,
            severity: "success",
          });
        }
      } catch (error) {
        setSnackbar({
          open: true,
          message: translation.admin.update_failed,
          severity: "error",
        });
      }
      // Refresh table
      fetchUsers();
    } else {
      // If not banned, open dialog to get reason
      setUserToBan(user);
      setBanDialogOpen(true);
    }
  };

  const handleConfirmBan = async (reason: string, userId: number) => {
    if (userToBan) {
      try {
        const url = `${ENV.API_BASE_URL}/users/ban/${userId}`;
        const banRequest = new BanRequest(reason);
        const response = await fetch(url, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
          body: JSON.stringify(banRequest),
        });
        if (!response.ok) {
          setSnackbar({
            open: true,
            message: translation.admin.update_failed,
            severity: "error",
          });
        } else {
          setSnackbar({
            open: true,
            message: translation.admin.user_updated,
            severity: "success",
          });
        }
      } catch (error) {
        setSnackbar({
          open: true,
          message: translation.admin.update_failed,
          severity: "error",
        });
      }

      // Refresh table
      fetchUsers();
      setBanDialogOpen(false);
      setUserToBan(null);
    }
  };

  const handleRoleChange = (user: ListUserResponse, newRole: string) => {
    if (!canEdit(user)) return;
    handleChange(user.id, "roles", [newRole]);
  };

  // State for Delete Dialog
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [userToDelete, setUserToDelete] = useState<ListUserResponse | null>(
    null,
  );

  const handleDeleteClick = (user: ListUserResponse) => {
    setUserToDelete(user);
    setDeleteDialogOpen(true);
  };

  const handleConfirmDelete = async () => {
    if (!userToDelete) return;

    setLoading(true);
    try {
      const response = await fetch(
        `${ENV.API_BASE_URL}/users/${userToDelete.id}`,
        {
          method: "DELETE",
          headers: { Authorization: `Bearer ${token}` },
        },
      );

      if (!response.ok) throw new Error("Delete failed");

      setSnackbar({
        open: true,
        message: translation.admin.user_deleted,
        severity: "success",
      });

      fetchUsers(); // Refresh the list
    } catch (error) {
      setSnackbar({
        open: true,
        message: translation.admin.delete_failed,
        severity: "error",
      });
    } finally {
      setLoading(false);
      setDeleteDialogOpen(false);
      setUserToDelete(null);
    }
  };

  const handleSnackbarClose = (
    _event?: React.SyntheticEvent | Event,
    reason?: string,
  ) => {
    // This prevents the snackbar from closing if the user clicks outside
    if (reason === "clickaway") {
      return;
    }

    setSnackbar((prev) => ({ ...prev, open: false }));
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
              headerName: translation.admin.role_control,
              width: 180,
              sortable: false,
              renderCell: (params: any) => {
                const targetUser = params.row;
                const isTargetSuperAdmin =
                  targetUser.roles[0] === "ROLE_SUPER_ADMIN";

                // 1. If the target is a Super Admin, nobody can change their role (not even another Super Admin, to prevent lockouts)
                if (isTargetSuperAdmin) {
                  return (
                    <Chip
                      label="SUPER ADMIN"
                      size="small"
                      color="secondary"
                      variant="outlined"
                      sx={{ mt: 1 }}
                    />
                  );
                }

                // 2. Logic for regular Admins
                if (currentUser.role === "ROLE_ADMIN") {
                  // Admins can only change roles of ROLE_USER (to promote them) or other ROLE_ADMINs
                  // But they should NOT be able to see or select ROLE_SUPER_ADMIN
                  return (
                    <Select
                      size="small"
                      value={targetUser.roles[0]}
                      onChange={(e) =>
                        handleRoleChange(targetUser, e.target.value)
                      }
                      sx={{ width: "100%", mt: 0.5 }}
                    >
                      <MenuItem value="ROLE_USER">user</MenuItem>
                      <MenuItem value="ROLE_ADMIN">admin</MenuItem>
                    </Select>
                  );
                }

                // 3. Logic for Super Admins
                if (isSuperAdmin) {
                  return (
                    <Select
                      size="small"
                      value={targetUser.roles[0]}
                      onChange={(e) =>
                        handleRoleChange(targetUser, e.target.value)
                      }
                      sx={{ width: "100%", mt: 0.5 }}
                    >
                      <MenuItem value="ROLE_USER">user</MenuItem>
                      <MenuItem value="ROLE_ADMIN">admin</MenuItem>
                      <MenuItem value="ROLE_SUPER_ADMIN">super admin</MenuItem>
                    </Select>
                  );
                }

                return null;
              },
            },
          ]
        : []),

      {
        field: "actions",
        headerName: "",
        width: 120,
        sortable: false,
        renderCell: (params) => {
          const user = params.row;
          const isTargetSuperAdmin = user.roles[0] === "ROLE_SUPER_ADMIN";

          return (
            <Box sx={{ display: "flex", gap: 1 }}>
              <IconButton
                disabled={!canEdit(user)}
                onClick={() => handleSave(user)}
                color="primary"
                size="small"
              >
                <SaveIcon />
              </IconButton>

              {/* DELETE OPTION FOR SUPER ADMIN */}
              {isSuperAdmin && !isTargetSuperAdmin && (
                <IconButton
                  onClick={() => handleDeleteClick(user)}
                  color="error"
                  size="small"
                >
                  <DeleteIcon />
                </IconButton>
              )}
            </Box>
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
        userId={userToBan?.id!}
        onClose={() => setBanDialogOpen(false)}
        onConfirm={handleConfirmBan}
      />
      {/* Delete Confirmation Dialog */}
      <Dialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
      >
        <DialogTitle>{translation.admin.delete_user}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            {translation.admin.delete_question}{" "}
            <strong>{userToDelete?.name}</strong>
            {translation.admin.delete_question_continuation}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>
            {translation.admin.cancel}
          </Button>
          <Button
            onClick={handleConfirmDelete}
            color="error"
            variant="contained"
          >
            {translation.admin.delete}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={5000} // 5 seconds
        onClose={handleSnackbarClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "right" }} // Optional: move it out of the way
      >
        <Alert
          onClose={handleSnackbarClose} // Adds a 'X' close button to the alert itself
          severity={snackbar.severity}
          variant="filled"
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
