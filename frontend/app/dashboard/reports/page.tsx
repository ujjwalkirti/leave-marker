'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import DashboardLayout from '@/components/dashboard-layout';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { reportsAPI } from '@/lib/api';
import { useSubscription } from '@/lib/subscription-context';
import { toast } from 'sonner';
import { Loader2, Download, FileSpreadsheet, FileText, Calendar, Clock, BarChart3, Lock, Sparkles } from 'lucide-react';

export default function ReportsPage() {
  const router = useRouter();
  const { features, loading: featuresLoading } = useSubscription();
  const [loading, setLoading] = useState<string | null>(null);
  const [dateRange, setDateRange] = useState({
    startDate: new Date(new Date().setDate(new Date().getDate() - 30)).toISOString().split('T')[0],
    endDate: new Date().toISOString().split('T')[0],
  });

  const hasReportsAccess = features?.advancedReports ?? false;

  const downloadReport = async (reportType: string, format: 'excel' | 'csv') => {
    if (!hasReportsAccess) {
      toast.error('Reports are available on Pro and Enterprise plans. Please upgrade to access.');
      return;
    }

    const loadingKey = `${reportType}-${format}`;
    setLoading(loadingKey);

    try {
      let response;
      const config = { responseType: 'blob' as const };

      switch (reportType) {
        case 'leave-balance':
          response = format === 'excel'
            ? await reportsAPI.downloadLeaveBalanceExcel(config)
            : await reportsAPI.downloadLeaveBalanceCSV(config);
          break;
        case 'attendance':
          response = format === 'excel'
            ? await reportsAPI.downloadAttendanceExcel(dateRange.startDate, dateRange.endDate, config)
            : await reportsAPI.downloadAttendanceCSV(dateRange.startDate, dateRange.endDate, config);
          break;
        case 'leave-usage':
          response = format === 'excel'
            ? await reportsAPI.downloadLeaveUsageExcel(dateRange.startDate, dateRange.endDate, config)
            : await reportsAPI.downloadLeaveUsageCSV(dateRange.startDate, dateRange.endDate, config);
          break;
        default:
          throw new Error('Invalid report type');
      }

      // Create a blob URL and trigger download
      const blob = new Blob([response.data]);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;

      const timestamp = new Date().toISOString().split('T')[0];
      const extension = format === 'excel' ? 'xlsx' : 'csv';
      link.setAttribute('download', `${reportType}-report-${timestamp}.${extension}`);

      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);

      toast.success('Report downloaded successfully!');
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Failed to download report');
    } finally {
      setLoading(null);
    }
  };

  const isLoading = (reportType: string, format: string) => {
    return loading === `${reportType}-${format}`;
  };

  // Show upgrade prompt for free users
  if (!featuresLoading && !hasReportsAccess) {
    return (
      <DashboardLayout>
        <div className="space-y-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Reports</h1>
            <p className="text-gray-500 mt-1">Download various reports in Excel or CSV format</p>
          </div>

          <Card className="border-amber-200 bg-amber-50">
            <CardContent className="pt-6">
              <div className="flex flex-col items-center justify-center text-center py-8">
                <div className="bg-amber-100 p-4 rounded-full mb-4">
                  <Lock className="h-8 w-8 text-amber-600" />
                </div>
                <h2 className="text-2xl font-bold text-gray-900 mb-2">
                  Unlock Advanced Reports
                </h2>
                <p className="text-gray-600 max-w-md mb-6">
                  Advanced reports are available on Pro and Enterprise plans. Upgrade now to download
                  comprehensive leave balance, attendance, and usage reports.
                </p>
                <div className="flex gap-4">
                  <Button
                    onClick={() => router.push('/pricing')}
                    className="bg-indigo-600 hover:bg-indigo-700"
                  >
                    <Sparkles className="mr-2 h-4 w-4" />
                    Upgrade Now
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => router.push('/dashboard')}
                  >
                    Back to Dashboard
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Preview of reports (disabled) */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 opacity-50 pointer-events-none">
            <Card>
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className="bg-indigo-100 p-3 rounded-lg">
                    <BarChart3 className="h-6 w-6 text-indigo-600" />
                  </div>
                  <div>
                    <CardTitle>Leave Balance Report</CardTitle>
                    <CardDescription>
                      Employee leave balances for current year
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex gap-3">
                  <Button disabled className="flex-1">
                    <Lock className="mr-2 h-4 w-4" />
                    Excel
                  </Button>
                  <Button disabled variant="outline" className="flex-1">
                    <Lock className="mr-2 h-4 w-4" />
                    CSV
                  </Button>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className="bg-green-100 p-3 rounded-lg">
                    <Clock className="h-6 w-6 text-green-600" />
                  </div>
                  <div>
                    <CardTitle>Attendance Report</CardTitle>
                    <CardDescription>
                      Employee attendance for selected period
                    </CardDescription>
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <div className="flex gap-3">
                  <Button disabled className="flex-1">
                    <Lock className="mr-2 h-4 w-4" />
                    Excel
                  </Button>
                  <Button disabled variant="outline" className="flex-1">
                    <Lock className="mr-2 h-4 w-4" />
                    CSV
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </DashboardLayout>
    );
  }

  return (
    <DashboardLayout>
      <div className="space-y-6">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Reports</h1>
          <p className="text-gray-500 mt-1">Download various reports in Excel or CSV format</p>
        </div>

        {/* Date Range Filter */}
        <Card>
          <CardHeader>
            <CardTitle>Date Range Filter</CardTitle>
            <CardDescription>
              Select date range for attendance and leave usage reports
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
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
            </div>
          </CardContent>
        </Card>

        {/* Reports Grid */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* Leave Balance Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className="bg-indigo-100 p-3 rounded-lg">
                  <BarChart3 className="h-6 w-6 text-indigo-600" />
                </div>
                <div>
                  <CardTitle>Leave Balance Report</CardTitle>
                  <CardDescription>
                    Employee leave balances for current year
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <p className="text-sm text-gray-600">
                  Download a comprehensive report showing leave balances for all employees,
                  including available, used, and carried forward leaves.
                </p>
                <div className="flex gap-3">
                  <Button
                    onClick={() => downloadReport('leave-balance', 'excel')}
                    disabled={loading !== null}
                    className="flex-1"
                  >
                    {isLoading('leave-balance', 'excel') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileSpreadsheet className="mr-2 h-4 w-4" />
                        Excel
                      </>
                    )}
                  </Button>
                  <Button
                    onClick={() => downloadReport('leave-balance', 'csv')}
                    disabled={loading !== null}
                    variant="outline"
                    className="flex-1"
                  >
                    {isLoading('leave-balance', 'csv') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileText className="mr-2 h-4 w-4" />
                        CSV
                      </>
                    )}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Attendance Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Clock className="h-6 w-6 text-green-600" />
                </div>
                <div>
                  <CardTitle>Attendance Report</CardTitle>
                  <CardDescription>
                    Employee attendance for selected period
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <p className="text-sm text-gray-600">
                  Download attendance records for all employees within the selected date range,
                  including punch in/out times and work hours.
                </p>
                <div className="flex gap-3">
                  <Button
                    onClick={() => downloadReport('attendance', 'excel')}
                    disabled={loading !== null}
                    className="flex-1"
                  >
                    {isLoading('attendance', 'excel') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileSpreadsheet className="mr-2 h-4 w-4" />
                        Excel
                      </>
                    )}
                  </Button>
                  <Button
                    onClick={() => downloadReport('attendance', 'csv')}
                    disabled={loading !== null}
                    variant="outline"
                    className="flex-1"
                  >
                    {isLoading('attendance', 'csv') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileText className="mr-2 h-4 w-4" />
                        CSV
                      </>
                    )}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Leave Usage Report */}
          <Card>
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className="bg-purple-100 p-3 rounded-lg">
                  <Calendar className="h-6 w-6 text-purple-600" />
                </div>
                <div>
                  <CardTitle>Leave Usage Report</CardTitle>
                  <CardDescription>
                    Leave applications for selected period
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                <p className="text-sm text-gray-600">
                  Download detailed leave usage statistics, including approved leaves by type,
                  employee, and department for the selected period.
                </p>
                <div className="flex gap-3">
                  <Button
                    onClick={() => downloadReport('leave-usage', 'excel')}
                    disabled={loading !== null}
                    className="flex-1"
                  >
                    {isLoading('leave-usage', 'excel') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileSpreadsheet className="mr-2 h-4 w-4" />
                        Excel
                      </>
                    )}
                  </Button>
                  <Button
                    onClick={() => downloadReport('leave-usage', 'csv')}
                    disabled={loading !== null}
                    variant="outline"
                    className="flex-1"
                  >
                    {isLoading('leave-usage', 'csv') ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Downloading...
                      </>
                    ) : (
                      <>
                        <FileText className="mr-2 h-4 w-4" />
                        CSV
                      </>
                    )}
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Info Card */}
          <Card className="bg-blue-50 border-blue-200">
            <CardHeader>
              <div className="flex items-center gap-3">
                <div className="bg-blue-100 p-3 rounded-lg">
                  <Download className="h-6 w-6 text-blue-600" />
                </div>
                <div>
                  <CardTitle className="text-blue-900">Report Information</CardTitle>
                  <CardDescription className="text-blue-700">
                    Tips for using reports
                  </CardDescription>
                </div>
              </div>
            </CardHeader>
            <CardContent className="space-y-3 text-sm text-blue-900">
              <div className="flex items-start gap-2">
                <FileSpreadsheet className="h-4 w-4 mt-0.5 flex-shrink-0" />
                <p>
                  <strong>Excel files (.xlsx)</strong> include formatting and are best for
                  viewing and analysis in spreadsheet applications.
                </p>
              </div>
              <div className="flex items-start gap-2">
                <FileText className="h-4 w-4 mt-0.5 flex-shrink-0" />
                <p>
                  <strong>CSV files (.csv)</strong> are plain text and can be imported into
                  any system or database.
                </p>
              </div>
              <div className="flex items-start gap-2">
                <Calendar className="h-4 w-4 mt-0.5 flex-shrink-0" />
                <p>
                  Use the date range filter above to customize attendance and leave usage
                  reports for specific time periods.
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </DashboardLayout>
  );
}
