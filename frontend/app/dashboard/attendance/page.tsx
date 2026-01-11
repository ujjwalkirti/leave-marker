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
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { attendanceAPI } from '@/lib/api';
import { useAuth } from '@/lib/auth-context';
import { toast } from 'sonner';
import { Loader2, AlertCircle, Check, X } from 'lucide-react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';

interface Attendance {
  id: number;
  date: string;
  punchInTime: string;
  punchOutTime?: string;
  status: string;
  hoursWorked?: number;
}

interface AttendanceCorrection {
  id: number;
  employeeName: string;
  date: string;
  currentPunchIn?: string;
  currentPunchOut?: string;
  requestedPunchIn: string;
  requestedPunchOut: string;
  reason: string;
  status: string;
}

export default function AttendancePage() {
  const { user } = useAuth();
  const [attendance, setAttendance] = useState<Attendance[]>([]);
  const [corrections, setCorrections] = useState<AttendanceCorrection[]>([]);
  const [loading, setLoading] = useState(true);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
  });
  const [correctionFormData, setCorrectionFormData] = useState({
    attendanceId: null as number | null,
    punchInTime: '',
    punchOutTime: '',
    workType: 'OFFICE',
    remarks: '',
  });

  const isHROrAdmin = user?.role === 'HR_ADMIN' || user?.role === 'SUPER_ADMIN';

  useEffect(() => {
    fetchAttendance();
    if (isHROrAdmin) {
      fetchPendingCorrections();
    }
  }, [dateRange, isHROrAdmin]);

  const fetchAttendance = async () => {
    try {
      setLoading(true);
      const response = await attendanceAPI.getMyAttendanceByDateRange(
        dateRange.startDate,
        dateRange.endDate
      );
      if (response.data.success) {
        setAttendance(response.data.data);
      }
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to fetch attendance');
    } finally {
      setLoading(false);
    }
  };

  const fetchPendingCorrections = async () => {
    try {
      const response = await attendanceAPI.getPendingCorrections();
      if (response.data.success) {
        setCorrections(response.data.data);
      }
    } catch (error: any) {
      console.error('Failed to fetch pending corrections:', error);
    }
  };

  const handleCorrectionSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!correctionFormData.attendanceId) {
      toast.error('Please select an attendance record');
      return;
    }

    setSubmitting(true);

    try {
      await attendanceAPI.requestCorrection(correctionFormData.attendanceId, {
        punchInTime: correctionFormData.punchInTime,
        punchOutTime: correctionFormData.punchOutTime,
        workType: correctionFormData.workType,
        remarks: correctionFormData.remarks,
      });
      toast.success('Correction request submitted successfully!');
      setDialogOpen(false);
      resetCorrectionForm();
      await fetchAttendance();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to submit correction');
    } finally {
      setSubmitting(false);
    }
  };

  const handleApproveCorrection = async (id: number) => {
    try {
      await attendanceAPI.approveCorrection(id);
      toast.success('Correction approved successfully!');
      await fetchPendingCorrections();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to approve correction');
    }
  };

  const handleRejectCorrection = async (id: number) => {
    try {
      await attendanceAPI.rejectCorrection(id);
      toast.success('Correction rejected successfully!');
      await fetchPendingCorrections();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to reject correction');
    }
  };

  const resetCorrectionForm = () => {
    setCorrectionFormData({
      attendanceId: null,
      punchInTime: '',
      punchOutTime: '',
      workType: 'OFFICE',
      remarks: '',
    });
  };

  const formatTime = (dateTime: string) => {
    if (!dateTime) return '-';
    return new Date(dateTime).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getStatusBadge = (status: string) => {
    const variants: { [key: string]: 'default' | 'secondary' | 'destructive' | 'outline' } = {
      PRESENT: 'default',
      ABSENT: 'destructive',
      HALF_DAY: 'secondary',
      ON_LEAVE: 'outline',
      PENDING: 'outline',
      APPROVED: 'default',
      REJECTED: 'destructive',
    };
    return <Badge variant={variants[status] || 'outline'}>{status.replace(/_/g, ' ')}</Badge>;
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Attendance</h1>
            <p className="text-gray-500 mt-1">View and manage your attendance records</p>
          </div>
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger asChild>
              <Button variant="outline">
                <AlertCircle className="h-4 w-4 mr-2" />
                Request Correction
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Request Attendance Correction</DialogTitle>
                <DialogDescription>
                  Submit a request to correct your attendance record
                </DialogDescription>
              </DialogHeader>
              <form onSubmit={handleCorrectionSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="attendanceId">Select Attendance Record</Label>
                  <Select
                    value={correctionFormData.attendanceId?.toString() || ''}
                    onValueChange={(value) =>
                      setCorrectionFormData({ ...correctionFormData, attendanceId: parseInt(value) })
                    }
                    disabled={submitting}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select attendance to correct" />
                    </SelectTrigger>
                    <SelectContent>
                      {attendance.map((record) => (
                        <SelectItem key={record.id} value={record.id.toString()}>
                          {new Date(record.date).toLocaleDateString()} - {record.status}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="punchInTime">Punch In Time</Label>
                    <Input
                      id="punchInTime"
                      type="time"
                      value={correctionFormData.punchInTime}
                      onChange={(e) =>
                        setCorrectionFormData({
                          ...correctionFormData,
                          punchInTime: e.target.value,
                        })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="punchOutTime">Punch Out Time</Label>
                    <Input
                      id="punchOutTime"
                      type="time"
                      value={correctionFormData.punchOutTime}
                      onChange={(e) =>
                        setCorrectionFormData({
                          ...correctionFormData,
                          punchOutTime: e.target.value,
                        })
                      }
                      required
                      disabled={submitting}
                    />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="remarks">Remarks</Label>
                  <Textarea
                    id="remarks"
                    value={correctionFormData.remarks}
                    onChange={(e) =>
                      setCorrectionFormData({ ...correctionFormData, remarks: e.target.value })
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
                      'Submit Request'
                    )}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        {/* Date Range Filter */}
        <Card>
          <CardHeader>
            <CardTitle>Filter by Date Range</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 items-end">
              <div className="space-y-2">
                <Label htmlFor="startDate">Start Date</Label>
                <Input
                  id="startDate"
                  type="date"
                  value={dateRange.startDate}
                  onChange={(e) =>
                    setDateRange({ ...dateRange, startDate: e.target.value })
                  }
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="endDate">End Date</Label>
                <Input
                  id="endDate"
                  type="date"
                  value={dateRange.endDate}
                  onChange={(e) =>
                    setDateRange({ ...dateRange, endDate: e.target.value })
                  }
                />
              </div>
              <Button onClick={fetchAttendance}>Apply Filter</Button>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="pt-6">
            <Tabs defaultValue="attendance">
              <TabsList className="mb-4">
                <TabsTrigger value="attendance">My Attendance</TabsTrigger>
                {isHROrAdmin && (
                  <TabsTrigger value="corrections">
                    Pending Corrections ({corrections.length})
                  </TabsTrigger>
                )}
              </TabsList>

              <TabsContent value="attendance">
                {loading ? (
                  <div className="flex items-center justify-center py-8">
                    <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
                  </div>
                ) : attendance.length === 0 ? (
                  <p className="text-center text-gray-500 py-8">
                    No attendance records found for the selected date range
                  </p>
                ) : (
                  <div className="overflow-x-auto">
                    <Table>
                      <TableHeader>
                        <TableRow>
                          <TableHead>Date</TableHead>
                          <TableHead>Punch In</TableHead>
                          <TableHead>Punch Out</TableHead>
                          <TableHead>Hours Worked</TableHead>
                          <TableHead>Status</TableHead>
                        </TableRow>
                      </TableHeader>
                      <TableBody>
                        {attendance.map((record) => (
                          <TableRow key={record.id}>
                            <TableCell>
                              {new Date(record.date).toLocaleDateString()}
                            </TableCell>
                            <TableCell>{formatTime(record.punchInTime)}</TableCell>
                            <TableCell>{formatTime(record.punchOutTime || '')}</TableCell>
                            <TableCell>
                              {record.hoursWorked ? `${record.hoursWorked.toFixed(2)} hrs` : '-'}
                            </TableCell>
                            <TableCell>{getStatusBadge(record.status)}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  </div>
                )}
              </TabsContent>

              {isHROrAdmin && (
                <TabsContent value="corrections">
                  {corrections.length === 0 ? (
                    <p className="text-center text-gray-500 py-8">
                      No pending correction requests
                    </p>
                  ) : (
                    <div className="overflow-x-auto">
                      <Table>
                        <TableHeader>
                          <TableRow>
                            <TableHead>Employee</TableHead>
                            <TableHead>Date</TableHead>
                            <TableHead>Current In/Out</TableHead>
                            <TableHead>Requested In/Out</TableHead>
                            <TableHead>Reason</TableHead>
                            <TableHead>Status</TableHead>
                            <TableHead>Actions</TableHead>
                          </TableRow>
                        </TableHeader>
                        <TableBody>
                          {corrections.map((correction) => (
                            <TableRow key={correction.id}>
                              <TableCell>{correction.employeeName}</TableCell>
                              <TableCell>
                                {new Date(correction.date).toLocaleDateString()}
                              </TableCell>
                              <TableCell>
                                {formatTime(correction.currentPunchIn || '')} /{' '}
                                {formatTime(correction.currentPunchOut || '')}
                              </TableCell>
                              <TableCell>
                                {formatTime(correction.requestedPunchIn)} /{' '}
                                {formatTime(correction.requestedPunchOut)}
                              </TableCell>
                              <TableCell className="max-w-xs truncate">
                                {correction.reason}
                              </TableCell>
                              <TableCell>{getStatusBadge(correction.status)}</TableCell>
                              <TableCell>
                                {correction.status === 'PENDING' && (
                                  <div className="flex gap-2">
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleApproveCorrection(correction.id)}
                                    >
                                      <Check className="h-4 w-4 text-green-600" />
                                    </Button>
                                    <Button
                                      size="sm"
                                      variant="outline"
                                      onClick={() => handleRejectCorrection(correction.id)}
                                    >
                                      <X className="h-4 w-4 text-red-600" />
                                    </Button>
                                  </div>
                                )}
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </div>
                  )}
                </TabsContent>
              )}
            </Tabs>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
