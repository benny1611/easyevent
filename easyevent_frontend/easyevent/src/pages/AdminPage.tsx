import React, { useState } from "react";
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
} from "@mui/material";

interface User {
  id: number;
  name: string;
  email: string;
  role: "ROLE_USER" | "ROLE_ADMIN" | "ROLE_SUPER_ADMIN";
  language: string;
  active: boolean;
  profilePicture?: string;
}

const mockUsers: User[] = Array.from({ length: 55 }, (_, i) => ({
  id: i + 1,
  name: `User ${i + 1}`,
  email: `user${i + 1}@example.com`,
  role: i % 10 === 0 ? "ROLE_ADMIN" : "ROLE_USER",
  language: "en",
  active: i % 3 !== 0,
}));

function TabPanel({ children, value, index }: any) {
  return value === index ? <Box sx={{ p: 2 }}>{children}</Box> : null;
}

export default function AdminPage() {
  const [tab, setTab] = useState(0);
  const [users, setUsers] = useState<User[]>(mockUsers);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(20);

  const currentUser: { id: number; role: "ROLE_USER" | "ROLE_ADMIN" | "ROLE_SUPER_ADMIN" } = {
    id: 2,
    role: "ROLE_ADMIN", // change for testing
  };

  const isSuperAdmin = currentUser.role === "ROLE_SUPER_ADMIN";

  const handleChange = (id: number, field: keyof User, value: any) => {
    setUsers((prev) =>
      prev.map((u) => (u.id === id ? { ...u, [field]: value } : u))
    );
  };

  const canEditBasic = (target: User) => {
    if (isSuperAdmin) return true;
    if (currentUser.role === "ROLE_ADMIN") {
      return target.role === "ROLE_USER";
    }
    return false;
  };

  const isDisabledRow = (target: User) => !canEditBasic(target);

  const paginatedUsers = users.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  const handlePageChange = (_: any, newPage: number) => {
    setPage(newPage);
    document.getElementById("admin-top")?.scrollIntoView({ behavior: "smooth" });
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
              {paginatedUsers.map((user) => {
                const basicEditable = canEditBasic(user);
                const disabled = isDisabledRow(user);
                const isSelf = user.id === currentUser.id;

                return (
                  <TableRow
                    key={user.id}
                    sx={{ opacity: disabled ? 0.5 : 1 }}
                  >
                    <TableCell>
                      <Avatar src={user.profilePicture} />
                    </TableCell>

                    <TableCell>
                      <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                        <TextField
                          value={user.name}
                          size="small"
                          disabled={!basicEditable}
                          onChange={(e) =>
                            handleChange(user.id, "name", e.target.value)
                          }
                        />
                        {isSelf && <Chip label="you" size="small" />}
                        {disabled && !isSelf && (
                          <Chip
                            label={
                              user.role === "ROLE_ADMIN"
                                ? "admin"
                                : "super admin"
                            }
                            size="small"
                            color="default"
                          />
                        )}
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
                      >
                        Save
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>

          <TablePagination
            component="div"
            count={users.length}
            page={page}
            onPageChange={handlePageChange}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={(e) => {
              setRowsPerPage(parseInt(e.target.value, 10));
              setPage(0);
              document.getElementById("admin-top")?.scrollIntoView({ behavior: "smooth" });
            }}
            rowsPerPageOptions={[10, 20, 50]}
            showFirstButton
            showLastButton
          />
        </TableContainer>
      </TabPanel>
    </Box>
  );
}
