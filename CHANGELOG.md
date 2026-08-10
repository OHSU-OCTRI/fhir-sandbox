# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

###

- New actions workflow to update Node.js dependencies on a schedule (CIS-3820)

### Dependencies

- Bump `com.github.eirslett:frontend-maven-plugin` from 1.15.1 to 2.0.1 ([#13](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/13))

## [0.1.0] - 2026-06-30

### Added

- Add initial application scaffolding
- Add Spring Authorization Server (RFS-237)
- Add Sandbox entity (RFS-237)
- Add FHIR server configuration (RFS-237)
- Manage FHIR server partition corresponding to each sandbox (RFS-237)
- Add static OAuth client for debugging
- Add GitHub Actions and Dependabot configuration
- Add Maven plugins and repository definitions used by workflows
- Add interface to manage clients (RFS-254)
- Add private key configuration to application.properties and env.sample (RFS-255)
- Add instructions on how to configure and run the application (RFS-255)
- Add initial OpenID Connect support (RFS-275)
- Add initial SMART launch context support (RFS-278)
- Add JavaScript to construct a launch context when launching an app (RFS-278)
- Add `fhirUser` to OpenID Connect ID token if present in launch context (RFS-278)
- Add autoloading for configurable FHIR data samples (RFS-272)
- Add audience claim to access tokens to facilitate limiting sandbox access (RFS-250)
- Add checkbox to sandbox creation forms, triggering sample data import (RFS-284)
- Configurable property to override default socket timeout of FHIR client (RFS-284)
- Add a service to generate access tokens without going through the OAuth flow. (RFS-249)
- Add ability to easily copy client ID (RFS-258)
- Add UI for selecting app launch practitioner and patient. (RFS-249)
- Added Sandbox sharing between users (RFS-259)
- Ensure that requests to create partitions and load sample data have a valid bearer token (RFS-293)
- Configurable token TTL (RFS-301)
- Enable refresh tokens for public clients (RFS-301)
- Add a copy link to Sandbox Details for an access token that can be used to load data (RFS-303)
- Add a copy link to the FHIR server url to Sandbox Details (RFS-303)
- Add support for standalone launch (RFS-307)

### Changed

- Move `SecurityConfiguration` class to `config` package
- Upgrade dependencies (common-lib 2.0.0, authlib 4.0.0, DataTables 2)
- Change default ports for app and backend to avoid conflicts with existing apps
- Localhost wildcard CORS configuration (RFS-255)
- Merged `SmartClient` and `Client` entities to resolve update anomalies (RFS-277)
- Increased the maximum size of SMART client scopes (RFS-271)
- Enabled asynchronous processing (RFS-272)
- Run `npm audit fix`
- Upgrade GitHub Actions dependencies
- Extracted partition methods from SandboxService into new PartitionService (RFS-291)
- Prevent users from deleting Sandboxes with the INITIALIZING status (RFS-284)
- Replace Webpack asset pipeline with Vite (RFS-249)
- Upgrade to ESLint 10 (RFS-249)
- Update sample data to Synthea data set (RFS-290)
- Enable configuration of multiple sample data directories (RFS-290)
- Updated sample data processing to fail if any files cannot be read (RFS-290)
- Use vitest for JavaScript tests. (RFS-249)
- Use shared GitHub Actions workflows. (RFS-256)
- Add GitHub Actions workflow to build container image. (RFS-256)
- Add initial Kubernetes deployment manifests. (RFS-256)
- Update manifests for initial prod deployment. (RFS-297)
- Create a test build workflow to handle PRs (CIS-3773)

### Dependencies

- Bump `org.octri.common:common_lib` from 2.0.2-SNAPSHOT to 2.1.0 ([#40](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/40))
- Bump `authlib.version` from 4.0.0 to 4.2.0 ([#39](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/39))
- Bump `org.octri.common:common_lib` from 2.0.2-SNAPSHOT to 2.1.0 ([#40](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/40))
- Bump `org.springframework.boot:spring-boot-starter-parent` from 3.5.14 to 3.5.15 ([#49](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/49))
- Run `npm audit fix`
- Bump `org.springframework.boot:spring-boot-starter-parent` from 3.5.15 to 3.5.16
- Bump `org.octri.common:common_lib` from 2.1.0 to 2.1.1
- Bump `com.github.ben-manes.caffeine:caffeine` from 3.1.8 to 3.2.4

### Fixed

- Use `SmartClientService` to delete clients in `SmartClientController` to ensure that
  OAuth clients are cleaned up. Fixes a bug where OAuth clients were orphaned on delete.
- Only add `fhirUser` claim to the OIDC ID token if the `fhirUser` scope was approved. (RFS-287)
- Corrected repository URLs in pom.xml
- Add ID to checkbox in sandbox form to fix accessibility issue
- Fix error mounting the launch modal component when a sandbox has no clients (RFS-296)
- Fix Bootstrap modal import in `LaunchModal` component that broke dropdown menus (RFS-296)
- Correct FHIR server base URLs in Kubernetes configuration (RFS-256)
- Increase Kubernetes deployment resources to prevent out of memory errors (RFS-256)
- Allow cross-origin use of session cookie so that client launch does not redirect to login page (RFS-256)
- Ensure that pre-authorized tokens have same format as those from the authorization server (RFS-293)

[unreleased]: https://github.com/OHSU-OCTRI/fhir-sandbox/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/OHSU-OCTRI/fhir-sandbox/compare/c961153e520e35192c49b6d394b7526b758cf0be...v0.1.0
