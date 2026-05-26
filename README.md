# template

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![Coverage](https://img.shields.io/badge/coverage-%3E85%25-green.svg)](#)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18%2B-blue.svg)](https://react.dev/)
[![Docker](https://img.shields.io/badge/Docker-enabled-blue.svg)](https://www.docker.com/)

A secure, production-ready full-stack boilerplate template engineered for modern cloud-native deployment. Built from the ground up utilizing **Java 21**, **Spring Boot 4**, **Vite + React + TypeScript**, and **PostgreSQL 16**.

This repository implements rigorous security standards, including automated account lockouts, hierarchical role management, full end-to-end SSL/TLS encryption, and multi-stage container isolation optimized for rootless runtime environments (such as Podman and Docker with SELinux enabled).

---

## 🚀 Key Features

* **Robust Authentication & Identity Provisioning**:
    * Native email/password authentication alongside social sign-in via **Google OAuth2** ("Continue with Google").
    * Secure stateless session management powered by cryptographically signed high-entropy **JWT tokens**.
    * Automated account lockout safety mechanisms that block user access after $X$ consecutive failed password attempts.
    * Secure tokenized password reset flow integrating SMTP configurations.
* **Hierarchical Role-Based Access Control (RBAC) & Immutable Auditing**:
    * Strict tiered authorization architecture: `User`, `Admin`, and `Super-Admin`.
    * Operational hierarchy control: Super-Admins possess operational authority to ban/manage Admins; Admins possess authority to manage and ban standard Users.
    * **Comprehensive Audit Trail**: Fully accountable governance layer tracking destructive and administrative actions via dedicated ledger tables (`USER_BAN_LOG`, `USER_DELETION_LOG`, `USER_RECOVERY_LOG`). Every ban, deletion, and recovery operation meticulously logs the executing actor, the target entity, and the action timestamp.
    * **Automated Log Pruning**: To prevent database bloat and comply with modern data-minimization regulations, log retention windows are bounded by a background cleaner that automatically purges historical entries after a customizable $X$-day threshold.
* **Advanced Account Lifecycle & Soft-Deletion**:
    * Multi-tiered deletion privileges: Users can trigger the deletion of their own accounts, Admins can delete standard Users, and Super-Admins hold deletion rights over both Admins and Users.
    * Temporal safety buffer: To preserve immediate audit capabilities and prevent catastrophic accidental data loss, accounts are initially **soft-deleted**. The background daemon permanently expunges the underlying record only after a customizable threshold of $X$ days.
    * Account Restoration Lifecycle: Soft-deleted accounts can be fully restored/reverted within the retention window by the user themselves or by authorized (Super)Admins, with the entire operational recovery sequence fully audited.
* **Proactive Event Notifications & Global Localization (i18n)**:
    * Fully integrated multi-language translation architecture operational across both the React presentation layer and backend transactional messaging.
    * Highly communicative, localized email notification system. The application keeps the user heavily informed by dispatching real-time, translated emails whenever sensitive account mutations occur (e.g., password resets, email modifications, account soft-deletions, or account restorations).
* **End-to-End SSL/TLS Encryption**:
    * Zero-cleartext traffic rule. The edge reverse proxy (**Nginx**) handles incoming HTTPS traffic on port `443`.
    * Internal traffic is securely routed via HTTPS to the Spring Boot backend container operating a local **PKCS12 keystore** on port `8443`.
* **Asset Management & Storage Persistence**:
    * Dynamic user profile picture provisioning and storage.
    * File storage layers mapped to isolated Docker volumes for high-availability data persistence.
* **Enterprise-Grade Containerization**:
    * Highly optimized, multi-stage Docker builds resulting in minimized attack surfaces.
    * Rootless security compliance: Backend services execute under a restricted non-root runtime environment (`spring:spring`).
    * SELinux capability alignment using local volume flags (`:Z`, `:ro`, `:rw`).
	
---

## 🛠️ Technology Stack

| Layer | Technology | Version / Specification |
| :--- | :--- | :--- |
| **Backend Framework** | Java / Spring Boot 4 | Eclipse Temurin 21 (Alpine JRE) |
| **Database Migration** | Flyway | Structured SQL versioning control |
| **Database Engine** | PostgreSQL 16 | Alpine-optimized image |
| **Frontend Framework** | Vite + React + TypeScript | Strict compilation mode |
| **Component Library** | Material UI (MUI) | Component-driven design tokens |
| **Web Server / Proxy** | Nginx | Reverse proxy with custom TLS routing |
| **Infrastructure** | Docker / Docker Compose | Isolated multi-container environments |

---

## 📊 Database Architecture

The data layer models decoupled identity structures, token tables, audit parameters, and status indicators. Below is the Entity-Relationship Diagram (ERD) defining the schema constraints:

![ERD Diagram](https://github.com/benny1611/template/blob/main/backend/sql/user_diagram.jpg?raw=true)

---

## 📁 Repository Structure

```text
├── backend/
│   ├── cert/                # Bound SSL Keystores and Certificates
│   ├── uploads/             # Persistent profile asset folder
│   ├── src/                 # Spring Boot application context
│   ├── pom.xml              # Maven dependencies mapping
│   └── Dockerfile           # Multi-stage non-root runtime build script
├── frontend/
│   ├── cert/                # Nginx TLS bindings
│   ├── src/                 # React component structure & TypeScript assets
│   ├── nginx.conf           # Production server context & routing definitions
│   ├── vite.config.ts       # Build tools and developer runtime configs
│   └── Dockerfile           # Node compilation & static asset distribution 
├── .env.example             # Schema for runtime environmental variables
└── docker-compose.yml       # Network orchestration schema
```
## ⚙️ Initial Setup & Provisioning
1. Cryptographic Certificate Generation
Because the system runs strictly over TLS, you must generate a local self-signed certificate and a Java Keystore (PKCS12 format) before booting the application. Execute the following commands within your terminal layout:

```bash
# Create the target directory
mkdir -p backend/cert

# 1. Generate standard RSA private key and self-signed certificate for Nginx
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout ./backend/cert/server.key \
  -out ./backend/cert/server.crt \
  -subj "/CN=localhost"

# 2. Package the certificates into a PKCS12 keystore format for the Spring Boot application
openssl pkcs12 -export \
  -in ./backend/cert/server.crt \
  -inkey ./backend/cert/server.key \
  -out ./backend/cert/keystore.p12 \
  -name springboot \
  -password pass:your_keystore_password
```

2. Environment Configuration
Duplicate the configuration boilerplate layout into an operational .env file at the repository root:
```bash
cp .env.example .env
```

Open ```.env``` and configure your credentials accurately:

* Ensure ```SSL_KEYSTORE_PASSWORD``` matches the password specified during the OpenSSL conversion above (```your_keystore_password```).
* Provision your explicit ```GOOGLE_CLIENT_ID``` and ```GOOGLE_CLIENT_SECRET``` via the Google Cloud Developer Console to enable external authentication.
* Populate the ```MAIL_USERNAME``` and ```MAIL_PASSWORD``` parameters with your Mailtrap sandbox inbox credentials to support the registration/reset/lifecycle messaging routines.

## 🐳 Container Orchestration (```docker-compose```)
To spin up the entire encrypted application environment locally in production/staging simulation mode, run:
```bash
docker-compose up --build
```

Infrastructure Startup Sequence:
1. ```postgres-db``` provisions its environment using the defined ```.env``` parameters. A strict health check execution (```pg_isready```) routinely analyzes its availability.

2. ```backend``` triggers compilation upon confirmation of a healthy database status, processing database version schemas via Flyway migrations instantly.

3. ```frontend``` boots an Nginx layout binding local TLS paths directly into its routing server blocks.

Operational Endpoints:
* Web Application Interface (UI): ```https://localhost``` (Port ```443```)
* API Engine Access: ```https://localhost:8443```
* Database Interface Connection: ```localhost:5432```

## 💻 Local Development Workflow
When running active development feedback loops without complete containerization steps, services can be run individually:

Backend Development Context
Ensure you have a local PostgreSQL engine running or utilize ```docker-compose up postgres-db``` to provide the data layer.

```bash
cd backend
# Set your environmental variables within your shell or IDE environment profile
./mvnw spring-boot:run
```

Frontend Development Context
The development environment layout features dynamic certificate allocation via the ```basicSsl``` plugin configuration within Vite.
```bash
cd frontend
npm install
npm run dev
```

* The system intercepts the compiler invocation context (```command === 'serve'```), auto-injecting developer-centric certificates dynamically.
* Local Dev Interface access endpoint: ```https://localhost:5173```

## 🧪 Quality Assurance & Test Suites
Quality vectors are explicitly monitored via continuous validation structures. The backend microservice layer adheres strictly to a target framework criteria specifying greater than 85% test coverage.

To execute the unit and integration testing lifecycle suites against mocks and context slices, type:
```bash
cd backend
./mvnw test
```

## 📝 Engineering Note & Transparency
This boilerplate repository leverages cutting-edge software development lifecycles. Parts of the initial architecture boilerplate structures and scaffolding implementations were optimized utilizing generative artificial intelligence frameworks. Following automated code scaffolding phases, 100% of the files, type declarations, security mappings, and configurations have been manually audited, corrected, and hand-reviewed by human maintainers to guarantee absolute precision, safety, and performance.

## 📄 License
This architecture framework template is open-source software licensed under the MIT License. Feel free to adapt, fork, and use it commercially for your independent applications.