# OCTRI FHIR Sandbox

## Development Info

* [Wiki](https://octri.ohsu.edu/wiki/display/ENTER_CONFLUENCE_KEY/ENTER_PAGE_NAME)
* [Issues](https://octri.ohsu.edu/issues/projects/ENTER_JIRA_KEY/issues/)

This is a [Spring Boot](https://projects.spring.io/spring-boot/) project. It uses a mysql database for storage, managed using Flyway.

## Setup

### Recommended VS Code Extensions

When you open the project for the first time with Visual Studio Code, it should offer to install recommended extensions for Java and JavaScript development. To install the extensions manually, open the command palette (CMD-Shift-P), type "recommended", and select the option for "Extensions: Show Recommended Extensions".

### Running for Local Development

See [RUNNING.md](./RUNNING.md) for instructions on how to configure and run the application for local development.

## Front End

Mustache templates are stored in `src/main/resources/mustache-templates` which was overridden in [`src/main/resources/application.properties`](src/main/resources/application.properties) by the property `spring.mustache.prefix`.

By default there is a `home.mustache` template that uses a header layout (`layout/header.mustache`) and a footer layout (`layout/footer.mustache`).

Bootstrap 5 and jQuery 3 are both included in the templates. Additional CSS styles may be added to `static/css/main.css`.

Tooling to integrate Vue components is also provided. Scripts to mount Vue applications should be saved to the `src/main/resources/frontend` directory and added to the `entry` block in [`webpack.config.js`](webpack.config.js), while single-file components should be added to `src/main/resources/frontend/components`. To mount a Vue application into a page, add the corresponding entrypoint script to the `pageScripts` array in a controller class. See `webpack.config.js`, `managed-content.js`, and `TranslationController.java` for a full example.

## Integration tests requiring a database

To bring up a testing database you may use the Docker Compose file `docker-compose.test.yml`:

```
docker-compose -f docker-compose.test.yml up -d
```

This brings up a second MySQL database container on port 3307. `test-application.properties` overrides the datasource URL.

Add the following annotations to your test class which will bring up a full application context that uses this test datasource.

```
@RunWith(SpringRunner.class)
@TestPropertySource(locations = { "classpath:application.properties", "classpath:test-application.properties" })
@SpringBootTest
```

## Flyway Migrations

To create a Flyway migration, create a version directory in `src/main/resources/db/migration`. For example:

```
mkdir src/main/resources/db/migration/0.0.1
```

Now add your migrations in this directory. For example, `V19700101000042__my_first_migration.sql` which follows the format: `V`, followed by the year, month, day, hours, minutes, seconds (YYYYMMDDhhmmss), two underscores, a short description, and finally `.sql`.
