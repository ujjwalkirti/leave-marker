/* eslint-disable @typescript-eslint/no-explicit-any */
'use client';

import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import { useSubscription } from '@/lib/subscription-context';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { leaveBalanceAPI, leaveApplicationAPI, employeeAPI } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { Calendar, Users, FileText, Loader2 } from 'lucide-react';

export default function DashboardPage() {
  const router = useRouter();
  const { user } = useAuth();
  const [leaveBalance, setLeaveBalance] = useState<number>(0);
  const [pendingApplications, setPendingApplications] = useState<number>(0);
  const [totalEmployees, setTotalEmployees] = useState<number>(0);
  const [statsLoading, setStatsLoading] = useState(true);

  const fetchDashboardStats = useCallback(async () => {
    try {
      setStatsLoading(true);

      // Fetch leave balance
      try {
        const balanceResponse = await leaveBalanceAPI.getMyLeaveBalance();
        if (balanceResponse.data.success && balanceResponse.data.data) {
          const balances = balanceResponse.data.data;
          const totalAvailable = balances.reduce((sum: number, balance: any) => sum + (balance.available || 0), 0);
          setLeaveBalance(totalAvailable);
        }
      } catch (error: any) {
        console.error('Error fetching leave balance:', error);
      }

      // Fetch pending applications count
      try {
        const pendingResponse = await leaveApplicationAPI.getPendingApplicationsCount();
        if (pendingResponse.data.success) {
          setPendingApplications(pendingResponse.data.data || 0);
        }
      } catch (error: any) {
        console.error('Error fetching pending applications:', error);
      }

      // Fetch total employees (only for admins)
      if (user?.role === 'SUPER_ADMIN' || user?.role === 'HR_ADMIN') {
        try {
          const employeesResponse = await employeeAPI.getActiveEmployeesCount();
          if (employeesResponse.data.success) {
            setTotalEmployees(employeesResponse.data.data || 0);
          }
        } catch (error: any) {
          console.error('Error fetching employees count:', error);
        }
      }
    } catch (error: any) {
      console.error('Error fetching dashboard stats:', error);
    } finally {
      setStatsLoading(false);
    }
  }, [user?.role]);

  useEffect(() => {
    fetchDashboardStats();
  }, [fetchDashboardStats]);

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

        {/* Stats Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between pb-2">
              <CardTitle className="text-sm font-medium text-gray-500">
                Total Leave Balance
              </CardTitle>
              <Calendar className="h-4 w-4 text-gray-400" />
            </CardHeader>
            <CardContent>
              {statsLoading ? (
                <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
              ) : (
                <>
                  <div className="text-2xl font-bold">{leaveBalance.toFixed(1)}</div>
                  <p className="text-xs text-gray-500 mt-1">days available</p>
                </>
              )}
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
              {statsLoading ? (
                <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
              ) : (
                <>
                  <div className="text-2xl font-bold">{pendingApplications}</div>
                  <p className="text-xs text-gray-500 mt-1">awaiting approval</p>
                </>
              )}
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
                {statsLoading ? (
                  <Loader2 className="h-6 w-6 animate-spin text-gray-400" />
                ) : (
                  <>
                    <div className="text-2xl font-bold">{totalEmployees}</div>
                    <p className="text-xs text-gray-500 mt-1">active employees</p>
                  </>
                )}
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
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
