# Running the Application for Development

This application provides the user interface for the OCTRI FHIR Sandbox application. This includes the account management, the user interface for managing FHIR sandboxes and SMART on FHIR client applications, and the OAuth 2.0 authorization server used to authenticate clients.

## Prerequisites

- [Java 17](https://adoptium.net/)
- [Maven](https://maven.apache.org/)
- [Docker](https://www.docker.com/) and Docker Compose, for running the MySQL database
- [OpenSSL](https://www.openssl.org/), for generating an RSA key

Node.js and npm do not need to be installed separately unless you want to run the unit tests. They are provisioned automatically by the Maven build via the `frontend-maven-plugin`, which also installs npm dependencies and runs the Vite build.

## Dependencies

The application depends on a multi-tenant HAPI FHIR server that is found in the following repository:

[OHSU-OCTRI/fhir-sandbox-backend]

Clone the repository and follow the instructions to run the FHIR server. The server will need to be running any time that you need to create or delete a sandbox.

You will also need an RSA key in PEM-encoded PKCS#8 format to use when signing JSON web tokens.

```bash
openssl genrsa -out sandbox-key.pem 2048
```

## Configuration

Like all Spring Boot applications, the OCTRI FHIR sandbox supports [externalized configuration](https://docs.spring.io/spring-boot/reference/features/external-config.html) via YAML files, properties files, environment variables, and command line parameters.

The simplest way to set up data sources and other configuration for the application is to create a properties file to override expected properties defined in `application.properties`. For convenience during development, properties are automatically loaded from files named `dev.properties` or `dev.yaml` in [src/main/resources/](src/main/resources/).

To use a custom configuration path, run the jar providing the override location:

```bash
java -jar fhir-sandbox.jar --spring.config.location=classpath:/application.properties,file:///Users/home/someuser/override.properties
```

### Server Port

By default, the application binds to port 8080. Depending on the constraints of your client applications, you may need to adjust the port that the FHIR sandbox binds to. To do this, set the `server.port` property.

```properties
server.port=8081
```

This also impacts the OAuth2 issuer URL.

```properties
octri.sandbox.oauth2.issuer-url=http://localhost:8081/fhir-sandbox
```

The port _must_ be different from the one configured for the FHIR server.

### OAuth2 Private Key

You will need to configure the path to the RSA key that you created above.

```properties
octri.sandbox.oauth2.private-key-location=file:/path/to/your/sandbox-key.pem
```

### FHIR Server URL

By default, the FHIR server is expected to be available at the URL http://localhost:8000/fhir. If you modified the FHIR server's port or context path, adjust the `octri.sandbox.fhir.base-url` property. For example, if you configured the FHIR server to bind to port 8001, this would be the FHIR base URL.

```properties
octri.sandbox.fhir.base-url=http://localhost:8001/fhir
```

### Authentication Properties

The application supports two main methods of authentication, and both may be used. If your organization uses LDAP and all users are internal, you can enable LDAP authentication and provide the properties below:

```properties
octri.authentication.enable-ldap=true
ldap.context-source.url=
ldap.context-source.user-dn=
ldap.context-source.password=
ldap.context-source.search-base=
ldap.context-source.search-filter=
ldap.context-source.organization=
ldap.context-source.email-domain=
```

If you will have external users or do not have LDAP, you can enable table-based users:

```properties
octri.authentication.enable-table-based=true
```

SAML authentication is also supported for production use. See [the OCTRI authentication lib SAML documentation](https://github.com/OHSU-OCTRI/authentication-lib/blob/main/docs/CONFIGURATION_PROPERTIES.md#saml-authentication).

## Spring Mail (Table-Based Authentication)

Spring Mail is used to communicate with table-based users when their account is created and during password resets. If the application will have table-based users, the settings should be configured for your organization.

```properties
octri.messaging.email.default-sender-address=
spring.mail.default-encoding=UTF-8
spring.mail.host=
spring.mail.port=
spring.mail.protocol=
spring.mail.test-connection=
spring.mail.username=
spring.mail.password=
spring.mail.properties.mail.smtp.auth=
spring.mail.properties.mail.smtp.starttls.enable=
spring.mail.properties.mail.smtp.starttls.required=
```

This should be all the configuration needed. When you run the jar for the first time, the writable database schema will be initialized and the database will be empty.

## Add a User

To log into the application, the first user needs to be created directly in the database and assigned a role of Admin. That user will then be able to log on and create other users in the database.

If your organization is using LDAP, you can amend the following SQL statements to create the first user:

```sql
INSERT INTO `fhir_sandbox`.`user` (`authentication_method`, `account_locked`, `consecutive_login_failures`, `email`, `enabled`, `first_name`, `last_name`, `username`)
VALUES ('LDAP', 0, 0, '<org_email>', 1, '<first_name>', '<last_name>', '<username>');

SET @userid = (SELECT last_insert_id());
SET @admin = (SELECT id FROM user_role WHERE role_name = 'ROLE_ADMIN' limit 1);
INSERT INTO user_user_role (user, user_role)
VALUES (@userid, @admin);
````

If your organization will have table-based users only, start by creating the first user and role, setting the credentials to expired, and providing an email that will be used for confirmation of password reset:

```sql
INSERT INTO `fhir_sandbox`.`user` (`authentication_method`, `account_locked`, `consecutive_login_failures`, `credentials_expiration_date`, `email`, `enabled`, `first_name`, `last_name`, `username`)
VALUES ('TABLE_BASED', 0, 0, NOW(), '<email>', 1, '<first_name>', '<last_name>', '<username>');

SET @userid = (SELECT last_insert_id());
SET @admin = (SELECT id FROM user_role WHERE role_name = 'ROLE_ADMIN' limit 1);
INSERT INTO user_user_role (user, user_role)
VALUES (@userid, @admin);
````

Now, create a password reset token for the user. The token can be any unique string up to 255 characters. This example uses '12345':

```sql
SET @expiration = (SELECT DATE_ADD(NOW(), INTERVAL 10 DAY));
INSERT INTO password_reset_token(token, expiry_date, user)
VALUES ('12345', @expiration, @userid);
```

With the jar running, navigate to the application's password reset token url, providing the token parameter:

```
http://localhost:8080/fhir-sandbox/user/password/reset?token=12345
```

You will be prompted to set a new password and will be able to log in once completed.

## Starting the Applications

Start the FHIR server according to the instructions in [OHSU-OCTRI/fhir-sandbox-backend](https://github.com/OHSU-OCTRI/fhir-sandbox-backend/blob/main/RUNNING.md).

https://github.com/OHSU-OCTRI/fhir-sandbox-backend/blob/main/RUNNING.md

Use `docker compose` to start the MySQL container.

```bash
docker compose up -d mysql
```

Start the application using Visual Studio Code (e.g. using the Spring Boot Dashboard) or `mvn spring-boot:run`.

After a few seconds, the application should be running here:

http://localhost:8081/fhir-sandbox/

The first time that you run the application, you will also need to create at least one user account to log in.

[OHSU-OCTRI/fhir-sandbox-backend]: https://github.com/OHSU-OCTRI/fhir-sandbox-backend
[add_users.sql]: https://source.ohsu.edu/OCTRI-Apps/auth-default-users/blob/master/sql/add_users.sql