# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased

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

### Changed

- Move `SecurityConfiguration` class to `config` package
- Upgrade dependencies (common-lib 2.0.0, authlib 4.0.0, DataTables 2)
- Change default ports for app and backend to avoid conflicts with existing apps
- Localhost wildcard CORS configuration (RFS-255)
- Merged `SmartClient` and `Client` entities to resolve update anomalies (RFS-277)
- Increased the maximum size of SMART client scopes (RFS-271)

### Fixed

- Use `SmartClientService` to delete clients in `SmartClientController` to ensure that
  OAuth clients are cleaned up. Fixes a bug where OAuth clients were orphaned on delete.

[unreleased]: https://github.com/OCTRI-Apps/fhir-sandbox/compare/v0.1.0...HEAD
[0.1.0]: https://source.ohsu.edu/OCTRI-Apps/fhir-sandbox/releases/tag/v0.1.0
