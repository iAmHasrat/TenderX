# TenderX — LLM-Assisted Tender Evaluation Platform

> **Capstone Demonstration Project**  
> An AI-assisted public procurement platform where the issuer describes a project in natural language, an LLM design engine (Google Gemini / xAI Grok / Smart Heuristic Engine) generates structured criteria and dynamic response forms, and a deterministic scoring engine evaluates bids with 100% mathematical reproducibility.

---

## ⚡ Quick Start

### 🌟 Modern Web Platform (Recommended for Video Recording & Evaluator Demos)
Double-click:
```cmd
start_web.bat
```
*(Starts the local Vite server and automatically opens `http://localhost:5173` in your default browser).*

---

### ☕ JavaFX & Academic Backend
The original Java domain models, SQLite persistence, and JUnit 5 deterministic scoring tests remain in `src/`:
```cmd
run.bat
```
Or run automated tests:
```cmd
.\mvnw.bat test
```

---

## 🔐 Authentication: Preconfigured Demo Accounts & Custom Registration

The login portal supports **both one-click demo presets and custom registration**:

### 1. One-Click Preconfigured Demo Accounts:
- **Tender Issuer / Publisher**: `issuer@nhai.gov.in` (Eng. Rajesh Verma, NHAI)
- **Bidder 1 (Highway Grade-A)**: `bids@apexinfra.com` (Apex Infrastructure & Geotech Ltd)
- **Bidder 2 (Civil Roadworks)**: `bids@buildwell.com` (BuildWell Roadways & Civil Corp)
- **Bidder 3 (Heavy Engineering)**: `bids@sterling.com` (Sterling Heavy Engineering Pvt Ltd)
- **Procurement Auditor / Admin**: `admin@tenderx.gov.in` (Dr. Shalini Mehta, Vigilance Commission)
*(Password for all demo accounts: `password123`)*

### 2. Manual Custom Account Registration ("Make Your Own ID"):
- Click the **"Register Custom ID (Make Own)"** tab on the login screen.
- Enter your name, choose role (**Bidder**, **Publisher**, or **Auditor**), enter your organization, email, and password.
- TenderX saves your custom account to `localStorage` and logs you in immediately.

---

## 🤖 Live LLM Engine Integration (Google Gemini & xAI Grok)

TenderX supports **both live cloud LLMs and zero-latency smart heuristic generation**:

1. Click **"LLM Engine Settings"** in the AI Designer header.
2. Select **Google Gemini** (`gemini-1.5-flash`) or **xAI Grok** (`grok-beta`).
3. Paste your API key. TenderX will dispatch structured JSON requests directly to the LLM API to extract criteria, weights, rationales, and dynamic response fields!
4. **Offline / Demo Safe Fallback**: If no API key is entered, TenderX automatically runs its built-in deterministic schema engine with 0 ms latency, guaranteeing that video recordings and live presentations never stall due to network or rate limits.

---

## 📝 Manual Custom Bidding ("Make Your Own Bid")

When logged in as any contractor (preconfigured or custom registered):
1. Navigate to **"Submit Bid"** and select any published tender.
2. Manually enter your **Commercial Quote (₹)** and **Proposed Timeline (days)**.
3. Manually type your technical answers into each dynamically generated requirement field (materials, drainage plan, subgrade specs, QA certifications).
4. Click **"Clear Fields"** to start blank, or use **"🪄 Auto-Fill Proposal"** for rapid presentation demo.
5. Click **"Submit Official Sealed Bid"** — the proposal is instantly registered into the evaluation database.

---

## 💡 Architectural Decision: Why NOT MongoDB?

| Feature | Relational / SQLite + LocalStorage (Chosen) | MongoDB (NoSQL) |
| :--- | :--- | :--- |
| **Procurement Legal Standards** | **Strict ACID transactions & immutable audit trails** required by public procurement law (GeM, CPPP). | Eventual consistency; not standard for state financial contract audits. |
| **Deployment & Demo Friction** | **Zero-setup**: Works immediately on any machine out of the box with zero background services. | Requires installing MongoDB daemon / Atlas cloud configuration, IP whitelisting, and connection string handling. |
| **Dynamic Form Capability** | Achieved via **normalized dynamic schema tables** and JSON response structures. | Flexible documents, but overkill for form fields. |
| **Academic Syllabus Alignment** | 100% compliant with standard Java, JDBC, and SQL curriculum requirements. | Deviates from core relational syllabus requirements. |

> **Viva / Defense Answer**: *"We evaluated MongoDB, but chose a normalized relational model with dynamic JSON attributes. This provides the document flexibility of NoSQL for dynamic bidder forms while guaranteeing strict ACID financial auditability and zero-setup deployment for public procurement."*
