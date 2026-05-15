# NammaSanteLedger — Digital Khata App

> **Course:** Android App Development Using Gen AI &nbsp;|&nbsp; **Project No:** 56 &nbsp;|&nbsp; **Intern:** Puneetha G

---

## Problem Statement

Small vendors at weekly rural markets (Santhe) in Karnataka manage customer credit using paper notebooks that are easily lost, damaged, or incorrect. They forget who owes them money, cannot quickly total outstanding dues, and have no way to send payment reminders — leading to regular financial losses.

**NammaSanteLedger** replaces the paper khata with a fast, offline Android app. Adding a credit transaction takes under 5 seconds. The home screen shows total outstanding dues at a glance.

---

## Features

| Feature | Description |
|---|---|
| **Add Udari (Credit)** | Record goods given on credit in 2 steps — select customer, enter amount |
| **Record Payment** | Reduce a customer's outstanding balance; per-customer isolated |
| **Cash Sale** | Record immediate cash purchases; tracked in daily sales but no credit impact |
| **Total Outstanding** | Real-time sum of all positive per-customer balances on home screen |
| **Today's Summary** | Total Sales, Today's Dues, Udari, Cash Sales, Payments for the current day |
| **Customer Ledger** | Full transaction history per customer with running balance |
| **Customer Search** | Real-time search by name or phone on the Customers screen |
| **Overdue Detection** | Identifies customers with unpaid dues older than 7 days |
| **WhatsApp Reminder** | One-tap reminder with pre-filled pending amount via WhatsApp Intent |
| **SMS Reminder** | One-tap SMS reminder with pre-filled message |
| **Sales Trend Graph** | Interactive day-by-day Udari vs Payment line graph (Canvas API) |
| **Monthly Summary** | Udari and Payments grouped by calendar month |
| **Sign Out** | Clears all data and returns to login screen |
| **Offline First** | Works with zero internet — all data in Room Database on device |

---

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI Framework | Jetpack Compose (Material 3) |
| Architecture | MVVM with Repository Pattern |
| Database | Room Database (SQLite) |
| Async / Reactive | Kotlin Coroutines + Flow + StateFlow |
| Navigation | Jetpack Navigation Component |
| Annotation Processing | KSP (Kotlin Symbol Processing) |
| Build System | Gradle with Kotlin DSL |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 35 |
| AI Tool | Google AI Studio (Gemini) |

---

## Project Structure

```
NammaSanteLedger/
├── app/
│   └── src/main/
│       ├── java/com/example/nammasanthe/
│       │   ├── data/
│       │   │   ├── dao/          # CustomerDao, TransactionDao
│       │   │   ├── database/     # AppDatabase (Room singleton)
│       │   │   ├── model/        # Customer, Transaction entities
│       │   │   └── repository/   # LedgerRepository (single source of truth)
│       │   ├── navigation/       # NavGraph, Screen sealed class
│       │   ├── ui/
│       │   │   ├── screens/      # 10 Compose screens
│       │   │   ├── theme/        # Color, Typography, Theme
│       │   │   └── viewmodel/    # 7 ViewModels + ViewModelFactory
│       │   └── utils/            # Date formatting utilities
│       ├── res/
│       │   ├── drawable/         # App icons and custom PNGs
│       │   ├── mipmap-*/         # Launcher icons (all densities)
│       │   └── values/           # Strings, themes
│       └── AndroidManifest.xml
├── gradle/
│   ├── libs.versions.toml        # Version catalog
│   └── wrapper/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## Screens

| Screen | Route | Purpose |
|---|---|---|
| Splash | `splash` | App launch; auto-navigates to Login or Dashboard |
| Login | `login` | Vendor name setup via SharedPreferences |
| Dashboard | `dashboard` | Home: totals, today's summary, recent transactions |
| Customers | `customers` | List all customers; search; add new |
| Customer Profile | `customer_profile/{customerId}` | Per-customer ledger and transaction entry |
| Add Transaction | `add_transaction/{customerId}/{type}` | Large keypad; CREDIT / PAYMENT / CASH_SALE |
| Overdue | `overdue` | Customers with dues older than 7 days |
| History | `history` | Day-wise grouped transaction history |
| Full Dashboard | `full_dashboard` | Line graph + monthly summary |
| Menu | `menu` | Navigation hub; sign out |

---

## Database Schema

**Customer Table:** `id` (PK) · `name` · `phone` · `address` · `createdAt`

**Transaction Table:** `id` (PK) · `customerId` (FK, CASCADE) · `amount` · `type` (CREDIT / PAYMENT / CASH_SALE) · `date` · `description`

---

## Installation and Setup

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or later
- Android SDK API 24+

### Clone and Run

```bash
# 1. Clone the repository
git clone https://github.com/suvi-p/NammaSanteLedger.git

