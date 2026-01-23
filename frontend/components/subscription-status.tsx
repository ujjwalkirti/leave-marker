'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { subscriptionAPI } from '@/lib/api';
import { toast } from 'sonner';
import { CreditCard, AlertCircle, CheckCircle, XCircle, Loader2 } from 'lucide-react';

interface Subscription {
  id: number;
  plan: {
    name: string;
    planType: 'FREE' | 'PAID';
    pricePerEmployee: number;
  };
  status: 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'TRIAL';
  startDate: string;
  endDate: string;
  employeeCount: number;
  monthlyAmount: number;
  autoRenew: boolean;
}

export default function SubscriptionStatus() {
  const router = useRouter();
  const [subscription, setSubscription] = useState<Subscription | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    fetchSubscription();
  }, []);

  const fetchSubscription = async () => {
    try {
      setLoading(true);
      const response = await subscriptionAPI.getActiveSubscription();
      if (response.data.success) {
        setSubscription(response.data.data);
        setError(false);
      }
    } catch (error: any) {
      console.error('Error fetching subscription:', error);
      setError(true);
    } finally {
      setLoading(false);
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircle className="h-5 w-5 text-green-600" />;
      case 'TRIAL':
        return <AlertCircle className="h-5 w-5 text-blue-600" />;
      case 'EXPIRED':
      case 'CANCELLED':
        return <XCircle className="h-5 w-5 text-red-600" />;
      default:
        return null;
    }
  };

  const getStatusBadgeColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'bg-green-100 text-green-800';
      case 'TRIAL':
        return 'bg-blue-100 text-blue-800';
      case 'EXPIRED':
      case 'CANCELLED':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-IN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center py-8">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
        </CardContent>
      </Card>
    );
  }

  if (error || !subscription) {
    return (
      <Card className="border-red-200 bg-red-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-red-600">
            <AlertCircle className="h-5 w-5" />
            No Active Subscription
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-gray-600">
            You don't have an active subscription. Subscribe to a plan to continue using the platform.
          </p>
          <Button
            onClick={() => router.push('/pricing')}
            className="w-full"
          >
            View Pricing Plans
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CreditCard className="h-5 w-5" />
          Subscription Status
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-500">Current Plan</p>
            <p className="text-lg font-semibold">{subscription.plan.name}</p>
          </div>
          <Badge className={getStatusBadgeColor(subscription.status)}>
            <div className="flex items-center gap-1">
              {getStatusIcon(subscription.status)}
              {subscription.status}
            </div>
          </Badge>
        </div>

        {subscription.plan.planType === 'PAID' && (
          <div>
            <p className="text-sm text-gray-500">Monthly Cost</p>
            <p className="text-2xl font-bold text-indigo-600">
              ₹{subscription.monthlyAmount.toLocaleString('en-IN')}
            </p>
            <p className="text-xs text-gray-500">
              {subscription.employeeCount} employees × ₹{subscription.plan.pricePerEmployee}
            </p>
          </div>
        )}

        <div className="grid grid-cols-2 gap-4 pt-4 border-t">
          <div>
            <p className="text-sm text-gray-500">Start Date</p>
            <p className="font-medium">{formatDate(subscription.startDate)}</p>
          </div>
          <div>
            <p className="text-sm text-gray-500">Renewal Date</p>
            <p className="font-medium">{formatDate(subscription.endDate)}</p>
          </div>
        </div>

        <div className="flex items-center gap-2 pt-2">
          <div className={`h-2 w-2 rounded-full ${subscription.autoRenew ? 'bg-green-500' : 'bg-gray-300'}`} />
          <p className="text-sm text-gray-600">
            Auto-renewal {subscription.autoRenew ? 'enabled' : 'disabled'}
          </p>
        </div>

        <div className="flex gap-2 pt-4">
          <Button
            variant="outline"
            size="sm"
            onClick={() => router.push('/pricing')}
            className="flex-1"
          >
            Change Plan
          </Button>
          {subscription.plan.planType === 'PAID' && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => router.push('/dashboard/billing')}
              className="flex-1"
            >
              View Billing
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
