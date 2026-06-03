# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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

### Fixed

- Use `SmartClientService` to delete clients in `SmartClientController` to ensure that
  OAuth clients are cleaned up. Fixes a bug where OAuth clients were orphaned on delete.
- Only add `fhirUser` claim to the OIDC ID token if the `fhirUser` scope was approved. (RFS-287)
- Corrected repository URLs in pom.xml

[unreleased]: https://github.com/OCTRI-Apps/fhir-sandbox/compare/v0.1.0...HEAD
[0.1.0]: https://source.ohsu.edu/OCTRI-Apps/fhir-sandbox/releases/tag/v0.1.0

### Dependencies

- Bump `actions/checkout` from 5 to 6 ([#1](https://github.com/OHSU-OCTRI/fhir-sandbox/pull/1))
