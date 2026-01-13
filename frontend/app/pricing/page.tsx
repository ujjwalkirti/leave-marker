/* eslint-disable @typescript-eslint/no-explicit-any */
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { planAPI, subscriptionAPI, paymentAPI } from '@/lib/api';
import { toast } from 'sonner';
import { Check, X, Loader2, ArrowLeft, Sparkles, Building2, Crown } from 'lucide-react';
import { useAuth } from '@/lib/auth-context';

interface Plan {
  id: number;
  name: string;
  description: string;
  tier: 'FREE' | 'PRO' | 'ENTERPRISE';
  billingCycle: 'MONTHLY' | 'YEARLY';
  monthlyPrice: number;
  yearlyPrice: number;
  minEmployees: number;
  maxEmployees: number;
  maxLeavePolicies: number;
  active: boolean;
  attendanceTracking: boolean;
  advancedReports: boolean;
  customLeaveTypes: boolean;
  apiAccess: boolean;
  prioritySupport: boolean;
  attendanceRateAnalytics: boolean;
}

interface SubscriptionFeatures {
  hasActiveSubscription: boolean;
  tier: 'FREE' | 'PRO' | 'ENTERPRISE';
  currentEmployees: number;
  remainingEmployeeSlots: number;
}

export default function PricingPage() {
  const router = useRouter();
  const { user } = useAuth();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [loading, setLoading] = useState(true);
  const [isYearly, setIsYearly] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState<Plan | null>(null);
  const [subscribing, setSubscribing] = useState(false);
  const [currentFeatures, setCurrentFeatures] = useState<SubscriptionFeatures | null>(null);

  useEffect(() => {
    fetchPlans();
    if (user) {
      fetchCurrentSubscription();
    }
  }, [user]);

  const fetchPlans = async () => {
    try {
      setLoading(true);
      const response = await planAPI.getActivePlans();
      if (response.data.success) {
        setPlans(response.data.data);
      }
    } catch (error: any) {
      console.error('Error fetching plans:', error);
      toast.error('Failed to load pricing plans');
    } finally {
      setLoading(false);
    }
  };

  const fetchCurrentSubscription = async () => {
    try {
      const response = await subscriptionAPI.getFeatures();
      if (response.data.success) {
        setCurrentFeatures(response.data.data);
      }
    } catch (error: any) {
      console.error('Error fetching subscription:', error);
    }
  };

  const getPrice = (plan: Plan) => {
    if (plan.tier === 'FREE') return 0;
    return isYearly ? plan.yearlyPrice : plan.monthlyPrice;
  };

  const getSavingsPercentage = (plan: Plan) => {
    if (plan.tier === 'FREE' || plan.monthlyPrice === 0) return 0;
    const yearlyEquivalent = plan.monthlyPrice * 12;
    return Math.round(((yearlyEquivalent - plan.yearlyPrice) / yearlyEquivalent) * 100);
  };

  const handleSubscribe = async (plan: Plan) => {
    if (!user) {
      router.push('/login');
      return;
    }

    // Check if trying to subscribe to current tier
    if (currentFeatures && currentFeatures.tier === plan.tier) {
      toast.info('You are already on this plan');
      return;
    }

    setSelectedPlan(plan);
    setSubscribing(true);

    try {
      // Create subscription
      const subResponse = await subscriptionAPI.createSubscription({
        planId: plan.id,
        billingCycle: isYearly ? 'YEARLY' : 'MONTHLY',
        autoRenew: true,
      });

      if (subResponse.data.success) {
        const subscription = subResponse.data.data;

        if (plan.tier === 'FREE') {
          toast.success('Successfully subscribed to Free plan!');
          router.push('/dashboard');
        } else {
          // Initiate payment for paid plan
          const amount = isYearly ? plan.yearlyPrice : plan.monthlyPrice;
          const paymentResponse = await paymentAPI.initiatePayment({
            subscriptionId: subscription.id,
            amount: amount,
          });

          if (paymentResponse.data.success) {
            const paymentData = paymentResponse.data.data;
            // Redirect to Dodo payment page
            window.location.href = paymentData.paymentUrl;
          }
        }
      }
    } catch (error: any) {
      console.error('Error subscribing:', error);
      toast.error(error.response?.data?.message || 'Failed to subscribe');
    } finally {
      setSubscribing(false);
      setSelectedPlan(null);
    }
  };

  const Feature = ({ included, text }: { included: boolean; text: string }) => (
    <div className="flex items-start gap-3">
      {included ? (
        <Check className="h-5 w-5 mt-0.5 text-green-600 shrink-0" />
      ) : (
        <X className="h-5 w-5 mt-0.5 text-gray-300 shrink-0" />
      )}
      <span className={included ? 'text-gray-700' : 'text-gray-400'}>{text}</span>
    </div>
  );

  const getTierIcon = (tier: string) => {
    switch (tier) {
      case 'FREE':
        return <Sparkles className="h-6 w-6 text-gray-500" />;
      case 'PRO':
        return <Building2 className="h-6 w-6 text-indigo-600" />;
      case 'ENTERPRISE':
        return <Crown className="h-6 w-6 text-amber-500" />;
      default:
        return null;
    }
  };

  const getTierStyles = (tier: string) => {
    switch (tier) {
      case 'FREE':
        return 'border-gray-200';
      case 'PRO':
        return 'border-indigo-600 border-2 shadow-xl';
      case 'ENTERPRISE':
        return 'border-amber-500 border-2 shadow-xl';
      default:
        return 'border-gray-200';
    }
  };

  // Group plans by tier for display (we'll show one card per tier)
  const plansByTier = plans.reduce((acc, plan) => {
    if (!acc[plan.tier]) {
      acc[plan.tier] = plan;
    }
    return acc;
  }, {} as Record<string, Plan>);

  const tierOrder: ('FREE' | 'PRO' | 'ENTERPRISE')[] = ['FREE', 'PRO', 'ENTERPRISE'];
  const orderedPlans = tierOrder
    .map(tier => plansByTier[tier])
    .filter(Boolean);

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <Loader2 className="h-12 w-12 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-linear-to-br from-indigo-50 via-white to-purple-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Header */}
        <div className="text-center mb-12">
          {user && (
            <Button
              variant="ghost"
              onClick={() => router.push('/dashboard')}
              className="mb-6"
            >
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Dashboard
            </Button>
          )}
          <h1 className="text-4xl font-bold text-gray-900 mb-4">
            Simple, Transparent Pricing
          </h1>
          <p className="text-xl text-gray-600 max-w-2xl mx-auto">
            Choose the plan that&apos;s right for your team. Start free and scale as you grow.
          </p>
        </div>

        {/* Billing Toggle */}
        <div className="flex items-center justify-center gap-4 mb-12">
          <span className={`text-sm font-medium ${!isYearly ? 'text-gray-900' : 'text-gray-500'}`}>
            Monthly
          </span>
          <Switch
            checked={isYearly}
            onCheckedChange={setIsYearly}
          />
          <span className={`text-sm font-medium ${isYearly ? 'text-gray-900' : 'text-gray-500'}`}>
            Yearly
            <span className="ml-2 text-green-600 font-semibold">(Save up to 20%)</span>
          </span>
        </div>

        {/* Current Plan Info */}
        {currentFeatures && (
          <div className="max-w-md mx-auto mb-8">
            <Card className="bg-indigo-50 border-indigo-200">
              <CardContent className="pt-6">
                <div className="text-center">
                  <p className="text-sm text-indigo-600 font-medium">Your Current Plan</p>
                  <p className="text-2xl font-bold text-indigo-900">{currentFeatures.tier}</p>
                  <p className="text-sm text-gray-600 mt-1">
                    {currentFeatures.currentEmployees} employees
                    {currentFeatures.remainingEmployeeSlots > 0 && (
                      <span> ({currentFeatures.remainingEmployeeSlots} slots remaining)</span>
                    )}
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        )}

        {/* Pricing Cards */}
        <div className="grid md:grid-cols-3 gap-8 max-w-6xl mx-auto">
          {orderedPlans.map((plan) => {
            const isCurrentPlan = currentFeatures?.tier === plan.tier;
            const savings = getSavingsPercentage(plan);

            return (
              <Card
                key={plan.id}
                className={`relative ${getTierStyles(plan.tier)} ${
                  isCurrentPlan ? 'ring-2 ring-indigo-500' : ''
                }`}
              >
                {plan.tier === 'PRO' && (
                  <div className="absolute -top-4 left-1/2 transform -translate-x-1/2">
                    <span className="bg-indigo-600 text-white px-4 py-1 rounded-full text-sm font-medium">
                      Most Popular
                    </span>
                  </div>
                )}
                {isCurrentPlan && (
                  <div className="absolute -top-4 right-4">
                    <span className="bg-green-600 text-white px-3 py-1 rounded-full text-xs font-medium">
                      Current Plan
                    </span>
                  </div>
                )}
                <CardHeader>
                  <div className="flex items-center gap-2 mb-2">
                    {getTierIcon(plan.tier)}
                    <CardTitle className="text-2xl">{plan.name}</CardTitle>
                  </div>
                  <CardDescription>{plan.description}</CardDescription>
                  <div className="mt-4">
                    {plan.tier === 'FREE' ? (
                      <div>
                        <span className="text-4xl font-bold">Free</span>
                        <p className="text-sm text-gray-500 mt-1">
                          Up to {plan.maxEmployees} employees
                        </p>
                      </div>
                    ) : (
                      <div>
                        <span className="text-4xl font-bold">
                          ₹{getPrice(plan).toLocaleString('en-IN')}
                        </span>
                        <span className="text-gray-500">/{isYearly ? 'year' : 'month'}</span>
                        {isYearly && savings > 0 && (
                          <p className="text-sm text-green-600 font-medium mt-1">
                            Save {savings}% with yearly billing
                          </p>
                        )}
                        <p className="text-sm text-gray-500 mt-1">
                          {plan.minEmployees}-{plan.maxEmployees === 999 ? 'unlimited' : plan.maxEmployees} employees
                        </p>
                      </div>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="space-y-3">
                    <Feature
                      included={true}
                      text={`Up to ${plan.maxEmployees === 999 ? 'unlimited' : plan.maxEmployees} employees`}
                    />
                    <Feature
                      included={true}
                      text={`${plan.maxLeavePolicies} leave ${plan.maxLeavePolicies === 1 ? 'policy' : 'policies'}`}
                    />
                    <Feature included={true} text="Leave management" />
                    <Feature included={true} text="Holiday management" />
                    <Feature
                      included={plan.attendanceTracking}
                      text="Attendance tracking"
                    />
                    <Feature
                      included={plan.attendanceRateAnalytics}
                      text="Attendance rate analytics"
                    />
                    <Feature
                      included={plan.advancedReports}
                      text="Advanced reports & analytics"
                    />
                    <Feature
                      included={plan.customLeaveTypes}
                      text="Custom leave types"
                    />
                    <Feature included={plan.apiAccess} text="API access" />
                    <Feature
                      included={plan.prioritySupport}
                      text="Priority support"
                    />
                  </div>

                  <Button
                    className="w-full"
                    variant={plan.tier === 'PRO' ? 'default' : 'outline'}
                    size="lg"
                    onClick={() => handleSubscribe(plan)}
                    disabled={subscribing || isCurrentPlan}
                  >
                    {subscribing && selectedPlan?.id === plan.id ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Processing...
                      </>
                    ) : isCurrentPlan ? (
                      'Current Plan'
                    ) : plan.tier === 'FREE' ? (
                      'Get Started Free'
                    ) : (
                      `Upgrade to ${plan.name}`
                    )}
                  </Button>
                </CardContent>
              </Card>
            );
          })}
        </div>

        {/* FAQ Section */}
        <div className="mt-20 max-w-3xl mx-auto">
          <h2 className="text-3xl font-bold text-center mb-8">
            Frequently Asked Questions
          </h2>
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  How does the free plan work?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">
                  The free plan is perfect for small teams of up to 10 employees. You get full access to leave and holiday management with basic features. No credit card required!
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  Can I upgrade or downgrade my plan?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">
                  Yes! You can upgrade your plan at any time to access more features and employee slots. For downgrades, changes will be reflected at the end of your current billing period.
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  What payment methods do you accept?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">
                  We accept all major payment methods through Dodo Payments including credit cards, debit cards, UPI, and net banking.
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  Is there a long-term contract?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-gray-600">
                  No! Both monthly and yearly plans can be cancelled anytime with no penalties. Yearly plans offer better value with up to 20% savings.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
