# Payment Integration Setup Guide

This document explains the payment functionality that has been implemented using Dodo Payments.

## Overview

The system now supports:
- **Free Plan**: Up to 9 employees
- **Paid Plans**: Starting at ₹100 per employee/month with unlimited users

## Backend Implementation

### Database Entities

1. **Plan Entity** (`backend/src/main/java/com/leavemarker/entity/Plan.java`)
   - Stores plan details (name, price, features)
   - Supports FREE and PAID plan types
   - Feature flags for advanced reports, custom leave types, API access, priority support

2. **Subscription Entity** (`backend/src/main/java/com/leavemarker/entity/Subscription.java`)
   - Links companies to plans
   - Tracks subscription status (ACTIVE, EXPIRED, CANCELLED, TRIAL)
   - Manages auto-renewal settings

3. **Payment Entity** (`backend/src/main/java/com/leavemarker/entity/Payment.java`)
   - Records payment transactions
   - Integrates with Dodo Payments
   - Tracks payment status

### API Endpoints

#### Plans
- `GET /api/plans` - Get all plans (SUPER_ADMIN only)
- `GET /api/plans/active` - Get active plans (public)
- `GET /api/plans/{id}` - Get plan by ID
- `POST /api/plans` - Create plan (SUPER_ADMIN only)
- `PUT /api/plans/{id}` - Update plan (SUPER_ADMIN only)
- `DELETE /api/plans/{id}` - Delete plan (SUPER_ADMIN only)

#### Subscriptions
- `GET /api/subscriptions/active` - Get active subscription
- `GET /api/subscriptions` - Get company subscriptions
- `POST /api/subscriptions` - Create subscription
- `PUT /api/subscriptions/{id}` - Update subscription
- `POST /api/subscriptions/{id}/cancel` - Cancel subscription

#### Payments
- `GET /api/payments` - Get company payments
- `GET /api/payments/{id}` - Get payment by ID
- `POST /api/payments/initiate` - Initiate payment
- `POST /api/payments/webhook` - Webhook for payment status (public)

### Configuration

Add these environment variables to your `.env` or `application.yml`:

```yaml
dodo:
  payment:
    api-key: ${DODO_PAYMENT_API_KEY}
    api-secret: ${DODO_PAYMENT_API_SECRET}
    webhook-secret: ${DODO_PAYMENT_WEBHOOK_SECRET}
    base-url: https://api.dodopayments.com
    return-url: http://localhost:3000/payment/success
    cancel-url: http://localhost:3000/payment/cancel
```

### Subscription Middleware

A `SubscriptionInterceptor` has been added that checks if a company has an active subscription before allowing access to protected endpoints. The following endpoints are excluded:
- `/api/auth/**`
- `/api/plans/**`
- `/api/subscriptions/**`
- `/api/payments/**`

## Frontend Implementation

### Pages Created

1. **Pricing Page** (`frontend/app/pricing/page.tsx`)
   - Shows all available plans
   - Dynamic pricing calculator based on employee count
   - Handles subscription creation and payment initiation

2. **Payment Success Page** (`frontend/app/payment/success/page.tsx`)
   - Confirmation page after successful payment
   - Auto-redirects to dashboard

3. **Payment Cancel Page** (`frontend/app/payment/cancel/page.tsx`)
   - Shown when user cancels payment
   - Options to retry or return to dashboard

### Components

1. **Subscription Status Component** (`frontend/components/subscription-status.tsx`)
   - Displays current subscription details
   - Shows plan name, status, renewal date
   - Quick actions to change plan or view billing

### API Integration

Updated `frontend/lib/api.ts` with new endpoints:
- `planAPI` - Plan management
- `subscriptionAPI` - Subscription management
- `paymentAPI` - Payment operations

## Setup Instructions

### Backend Setup

1. **Add Dodo Payments Credentials**

   Create environment variables:
   ```bash
   DODO_PAYMENT_API_KEY=your_api_key
   DODO_PAYMENT_API_SECRET=your_api_secret
   DODO_PAYMENT_WEBHOOK_SECRET=your_webhook_secret
   ```

2. **Create Initial Plans**

   Use the following API call to create plans (requires SUPER_ADMIN role):

   **Free Plan:**
   ```json
   POST /api/plans
   {
     "name": "Free Plan",
     "description": "Perfect for small teams",
     "planType": "FREE",
     "pricePerEmployee": 0,
     "maxEmployees": 9,
     "unlimitedEmployees": false,
     "active": true,
     "advancedReports": false,
     "customLeaveTypes": false,
     "apiAccess": false,
     "prioritySupport": false
   }
   ```

   **Paid Plan:**
   ```json
   POST /api/plans
   {
     "name": "Professional Plan",
     "description": "Unlimited employees with advanced features",
     "planType": "PAID",
     "pricePerEmployee": 100,
     "maxEmployees": 0,
     "unlimitedEmployees": true,
     "active": true,
     "advancedReports": true,
     "customLeaveTypes": true,
     "apiAccess": true,
     "prioritySupport": true
   }
   ```

3. **Configure Webhook**

   Set up Dodo Payments webhook to point to:
   ```
   POST https://your-domain.com/api/payments/webhook
   ```

### Frontend Setup

1. **Update Environment Variables**

   In `frontend/.env.local`:
   ```
   NEXT_PUBLIC_API_URL=http://localhost:8080/api
   ```

2. **Access Pricing Page**

   Navigate to: `http://localhost:3000/pricing`

## User Flow

### For New Companies

1. User signs up and creates a company
2. User is redirected to pricing page
3. User selects a plan and enters employee count
4. For FREE plan:
   - Subscription is created immediately
   - User gets access to the dashboard
5. For PAID plan:
   - Subscription is created
   - Payment is initiated with Dodo Payments
   - User is redirected to Dodo payment page
   - After payment, user is redirected to success page
   - Webhook updates payment and subscription status

### Subscription Status Check

- Middleware checks for active subscription on protected routes
- Returns HTTP 402 (Payment Required) if no active subscription
- Frontend can redirect to pricing page

## Payment Webhook Flow

1. Dodo Payments sends webhook to `/api/payments/webhook`
2. System finds payment by `dodoPaymentId`
3. Updates payment status based on webhook data
4. For successful payment:
   - Extends subscription end date by 1 month
   - Marks subscription as ACTIVE
5. For failed payment:
   - Marks payment as FAILED
   - Logs failure reason

## Testing

### Testing Free Plan
1. Go to pricing page
2. Set employee count to 9 or less
3. Click "Subscribe to Free Plan"
4. Verify subscription is created and active

### Testing Paid Plan
1. Go to pricing page
2. Set employee count to any number
3. Click "Subscribe to Professional Plan"
4. Use Dodo Payments test credentials
5. Complete test payment
6. Verify redirect to success page
7. Check subscription status in dashboard

## Monitoring

- Check logs for payment webhook events
- Monitor subscription expiration
- Track payment failures
- Review subscription status changes

## Future Enhancements

- Automated subscription renewal
- Prorated billing for plan changes
- Invoice generation
- Payment history page
- Usage analytics per subscription tier
- Grace period for expired subscriptions
- Email notifications for payment events

## Support

For issues related to:
- **Dodo Payments Integration**: Check Dodo Payments documentation
- **Subscription Management**: Review subscription service logs
- **Payment Webhooks**: Verify webhook signature and payload format
