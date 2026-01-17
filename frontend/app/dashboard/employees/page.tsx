'use client';

import { useEffect, useState } from 'react';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { employeeAPI } from '@/lib/api';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/utils';
import { Loader2, Plus, Pencil, UserX, UserCheck } from 'lucide-react';

const ROLES = ['EMPLOYEE', 'MANAGER', 'HR_ADMIN', 'SUPER_ADMIN'];

const INDIAN_STATES = [
  'ANDHRA_PRADESH', 'ARUNACHAL_PRADESH', 'ASSAM', 'BIHAR', 'CHHATTISGARH',
  'GOA', 'GUJARAT', 'HARYANA', 'HIMACHAL_PRADESH', 'JHARKHAND',
  'KARNATAKA', 'KERALA', 'MADHYA_PRADESH', 'MAHARASHTRA', 'MANIPUR',
  'MEGHALAYA', 'MIZORAM', 'NAGALAND', 'ODISHA', 'PUNJAB',
  'RAJASTHAN', 'SIKKIM', 'TAMIL_NADU', 'TELANGANA', 'TRIPURA',
  'UTTAR_PRADESH', 'UTTARAKHAND', 'WEST_BENGAL', 'DELHI'
];

interface Employee {
  id: number;
  employeeId: string;
  fullName: string;
  email: string;
  role: string;
  workLocation: string;
  dateOfJoining: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export default function EmployeesPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingEmployee, setEditingEmployee] = useState<Employee | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    password: '',
    employeeId: '',
    role: 'EMPLOYEE',
    department: 'Engineering',
    jobTitle: 'Software Engineer',
    dateOfJoining: new Date().toISOString().split('T')[0],
    employmentType: 'FULL_TIME',
    workLocation: '',
    managerId: null as number | null,
  });

  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const response = await employeeAPI.getAllEmployees();
      if (response.data.success) {
        setEmployees(response.data.data);
      }
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to fetch employees'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (editingEmployee) {
        await employeeAPI.updateEmployee(editingEmployee.id, formData);
        toast.success('Employee updated successfully!');
      } else {
        await employeeAPI.createEmployee(formData);
        toast.success('Employee created successfully!');
      }
      setDialogOpen(false);
      resetForm();
      await fetchEmployees();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (employee: Employee) => {
    setEditingEmployee(employee);
    setFormData({
      fullName: employee.fullName,
      email: employee.email,
      password: '',
      employeeId: employee.employeeId,
      role: employee.role,
      department: 'Engineering',
      jobTitle: 'Software Engineer',
      dateOfJoining: employee.dateOfJoining,
      employmentType: 'FULL_TIME',
      workLocation: employee.workLocation,
      managerId: null,
    });
    setDialogOpen(true);
  };

  const handleDeactivate = async (id: number) => {
    if (!confirm('Are you sure you want to deactivate this employee?')) return;

    try {
      await employeeAPI.deactivateEmployee(id);
      toast.success('Employee deactivated successfully!');
      await fetchEmployees();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to deactivate employee'));
    }
  };

  const handleReactivate = async (id: number) => {
    if (!confirm('Are you sure you want to reactivate this employee?')) return;

    try {
      await employeeAPI.reactivateEmployee(id);
      toast.success('Employee reactivated successfully!');
      await fetchEmployees();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to reactivate employee'));
    }
  };

  const resetForm = () => {
    setEditingEmployee(null);
    setFormData({
      fullName: '',
      email: '',
      password: '',
      employeeId: '',
      role: 'EMPLOYEE',
      department: 'Engineering',
      jobTitle: 'Software Engineer',
      dateOfJoining: new Date().toISOString().split('T')[0],
      employmentType: 'FULL_TIME',
      workLocation: '',
      managerId: null,
    });
  };

  const handleDialogClose = (open: boolean) => {
    setDialogOpen(open);
    if (!open) {
      resetForm();
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold">Employees</h1>
            <p className="text-muted-foreground mt-1">Manage your company employees</p>
          </div>
          <Dialog open={dialogOpen} onOpenChange={handleDialogClose}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Add Employee
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>
                  {editingEmployee ? 'Edit Employee' : 'Add New Employee'}
                </DialogTitle>
                <DialogDescription>
                  {editingEmployee
                    ? 'Update employee information'
                    : 'Create a new employee account'}
                </DialogDescription>
              </DialogHeader>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="fullName">Full Name</Label>
                    <Input
                      id="fullName"
                      value={formData.fullName}
                      onChange={(e) =>
                        setFormData({ ...formData, fullName: e.target.value })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <Input
                      id="email"
                      type="email"
                      value={formData.email}
                      onChange={(e) =>
                        setFormData({ ...formData, email: e.target.value })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="employeeId">Employee ID</Label>
                    <Input
                      id="employeeId"
                      value={formData.employeeId}
                      onChange={(e) =>
                        setFormData({ ...formData, employeeId: e.target.value })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="password">
                      Password {editingEmployee && '(leave empty to keep current)'}
                    </Label>
                    <Input
                      id="password"
                      type="password"
                      value={formData.password}
                      onChange={(e) =>
                        setFormData({ ...formData, password: e.target.value })
                      }
                      required={!editingEmployee}
                      minLength={8}
                      disabled={submitting}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="role">Role</Label>
                    <Select
                      value={formData.role}
                      onValueChange={(value) =>
                        setFormData({ ...formData, role: value })
                      }
                      disabled={submitting}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {ROLES.map((role) => (
                          <SelectItem key={role} value={role}>
                            {role.replace(/_/g, ' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="workLocation">Work Location</Label>
                    <Select
                      value={formData.workLocation}
                      onValueChange={(value) =>
                        setFormData({ ...formData, workLocation: value })
                      }
                      disabled={submitting}
                    >
                      <SelectTrigger>
                        <SelectValue placeholder="Select state" />
                      </SelectTrigger>
                      <SelectContent>
                        {INDIAN_STATES.map((state) => (
                          <SelectItem key={state} value={state}>
                            {state.replace(/_/g, ' ')}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="dateOfJoining">Date of Joining</Label>
                  <Input
                    id="dateOfJoining"
                    type="date"
                    value={formData.dateOfJoining}
                    onChange={(e) =>
                      setFormData({ ...formData, dateOfJoining: e.target.value })
                    }
                    required
                    disabled={submitting}
                  />
                </div>

                <div className="flex justify-end gap-4 pt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => handleDialogClose(false)}
                    disabled={submitting}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={submitting}>
                    {submitting ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        {editingEmployee ? 'Updating...' : 'Creating...'}
                      </>
                    ) : editingEmployee ? (
                      'Update Employee'
                    ) : (
                      'Create Employee'
                    )}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Employee List</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            ) : employees.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                No employees found. Add your first employee to get started.
              </p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Employee ID</TableHead>
                      <TableHead>Name</TableHead>
                      <TableHead>Email</TableHead>
                      <TableHead>Role</TableHead>
                      <TableHead>Location</TableHead>
                      <TableHead>Joining Date</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {employees.map((employee) => (
                      <TableRow key={employee.id}>
                        <TableCell className="font-medium">
                          {employee.employeeId}
                        </TableCell>
                        <TableCell>{employee.fullName}</TableCell>
                        <TableCell>{employee.email}</TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {employee.role.replace(/_/g, ' ')}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {employee.workLocation.replace(/_/g, ' ')}
                        </TableCell>
                        <TableCell>
                          {new Date(employee.dateOfJoining).toLocaleDateString()}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={employee.status === 'ACTIVE' ? 'default' : 'secondary'}
                          >
                            {employee.status === 'ACTIVE' ? 'Active' : 'Inactive'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleEdit(employee)}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            {employee.status === 'ACTIVE' ? (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => handleDeactivate(employee.id)}
                              >
                                <UserX className="h-4 w-4" />
                              </Button>
                            ) : (
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() => handleReactivate(employee.id)}
                              >
                                <UserCheck className="h-4 w-4" />
                              </Button>
                            )}
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
