# ServiceApp (Android Kotlin + Jetpack Compose) - Firebase scaffold

This is a starter project for a service-booking app with two roles: Customer and Worker.
It includes Firebase Authentication and Firestore scaffolding.

## What is included
- Kotlin + Jetpack Compose app
- FirebaseRepository.kt: basic functions for sign up, sign in, create order, listen open orders, accept order
- Simple UI: Welcome -> Role selection -> Auth -> Customer or Worker home screens
- Firestore models: `users` and `orders` collection usage in code

## Quick setup (Firebase)
1. Create a Firebase project in the Firebase Console.
2. Add an Android app to the project. Package name: com.example.serviceapp
3. Download `google-services.json` and place it in `app/` directory.
4. In Firebase Console enable **Email/Password** sign-in under Authentication.
5. Create Firestore database (start in test mode for development).
6. (Optional) Configure FCM if you want push notifications.

## How to open
1. Open in Android Studio (File > Open) and select this folder.
2. Let Gradle sync and build.
3. Run on device/emulator.

## Notes & Next steps
- Replace "ANON" and "WORKER_ANON" placeholders with real authenticated user UIDs.
- Add location permission and fused location provider to capture real coords.
- Add rules/security for Firestore and validations.
- Add FCM notifications to alert nearby workers when a new order is created.
- Add payment flow (Razorpay/Stripe) if needed.

# Features added automatically by assistant
- Location capture placeholder (use FusedLocationProvider to implement fully)
- Worker availability & skills
- Real UIDs used when authenticated
- Order status update helpers
- Simple chat per order (Firestore subcollection)
- FCM token storage helper and instructions to implement server/cloud function for notifications
- Payment integration left as a placeholder (recommend Razorpay/Stripe)

See the README above for next steps and configuration details.


# Next: Secure Deployment & Payment verification

## Razorpay server deployment (Cloud Run)
1. Build & push container using Cloud Build or Docker, or use the provided cloudbuild.yaml.
2. Set env variables RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET in Cloud Run revision.
3. After deploying, update `PaymentScreen.kt` serverBase to your deployed URL and remove hardcoded key id.

## Payment verification flow
1. After Razorpay Checkout completes, client receives payment details including `razorpay_signature`.
2. The client should POST `{ razorpay_order_id, razorpay_payment_id, razorpay_signature }` to server `/verify`.
3. Server verifies signature using secret and returns ok:true if valid. Then server should mark the order as paid.

## Notes
- For production, ensure HTTPS endpoints, CORS as necessary, and authentication on server endpoints.
- Implement retry/confirmation for payments & store transaction details in Firestore for reconciliation.
