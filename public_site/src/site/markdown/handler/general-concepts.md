## wcm.io Handler General Concepts

The wcm.io Handler is a set of libraries that built on each other to provide the following high-level functionality:

* Build and externalize URLs
* Detect Site Root
* Build and validate links to internal and external targets
* Display images based on AEM Assets or other source with dynamic generation of virtual renditions, support for cropping and further transformations
* Rendering and processing of rich text markup

All these features are _not_ implemented as components, but as set of APIs, Sling models and Granite UI components that can be added as "aspects" to existing components. This makes it easy to add this functionality to any existing or new component while ensuring a consistent behavior and configuration across the whole AEM application.

All this was built with multi-tenancy in mind, that means sharing one AEM instance for multiple sites probably using different templates sets, AEM applications or completely different tenants with their own project-specific needs is possible.

The main libraries are:

* [URL Handler][url-handler]: URL resolving and processing.
* [Link Handler][link-handler]: Link resolving, processing and markup generation.
* [Media Handler][media-handler]: Media resolving, processing and markup generation.
* [RichText Handler][richtext-handler]: Rich text processing and markup generation.


### Context-Aware Configuration

The wcm.io Handler make use of [Sling Context-Aware Configuration][sling-caconfig] to provide configuration in way that supports multiple sites and multiple tenants.

Currently there is one configuration for defining the domain names that are used for externalizing of links and URLs.


### Context-Aware Services

The SPI implementation of the handlers is implemented using [Context-Aware Services][wcmmio-sling-caservices], because the SPI does not only require configuration parameters, but also allows to hook in custom methods or custom classes. It allows to define customization of the handler processing in different ways for different context paths (e.g. sites, tenants) within the repository.

Thus multi tenancy is fully supported for both configuration and SPI implementation of the wcm.io Handler.


### Common patterns for using the handlers

The implementations for the handlers share common patterns:

* The Java APIs of the handlers follow a "fluent" style using "builder" interfaces.
* Most of the use cases can be solved with a single or few lines of code, whilst supporting the more complex use cases with more parameters as well.
* The handler objects are Sling Models, making it easy to inject them in the Sling Models of the applications using the `@Self` annotation - or by adapting from a request or resource. The handler have immediate access to the context (e.g. the current resource).
* The handler use sensible defaults for their configuration and SPI, and allow to override them partially or in complete as needed.
* All handler provide an SPI to allow a wide range of customization to the project needs - although in most cases the built-in functionality is sufficient.
* The handler provide "convenience" Sling Models that can be used directly in HTL templates for the basic processing of links, images and rich text. Thus it's often not even required to use any Java code to use their functionality.


### More general concepts

Each of the handler has their own general concepts page for more details:

* [URL Handler General Concepts][url-handler-general-concepts]
* [Link Handler General Concepts][link-handler-general-concepts]
* [Media Handler General Concepts][media-handler-general-concepts]
* [RichText Handler General Concepts][richtext-handler-general-concepts]


[url-handler]: url/
[link-handler]: link/
[media-handler]: media/
[richtext-handler]: richtext/
[url-handler-general-concepts]: url/general-concepts.html
[link-handler-general-concepts]: link/general-concepts.html
[media-handler-general-concepts]: media/general-concepts.html
[richtext-handler-general-concepts]: richtext/general-concepts.html
[sling-caconfig]: https://sling.apache.org/documentation/bundles/context-aware-configuration/context-aware-configuration.html
[wcmmio-sling-caservices]: https://wcm.io/sling/commons/context-aware-services.html
