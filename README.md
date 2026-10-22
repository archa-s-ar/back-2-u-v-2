# 🔎 Back2U — Smart Campus Lost & Found System

<p align="center">
  <strong>There’s Always a Way Back.</strong>
  <br />
  A Java Swing and MySQL application that helps campus communities reconnect people with their lost belongings.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-Swing-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Swing" />
  <img src="https://img.shields.io/badge/Database-MySQL-blue?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL" />
  <img src="https://img.shields.io/badge/Connectivity-JDBC-green?style=for-the-badge" alt="JDBC" />
  <img src="https://img.shields.io/badge/IDE-NetBeans-lightgrey?style=for-the-badge&logo=apache-netbeans-ide&logoColor=white" alt="NetBeans" />
</p>

---

## 📌 About the Project

**Back2U** is a campus-focused lost-and-found management system designed to make reporting, discovering, matching, and recovering lost belongings easier.

Instead of depending entirely on informal announcements and scattered messages, users can report lost or found items, review possible matches, receive notifications, and arrange handovers.

Back2U v2 also introduces an **advertisement trust and verification workflow**. Users can report suspicious found-item advertisements and rate approved advertisements. Trust scores help prioritize credible listings, while administrators can review advertisements that require attention.

## ✨ Features

### 👤 User Management
- User registration and login.
- User-specific dashboards.
- Separate access paths for regular users and administrative functions.

### 📦 Lost & Found Reporting
- Submit lost-item reports.
- Submit found-item advertisements.
- Record item names, categories, descriptions, dates, times, and locations.
- Store lost and found reports in a shared `items` table, distinguished by report type.
- Record additional unique details for lost-item ownership verification.

### 🔍 Possible Matches
- Compare lost and found reports to identify potential matches.
- Display match information to help users evaluate possible matches.
- Track match responses and status.
- Generate notifications for relevant matches.

### 🔔 Match Notifications
- View possible matches associated with a user's reports.
- Review match details.
- Respond to matching requests where permitted.
- Refresh notification information.

### ⭐ Advertisement Trust & Verification
- Calculate an initial trust score for a submitted advertisement.
- Assign an advertisement review status based on the configured review threshold.
- Allow users to browse approved found-item advertisements.
- Display advertisement trust scores and available rating information.
- Allow eligible users to rate approved advertisements.
- Prevent users from rating their own advertisements.
- Allow users to report suspicious advertisements.
- Update trust scores using the initial score and user-rating information.
- Move advertisements below the configured trust threshold into pending review when applicable.

**Current review rule:** The configured trust-score threshold is **50**. Advertisements with an initial score below 50 are submitted for review; advertisements scoring 50 or above can be approved automatically, subject to the application's implemented workflow.

The trust score is an indicator for review and discovery, not a guarantee that an advertisement is genuine.

### 🛡️ Administrator Review
- View advertisements awaiting review.
- Inspect advertisement information and suspicious-ad reports.
- Approve or reject eligible advertisements.
- Review reports submitted by users.
- Apply moderation decisions without unnecessarily changing existing match and handover records.

### 🤝 Item Recovery & Handover
- Maintain match information between lost and found reports.
- Arrange a handover location, date, time, and instructions.
- Use a handover PIN as part of the recovery workflow.
- Track handover status and completed recoveries.

---

## 🧰 Technology Stack

| Technology | Purpose |
|---|---|
| Java | Application logic and object-oriented design |
| Java Swing | Desktop user interface |
| JDBC | Database connectivity |
| MySQL | Persistent storage |
| Apache NetBeans | Development environment |
| Git and GitHub | Version control and collaboration |

---

## 🏗️ Application Structure

The project separates user-interface screens, database access, matching logic, notification handling, and advertisement verification into different classes.

```text
Back2U-V2/
├── src/
│   └── back2u/
│       ├── Login.java
│       ├── UserDashboard.java
│       ├── ReportLostItem.java
│       ├── ReportFounditem.java
│       ├── BrowseFoundAdvertisements.java
│       ├── RateAdvertisement.java
│       ├── Notifications.java
│       ├── PossibleMatches.java
│       ├── DetailsMatch.java
│       ├── ItemMatcher.java
│       ├── ItemMatchingService.java
│       ├── MatchNotificationService.java
│       ├── AdvertisementRatingService.java
│       ├── HandoverArrangement.java
│       ├── handover.java
│       ├── AdminDashboard.java
│       ├── AdminReport.java
│       └── Conn.java
├── database/
│   └── database schema.sql
├── back2u.example.properties
└── README.md
```

*The tree highlights key application classes and may not list every file in the repository. Keep only filenames that exist in your current branch.*

---

## 🗃️ Database Overview

Back2U uses MySQL to store user accounts, lost-and-found advertisements, match records, handover arrangements, and advertisement moderation data.

| Table | Purpose |
|---|---|
| `accountdetails` | Stores user account and profile information |
| `admin_reports` | Stores administrative reports |
| `advertisement_ratings` | Stores user ratings for advertisements |
| `advertisement_reports` | Stores reports about suspicious advertisements |
| `handover` | Stores item handover arrangements and recovery status |
| `item_matches` | Stores potential matches between lost and found items |
| `items` | Stores both lost-item reports and found-item advertisements |