# 2. Open in Android Studio
#    File → Open → select the NammaSanteLedger folder

# 3. Sync Gradle
#    Android Studio will auto-sync; or go to File → Sync Project with Gradle Files

# 4. Run the app
#    Connect an Android device or start an emulator
#    Click Run ▶ or press Shift+F10
```

### Build from Command Line

```bash
cd NammaSanteLedger

# Debug build
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

---

## Download APK

📲 **[Download NammaSanteLedger APK](https://drive.google.com/file/d/1zahwmOg9ukfxM281STBtNp8e121lrGl6/view?usp=drive_link)**

Install directly on any Android 7.0+ device (enable "Install from unknown sources" in device settings).

---

## How to Use

1. **First Launch** — Enter your vendor name on the Login screen phone number and address is optional and click continue
2. **Add a Customer** — Tap Customers → + → enter name (and phone for reminders)
3. **Record Udari** — Tap a customer → Add Udari → enter amount → confirm (2 steps, under 5 seconds)
4. **Record Payment** — Tap a customer → Add Payment → enter amount → confirm
5. **Record Cash Sale** — Tap a customer → Add Cash Sale → enter amount (no credit impact)
6. **Send Reminder** — Open a customer profile → tap WhatsApp or SMS icon
7. **View Overdue** — Home screen → Overdue button → see all customers with dues older than 7 days
8. **View History** — Menu → History → day-wise grouped transactions with daily totals
9. **View Analytics** — Menu → Dashboard → interactive Udari vs Payment trend graph
10. **Sign Out** — Menu → Sign Out → clears all data and returns to login

---

## Key Business Logic

- **Today's Dues** = sum of `max(0, customer_today_credit - customer_today_payment)` for all customers who took credit today only. A customer paying old dues does not affect another customer's today due.
- **Total Outstanding** = sum of `max(0, all_time_credit - all_time_payment)` per customer across all customers — shown on home screen.
- **Balance never goes negative** — overpayment is shown as advance paid.

---

## Dependencies

```toml
# gradle/libs.versions.toml
agp            = "8.5.2"
kotlin         = "2.0.21"
ksp            = "2.0.21-1.0.27"
room           = "2.6.1"
navigationCompose = "2.8.3"
composeBom     = "2024.10.00"
lifecycleViewmodelCompose = "2.8.6"
```

---

## Future Improvements

- Cloud sync across multiple devices
- UPI / online payment integration
- Inventory and product management
- Multi-language support (Kannada, Hindi)
- PDF receipt generation
- Customer-level analytics and repayment trends

---

## Project Info

- **App Name:** Namma Santhe Ledger
- **Package:** `com.example.nammasanthe`
- **Version:** 1.0.0
- **Min Android:** 7.0 (API 24)
- **Target Android:** API 35
- **Architecture:** MVVM + Repository + Room + Jetpack Compose
- **Total Kotlin Files:** 28
- **Total Lines of Code:** ~4,000

---

*"Namma Santhe" means "Our Market" in Kannada. This app moves the weekly rural market economy from paper to pixels.*
