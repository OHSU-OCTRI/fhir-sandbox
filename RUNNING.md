# Running the Application for Development

This application provides the user interface for the OCTRI FHIR Sandbox application. This includes the account management, the user interface for managing FHIR sandboxes and SMART on FHIR client applications, and the OAuth 2.0 authorization server used to authenticate clients.

## Dependencies

The application depends on a multi-tenant FHIR server that is found in the following repository:

[OHSU-OCTRI/fhir-sandbox-backend]

Clone the repository and follow the instructions to run the FHIR server. The server will need to be running any time that you need to create or delete a sandbox.

You will also need an RSA key in PEM-encoded PKCS#8 format to use when signing JSON web tokens.

```bash
openssl genrsa -out sandbox-key.pem 2048
```

## Configuration

Copy `env.sample` to `.env` and update as needed.

In `.env`, fill in your LDAP credentials at `YOUR_USERNAME` and `YOUR_PASSWORD`.

```
LDAP_CONTEXTSOURCE_USERDN=cn=YOUR_USERNAME,ou=User Accounts,dc=ohsum01,dc=ohsu,dc=edu
LDAP_CONTEXTSOURCE_PASSWORD=YOUR_PASSWORD
```

You will also need to configure the path to the RSA key that you created.

```
OCTRI_SANDBOX_OAUTH2_PRIVATE_KEY_LOCATION=file:/path/to/your/sandbox-key.pem
```

Alternatively, you can create a `dev.properties` file in `src/main/resources` and configure the properties there.

```properties
ldap.context-source.user-dn=cn=YOUR_USERNAME,ou=User Accounts,dc=ohsum01,dc=ohsu,dc=edu
ldap.context-source.password=YOUR_PASSWORD
octri.sandbox.oauth2.private-key-location=file:/path/to/your/sandbox-key.pem
```

## Starting the Applications

Start the PostgreSQL database and FHIR server according to the instructions in [OHSU-OCTRI/fhir-sandbox-backend/].

https://github.com/OHSU-OCTRI/fhir-sandbox-backend/blob/main/RUNNING.md

Use `docker compose` to start the MySQL container.

```bash
docker compose up -d mysql
```

Start the application using Visual Studio Code (e.g. using the Spring Boot Dashboard) or `mvn spring-boot:run`.

After a few seconds, the application should be running here:

http://localhost:8081/fhir-sandbox/

The first time that you run the application, you will also need to populate the `user` table before you can log in. You can use the [add_users.sql] script to add OCTRI's standard user accounts.


[OHSU-OCTRI/fhir-sandbox-backend]: https://github.com/OHSU-OCTRI/fhir-sandbox-backend
[add_users.sql]: https://source.ohsu.edu/OCTRI-Apps/auth-default-users/blob/master/sql/add_users.sql