import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

/**
 * Extracts error message from API error response.
 * Handles validation errors by formatting field-specific messages.
 */
export function getErrorMessage(error: unknown, fallback = 'Operation failed'): string {
  if (!error || typeof error !== 'object') return fallback;

  const axiosError = error as {
    response?: {
      data?: {
        message?: string;
        data?: Record<string, string>;
      };
    };
  };

  const responseData = axiosError.response?.data;
  if (!responseData) return fallback;

  // Check if this is a validation error with field-specific messages
  if (responseData.message === 'Validation failed' && responseData.data) {
    const fieldErrors = responseData.data;
    const errorMessages = Object.entries(fieldErrors)
      .map(([field, message]) => message)
      .filter(Boolean);

    if (errorMessages.length > 0) {
      return errorMessages.join('. ');
    }
  }

  return responseData.message || fallback;
}
