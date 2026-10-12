# Luklan (ลูกหลาน) 📱💊

> A cross-platform mobile application designed to bridge elderly healthcare management and family caregiving through real-time medication tracking, computer-vision prescription scanning, and emergency response.

Built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**, sharing over 85% of business logic and UI across Android and iOS.

---

## 🌟 Key Features

- **Medication Adherence Tracking:** Scheduled dosage reminders, intake logging, and progress synchronization for family caregivers.
- **Prescription & Pill Scanning:** Optical Character Recognition (OCR) pipeline for extracting medication dosage instructions directly from camera input.
- **Real-Time SOS & Emergency Alerts:** Instant push notification broadcasting to designated family members via Firebase Cloud Messaging (FCM).
- **Cross-Platform Shared Architecture:** Unified state management, network, and data persistence layers shared seamlessly between Android and iOS targets.

---

## 🛠️ Architecture & Tech Stack

- **Language:** Kotlin (100% Shared Business Logic & UI)
- **Framework:** Compose Multiplatform (Jetpack Compose for Android & iOS)
- **Cloud & Backend Services:** 
  * Firebase Authentication
  * Cloud Firestore (Real-time NoSQL sync)
  * Firebase Cloud Messaging (FCM)
- **Dependency Management:** Gradle Version Catalogs, CocoaPods (iOS integration)
- **Local Storage & State:** Multiplatform settings / local cache architecture

---

## 📂 Project Structure

```text
├── composeApp/
│   ├── commonMain/       # Shared Compose UI, ViewModels, and domain logic
│   ├── androidMain/      # Android-specific platform implementations & entry point
│   └── iosMain/          # iOS bridge and CocoaPods bindings
└── iosApp/               # Xcode project wrapper for native iOS deployment
```

---

## 🚀 Getting Started

### Prerequisites

- JDK 17+
- Android Studio (Ladybug or newer) with KMP plugin
- Xcode 15+ (for iOS build & simulator)
- CocoaPods installed (`pod --version`)

### Android

```Bash
./gradlew :composeApp:assembleDebug
```

### iOS

- Open `iosApp/iosApp.xcworkspace` in Xcode, select a simulator, and press Run (`Cmd + R`).

---

## 👨‍💻 Engineering Highlights & Learnings

- **Declarative Cross-Platform UI:** Designed a resilient design system in Compose Multiplatform that renders consistently across Android Material and iOS Human Interface guidelines.
- **Native Platform Interop:** Implemented platform-specific expect/actual patterns for hardware camera access and background push notification handlers.
- **Production Optimization:** Configured CI and CocoaPods dependency pipelines to streamline TestFlight deployment.
