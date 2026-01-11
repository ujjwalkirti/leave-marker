'use client';

import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { Card, CardContent } from '@/components/ui/card';
import {
  Calendar,
  Clock,
  Users,
  FileText,
  TrendingUp,
  Shield,
  Zap,
  CheckCircle,
  ArrowRight,
  Loader2,
} from 'lucide-react';

export default function Home() {
  const router = useRouter();
  const { user, isLoading } = useAuth();

  // Show loading state while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100">
        <div className="flex flex-col items-center gap-4">
          <Loader2 className="h-8 w-8 animate-spin text-indigo-600" />
          <p className="text-sm text-muted-foreground">Loading...</p>
        </div>
      </div>
    );
  }

  // Redirect to dashboard if user is already authenticated
  if (user) {
    router.push('/dashboard');
    return null;
  }

  const features = [
    {
      icon: Calendar,
      title: 'Leave Management',
      description: 'Streamline leave requests and approvals with an intuitive workflow system.',
    },
    {
      icon: Clock,
      title: 'Attendance Tracking',
      description: 'Track employee attendance with punch-in/out and automated timesheet generation.',
    },
    {
      icon: Users,
      title: 'Employee Management',
      description: 'Manage your workforce efficiently with comprehensive employee profiles.',
    },
    {
      icon: FileText,
      title: 'Smart Reporting',
      description: 'Generate detailed reports on leave balance, attendance, and usage patterns.',
    },
    {
      icon: TrendingUp,
      title: 'Analytics Dashboard',
      description: 'Gain insights with real-time analytics and performance metrics.',
    },
    {
      icon: Shield,
      title: 'Secure & Compliant',
      description: 'Enterprise-grade security with role-based access control.',
    },
  ];

  const benefits = [
    'Reduce administrative overhead by 60%',
    'Improve employee satisfaction',
    'Real-time visibility into team availability',
    'Automated policy enforcement',
    'Mobile-friendly interface',
    'Seamless integration capabilities',
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      {/* Hero Section */}
      <header className="border-b bg-white/80 backdrop-blur-sm sticky top-0 z-50">
        <div className="container mx-auto px-6 py-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="h-10 w-10 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
              <Calendar className="h-6 w-6 text-white" />
            </div>
            <span className="text-2xl font-bold bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
              LeaveMarker
            </span>
          </div>
          <div className="flex items-center gap-4">
            <Button variant="ghost" onClick={() => router.push('/login')}>
              Sign In
            </Button>
            <Button
              className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700"
              onClick={() => router.push('/signup')}
            >
              Get Started
            </Button>
          </div>
        </div>
      </header>

      {/* Hero Content */}
      <section className="container mx-auto px-6 py-20 md:py-32">
        <div className="grid lg:grid-cols-2 gap-12 items-center">
          <div className="space-y-8">
            <div className="inline-block">
              <span className="px-4 py-2 rounded-full bg-indigo-100 text-indigo-700 text-sm font-semibold">
                Modern HR Solution
              </span>
            </div>
            <h1 className="text-5xl md:text-6xl font-bold text-gray-900 leading-tight">
              Simplify Your
              <span className="block bg-gradient-to-r from-indigo-600 to-purple-600 bg-clip-text text-transparent">
                Leave & Attendance
              </span>
              Management
            </h1>
            <p className="text-xl text-gray-600 leading-relaxed">
              Empower your HR team with a comprehensive platform that streamlines leave
              requests, attendance tracking, and employee management all in one place.
            </p>
            <div className="flex flex-col sm:flex-row gap-4">
              <Button
                size="lg"
                className="bg-gradient-to-r from-indigo-600 to-purple-600 hover:from-indigo-700 hover:to-purple-700 text-lg px-8"
                onClick={() => router.push('/signup')}
              >
                Start Free Trial
                <ArrowRight className="ml-2 h-5 w-5" />
              </Button>
              <Button size="lg" variant="outline" className="text-lg px-8">
                Watch Demo
              </Button>
            </div>
            <div className="flex items-center gap-8 pt-4">
              <div>
                <p className="text-3xl font-bold text-gray-900">10K+</p>
                <p className="text-sm text-gray-600">Active Users</p>
              </div>
              <div className="h-12 w-px bg-gray-300" />
              <div>
                <p className="text-3xl font-bold text-gray-900">98%</p>
                <p className="text-sm text-gray-600">Satisfaction Rate</p>
              </div>
              <div className="h-12 w-px bg-gray-300" />
              <div>
                <p className="text-3xl font-bold text-gray-900">24/7</p>
                <p className="text-sm text-gray-600">Support</p>
              </div>
            </div>
          </div>

          {/* Hero Image/Illustration */}
          <div className="relative">
            <div className="absolute inset-0 bg-gradient-to-r from-indigo-600 to-purple-600 rounded-3xl blur-3xl opacity-20" />
            <Card className="relative overflow-hidden border-2">
              <CardContent className="p-0">
                <div className="bg-gradient-to-br from-indigo-50 to-purple-50 p-12">
                  <div className="space-y-4">
                    <div className="flex items-center gap-4 bg-white p-4 rounded-lg shadow-sm">
                      <div className="h-12 w-12 rounded-full bg-green-100 flex items-center justify-center">
                        <CheckCircle className="h-6 w-6 text-green-600" />
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-gray-900">Leave Approved</p>
                        <p className="text-sm text-gray-600">Your vacation request was approved</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 bg-white p-4 rounded-lg shadow-sm">
                      <div className="h-12 w-12 rounded-full bg-blue-100 flex items-center justify-center">
                        <Clock className="h-6 w-6 text-blue-600" />
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-gray-900">Attendance Marked</p>
                        <p className="text-sm text-gray-600">Punched in at 09:00 AM</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4 bg-white p-4 rounded-lg shadow-sm">
                      <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center">
                        <TrendingUp className="h-6 w-6 text-purple-600" />
                      </div>
                      <div className="flex-1">
                        <p className="font-semibold text-gray-900">Monthly Report Ready</p>
                        <p className="text-sm text-gray-600">95% attendance this month</p>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-4xl font-bold text-gray-900 mb-4">
              Everything You Need to Manage Your Team
            </h2>
            <p className="text-xl text-gray-600 max-w-2xl mx-auto">
              Powerful features designed to simplify HR operations and improve productivity
            </p>
          </div>

          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
            {features.map((feature, index) => (
              <Card
                key={index}
                className="border-2 hover:border-indigo-200 hover:shadow-lg transition-all duration-300"
              >
                <CardContent className="p-6">
                  <div className="h-12 w-12 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center mb-4">
                    <feature.icon className="h-6 w-6 text-white" />
                  </div>
                  <h3 className="text-xl font-semibold text-gray-900 mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-gray-600">{feature.description}</p>
                </CardContent>
              </Card>
            ))}
          </div>
        </div>
      </section>

      {/* Benefits Section */}
      <section className="py-20 bg-gradient-to-br from-indigo-50 to-purple-50">
        <div className="container mx-auto px-6">
          <div className="grid lg:grid-cols-2 gap-12 items-center">
            <div>
              <h2 className="text-4xl font-bold text-gray-900 mb-6">
                Why Choose LeaveMarker?
              </h2>
              <p className="text-xl text-gray-600 mb-8">
                Join thousands of companies that trust LeaveMarker to streamline their HR
                operations and boost productivity.
              </p>
              <div className="space-y-4">
                {benefits.map((benefit, index) => (
                  <div key={index} className="flex items-center gap-3">
                    <div className="h-6 w-6 rounded-full bg-green-100 flex items-center justify-center flex-shrink-0">
                      <CheckCircle className="h-4 w-4 text-green-600" />
                    </div>
                    <p className="text-gray-700 font-medium">{benefit}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-6">
              <Card className="border-2">
                <CardContent className="p-6 text-center">
                  <Zap className="h-12 w-12 text-yellow-500 mx-auto mb-4" />
                  <p className="text-3xl font-bold text-gray-900 mb-2">2x</p>
                  <p className="text-gray-600">Faster Approvals</p>
                </CardContent>
              </Card>
              <Card className="border-2 mt-8">
                <CardContent className="p-6 text-center">
                  <Users className="h-12 w-12 text-blue-500 mx-auto mb-4" />
                  <p className="text-3xl font-bold text-gray-900 mb-2">500+</p>
                  <p className="text-gray-600">Companies</p>
                </CardContent>
              </Card>
              <Card className="border-2">
                <CardContent className="p-6 text-center">
                  <TrendingUp className="h-12 w-12 text-green-500 mx-auto mb-4" />
                  <p className="text-3xl font-bold text-gray-900 mb-2">60%</p>
                  <p className="text-gray-600">Time Saved</p>
                </CardContent>
              </Card>
              <Card className="border-2 mt-8">
                <CardContent className="p-6 text-center">
                  <Shield className="h-12 w-12 text-purple-500 mx-auto mb-4" />
                  <p className="text-3xl font-bold text-gray-900 mb-2">100%</p>
                  <p className="text-gray-600">Secure</p>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-gradient-to-r from-indigo-600 to-purple-600">
        <div className="container mx-auto px-6 text-center">
          <h2 className="text-4xl font-bold text-white mb-6">
            Ready to Transform Your HR Operations?
          </h2>
          <p className="text-xl text-indigo-100 mb-8 max-w-2xl mx-auto">
            Join thousands of companies using LeaveMarker to simplify leave management and
            boost productivity.
          </p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button
              size="lg"
              className="bg-white text-indigo-600 hover:bg-gray-100 text-lg px-8"
              onClick={() => router.push('/signup')}
            >
              Start Free Trial
              <ArrowRight className="ml-2 h-5 w-5" />
            </Button>
            <Button
              size="lg"
              variant="outline"
              className="border-2 border-white text-white hover:bg-white/10 text-lg px-8"
            >
              Contact Sales
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-gray-900 text-gray-300 py-12">
        <div className="container mx-auto px-6">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <div className="flex items-center gap-2 mb-4">
                <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-indigo-600 to-purple-600 flex items-center justify-center">
                  <Calendar className="h-5 w-5 text-white" />
                </div>
                <span className="text-xl font-bold text-white">LeaveMarker</span>
              </div>
              <p className="text-sm text-gray-400">
                Simplifying leave and attendance management for modern teams.
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Product</h4>
              <ul className="space-y-2 text-sm">
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Features
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Pricing
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Security
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Company</h4>
              <ul className="space-y-2 text-sm">
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    About
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Blog
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Careers
                  </a>
                </li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold text-white mb-4">Support</h4>
              <ul className="space-y-2 text-sm">
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Help Center
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Contact
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-white transition-colors">
                    Privacy
                  </a>
                </li>
              </ul>
            </div>
          </div>
          <div className="border-t border-gray-800 pt-8 text-center text-sm text-gray-400">
            <p>&copy; 2026 LeaveMarker. All rights reserved.</p>
          </div>
        </div>
      </footer>
    </div>
  );
}
