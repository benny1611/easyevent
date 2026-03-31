import { useState, useEffect } from "react";
import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import { Snackbar, Alert } from "@mui/material";
import {
  Box,
  Tabs,
  Tab,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  TextField,
  Select,
  MenuItem,
  Button,
  Avatar,
  Chip,
  Switch,
  TablePagination,
  CircularProgress,
} from "@mui/material";
import { useAuth } from "../auth/AuthContext";
import { ENV } from "../config/env";

interface User {
  id: number;
  name: string;
  email: string;
  role: string;
  language: string;
  active: boolean;
  profilePicture?: string;
}

function TabPanel({ children, value, index }: any) {
  return value === index ? <Box sx={{ p: 2 }}>{children}</Box> : null;
}

export default function AdminPage() {
  const { token, userId, roles } = useAuth();
  const [selectedFiles, setSelectedFiles] = useState<Record<number, File>>({});
  const [passwords, setPasswords] = useState<Record<number, string>>({});
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: "success" | "error";
  }>({
    open: false,
    message: "",
    severity: "success",
  });

  const [tab, setTab] = useState(0);
  const [users, setUsers] = useState<User[]>([]);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);
  const [totalCount, setTotalCount] = useState(0);

  const [loading, setLoading] = useState(false);

  const currentUser = {
    id: userId,
    role: roles[0],
  };

  const isSuperAdmin = currentUser.role === "ROLE_SUPER_ADMIN";

  const fetchUsers = async () => {
    try {
      setLoading(true);

      const apiEndpoint = `${ENV.API_BASE_URL}/users/all?page=${page}&size=${rowsPerPage}`;
      const response = await fetch(apiEndpoint, {
        method: "GET",
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        const message = await response.text();
        const err = new Error(message);
        (err as any).cause = response.status;
        throw err;
      }

      const data = await response.json();

      const mappedUsers: User[] = data._embedded.userDTOList.map((u: any) => {
        let role: User["role"] = "ROLE_USER";

        if (u.roles?.includes("ROLE_SUPER_ADMIN")) {
          role = "ROLE_SUPER_ADMIN";
        } else if (u.roles?.includes("ROLE_ADMIN")) {
          role = "ROLE_ADMIN";
        }

        return {
          id: u.id,
          name: u.name,
          email: u.email,
          role,
          language: u.language,
          active: u.active,
          profilePicture: u.profilePicture
            ? `${ENV.BARE_URL_BASE}${u.profilePicture}`
            : undefined,
        };
      });

      setUsers(mappedUsers);
      setTotalCount(data.page.totalElements);
    } catch (err) {
      setSnackbar({
        open: true,
        message: err instanceof Error ? err.message : "Update failed",
        severity: "error",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [page, rowsPerPage]);

  const handleChange = (id: number, field: keyof User, value: any) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === id ? { ...u, [field]: value } : u)),
    );
  };

  const canEditBasic = (target: User) => {
    if (isSuperAdmin) return true;
    if (currentUser.role === "ROLE_ADMIN") {
      let targetId: number;
      let currentUserId: number;
      if (typeof target.id === "string") {
        targetId = parseInt(target.id);
      } else {
        targetId = target.id;
      }
      if (typeof currentUser.id === "string") {
        currentUserId = parseInt(currentUser.id);
      } else {
        currentUserId = currentUser.id!;
      }
      const isSelf = targetId === currentUserId;
      if (isSelf) {
        return true;
      } else {
        return target.role === "ROLE_USER";
      }
    }
    return false;
  };

  const isDisabledRow = (target: User) => !canEditBasic(target);

  const handleSave = async (user: User) => {
    try {
      const endpoint = isSuperAdmin
        ? `${ENV.API_BASE_URL}/users/update/admin/${user.id}`
        : `${ENV.API_BASE_URL}/users/update/${user.id}`;

      const selectedFile = selectedFiles[user.id];
      const newPassword = passwords[user.id];

      const userDTO = {
        id: user.id,
        email: user.email,
        name: user.name,
        language: user.language,
        active: user.active,
        roles: [user.role],
        newPassword: isSuperAdmin && newPassword ? newPassword : null,
        oldPassword: null,
        profilePicture: user.profilePicture ?? null,
        token: null,
        isAdmin: user.role === "ROLE_ADMIN",
        isLocalPasswordSet: true,
      };

      const formData = new FormData();

      formData.append(
        "userDTO",
        new Blob([JSON.stringify(userDTO)], { type: "application/json" }),
      );

      if (selectedFile) {
        formData.append("profilePicture", selectedFile);
      }

      const response = await fetch(endpoint, {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      if (!response.ok) {
        const message = await response.text();
        throw new Error(message);
      }

      setSnackbar({
        open: true,
        message: "User updated successfully",
        severity: "success",
      });

      await fetchUsers();
    } catch (err) {
      setSnackbar({
        open: true,
        message: err instanceof Error ? err.message : "Update failed",
        severity: "error",
      });
    }
  };

  const handlePageChange = (_: any, newPage: number) => {
    setPage(newPage);
    document
      .getElementById("admin-top")
      ?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <Box id="admin-top" sx={{ mt: 4, px: 2 }}>
      <Typography variant="h4" align="center" sx={{ mb: 3 }}>
        Admin Panel
      </Typography>

      <Tabs value={tab} onChange={(_, v) => setTab(v)}>
        <Tab label="Manage Users" />
      </Tabs>

      <TabPanel value={tab} index={0}>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Profile</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>Language</TableCell>
                {isSuperAdmin && <TableCell>Password</TableCell>}
                <TableCell>Active</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : (
                users.map((user) => {
                  const basicEditable = canEditBasic(user);
                  const disabled = isDisabledRow(user);
                  let userId: number;
                  let currentUserId: number;
                  if (typeof user.id === "string") {
                    userId = parseInt(user.id);
                  } else {
                    userId = user.id;
                  }
                  if (typeof currentUser.id === "string") {
                    currentUserId = parseInt(currentUser.id);
                  } else {
                    currentUserId = currentUser.id!;
                  }
                  const isSelf = userId === currentUserId;

                  return (
                    <TableRow
                      key={user.id}
                      sx={{ opacity: disabled ? 0.5 : 1 }}
                    >
                      <TableCell>
                        <Box
                          sx={{ display: "flex", alignItems: "center", gap: 1 }}
                        >
                          <Box
                            sx={{ position: "relative", width: 40, height: 40 }}
                          >
                            <Avatar
                              src={
                                selectedFiles[user.id]
                                  ? URL.createObjectURL(selectedFiles[user.id])
                                  : user.profilePicture
                              }
                              sx={{ width: 40, height: 40 }}
                            >
                              {!user.profilePicture &&
                                !selectedFiles[user.id] &&
                                user.name?.charAt(0)?.toUpperCase()}
                            </Avatar>

                            {basicEditable && (
                              <>
                                <Box
                                  component="label"
                                  sx={{
                                    position: "absolute",
                                    top: 0,
                                    left: 0,
                                    width: "100%",
                                    height: "100%",
                                    borderRadius: "50%",
                                    cursor: "pointer",
                                    display: "flex",
                                    alignItems: "center",
                                    justifyContent: "center",
                                    bgcolor: "rgba(0,0,0,0)",
                                    transition: "0.2s",
                                    "&:hover": {
                                      bgcolor: "rgba(0,0,0,0.4)",
                                    },
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

                                  {/* Upload Icon */}
                                  <Box
                                    className="upload-icon"
                                    sx={{
                                      opacity: 0,
                                      transition: "0.2s",
                                      color: "white",
                                      display: "flex",
                                    }}
                                  >
                                    <CloudUploadIcon />
                                  </Box>
                                </Box>
                              </>
                            )}
                          </Box>
                        </Box>
                      </TableCell>

                      <TableCell>
                        <Box
                          sx={{ display: "flex", alignItems: "center", gap: 1 }}
                        >
                          <TextField
                            value={user.name}
                            size="small"
                            disabled={!basicEditable}
                            onChange={(e) =>
                              handleChange(user.id, "name", e.target.value)
                            }
                          />
                          <Chip
                            label={
                              user.role === "ROLE_ADMIN"
                                ? "admin"
                                : user.role === "ROLE_SUPER_ADMIN"
                                  ? "super admin"
                                  : "user"
                            }
                            size="small"
                          />
                          {isSelf && <Chip label="you" size="small" />}
                        </Box>
                      </TableCell>

                      <TableCell>
                        <TextField
                          value={user.email}
                          size="small"
                          disabled={!isSuperAdmin}
                          onChange={(e) =>
                            handleChange(user.id, "email", e.target.value)
                          }
                        />
                      </TableCell>

                      <TableCell>
                        <Select
                          value={user.language}
                          size="small"
                          disabled={!basicEditable}
                          onChange={(e) =>
                            handleChange(user.id, "language", e.target.value)
                          }
                        >
                          <MenuItem value="en">EN</MenuItem>
                          <MenuItem value="de">DE</MenuItem>
                          <MenuItem value="ro">RO</MenuItem>
                        </Select>
                      </TableCell>

                      {isSuperAdmin && (
                        <TableCell>
                          <TextField
                            type="password"
                            size="small"
                            placeholder="New password"
                            value={passwords[user.id] || ""}
                            onChange={(e) =>
                              setPasswords((prev) => ({
                                ...prev,
                                [user.id]: e.target.value,
                              }))
                            }
                          />
                        </TableCell>
                      )}

                      <TableCell>
                        <Switch
                          checked={user.active}
                          disabled={!basicEditable}
                          onChange={(e) =>
                            handleChange(user.id, "active", e.target.checked)
                          }
                        />
                      </TableCell>

                      <TableCell>
                        <Button
                          variant="contained"
                          size="small"
                          disabled={!basicEditable && !isSuperAdmin}
                          onClick={() => handleSave(user)}
                        >
                          Save
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })
              )}
            </TableBody>
          </Table>

          <TablePagination
            component="div"
            count={totalCount}
            page={page}
            onPageChange={handlePageChange}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
              document
                .getElementById("admin-top")
                ?.scrollIntoView({ behavior: "smooth" });
            }}
            rowsPerPageOptions={[10, 20, 50]}
            showFirstButton
            showLastButton
          />
        </TableContainer>
        <Snackbar
          open={snackbar.open}
          autoHideDuration={4000}
          onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
          anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
        >
          <Alert
            onClose={() => setSnackbar((prev) => ({ ...prev, open: false }))}
            severity={snackbar.severity}
            variant="filled"
            sx={{ width: "100%" }}
          >
            {snackbar.message}
          </Alert>
        </Snackbar>
      </TabPanel>
    </Box>
  );
}
