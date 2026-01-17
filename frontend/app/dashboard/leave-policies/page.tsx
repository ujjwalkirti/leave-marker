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
import { leavePolicyAPI, leaveBalanceAPI } from '@/lib/api';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/utils';
import { Loader2, Plus, Pencil, Trash2, RefreshCw } from 'lucide-react';
import { Switch } from '@/components/ui/switch';
import { useAuth } from '@/lib/auth-context';

const LEAVE_TYPES = [
  'CASUAL_LEAVE',
  'SICK_LEAVE',
  'EARNED_LEAVE',
  'MATERNITY_LEAVE',
  'PATERNITY_LEAVE',
  'COMP_OFF',
  'LOSS_OF_PAY',
];

interface LeavePolicy {
  id: number;
  leaveType: string;
  annualQuota: number;
  monthlyAccrual: number;
  carryForward: boolean;
  maxCarryForward: number;
  encashmentAllowed: boolean;
  halfDayAllowed: boolean;
  active: boolean;
}

export default function LeavePoliciesPage() {
  const { user } = useAuth();
  const [policies, setPolicies] = useState<LeavePolicy[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editingPolicy, setEditingPolicy] = useState<LeavePolicy | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [initializingBalances, setInitializingBalances] = useState(false);
  const [togglingPolicy, setTogglingPolicy] = useState<number | null>(null);
  const [formData, setFormData] = useState({
    leaveType: 'CASUAL_LEAVE',
    annualQuota: 12,
    monthlyAccrual: 1.0,
    carryForward: false,
    maxCarryForward: 0,
    encashmentAllowed: false,
    halfDayAllowed: true,
    active: true,
  });

  useEffect(() => {
    fetchPolicies();
  }, []);

  const fetchPolicies = async () => {
    try {
      setLoading(true);
      const response = await leavePolicyAPI.getAllPolicies();
      if (response.data.success) {
        setPolicies(response.data.data);
      }
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to fetch policies'));
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      if (editingPolicy) {
        await leavePolicyAPI.updatePolicy(editingPolicy.id, formData);
        toast.success('Leave policy updated successfully!');
      } else {
        await leavePolicyAPI.createPolicy(formData);
        toast.success('Leave policy created successfully!');
      }
      setDialogOpen(false);
      resetForm();
      await fetchPolicies();
    } catch (error) {
      toast.error(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (policy: LeavePolicy) => {
    setEditingPolicy(policy);
    setFormData({
      leaveType: policy.leaveType,
      annualQuota: policy.annualQuota,
      monthlyAccrual: policy.monthlyAccrual,
      carryForward: policy.carryForward,
      maxCarryForward: policy.maxCarryForward,
      encashmentAllowed: policy.encashmentAllowed,
      halfDayAllowed: policy.halfDayAllowed,
      active: policy.active,
    });
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this leave policy?')) return;

    try {
      await leavePolicyAPI.deletePolicy(id);
      toast.success('Leave policy deleted successfully!');
      await fetchPolicies();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to delete policy'));
    }
  };

  const resetForm = () => {
    setEditingPolicy(null);
    setFormData({
      leaveType: 'CASUAL_LEAVE',
      annualQuota: 12,
      monthlyAccrual: 1.0,
      carryForward: false,
      maxCarryForward: 0,
      encashmentAllowed: false,
      halfDayAllowed: true,
      active: true,
    });
  };

  const handleDialogClose = (open: boolean) => {
    setDialogOpen(open);
    if (!open) {
      resetForm();
    }
  };

  const handleToggleActive = async (policy: LeavePolicy) => {
    try {
      setTogglingPolicy(policy.id);
      const newActiveState = !policy.active;

      await leavePolicyAPI.updatePolicy(policy.id, {
        ...policy,
        active: newActiveState,
      });

      toast.success(
        newActiveState
          ? 'Leave policy activated successfully!'
          : 'Leave policy deactivated successfully!'
      );

      await fetchPolicies();
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to update policy status'));
    } finally {
      setTogglingPolicy(null);
    }
  };

  const handleInitializeBalances = async () => {
    try {
      setInitializingBalances(true);
      const response = await leaveBalanceAPI.initializeLeaveBalances();
      if (response.data.success) {
        toast.success(response.data.message);
      }
    } catch (error) {
      toast.error(getErrorMessage(error, 'Failed to initialize leave balances'));
    } finally {
      setInitializingBalances(false);
    }
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-foreground">Leave Policies</h1>
            <p className="text-muted-foreground mt-1">Manage company leave policies</p>
          </div>
          <div className="flex gap-3">
            {(user?.role === 'SUPER_ADMIN' || user?.role === 'HR_ADMIN') && (
              <Button
                onClick={handleInitializeBalances}
                disabled={initializingBalances}
                variant="outline"
                className="border-primary text-primary hover:bg-primary/5"
              >
                {initializingBalances ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Initializing...
                  </>
                ) : (
                  <>
                    <RefreshCw className="h-4 w-4 mr-2" />
                    Initialize Balances
                  </>
                )}
              </Button>
            )}
            <Dialog open={dialogOpen} onOpenChange={handleDialogClose}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="h-4 w-4 mr-2" />
                  Add Policy
                </Button>
              </DialogTrigger>
              <DialogContent>
              <DialogHeader>
                <DialogTitle>
                  {editingPolicy ? 'Edit Leave Policy' : 'Add New Leave Policy'}
                </DialogTitle>
                <DialogDescription>
                  {editingPolicy
                    ? 'Update leave policy settings'
                    : 'Create a new leave policy for your company'}
                </DialogDescription>
              </DialogHeader>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="leaveType">Leave Type</Label>
                  <Select
                    value={formData.leaveType}
                    onValueChange={(value) =>
                      setFormData({ ...formData, leaveType: value })
                    }
                    disabled={submitting}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {LEAVE_TYPES.map((type) => (
                        <SelectItem key={type} value={type}>
                          {type.replace(/_/g, ' ')}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="annualQuota">Annual Quota</Label>
                    <Input
                      id="annualQuota"
                      type="number"
                      min="0"
                      value={formData.annualQuota}
                      onChange={(e) =>
                        setFormData({ ...formData, annualQuota: parseInt(e.target.value) })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="monthlyAccrual">Monthly Accrual</Label>
                    <Input
                      id="monthlyAccrual"
                      type="number"
                      min="0"
                      step="0.1"
                      value={formData.monthlyAccrual}
                      onChange={(e) =>
                        setFormData({ ...formData, monthlyAccrual: parseFloat(e.target.value) })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="flex items-center space-x-2">
                    <input
                      id="carryForward"
                      type="checkbox"
                      checked={formData.carryForward}
                      onChange={(e) =>
                        setFormData({ ...formData, carryForward: e.target.checked })
                      }
                      disabled={submitting}
                      className="h-4 w-4 rounded border-gray-300"
                    />
                    <Label htmlFor="carryForward" className="font-normal">
                      Carry Forward
                    </Label>
                  </div>

                  <div className="flex items-center space-x-2">
                    <input
                      id="halfDayAllowed"
                      type="checkbox"
                      checked={formData.halfDayAllowed}
                      onChange={(e) =>
                        setFormData({ ...formData, halfDayAllowed: e.target.checked })
                      }
                      disabled={submitting}
                      className="h-4 w-4 rounded border-gray-300"
                    />
                    <Label htmlFor="halfDayAllowed" className="font-normal">
                      Half Day Allowed
                    </Label>
                  </div>
                </div>

                <div className="flex items-center space-x-2">
                  <input
                    id="encashmentAllowed"
                    type="checkbox"
                    checked={formData.encashmentAllowed}
                    onChange={(e) =>
                      setFormData({ ...formData, encashmentAllowed: e.target.checked })
                    }
                    disabled={submitting}
                    className="h-4 w-4 rounded border-gray-300"
                  />
                  <Label htmlFor="encashmentAllowed" className="font-normal">
                    Encashment Allowed
                  </Label>
                </div>

                {formData.carryForward && (
                  <div className="space-y-2">
                    <Label htmlFor="maxCarryForward">Maximum Carry Forward Days</Label>
                    <Input
                      id="maxCarryForward"
                      type="number"
                      min="0"
                      value={formData.maxCarryForward}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          maxCarryForward: parseInt(e.target.value),
                        })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                )}

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
                        {editingPolicy ? 'Updating...' : 'Creating...'}
                      </>
                    ) : editingPolicy ? (
                      'Update Policy'
                    ) : (
                      'Create Policy'
                    )}
                  </Button>
                </div>
              </form>
              </DialogContent>
            </Dialog>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Policy List</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
              </div>
            ) : policies.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                No leave policies found. Add your first policy to get started.
              </p>
            ) : (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Leave Type</TableHead>
                      <TableHead>Annual Quota</TableHead>
                      <TableHead>Monthly Accrual</TableHead>
                      <TableHead>Carry Forward</TableHead>
                      <TableHead>Max Carry Forward</TableHead>
                      <TableHead>Active</TableHead>
                      <TableHead>Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {policies.map((policy) => (
                      <TableRow key={policy.id}>
                        <TableCell>
                          <Badge variant="outline">
                            {policy.leaveType.replace(/_/g, ' ')}
                          </Badge>
                        </TableCell>
                        <TableCell className="font-medium">
                          {policy.annualQuota} days
                        </TableCell>
                        <TableCell>
                          {policy.monthlyAccrual} days/month
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={policy.carryForward ? 'default' : 'secondary'}
                          >
                            {policy.carryForward ? 'Allowed' : 'Not Allowed'}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          {policy.carryForward
                            ? `${policy.maxCarryForward} days`
                            : '-'}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Switch
                              checked={policy.active}
                              onCheckedChange={() => handleToggleActive(policy)}
                              disabled={togglingPolicy === policy.id}
                            />
                            <span className="text-sm text-muted-foreground">
                              {policy.active ? 'Active' : 'Inactive'}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <div className="flex gap-2">
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleEdit(policy)}
                            >
                              <Pencil className="h-4 w-4" />
                            </Button>
                            <Button
                              size="sm"
                              variant="outline"
                              onClick={() => handleDelete(policy.id)}
                            >
                              <Trash2 className="h-4 w-4" />
                            </Button>
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
