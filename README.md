# SME Manager

**SME Manager** is a robust, production-ready desktop application built in JavaFX designed for Small and Medium Enterprises (SMEs) to manage vendor purchases, specialized cheque printing, and internal auditing. It provides secure, high-performance data processing with modern, glassmorphic UI elements and comprehensive business intelligence dashboards.

## 🚀 Key Features

### 🏢 Vendor & Purchase Management
- **Centralized Purchase History**: Log and track bags/quantities, turnover, and agency commission.
- **Vendor Insights**: Track individual vendor performance with rich line charts (price and supply history).
- **Auto-Reactivation**: Safely handle vendor deletions with smart auto-recovery mechanics to maintain data integrity and prevent unique constraint violations.
- **Smart Data Caching**: Search queries utilize in-memory vendor caches instead of massive iterative database calls, guaranteeing instantaneous load times.

### 🖨️ Intelligent Cheque Printing System
- **Precision Printing**: Calibrated to exact Indian banking standard dimensions (219mm x 95mm) with micro-adjustable X/Y offsets.
- **Batch Processing Queue**: Queue multiple purchases for bulk cheque printing. The print queue operates entirely via SQL bulk transactions ensuring high speed and reliability.
- **Cheque Book Tracking**: Define continuous cheque sequences. The system automatically selects the next available leaf and gracefully avoids skipped or voided leaves.
- **Cancel/Void Operations**: Safely record cancelled cheques directly in the database to prevent accidental issuance.

### 📈 Accountant Reports & Auditing
- **One-Click Tax Summaries**: Export customized, date-ranged transaction data as `.xlsx` (Excel) or `.pdf` files.
- **Consolidated Print Ledger**: Every generated cheque is logged permanently in an un-editable audit ledger attributing the print job to to the responsible user and timestamp.
- **Dashboard Analytics**: Visualize weekly business trends and payment mode distributions via dynamic Bar and Pie charts.

### 🛡️ Security & Role-Based Access
- **Multi-Tenant System**: Differentiate permissions between Administrative operators and standard employees.
- **BCrypt Encryption**: Passwords are mathematically hashed locally using industry-standard `org.mindrot.jbcrypt` libraries; plaintext passwords are never stored.
- **Two-Phase Password Recovery**: Forgot-password mechanics utilize admin verification + secondary security questions to restore access safely.

### 🧹 Database Health & Archival Services
- **Intelligent SQL Pagination**: Utilizes SQLite's native `LIMIT` and `OFFSET` properties coupled with `WHERE` search clauses to instantly load data ranges without stressing system RAM.
- **Automated Rolling Backups**: Automatically purges `.db` backups older than 30 days to protect disk storage.
- **Cold Storage Archive**: Migrate historically irrelevant data to an isolated "archive" table to ensure your daily tables remain light and snappy. You can use the "Archive Explorer" to restore single entries back into "Active" status.

---

## 🛠️ Technology Stack
*   **Language**: Java 21+ 
*   **UI Framework**: JavaFX
*   **Database**: SQLite (Local embedded storage interface)
*   **Password Cryptography**: BCrypt (`jbcrypt:0.4`)
*   **Build Tool**: Gradle 
*   **Reporting**: Apache POI (Excel) and PDFBox (PDF Generation)
*   **System Diagnostics**: SLF4J / Logback (Logging)

---

## 💻 Installation & Usage

### Prerequisites
*   Ensure **Java (JDK) 21** or later is installed on your local machine.
*   Ensure **Gradle** is installed (or use the provided Gradle Wrapper).

### Building The Project
Navigate to the project directory in your terminal:
```bash
# Clean the previous build items
./gradlew clean 

# Compile the application and resolve dependencies
./gradlew build -x test
```

### Running the Application
To run the software locally:
```bash
./gradlew run
```

---

## 🎯 Cheque Printing & Calibration Guide

SME Manager comes with a highly customizable cheque printing engine designed for standard Indian cheques (219mm x 95mm). Because individual hardware printers feed paper differently, we have built a **Micro-Calibration Tool** directly into the application so you can achieve pixel-perfect alignment. 

### Step 1: Accessing the Calibration Menu
1. Log into the application as an **Administrator**.
2. Navigate to **⚙️ Settings** using the sidebar.
3. Scroll to the **Cheque Writing Configuration** card.

