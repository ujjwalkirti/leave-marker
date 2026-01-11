'use client';

import { useEffect, useState } from 'react';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Textarea } from '@/components/ui/textarea';
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { leaveApplicationAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { toast } from 'sonner';
import { Loader2, Plus, Check, X } from 'lucide-react';

const LEAVE_TYPES = [
  'CASUAL_LEAVE',
  'SICK_LEAVE',
  'EARNED_LEAVE',
  'MATERNITY_LEAVE',
  'PATERNITY_LEAVE',
  'COMP_OFF',
  'LOSS_OF_PAY',
];

interface LeaveApplication {
  id: number;
  employeeName: string;
  leaveType: string;
  startDate: string;
  endDate: string;
  numberOfDays: number;
  reason: string;
  status: string;
  managerComments?: string;
  hrComments?: string;
}

export default function LeaveApplicationsPage() {
  const { user } = useAuth();
  const [myApplications, setMyApplications] = useState<LeaveApplication[]>([]);
  const [pendingApprovals, setPendingApprovals] = useState<LeaveApplication[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [formData, setFormData] = useState({
    leaveType: 'CASUAL_LEAVE',
    startDate: '',
    endDate: '',
    isHalfDay: false,
    reason: '',
    attachmentUrl: null as string | null,
  });

  const isManagerOrHR = user?.role === 'MANAGER' || user?.role === 'HR_ADMIN' || user?.role === 'SUPER_ADMIN';

  useEffect(() => {
    fetchMyApplications();
    if (isManagerOrHR) {
      fetchPendingApprovals();
    }
  }, [isManagerOrHR]);

  const fetchMyApplications = async () => {
    try {
      setLoading(true);
      const response = await leaveApplicationAPI.getMyApplications();
      if (response.data.success) {
        setMyApplications(response.data.data);
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to fetch applications');
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingApprovals = async () => {
    try {
      const isHR = user?.role === 'HR_ADMIN' || user?.role === 'SUPER_ADMIN';
      const response = isHR
        ? await leaveApplicationAPI.getPendingHRApprovals()
        : await leaveApplicationAPI.getPendingManagerApprovals();

      if (response.data.success) {
        setPendingApprovals(response.data.data);
      }
    } catch (error: any) {
      console.error('Failed to fetch pending approvals:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      await leaveApplicationAPI.applyForLeave(formData);
      toast.success('Leave application submitted successfully!');
      setDialogOpen(false);
      resetForm();
      await fetchMyApplications();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to submit application');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApprove = async (id: number, isHR: boolean) => {
    try {
      if (isHR) {
        await leaveApplicationAPI.hrApprove(id);
      } else {
        await leaveApplicationAPI.managerApprove(id);
      }
      toast.success('Leave approved successfully!');
      await fetchPendingApprovals();
      await fetchMyApplications();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to approve leave');
    }
  };

  const handleReject = async (id: number, isHR: boolean) => {
    const comments = prompt('Enter rejection reason:');
    if (!comments) return;

    try {
      if (isHR) {
        await leaveApplicationAPI.hrReject(id, { comments });
      } else {
        await leaveApplicationAPI.managerReject(id, { comments });
      }
      toast.success('Leave rejected successfully!');
      await fetchPendingApprovals();
      await fetchMyApplications();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to reject leave');
    }
  };

  const handleCancel = async (id: number) => {
    if (!confirm('Are you sure you want to cancel this leave application?')) return;

    try {
      await leaveApplicationAPI.cancelLeave(id);
      toast.success('Leave cancelled successfully!');
      await fetchMyApplications();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to cancel leave');
    }
  };

  const resetForm = () => {
    setFormData({
      leaveType: 'CASUAL_LEAVE',
      startDate: '',
      endDate: '',
      isHalfDay: false,
      reason: '',
      attachmentUrl: null,
    });
  };

  const getStatusBadge = (status: string) => {
    const variants: { [key: string]: 'default' | 'secondary' | 'destructive' | 'outline' } = {
      PENDING: 'outline',
      MANAGER_APPROVED: 'secondary',
      APPROVED: 'default',
      REJECTED: 'destructive',
      CANCELLED: 'secondary',
    };
    return <Badge variant={variants[status] || 'outline'}>{status.replace(/_/g, ' ')}</Badge>;
  };

  const renderLeaveTable = (applications: LeaveApplication[], showActions: boolean = false) => (
    <div className="overflow-x-auto">
      <Table>
        <TableHeader>
          <TableRow>
            {showActions && <TableHead>Employee</TableHead>}
            <TableHead>Type</TableHead>
            <TableHead>Start Date</TableHead>
            <TableHead>End Date</TableHead>
            <TableHead>Days</TableHead>
            <TableHead>Reason</TableHead>
            <TableHead>Status</TableHead>
            {showActions && <TableHead>Actions</TableHead>}
            {!showActions && <TableHead>Actions</TableHead>}
          </TableRow>
        </TableHeader>
        <TableBody>
          {applications.length === 0 ? (
            <TableRow>
              <TableCell colSpan={showActions ? 8 : 7} className="text-center text-gray-500 py-8">
                No applications found
              </TableCell>
            </TableRow>
          ) : (
            applications.map((app) => (
              <TableRow key={app.id}>
                {showActions && <TableCell>{app.employeeName}</TableCell>}
                <TableCell>
                  <Badge variant="outline">{app.leaveType.replace(/_/g, ' ')}</Badge>
                </TableCell>
                <TableCell>{new Date(app.startDate).toLocaleDateString()}</TableCell>
                <TableCell>{new Date(app.endDate).toLocaleDateString()}</TableCell>
                <TableCell>{app.numberOfDays}</TableCell>
                <TableCell className="max-w-xs truncate">{app.reason}</TableCell>
                <TableCell>{getStatusBadge(app.status)}</TableCell>
                <TableCell>
                  {showActions ? (
                    <div className="flex gap-2">
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleApprove(app.id, user?.role === 'HR_ADMIN' || user?.role === 'SUPER_ADMIN')}
                      >
                        <Check className="h-4 w-4 text-green-600" />
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleReject(app.id, user?.role === 'HR_ADMIN' || user?.role === 'SUPER_ADMIN')}
                      >
                        <X className="h-4 w-4 text-red-600" />
                      </Button>
                    </div>
                  ) : (
                    app.status === 'PENDING' && (
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => handleCancel(app.id)}
                      >
                        Cancel
                      </Button>
                    )
                  )}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Leave Applications</h1>
            <p className="text-gray-500 mt-1">Manage your leave requests</p>
          </div>
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                Apply for Leave
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Apply for Leave</DialogTitle>
                <DialogDescription>Submit a new leave application</DialogDescription>
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
                    <Label htmlFor="startDate">Start Date</Label>
                    <Input
                      id="startDate"
                      type="date"
                      value={formData.startDate}
                      onChange={(e) =>
                        setFormData({ ...formData, startDate: e.target.value })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="endDate">End Date</Label>
                    <Input
                      id="endDate"
                      type="date"
                      value={formData.endDate}
                      onChange={(e) =>
                        setFormData({ ...formData, endDate: e.target.value })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="reason">Reason</Label>
                  <Textarea
                    id="reason"
                    value={formData.reason}
                    onChange={(e) =>
                      setFormData({ ...formData, reason: e.target.value })
                    }
                    required
                    disabled={submitting}
                    rows={4}
                  />
                </div>

                <div className="flex justify-end gap-4 pt-4">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setDialogOpen(false)}
                    disabled={submitting}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={submitting}>
                    {submitting ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Submitting...
                      </>
                    ) : (
                      'Submit Application'
                    )}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        <Card>
          <CardContent className="pt-6">
            <Tabs defaultValue="my-applications">
              <TabsList className="mb-4">
                <TabsTrigger value="my-applications">My Applications</TabsTrigger>
                {isManagerOrHR && (
                  <TabsTrigger value="pending-approvals">
                    Pending Approvals ({pendingApprovals.length})
                  </TabsTrigger>
                )}
              </TabsList>

              <TabsContent value="my-applications">
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
                  </div>
                ) : (
                  renderLeaveTable(myApplications, false)
                )}
              </TabsContent>

              {isManagerOrHR && (
                <TabsContent value="pending-approvals">
                  {renderLeaveTable(pendingApprovals, true)}
                </TabsContent>
              )}
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
