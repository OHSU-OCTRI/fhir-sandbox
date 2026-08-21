# OCTRI FHIR Sandbox

## Development Info

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project. It uses a MySQL database for storage, managed using Flyway.

## Setup

### Recommended VS Code Extensions

When you open the project for the first time with Visual Studio Code, it should offer to install recommended extensions for Java and JavaScript development. To install the extensions manually, open the command palette (CMD-Shift-P), type "recommended", and select the option for "Extensions: Show Recommended Extensions".

### Running for Local Development

See [RUNNING.md](./RUNNING.md) for instructions on how to configure and run the application for local development.

## Front End

Mustache templates are stored in [`src/main/resources/mustache-templates`](src/main/resources/mustache-templates/). This location is configured by the property `spring.mustache.prefix` in [`src/main/resources/application.properties`](src/main/resources/application.properties).

Bootstrap 5 and jQuery 3 are both included in the templates. Additional CSS styles may be added to [`static/assets/css/main.css`](src/main/resources/static/assets/css/main.css).

Node.js 24 tooling to integrate Vue components is also provided. This is integrated with the Maven build process, so there is no need for a separate Node.js runtime unless you want to run the [Vitest tests](#front-end-tests).

Scripts to mount Vue applications should be added to the [`src/main/resources/frontend`](src/main/resources/frontend/) directory and added to the `input` array in [`vite.config.ts`](vite.config.ts).

```typescript
export default defineConfig({
  base: '',
  build: {
    manifest: true,
    outDir: 'target/classes/static',
    rollupOptions: {
      input: [
        // NOTE: Add entry scripts to the input array
        'src/main/resources/frontend/managed-content.js'
      ],
      output: {
        // ...
      }
    }
  },
  // ...
});
```

Components should be added to [`src/main/resources/frontend/components`](src/main/resources/frontend/components/). To mount a Vue application into a page, add the corresponding entrypoint script to the `pageScripts` array in a controller class. See [`vite.config.ts`](vite.config.ts), [`managed-content.js`](src/main/resources/frontend/managed-content.js), and [`TranslationController.java`](src/main/java/org/octri/fhir_sandbox/controller/TranslationController.java) for a full example.

## Front End Tests

Front end tests are implemented using [Vitest](https://vitest.dev/). To execute the front end tests, run one of the two provided npm scripts.

```bash
# To run tests once
npm run test:ci

# To watch files and run tests repeatedly
npm test
```

As noted above, you will need Node.js 24 to run the tests.

## Integration tests requiring a database

To bring up a testing database you may use the Docker Compose file [`docker-compose.test.yml`](docker-compose.test.yml):

```bash
docker-compose -f docker-compose.test.yml up -d
```

This brings up a second MySQL database container on port 3307. [`test-application.properties`](src/test/resources/test-application.properties) overrides the datasource URL.

Add the following annotations to your test class which will bring up a full application context that uses this test datasource.

```java
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:application.properties", "classpath:test-application.properties" })
@SpringBootTest
```

## Flyway Migrations

Flyway migrations should be added to [`src/main/resources/db/migration`](src/main/resources/db/migration/) and have filenames following the convention `V19700101000042__short_migration_description.sql`. Flyway will only find migrations matching this format: `V`, followed by the year, month, day, hours, minutes, seconds (YYYYMMDDhhmmss), two underscores, a short description, and finally `.sql`.

## Sample Data

When creating a new sandbox, you will be given the option to automatically import sample data. The application provides a default sample data set, but also allows you to configure any number of external directories of sample data.

### Importing Data and Sandbox Status

If you choose to install sample data, the sandbox will remain in the `INITIALIZING` state until the import is complete. During this time, you cannot create SMART on FHIR clients or delete the sandbox. If an error is encountered during data import, the sandbox will enter the `ERROR` state, at which point it can be deleted. Otherwise, the sandbox will become `READY` when the import competes.

### Configuring Sample Data

The `octri.sandbox.data.sample-directories` property in [`src/main/resources/application.properties`](src/main/resources/application.properties) specifies a comma-separated list of resource directories. The directories are processed sequentially, allowing you to ensure that data dependencies are met by installing referenced data earlier in the list. For example, you can ensure that provider information is installed before patient data that references the providers.

Each directory is processed as follows:

1. The directory will be scanned for JSON files (not recursively)
2. JSON files from that directory will be parsed as FHIR Bundle Resources
3. Each Bundle will be executed as a transaction with the HAPI FHIR client

If an error is encountered at any point during the process, the entire import will be aborted and the sandbox's status will be updated to `ERROR`.