### Step 2: Understanding the Offsets (X / Y Axis)
Inside the configuration card, you will find settings for **Global X Offset** and **Global Y Offset**, as well as specific offsets for the Date.
- **X Offset (Horizontal):** Controls left-to-right movement. 
  - *Increase* the number to push the text further to the **Right**.
  - *Decrease* (or use negative numbers) to pull the text further to the **Left**.
- **Y Offset (Vertical):** Controls up-and-down movement.
  - *Increase* the number to push the text further **Down**.
  - *Decrease* (or use negative numbers) to pull the text further **Up**.

### Step 3: The Calibration Process
1. Print a test cheque using the **"Preview & Test Print"** button in Settings on a blank piece of paper (cut to the size of a cheque).
2. Place the printed blank paper over a real cheque and hold it up to a light source to see exactly where the printed text lands relative to the cheque's pre-printed lines.
3. Determine how far off the text is in millimeters (mm).
4. Update the **X / Y Offsets** in the settings. Note: 1 unit roughly correlates to 1 pixel. Adjust by units of `5` or `10` to see significant movement.
5. Click **"Save Settings"**. Ensure you see the *“Configuration Saved Successfully”* message.
6. Print another test on blank paper and repeat until the alignment is perfect. 

> [!TIP]
> **Date Not Aligning?** The "Date" field on Indian cheques is often explicitly boxed. We have provided an independent **Date X Offset** and **Date Y Offset** so you can shift *only* the date characters without ruining the rest of your perfectly calibrated cheque.

---

## ☁️ Deploying Over-The-Air (OTA) Updates

SME Manager features a seamless Auto-Updater built directly into the Dashboard. When you release a new version of the app, users can click **Check for Updates** and the software will securely download, replace the `.jar`, and restart itself—requiring zero technical intervention.

### How to Release a New Update

1. **Package the Application**: 
   Build your new executable by running `./gradlew build -x test`. Ensure your new `sme_manager.jar` is functioning correctly.
2. **Upload the Jar to GitHub Releases**:
   Navigate to your GitHub repository (`murtuzalaxmidhar/sme_manager`) and create a new **Release** (e.g., `v2.2`). Attach your newly compiled `sme_manager.jar` to this release.
3. **Copy the Download Link**:
   Right-click the `sme_manager.jar` asset you just uploaded to the release and click **Copy link address**.
4. **Update `version.json`**:
   At the root of your GitHub repository, edit the `version.json` file. It must look exactly like this:
   ```json
   {
     "latestVersion": "2.2", 
     "downloadUrl": "[PASTE THE DIRECT LINK YOU COPIED HERE]",
     "releaseNotes": "• Added new feature X\n• Fixed printing bug Y"
   }
   ```
   *Note: Ensure the `latestVersion` number string is strictly higher than the version currently hardcoded in `UpdateService.java` (`CURRENT_VERSION`), otherwise the app will not trigger the update.*
5. **Commit Changes**:
   Save your changes to `version.json` on the `main` branch. 

### How Users Receive the Update
The moment you commit the `version.json` file, the update becomes live.
* The user clicks **Check for Updates** on the SME Manager dashboard.
* The system silently downloads the `.jar` using your provided `downloadUrl`.
* Once finished, the screen will flash, the app will close, and it will instantly restart running on `v2.2`. 

---

## 📌 Architecture Highlights
The application is structured into the following primary layers ensuring high maintainability and seperation of concerns (SoC):

*   **`com.lax.sme_manager.ui.*`**: Contains all JavaFX components, Controllers, and ViewModels (`LoginView`, `PurchaseHistoryView`, `DashboardViewModel`). State and data bindings are reactively managed here.
*   **`com.lax.sme_manager.repository.*`**: Pure Data Access Object (DAO) layer interacting directly with SQLite via standardized JDBC `PreparedStatements` to guarantee defense against SQL Injection.
*   **`com.lax.sme_manager.service.*`**: The Business Logic layer. Responsible for composing repository operations (like queuing prints, processing exports, handling business math, triggering rolling backups).
*   **`com.lax.sme_manager.util.*`**: Singletons and statics for Database Migrations, Theme handling, Caching mechanisms (`VendorCache`), and alerting.
