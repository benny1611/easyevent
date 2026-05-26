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
    * Automated automated account lockout safety mechanisms that block user access after $X$ consecutive failed password attempts.
    * Secure tokenized password reset flow integrating SMTP configurations.
* **Hierarchical Role-Based Access Control (RBAC)**:
    * Strict tiered authorization architecture: `User`, `Admin`, and `Super-Admin`.
    * Operational hierarchy control: Super-Admins possess operational authority to ban Admins; Admins possess authority to manage and ban standard Users.
* **End-to-End SSL/TLS Encryption**:
    * Zero-cleartext traffic rule. The edge reverse proxy (**Nginx**) handles incoming HTTPS traffic on port `443`.
    * Internal traffic is securely routed via HTTPS to the Spring Boot backend container operating a local **PKCS12 keystore** on port `8443`.
* **Asset Management & Storage Persistence**:
    * Dynamic user profile picture provisioning and storage.
    * File storage layers mapped to isolated Docker volumes for high-availability data persistence.
* **Global Localization (i18n)**:
    * Fully integrated multi-language translation architecture operational across both the React presentation layer and backend exception/message interpolation context.
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

![ERD Diagram](https://github.com/benny1611/template/blob/main/backend/sql/user_diagram.png?raw=true)