/* eslint-disable @typescript-eslint/no-explicit-any */
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { Badge } from '@/components/ui/badge';
import { planAPI, subscriptionAPI, paymentAPI } from '@/lib/api';
import { toast } from 'sonner';
import { Check, X, Loader2, ArrowLeft, Sparkles, Building2 } from 'lucide-react';
import { useAuth } from '@/lib/auth-context';

// Razorpay type declarations
declare global {
  interface Window {
    Razorpay: new (options: RazorpayOptions) => RazorpayInstance;
  }
}

interface RazorpayOptions {
  key: string;
  amount: number;
  currency: string;
  name: string;
  description: string;
  order_id: string;
  prefill?: {
    name?: string;
    email?: string;
    contact?: string;
  };
  theme?: {
    color?: string;
  };
  handler: (response: RazorpayResponse) => void;
  modal?: {
    ondismiss?: () => void;
  };
}

interface RazorpayResponse {
  razorpay_order_id: string;
  razorpay_payment_id: string;
  razorpay_signature: string;
}

interface RazorpayInstance {
  open: () => void;
  close: () => void;
}

interface Plan {
  id: number;
  name: string;
  description: string;
  tier: 'FREE' | 'MID_TIER';
  billingCycle: 'MONTHLY' | 'YEARLY';
  monthlyPrice: number;
  yearlyPrice: number;
  minEmployees: number;
  maxEmployees: number;
  maxLeavePolicies: number;
  maxHolidays: number;
  active: boolean;
  attendanceManagement: boolean;
  reportsDownload: boolean;
  multipleLeavePolicies: boolean;
  unlimitedHolidays: boolean;
  attendanceRateAnalytics: boolean;
  reportDownloadPriceUnder50: number;
  reportDownloadPrice50Plus: number;
}

