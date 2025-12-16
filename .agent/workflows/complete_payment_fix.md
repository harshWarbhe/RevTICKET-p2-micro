---
description: Fix Razorpay payment verification failure (400 Bad Request)
---

The issue was caused by the `RazorpayController` in `payment-service` requiring a mandatory `X-User-Id` header, which the Angular frontend was not sending. The `token.interceptor.ts` only attaches the `Authorization` header.

I have applied a fix to the backend to make it more robust:
1.  Modified `RazorpayController.java` to make `X-User-Id` optional.
2.  If `X-User-Id` is missing, the controller now extracts the user ID (subject) from the `Authorization` (Bearer) token using `JwtUtil`.

### Verification Steps
1.  **Restart Payment Service**: You must restart the `payment-service` for the changes to take effect.
    ```bash
    # If running manually:
    cd Microservices-Backend/payment-service
    mvn spring-boot:run
    ```
2.  **Retry Payment**:
    - Go to the frontend.
    - Book ticket and proceed to payment.
    - Complete Razorpay payment.
    - The verification step should now succeed and redirect to the success page.

### Technical Details of Fix
**File**: `Microservices-Backend/payment-service/src/main/java/com/revticket/payment/controller/RazorpayController.java`

**Changes**:
- Injected `JwtUtil`.
- Updated `verifyPayment` method:
  ```java
  @RequestHeader(value = "X-User-Id", required = false) String userId
  ```
- Added fallback logic:
  ```java
  if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      userId = jwtUtil.extractUsername(token);
  }
  ```
