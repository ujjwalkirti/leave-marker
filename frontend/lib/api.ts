import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

// Feature flag: Set to true once backend cookie support is implemented
const USE_COOKIES = true;

// Flag to prevent interceptor redirect during logout (persisted across page loads)
const getIsLoggingOut = (): boolean => {
  if (typeof window === 'undefined') return false;
  return sessionStorage.getItem('isLoggingOut') === 'true';
};

export const setLoggingOut = (value: boolean) => {
  if (typeof window !== 'undefined') {
    if (value) {
      sessionStorage.setItem('isLoggingOut', 'true');
    } else {
      sessionStorage.removeItem('isLoggingOut');
    }
  }
};

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: USE_COOKIES, // Send cookies with requests when enabled
});

// Request interceptor to add auth token from localStorage (fallback mode)
if (!USE_COOKIES) {
  api.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('auth_token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
}

// Response interceptor to handle errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Don't redirect if we're in the process of logging out
      if (getIsLoggingOut()) {
        return Promise.reject(error);
      }

      // Clear storage and redirect to login on unauthorized
      if (!USE_COOKIES) {
        localStorage.removeItem('auth_token');
        localStorage.removeItem('user');
      }
      // Only redirect to login if we're not on public pages (landing, login, signup, pricing)
      const publicPaths = ['/', '/login', '/signup', '/pricing'];
      const currentPath = typeof window !== 'undefined' ? window.location.pathname : '';
      if (typeof window !== 'undefined' && !publicPaths.includes(currentPath)) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// Auth APIs
export const authAPI = {
  signup: (data: any) => api.post('/auth/signup', data),
  login: (data: any) => api.post('/auth/login', data),
  logout: () => api.post('/auth/logout'),
  verifySession: () => api.get('/auth/verify-session'),
  requestPasswordReset: (email: string) => api.post('/auth/password-reset-request', { email }),
  resetPassword: (data: any) => api.post('/auth/password-reset-confirm', data),
};

// Employee APIs
export const employeeAPI = {
  getAllEmployees: () => api.get('/employees'),
  getActiveEmployees: () => api.get('/employees/active'),
  getActiveEmployeesCount: () => api.get('/employees/active/count'),
  getEmployeeById: (id: number) => api.get(`/employees/${id}`),
  createEmployee: (data: any) => api.post('/employees', data),
  updateEmployee: (id: number, data: any) => api.put(`/employees/${id}`, data),
  deactivateEmployee: (id: number) => api.delete(`/employees/${id}`),
  reactivateEmployee: (id: number) => api.put(`/employees/${id}/reactivate`),
};

// Leave Policy APIs
export const leavePolicyAPI = {
  getAllPolicies: () => api.get('/leave-policies'),
  getActivePolicies: () => api.get('/leave-policies/active'),
  getPolicyById: (id: number) => api.get(`/leave-policies/${id}`),
  createPolicy: (data: any) => api.post('/leave-policies', data),
  updatePolicy: (id: number, data: any) => api.put(`/leave-policies/${id}`, data),
  deletePolicy: (id: number) => api.delete(`/leave-policies/${id}`),
};

// Holiday APIs
export const holidayAPI = {
  getAllHolidays: () => api.get('/holidays'),
  getActiveHolidays: () => api.get('/holidays/active'),
  getHolidaysByDateRange: (startDate: string, endDate: string) =>
    api.get(`/holidays/date-range?startDate=${startDate}&endDate=${endDate}`),
  createHoliday: (data: any) => api.post('/holidays', data),
  updateHoliday: (id: number, data: any) => api.put(`/holidays/${id}`, data),
  deleteHoliday: (id: number) => api.delete(`/holidays/${id}`),
};

// Leave Application APIs
export const leaveApplicationAPI = {
  applyForLeave: (data: any) => api.post('/leave-applications', data),
  getMyApplications: () => api.get('/leave-applications/my-leaves'),
  getPendingApplicationsCount: () => api.get('/leave-applications/my-leaves/pending/count'),
  getApplicationById: (id: number) => api.get(`/leave-applications/${id}`),
  getPendingManagerApprovals: () => api.get('/leave-applications/pending-approvals/manager'),
  getPendingHRApprovals: () => api.get('/leave-applications/pending-approvals/hr'),
  getApplicationsByDateRange: (startDate: string, endDate: string) =>
    api.get(`/leave-applications/date-range?startDate=${startDate}&endDate=${endDate}`),
  // Manager approve/reject use same endpoint with approved: true/false
  managerApprove: (id: number) =>
    api.post(`/leave-applications/${id}/approve/manager`, { approved: true, reason: null }),
  managerReject: (id: number, data: { comments: string }) =>
    api.post(`/leave-applications/${id}/approve/manager`, { approved: false, reason: data.comments }),
  // HR approve/reject use same endpoint with approved: true/false
  hrApprove: (id: number) =>
    api.post(`/leave-applications/${id}/approve/hr`, { approved: true, reason: null }),
  hrReject: (id: number, data: { comments: string }) =>
    api.post(`/leave-applications/${id}/approve/hr`, { approved: false, reason: data.comments }),
  cancelLeave: (id: number) => api.post(`/leave-applications/${id}/cancel`),
};

// Attendance APIs
export const attendanceAPI = {
  // Punch in and out use same /attendance/punch endpoint with isPunchIn flag
  punchIn: () => api.post('/attendance/punch', {
    date: new Date().toISOString().split('T')[0],
    punchTime: new Date().toTimeString().split(' ')[0],
    isPunchIn: true,
    workType: 'OFFICE'
  }),
  punchOut: () => api.post('/attendance/punch', {
    date: new Date().toISOString().split('T')[0],
    punchTime: new Date().toTimeString().split(' ')[0],
    isPunchIn: false,
    workType: null
  }),
  getTodayAttendance: () => api.get('/attendance/today'),
  getMyAttendance: () => api.get('/attendance/my-attendance'),
  getMyAttendanceByDateRange: (startDate: string, endDate: string) =>
    api.get(`/attendance/my-attendance/date-range?startDate=${startDate}&endDate=${endDate}`),
  getMyAttendanceRate: (year?: number, month?: number) => {
    const params = new URLSearchParams();
    if (year) params.append('year', year.toString());
    if (month) params.append('month', month.toString());
    const queryString = params.toString();
    return api.get(`/attendance/my-attendance/rate${queryString ? `?${queryString}` : ''}`);
  },
  getCompanyAttendanceByDateRange: (startDate: string, endDate: string) =>
    api.get(`/attendance/date-range?startDate=${startDate}&endDate=${endDate}`),
  // Request correction requires attendance ID
  requestCorrection: (attendanceId: number, data: any) =>
    api.post(`/attendance/${attendanceId}/request-correction`, data),
  approveCorrection: (correctionId: number) =>
    api.post(`/attendance/corrections/${correctionId}/approve`),
  rejectCorrection: (correctionId: number) =>
    api.post(`/attendance/corrections/${correctionId}/reject`),
  getPendingCorrections: () => api.get('/attendance/corrections/pending'),
  markAttendanceManually: (data: any) => api.post('/attendance/mark', data),
};

// Leave Balance APIs
export const leaveBalanceAPI = {
  getMyLeaveBalance: (year?: number) => {
    const params = year ? `?year=${year}` : '';
    return api.get(`/leave-balance/my-balance${params}`);
  },
  initializeLeaveBalances: (year?: number) => {
    const params = year ? `?year=${year}` : '';
    return api.post(`/leave-balance/initialize${params}`);
  },
};

// Reports APIs
export const reportsAPI = {
  downloadLeaveBalanceExcel: (config?: any) =>
    api.get('/reports/leave-balance/excel', config),
  downloadLeaveBalanceCSV: (config?: any) =>
    api.get('/reports/leave-balance/csv', config),
  downloadAttendanceExcel: (startDate: string, endDate: string, config?: any) =>
    api.get(`/reports/attendance/excel?startDate=${startDate}&endDate=${endDate}`, config),
  downloadAttendanceCSV: (startDate: string, endDate: string, config?: any) =>
    api.get(`/reports/attendance/csv?startDate=${startDate}&endDate=${endDate}`, config),
  downloadLeaveUsageExcel: (startDate: string, endDate: string, config?: any) =>
    api.get(`/reports/leave-usage/excel?startDate=${startDate}&endDate=${endDate}`, config),
  downloadLeaveUsageCSV: (startDate: string, endDate: string, config?: any) =>
    api.get(`/reports/leave-usage/csv?startDate=${startDate}&endDate=${endDate}`, config),
};

// Plan APIs
export const planAPI = {
  getAllPlans: () => api.get('/plans'),
  getActivePlans: () => api.get('/plans/active'),
  getPlanById: (id: number) => api.get(`/plans/${id}`),
  createPlan: (data: any) => api.post('/plans', data),
  updatePlan: (id: number, data: any) => api.put(`/plans/${id}`, data),
  deletePlan: (id: number) => api.delete(`/plans/${id}`),
};

// Subscription APIs
export const subscriptionAPI = {
  getActiveSubscription: () => api.get('/subscriptions/active'),
  getCompanySubscriptions: () => api.get('/subscriptions'),
  getFeatures: () => api.get('/subscriptions/features'),
  createSubscription: (data: any) => api.post('/subscriptions', data),
  updateSubscription: (id: number, data: any) => api.put(`/subscriptions/${id}`, data),
  cancelSubscription: (id: number) => api.post(`/subscriptions/${id}/cancel`),
};

// Payment APIs
export const paymentAPI = {
  getCompanyPayments: () => api.get('/payments'),
  getPaymentById: (id: number) => api.get(`/payments/${id}`),
  initiatePayment: (data: { planId: number; billingCycle: 'MONTHLY' | 'YEARLY' }) =>
    api.post('/payments/initiate', data),
  verifyPayment: (data: {
    razorpayOrderId: string;
    razorpayPaymentId: string;
    razorpaySignature: string;
  }) => api.post('/payments/verify', data),
  retryPayment: (id: number) => api.post(`/payments/${id}/retry`),
};

// Contact API (public)
export const contactAPI = {
  sendMessage: (data: { name: string; email: string; phone?: string; message: string }) =>
    api.post('/contact', data),
};
