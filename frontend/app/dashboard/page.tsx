'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { attendanceAPI } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { Calendar, Clock, Users, FileText, Loader2 } from 'lucide-react';

export default function DashboardPage() {
  const router = useRouter();
  const { user } = useAuth();
  const [todayAttendance, setTodayAttendance] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [punchingIn, setPunchingIn] = useState(false);
  const [punchingOut, setPunchingOut] = useState(false);

  useEffect(() => {
    fetchTodayAttendance();
  }, []);

  const fetchTodayAttendance = async () => {
    try {
      setLoading(true);
      const response = await attendanceAPI.getTodayAttendance();
      if (response.data.success) {
        setTodayAttendance(response.data.data);
      }
    } catch (error: any) {
      // No attendance for today is fine
      if (error.response?.status !== 404) {
        console.error('Error fetching attendance:', error);
      }
    } finally {
      setLoading(false);
    }
  };

  const handlePunchIn = async () => {
    try {
      setPunchingIn(true);
      await attendanceAPI.punchIn();
      toast.success('Punched in successfully!');
      await fetchTodayAttendance();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to punch in');
    } finally {
      setPunchingIn(false);
    }
  };

  const handlePunchOut = async () => {
    try {
      setPunchingOut(true);
      await attendanceAPI.punchOut();
      toast.success('Punched out successfully!');
      await fetchTodayAttendance();
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to punch out');
    } finally {
      setPunchingOut(false);
    }
  };

  const formatTime = (dateTime: string) => {
    if (!dateTime) return '-';
    return new Date(dateTime).toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <DashboardLayout>
      <div className="space-y-6">
        {/* Welcome section */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900">
            Welcome back, {user?.fullName}!
          </h1>
          <p className="text-gray-500 mt-1">
            {new Date().toLocaleDateString('en-US', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}
          </p>
        </div>

        {/* Attendance Card */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              Today's Attendance
            </CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
              </div>
            ) : (
              <div className="space-y-4">
                {todayAttendance ? (
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div>
                      <p className="text-sm text-gray-500">Punch In</p>
                      <p className="text-2xl font-semibold text-green-600">
                        {formatTime(todayAttendance.punchInTime)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Punch Out</p>
                      <p className="text-2xl font-semibold text-red-600">
                        {formatTime(todayAttendance.punchOutTime)}
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-500">Status</p>
                      <p className="text-2xl font-semibold text-indigo-600">
                        {todayAttendance.status}
                      </p>
                    </div>
                  </div>
                ) : (
                  <p className="text-gray-500">No attendance recorded for today</p>
                )}

                <div className="flex gap-4 pt-4 border-t">
                  {!todayAttendance ? (
                    <Button
                      onClick={handlePunchIn}
                      disabled={punchingIn}
                      className="bg-green-600 hover:bg-green-700"
                    >
                      {punchingIn ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Punching In...
                        </>
                      ) : (
                        'Punch In'
                      )}
                    </Button>
                  ) : !todayAttendance.punchOutTime ? (
                    <Button
                      onClick={handlePunchOut}
                      disabled={punchingOut}
                      className="bg-red-600 hover:bg-red-700"
                    >
                      {punchingOut ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Punching Out...
                        </>
                      ) : (
                        'Punch Out'
                      )}
                    </Button>
                  ) : (
                    <p className="text-sm text-green-600 font-medium">
                      ✓ Attendance marked for today
                    </p>
                  )}
                </div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">
                Total Leave Balance
              </CardTitle>
              <Calendar className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">-</div>
              <p className="text-xs text-gray-500 mt-1">days available</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">
                Pending Applications
              </CardTitle>
              <FileText className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">-</div>
              <p className="text-xs text-gray-500 mt-1">awaiting approval</p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">
                Attendance Rate
              </CardTitle>
              <Clock className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">-</div>
              <p className="text-xs text-gray-500 mt-1">this month</p>
            </CardContent>
          </Card>

          {(user?.role === 'SUPER_ADMIN' || user?.role === 'HR_ADMIN') && (
            <Card>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-gray-500">
                  Total Employees
                </CardTitle>
                <Users className="h-4 w-4 text-gray-400" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">-</div>
                <p className="text-xs text-gray-500 mt-1">active employees</p>
              </CardContent>
            </Card>
          )}
        </div>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quick Actions</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <Button
                variant="outline"
                className="h-auto flex flex-col items-start p-4"
                onClick={() => router.push('/dashboard/leave-applications')}
              >
                <FileText className="h-6 w-6 mb-2 text-indigo-600" />
                <span className="font-semibold">Apply for Leave</span>
                <span className="text-xs text-gray-500 mt-1">
                  Submit a new leave request
                </span>
              </Button>
              <Button
                variant="outline"
                className="h-auto flex flex-col items-start p-4"
                onClick={() => router.push('/dashboard/attendance')}
              >
                <Clock className="h-6 w-6 mb-2 text-indigo-600" />
                <span className="font-semibold">View Attendance</span>
                <span className="text-xs text-gray-500 mt-1">
                  Check your attendance history
                </span>
              </Button>
              <Button
                variant="outline"
                className="h-auto flex flex-col items-start p-4"
                onClick={() => router.push('/dashboard/holidays')}
              >
                <Calendar className="h-6 w-6 mb-2 text-indigo-600" />
                <span className="font-semibold">View Holidays</span>
                <span className="text-xs text-gray-500 mt-1">
                  See upcoming company holidays
                </span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </DashboardLayout>
  );
}
