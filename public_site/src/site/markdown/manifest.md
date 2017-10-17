## Manifest

This is our manifest for all modules developed in this project.

### Semantic Versioning

* All wcm.io modules are released following the [Semantic versioning](https://www.osgi.org/wp-content/uploads/SemanticVersioning.pdf) rules.
* This applies to both package versions and bundle versions.
* We use the same even-odd Versioning strategy as the Apache Sling project (releases always use a even version number for the third digit).

### Support multi tenancy and configuration

* No configuration is just "global", but can be applied on any configuration level

### Minimal configuration and sensible defaults

* All features should usable "out-of-the-box" without the need of a lot of initial configuration and setup procedures
* Sensible defaults are applied for all configuration options
* Optionally relevant features can be configured and extended

### Good API Design

* Good API design leveraging best practices e.g. How To Design A Good API and Why it Matters (Joshua Bloch), Effective Java 2nd Edition (Joshua Bloch), Clean Code (Robert Martin)
* Export only APIs and SPIs in OSGi, not the implementation
* Enforce separation of concerns

### Internationalization

* Every user-visible part has to use i18n for translations (author and publish environment)
* By default we ship with English and German translations

### Testability

* Design and implement everything for good testability - unit tests, integration tests, acceptance tests

### AEM Product Dependencies

* Targeted at AEM 6 and up
* Targeted primary at the Touch UI, only basic support for Classic UI

### AEM Update-Safeness

* No overlays!
* Do not come into way when upgrading AEM versions or deploying service packs
* Test early with new AEM major versions