interface SubscriptionFeatures {
  hasActiveSubscription: boolean;
  tier: 'FREE' | 'MID_TIER';
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
      console.log('Plans API response:', response.data);
      if (response.data.success) {
        setPlans(response.data.data);
        console.log('Plans loaded:', response.data.data);
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
      if (plan.tier === 'FREE') {
        // For free plan, create subscription directly (no payment needed)
        const subResponse = await subscriptionAPI.createSubscription({
          planId: plan.id,
          billingCycle: isYearly ? 'YEARLY' : 'MONTHLY',
          autoRenew: true,
        });

        if (subResponse.data.success) {
          toast.success('Successfully subscribed to Free plan!');
          router.push('/dashboard');
        }
      } else {
        // For paid plans, initiate payment first (subscription created after payment success)
        const paymentResponse = await paymentAPI.initiatePayment({
          planId: plan.id,
          billingCycle: isYearly ? 'YEARLY' : 'MONTHLY',
        });

        if (paymentResponse.data.success) {
          const paymentData = paymentResponse.data.data;

          // Open Razorpay checkout
          const options: RazorpayOptions = {
            key: paymentData.razorpayKeyId,
            amount: paymentData.amount,
            currency: paymentData.currency,
            name: 'LeaveMarker',
            description: `${plan.name} - ${paymentData.employeeCount} employee${paymentData.employeeCount > 1 ? 's' : ''} × ₹${(paymentData.pricePerEmployee / 100).toLocaleString('en-IN')}/${isYearly ? 'year' : 'month'}`,
            order_id: paymentData.razorpayOrderId,
            prefill: {
              name: paymentData.companyName,
              email: paymentData.companyEmail,
            },
            theme: {
              color: '#6366f1',
            },
            handler: async (response: RazorpayResponse) => {
              try {
                // Verify payment with backend (subscription is created here)
                const verifyResponse = await paymentAPI.verifyPayment({
                  razorpayOrderId: response.razorpay_order_id,
                  razorpayPaymentId: response.razorpay_payment_id,
                  razorpaySignature: response.razorpay_signature,
                });

                if (verifyResponse.data.success) {
                  toast.success('Payment successful! Subscription activated.');
                  router.push('/payment/success');
                } else {
                  toast.error('Payment verification failed');
                  router.push('/payment/cancel');
                }
              } catch (error: any) {
                console.error('Payment verification error:', error);
                toast.error('Payment verification failed');
                router.push('/payment/cancel');
              }
            },
            modal: {
              ondismiss: () => {
                setSubscribing(false);
                setSelectedPlan(null);
                toast.info('Payment cancelled');
              },
            },
          };

          const razorpay = new window.Razorpay(options);
          razorpay.open();
          return; // Don't set subscribing to false yet, wait for modal dismiss
        }
      }
    } catch (error: any) {
      console.error('Error subscribing:', error);
      toast.error(error.response?.data?.message || 'Failed to subscribe');
    } finally {
      if (plan.tier === 'FREE') {
        setSubscribing(false);
        setSelectedPlan(null);
      }
    }
  };

  const Feature = ({ included, text }: { included: boolean; text: string }) => (
    <div className="flex items-start gap-3">
      {included ? (
        <Check className="h-5 w-5 mt-0.5 text-green-600 shrink-0" />
      ) : (
        <X className="h-5 w-5 mt-0.5 text-muted-foreground shrink-0" />
      )}
      <span className={included ? 'text-muted-foreground' : 'text-muted-foreground'}>{text}</span>
    </div>
  );

  const getTierIcon = (tier: string) => {
    switch (tier) {
      case 'FREE':
        return <Sparkles className="h-6 w-6 text-muted-foreground" />;
      case 'MID_TIER':
        return <Building2 className="h-6 w-6 text-primary" />;
      default:
        return null;
    }
  };

  const getTierStyles = (tier: string) => {
    switch (tier) {
      case 'FREE':
        return 'border-border';
      case 'MID_TIER':
        return 'border-primary border-2 shadow-xl';
      default:
        return 'border-border';
    }
  };

  // Group plans by tier for display (we'll show one card per tier)
  const plansByTier = plans.reduce((acc, plan) => {
    if (!acc[plan.tier]) {
      acc[plan.tier] = plan;
    }
    return acc;
  }, {} as Record<string, Plan>);

  const tierOrder: ('FREE' | 'MID_TIER')[] = ['FREE', 'MID_TIER'];
  const orderedPlans = tierOrder
    .map(tier => plansByTier[tier])
    .filter(Boolean);

  if (loading) {
    return (
      <div className="min-h-screen bg-background flex items-center justify-center">
        <Loader2 className="h-12 w-12 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-background relative">
      {/* Back Button */}
      {user && (
        <Button
          variant="ghost"
          size="icon"
          onClick={() => router.push('/dashboard')}
          className="absolute top-4 left-4 z-10"
        >
          <ArrowLeft className="h-5 w-5" />
        </Button>
      )}

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">

        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold mb-4">
            Simple, Transparent Pricing
          </h1>
          <p className="text-xl text-muted-foreground max-w-2xl mx-auto">
            Choose the plan that&apos;s right for your team. Start free and scale as you grow.
          </p>
        </div>

        {/* Billing Toggle */}
        <div className="flex items-center justify-center gap-4 mb-12">
          <span className={`text-sm font-medium ${!isYearly ? 'text-foreground' : 'text-muted-foreground'}`}>
            Monthly
          </span>
          <Switch
            checked={isYearly}
            onCheckedChange={setIsYearly}
          />
          <span className={`text-sm font-medium ${isYearly ? 'text-foreground' : 'text-muted-foreground'}`}>
            Yearly
            <span className="ml-2 text-primary font-semibold">(Save up to 20%)</span>
          </span>
        </div>

        {/* Pricing Cards */}
        {orderedPlans.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-xl text-muted-foreground">No pricing plans available at the moment.</p>
            <p className="text-sm text-muted-foreground mt-2">Please check back later or contact support.</p>
          </div>
        ) : (
        <div className="grid md:grid-cols-2 gap-8 max-w-5xl mx-auto">
          {orderedPlans.map((plan) => {
            const isCurrentPlan = currentFeatures?.tier === plan.tier;
            const savings = getSavingsPercentage(plan);

            return (
              <Card
                key={plan.id}
                className={`relative flex flex-col h-full ${getTierStyles(plan.tier)} ${
                  isCurrentPlan ? 'ring-2 ring-primary' : ''
                }`}
              >
                {(plan.tier === 'MID_TIER' || isCurrentPlan) && (
                  <div className="absolute -top-3 right-4 flex gap-2">
                    {plan.tier === 'MID_TIER' && (
                      <Badge variant="default" className="px-3 py-1 text-xs shadow-sm">
                        Most Popular
                      </Badge>
                    )}
                    {isCurrentPlan && (
                      <Badge variant="secondary" className="px-3 py-1 text-xs shadow-sm">
                        Current Plan
                      </Badge>
                    )}
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
                        <p className="text-sm text-muted-foreground mt-1">
                          Up to {plan.maxEmployees} employees
                        </p>
                      </div>
                    ) : (
                      <div>
                        <span className="text-4xl font-bold">
                          ₹{getPrice(plan).toLocaleString('en-IN')}
                        </span>
                        <span className="text-muted-foreground">
                          /employee/{isYearly ? 'year' : 'month'}
                        </span>
                        {isYearly && savings > 0 && (
                          <p className="text-sm text-green-600 font-medium mt-1">
                            Save {savings}% with yearly billing
                          </p>
                        )}
                        <p className="text-sm text-muted-foreground mt-1">
                          Unlimited employees
                        </p>
                      </div>
                    )}
                  </div>
                </CardHeader>
                <CardContent className="flex flex-col grow space-y-6">
                  <div className="space-y-3">
                    {/* Employee Limits */}
                    <Feature
                      included={true}
                      text={plan.tier === 'FREE' ? `Up to ${plan.maxEmployees} employees` : 'Unlimited employees'}
                    />

                    {/* Leave Policies */}
                    <Feature
                      included={true}
                      text={plan.multipleLeavePolicies ? 'Multiple leave policies' : '1 leave policy'}
                    />

                    {/* Holidays */}
                    <Feature
                      included={true}
                      text={plan.unlimitedHolidays ? 'Unlimited holidays' : `Up to ${plan.maxHolidays} holidays/year`}
                    />

                    {/* Core Features */}
                    <Feature included={true} text="Leave management system" />
                    <Feature included={true} text="Holiday calendar" />
                    <Feature included={true} text="Employee self-service portal" />

                    {/* Advanced Features */}
                    <Feature
                      included={plan.attendanceManagement}
                      text="Attendance management"
                    />
                    <Feature
                      included={plan.attendanceRateAnalytics}
                      text="Attendance rate analytics"
                    />
                    <Feature
                      included={plan.reportsDownload}
                      text="Reports download"
                    />
                  </div>

                  {/* Add-on pricing for MID_TIER */}
                  {plan.tier === 'MID_TIER' && plan.reportDownloadPriceUnder50 > 0 && (
                    <div className="pt-4 border-t">
                      <p className="text-xs font-semibold text-muted-foreground mb-2">Optional Add-on:</p>
                      <div className="text-sm text-muted-foreground space-y-1">
                        <p>• Report Download: ₹{plan.reportDownloadPriceUnder50}/month (&lt;50 employees)</p>
                        <p>• Report Download: ₹{plan.reportDownloadPrice50Plus}/month (≥50 employees)</p>
                      </div>
                    </div>
                  )}

                  <Button
                    className="w-full mt-auto"
                    variant={plan.tier === 'MID_TIER' ? 'default' : 'outline'}
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
        )}

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
                <p className="text-muted-foreground">
                  The free plan is perfect for small teams of up to 10 employees. You get full access to leave and holiday management with 1 leave policy and up to 6 holidays per year. No credit card required!
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  How is the Mid-Tier plan priced?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  The Mid-Tier plan is priced at ₹100 per employee per month (or ₹1,000 per employee per year). You only pay for the number of active employees in your organization, giving you complete flexibility as your team grows.
                </p>
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardTitle className="text-lg">
                  What&apos;s included with the Report Download add-on?
                </CardTitle>
              </CardHeader>
              <CardContent>
                <p className="text-muted-foreground">
                  The Report Download add-on allows you to export comprehensive reports. Pricing is ₹200/month for companies with less than 50 employees and ₹400/month for companies with 50 or more employees. This is optional and can be added to any Mid-Tier subscription.
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
                <p className="text-muted-foreground">
                  Yes! You can upgrade from Free to Mid-Tier at any time. Your billing will be prorated based on the remaining days in your billing cycle. Changes take effect immediately.
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
                <p className="text-muted-foreground">
                  We accept all major payment methods through Razorpay including credit cards, debit cards, UPI, and net banking.
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
                <p className="text-muted-foreground">
                  No! Both monthly and yearly plans can be cancelled anytime with no penalties. Yearly plans offer better value with significant savings compared to monthly billing.
                </p>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}