The `items` table also supports advertisement trust and moderation through fields such as `initial_trust_score`, `trust_score`, and `ad_review_status`, if these columns are present in the current schema.

### Trust Score Calculation

The rating service combines the initial trust score with the average user rating:

**Updated trust score**

\[
0.70 \times \text{Initial Trust Score}
+
0.30 \times \left(\frac{\text{Average Rating}}{5}\times100\right)
\]

The initial score contributes 70%, while the average user rating contributes 30%. When no ratings exist, the service preserves the initial score.

A score below the configured threshold can trigger renewed review for an approved advertisement. Administrator decisions and other review states are handled by the application's moderation workflow.

---

## ⚙️ Setup and Installation

### Prerequisites

Install the following before running the application:

- Java Development Kit (JDK).
- Apache NetBeans or another Java IDE.
- MySQL Server.
- MySQL Connector/J JDBC driver.
- Git, if cloning the repository.

### 1. Clone the Repository

```bash
git clone https://github.com/archa-s-ar/back-2-u-v-2.git
cd back-2-u
```

### 2. Open the Project

1. Launch Apache NetBeans.
2. Open the cloned project.
3. Allow NetBeans to resolve or configure the project's dependencies.
4. Confirm that the JDK and MySQL JDBC driver are configured.

### 3. Configure MySQL

1. Start the MySQL server.
2. Create the database required by the project.
3. Import `database/database schema_v2.sql`.
4. Verify that the required tables have been created.

For example, if your database is named `back2u`:

```sql
CREATE DATABASE back2u;
```

Then select that database and import the SQL schema from the repository. Use the actual database name expected by your project configuration.

### 4. Configure Database Connectivity

Update the database connection settings used by `Conn.java` or the project's configuration mechanism.

Typical JDBC URL format:

```text
jdbc:mysql://localhost:3306/back2u
```

Configure the appropriate database username and password for your local MySQL installation.

Use `back2u.example.properties`, copy it to the configuration filename expected by the application and fill in your local settings. Do not commit real database credentials to GitHub.

### 5. Run the Application

1. Build the project in NetBeans.
2. Resolve any compilation errors.
3. Run the project's configured main class.
4. Test registration, login, reporting, matching, notifications, advertisement review, ratings, and handover workflows.

---

## 🔄 Advertisement Moderation Workflow

```text
User submits a found-item advertisement
                 |
                 v
       Calculate initial score
                 |
                 v
       Check review threshold
          /           \
       Below 50      50 or above
          |              |
          v              v
    Pending review    Approved
          |              |
          v              v
   Admin reviews    Visible in approved
   the advertisement  advertisement browser
                         |
                         v
                 Users can rate or
                 report the listing
                         |
                         v
                 Recalculate trust score
                         |
                         v
                 Review again if required
```

The diagram describes the intended workflow. Confirm the complete flow against the current application code and database before treating it as fully tested.

---

## 🧪 Testing Checklist

Before declaring a release ready, test the following scenarios:

- [ ] A user can register and log in.
- [ ] A user can submit a lost-item report.
- [ ] A user can submit a found-item advertisement.
- [ ] Initial trust scores and review statuses are saved correctly.
- [ ] Pending or rejected advertisements do not appear in the approved-advertisement browser.
- [ ] Approved found advertisements appear with the correct information.
- [ ] A user cannot rate their own advertisement.
- [ ] A user cannot submit a rating outside the range of 1–5.
- [ ] A user's updated rating replaces their previous rating.
- [ ] Trust scores are recalculated correctly after ratings.
- [ ] Users can report suspicious advertisements.
- [ ] Administrators can review, approve, and reject eligible advertisements.
- [ ] Matching considers eligible advertisements only.
- [ ] Match notifications display the expected information.
- [ ] Handover details and PIN verification work correctly.
- [ ] Completing a handover updates the relevant records consistently.
- [ ] Existing features continue to work after integrating advertisement verification.

---

## 🔐 Security and Data Integrity

Back2U includes database-backed user accounts, advertisement moderation, match tracking, and handover records. When deploying or extending the application, ensure that:

- Database credentials remain outside public repositories.
- SQL operations use prepared statements where user input is involved.
- Passwords are stored using an appropriate password-hashing mechanism.
- User and administrator permissions are checked in application logic, not only hidden in the interface.
- Trust scores are not treated as proof of authenticity.
- Database transactions protect related updates during handover completion.
- User-submitted descriptions and report details are handled safely.

These are security requirements and recommendations; verify each against the current implementation rather than assuming every item is already satisfied.

---

## 🚀 Future Scope

Potential improvements include:

- Email notifications for important match updates.
- Improved match ranking and duplicate-report detection.
- Stronger ownership verification.
- Enhanced administrator analytics and moderation history.
- More detailed audit logs.
- Improved accessibility and user-interface consistency.
- Automated integration and database tests.

---

## 👥 Contributors

- **Aadithya K**
- **Archa S**
- **Gitto George**
- **Sreedurga P**

---

## 📌 Project Status

**Back2U v2 — Completed.**

---

<p align="center">
  <strong>Back2U — Because every lost item deserves a way back.</strong>
</p>
