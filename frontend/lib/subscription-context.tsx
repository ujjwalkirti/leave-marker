'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { subscriptionAPI } from './api';
import { useAuth } from './auth-context';

export interface SubscriptionFeatures {
  hasActiveSubscription: boolean;
  subscriptionId: number | null;
  isPaid: boolean;
  isValid: boolean;
  tier: 'FREE' | 'PRO' | 'ENTERPRISE';
  planName: string | null;
  maxEmployees: number;
  currentEmployees: number;
  remainingEmployeeSlots: number;
  maxLeavePolicies: number;
  currentLeavePolicies: number;
  remainingLeavePolicySlots: number;
  attendanceTracking: boolean;
  advancedReports: boolean;
  attendanceRateAnalytics: boolean;
  customLeaveTypes: boolean;
  apiAccess: boolean;
  prioritySupport: boolean;
  currentPeriodEnd: string | null;
}

interface SubscriptionContextType {
  features: SubscriptionFeatures | null;
  loading: boolean;
  refreshFeatures: () => Promise<void>;
}

const defaultFeatures: SubscriptionFeatures = {
  hasActiveSubscription: false,
  subscriptionId: null,
  isPaid: false,
  isValid: false,
  tier: 'FREE',
  planName: null,
  maxEmployees: 10,
  currentEmployees: 0,
  remainingEmployeeSlots: 10,
  maxLeavePolicies: 3,
  currentLeavePolicies: 0,
  remainingLeavePolicySlots: 3,
  attendanceTracking: false,
  advancedReports: false,
  attendanceRateAnalytics: false,
  customLeaveTypes: false,
  apiAccess: false,
  prioritySupport: false,
  currentPeriodEnd: null,
};

const SubscriptionContext = createContext<SubscriptionContextType>({
  features: null,
  loading: true,
  refreshFeatures: async () => {},
});

export function SubscriptionProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [features, setFeatures] = useState<SubscriptionFeatures | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchFeatures = async () => {
    if (!user) {
      setFeatures(defaultFeatures);
      setLoading(false);
      return;
    }

    try {
      const response = await subscriptionAPI.getFeatures();
      if (response.data.success) {
        setFeatures(response.data.data);
      } else {
        setFeatures(defaultFeatures);
      }
    } catch (error) {
      console.error('Error fetching subscription features:', error);
      setFeatures(defaultFeatures);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFeatures();
  }, [user]);

  const refreshFeatures = async () => {
    setLoading(true);
    await fetchFeatures();
  };

  return (
    <SubscriptionContext.Provider value={{ features, loading, refreshFeatures }}>
      {children}
    </SubscriptionContext.Provider>
  );
}

export function useSubscription() {
  const context = useContext(SubscriptionContext);
  if (!context) {
    throw new Error('useSubscription must be used within a SubscriptionProvider');
  }
  return context;
}
