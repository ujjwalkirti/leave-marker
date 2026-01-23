package enums

// Role represents user roles in the system
type Role string

const (
	RoleSuperAdmin Role = "SUPER_ADMIN"
	RoleHRAdmin    Role = "HR_ADMIN"
	RoleManager    Role = "MANAGER"
	RoleEmployee   Role = "EMPLOYEE"
)

// LeaveType represents types of leaves
type LeaveType string

const (
	LeaveTypeCasualLeave    LeaveType = "CASUAL_LEAVE"
	LeaveTypeSickLeave      LeaveType = "SICK_LEAVE"
	LeaveTypeEarnedLeave    LeaveType = "EARNED_LEAVE"
	LeaveTypeLossOfPay      LeaveType = "LOSS_OF_PAY"
	LeaveTypeCompOff        LeaveType = "COMP_OFF"
	LeaveTypeOptionalHoliday LeaveType = "OPTIONAL_HOLIDAY"
)

// LeaveStatus represents leave application status
type LeaveStatus string

const (
	LeaveStatusPending   LeaveStatus = "PENDING"
	LeaveStatusApproved  LeaveStatus = "APPROVED"
	LeaveStatusRejected  LeaveStatus = "REJECTED"
	LeaveStatusCancelled LeaveStatus = "CANCELLED"
)

// EmployeeStatus represents employee status
type EmployeeStatus string

const (
	EmployeeStatusActive   EmployeeStatus = "ACTIVE"
	EmployeeStatusInactive EmployeeStatus = "INACTIVE"
)

// EmploymentType represents employment type
type EmploymentType string

const (
	EmploymentTypeFullTime EmploymentType = "FULL_TIME"
	EmploymentTypeContract EmploymentType = "CONTRACT"
)

// AttendanceStatus represents attendance status
type AttendanceStatus string

const (
	AttendanceStatusPresent         AttendanceStatus = "PRESENT"
	AttendanceStatusAbsent          AttendanceStatus = "ABSENT"
	AttendanceStatusHalfDay         AttendanceStatus = "HALF_DAY"
	AttendanceStatusOnLeave         AttendanceStatus = "ON_LEAVE"
	AttendanceStatusWeeklyOff       AttendanceStatus = "WEEKLY_OFF"
	AttendanceStatusHoliday         AttendanceStatus = "HOLIDAY"
	AttendanceStatusWorkFromHome    AttendanceStatus = "WORK_FROM_HOME"
)

// WorkType represents work type for attendance
type WorkType string

const (
	WorkTypeOffice        WorkType = "OFFICE"
	WorkTypeRemote        WorkType = "REMOTE"
	WorkTypeHybrid        WorkType = "HYBRID"
	WorkTypeFieldWork     WorkType = "FIELD_WORK"
	WorkTypeClientSite    WorkType = "CLIENT_SITE"
)

// HolidayType represents holiday type
type HolidayType string

const (
	HolidayTypeNational HolidayType = "NATIONAL"
	HolidayTypeState    HolidayType = "STATE"
	HolidayTypeCompany  HolidayType = "COMPANY"
)

// PaymentStatus represents payment status
type PaymentStatus string

const (
	PaymentStatusPending  PaymentStatus = "PENDING"
	PaymentStatusSuccess  PaymentStatus = "SUCCESS"
	PaymentStatusFailed   PaymentStatus = "FAILED"
	PaymentStatusRefunded PaymentStatus = "REFUNDED"
)

// PaymentType represents payment type
type PaymentType string

const (
	PaymentTypeSubscription PaymentType = "SUBSCRIPTION"
	PaymentTypeAddOn        PaymentType = "ADD_ON"
)

// BillingCycle represents billing cycle
type BillingCycle string

const (
	BillingCycleMonthly BillingCycle = "MONTHLY"
	BillingCycleYearly  BillingCycle = "YEARLY"
)

// SubscriptionStatus represents subscription status
type SubscriptionStatus string

const (
	SubscriptionStatusActive    SubscriptionStatus = "ACTIVE"
	SubscriptionStatusExpired   SubscriptionStatus = "EXPIRED"
	SubscriptionStatusCancelled SubscriptionStatus = "CANCELLED"
	SubscriptionStatusTrial     SubscriptionStatus = "TRIAL"
)

// PlanTier represents plan tier
type PlanTier string

const (
	PlanTierFree    PlanTier = "FREE"
	PlanTierMidTier PlanTier = "MID_TIER"
)

// PlanType represents plan type
type PlanType string

const (
	PlanTypeFree PlanType = "FREE"
	PlanTypePaid PlanType = "PAID"
)

// IndianState represents Indian states and union territories
type IndianState string

const (
	StateAndhraPradesh         IndianState = "ANDHRA_PRADESH"
	StateArunachalPradesh      IndianState = "ARUNACHAL_PRADESH"
	StateAssam                 IndianState = "ASSAM"
	StateBihar                 IndianState = "BIHAR"
	StateChhattisgarh          IndianState = "CHHATTISGARH"
	StateGoa                   IndianState = "GOA"
	StateGujarat               IndianState = "GUJARAT"
	StateHaryana               IndianState = "HARYANA"
	StateHimachalPradesh       IndianState = "HIMACHAL_PRADESH"
	StateJharkhand             IndianState = "JHARKHAND"
	StateKarnataka             IndianState = "KARNATAKA"
	StateKerala                IndianState = "KERALA"
	StateMadhyaPradesh         IndianState = "MADHYA_PRADESH"
	StateMaharashtra           IndianState = "MAHARASHTRA"
	StateManipur               IndianState = "MANIPUR"
	StateMeghalaya             IndianState = "MEGHALAYA"
	StateMizoram               IndianState = "MIZORAM"
	StateNagaland              IndianState = "NAGALAND"
	StateOdisha                IndianState = "ODISHA"
	StatePunjab                IndianState = "PUNJAB"
	StateRajasthan             IndianState = "RAJASTHAN"
	StateSikkim                IndianState = "SIKKIM"
	StateTamilNadu             IndianState = "TAMIL_NADU"
	StateTelangana             IndianState = "TELANGANA"
	StateTripura               IndianState = "TRIPURA"
	StateUttarPradesh          IndianState = "UTTAR_PRADESH"
	StateUttarakhand           IndianState = "UTTARAKHAND"
	StateWestBengal            IndianState = "WEST_BENGAL"
	StateAndamanNicobar        IndianState = "ANDAMAN_AND_NICOBAR_ISLANDS"
	StateChandigarh            IndianState = "CHANDIGARH"
	StateDadraAndNagarHaveli   IndianState = "DADRA_AND_NAGAR_HAVELI_AND_DAMAN_AND_DIU"
	StateDelhi                 IndianState = "DELHI"
	StateJammuKashmir          IndianState = "JAMMU_AND_KASHMIR"
	StateLadakh                IndianState = "LADAKH"
	StateLakshadweep           IndianState = "LAKSHADWEEP"
	StatePuducherry            IndianState = "PUDUCHERRY"
)
