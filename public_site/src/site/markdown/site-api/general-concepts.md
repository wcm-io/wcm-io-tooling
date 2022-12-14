## wcm.io Site API General Concepts

When implementing headless projects in AEM, you mainly have two possibilities based on built-in product features:

* [AEM Sites with Sling Models Exporter][aem-sling-models-exporter] and REST API
* [Content Fragments with GraphQL][aem-cf-graphql]

wcm.io Site API helps building headless applications with AEM Sites based on pages and components. It extends the Sling Models Exporter approach by providing additional endpoints to crawl the API and access additional information like context-aware configuration via the API.

The main modules are:

* [Site API Processor][site-api-processor]: Processor API for Headless AEM projects based on AEM Sites.
* [Site API Handler Extensions][site-api-handler]: Support wcm.io Handler infrastructure in Site API.


### Why use AEM Sites for Headless AEM?

Using AEM Sites with pages and components has a lot of benefits for building a headless API in AEM:

* Navigation structure is driven by pages.
* Page content can be structured freely with AEM's container and component concepts, helped by Editable Templates.
* Sling Models Exporter provides OOTB serialization of page content.
* Sling Models for each component provide a **business abstraction** of the component content, so instead of returning the "raw" content from the repository the model returns a transformed and validated view which fits best for the business purpose.
* When using a SPA as frontend the [AEM SPA Editor][aem-spa-editor] can be used to provide a WYSIWYG authoring experience.
* Using REST as API approach fits naturally in the AEM architecture and the AEM caching infrastructure. AEM and Sling are build entirely around REST.
* Providing hypermedia links makes the crawling through the API very simple for both consuming applications and humans.

Using Content Fragments and GraphQL has a lot of advantages. Based on the defined content fragment models a full generic edit mode is automatically provided by AEM. The GraphQL schema is generated automatically and allows to benefit from all GraphQL features. However, compared to the AEM Sites approach and considering typical use cases for publishing marketing web sites in AEM it also has some drawbacks:

* Content Fragments shine when they are used to model well-structured data (supporting hierarchy, nesting and references). But it is very challenging to model a free-form "container and component" concept in Content Fragments which allow the same flexibility as you have when using pages, components and templates in AEM.
* There is **no business abstraction** in the API. So you always get the "raw" content from the repository without the possibility to transform and validate the data that is returned by your API. The responsibility for this is shifted to the consuming applications, making this difficult especially if you have multiple client applications.
* While GraphQL is great it comes with drawbacks looking at caching. Taking the limitations of GET requests into account, you have to revert to POST requests for more complex GraphQL queries which are difficult to cache properly. AEM provides a concept for [Persisted GraphQL queries][aem-persisted-graphql-queries] to solve this problem. However, using persisted GraphQL queries limits the usefulness of GraphQL (you have to know and model the query use cases upfront).
* One of the key benefits of GraphQL, the possibility to filter API responses and getting aggregates, is not really required for marketing website use cases compared to a page-based API approach as used by the Site API. If a client e.g. wants to render a page, it usually has to read the whole page content to render all components. If a client wants to read the navigation structures or context-aware configuration, it's often the best approach to read both structures fully and keep the response cached in the application/session state.


### Why is Sling Models Exporter not enough?

Sling Models Exporter integrated with AEM provides a great way to get the full page content serialized in a clean way as JSON. The models for each component provide a business abstraction of the content. The wcm.io Site API fully leverages this feature, but builds more support around it.

Besides the actual page content, we usually want to publish more information in the API, e.g.:
* Navigation structures
* Context-Aware Configuration
* i18n translations
* Sitemap
* Index with URLs for all available endpoints

This information should not be included in the JSON for each page to avoid duplication. With the Site API, we can provide separate API endpoints. These endpoints are attached to the site root page so live in context of the actual site.


### Site API URL Concept

The Site API URL concept leverages the Sling features *selector*, *extension* and *suffix*, ensuring that caching and cache invalidation works in AEM as usual.

Example URL with structure explained:

```
/content/myapp/us/en.site.api/index.json
|-------------------| Example for a site root path
                    |--------| Fixed selector and extension (configurable, may contain version)
                              |-----| Suffix mapped to endpoint/processor (e.g. content, index, config...)
```

API endpoints are consumed by applications, and not by humans or SEO bots, so usually no Sling Mapping/URL shortening needs to be applied.

The Site API is re-using the `.model.json` view provided by the Sling Models Exporter but maps it to a different URL ending with `.site.api/content.json` to fit into the URL structure (and optionally providing API versioning). Technically, it's just a request forward to the Sling Models Exporter response.

Examples for endpoint URLs using this concept following the example above:

* `/content/myapp/us.site.api/countryindex.json`: List all language endpoints of the country.
* `/content/myapp/us/en.site.api/index.json`: List all endpoints of the site.
* `/content/myapp/us/en.site.api/content.json`: Content of the homepage/site root page.
* `/content/myapp/us/en.site.api/navigation.json`: Site main an meta navigation structures.
* `/content/myapp/us/en.site.api/config.json`: Subset of context-aware configuration of the site.
* `/content/myapp/us/en/page-1/subpage-1.site.api/content.json`: Content of a page deeper in the site structure.

Starting at the `index` or `countryindex` endpoints, the whole API can be crawled by applications (or humans using a browser), including all pages, navigation structures and configurations.

It's up to the application which actual endpoints should be provided. Using the [Site API Processor][site-api-processor] module, each processor is implemented as OSGi service and serves the response for one specific suffix. The Site API comes with some built-in processors, but you are not limited to those and can add your own or replace the built-in ones.

For more complex use cases it's also possible to provide more addressing information in the suffix, e.g. `.site.api/config/MyConfig.json` to directly get the JSON representation of a specific context-aware configuration.


### JSON Schema of Site API and built-in processors

The Site API does not define or imply the actual JSON schema that is returned by the endpoints.

For the `content` endpoint serving the content of each AEM page the Sling Model Exporter defines the general layout of the JSON response, and we recommend to stick to it in the Site API. You have control over the Java APIs of the Sling Models used by your application, so inside the container structure you can model the content in any way you want.

The Site API comes with the following built-in processors:

* `index`: Index Processor listing all registered endpoints
* `content`: Content processor, forwards to `.model.json` view
* `config`: Context-Aware Configuration processor serving a subset of context-aware configuration (configurable)
* `navigation`: Returns navigation structure based on page hierarchy starting a site root page

These processors are deployed by default, but are not active. You can enable them via OSGi configuration. Whereas it is recommended to use the out-of-the-box `content` and `config` processors, the `index` and `navigation` processors are simple implementations and are often replaced by project-specific ones.


### Representation of links and images in the API

A headless API serving AEM content usually contains a lot of link and asset references all over the API (and not only in the `content` endpoint, but also e.g. in `config` and `navigation` endpoints). Example use cases:

* Links to other pages (mapped to API calls to `content` endpoints of those pages)
* External links
* Links to assets (downloads)
* References to bitmap images with various renditions and aspect ratios
* References to vector images

All these link and image references should be represented in the same way thorough the API (and usually represented in a small JSON substructure and not just an URL).

To help this, the [Site API Handler Extensions][site-api-handler] integrates the [wcm.io Handler][wcmio-handler] infrastructure in to the Site API. The Site API does not define or imply an actual JSON representation of those references in the API. It provides a simple default view, but usually you will define your own representation and use it in the processors serving the endpoint as well as in your Sling Models.


### API Versioning

Whe you publish your headless API and multiple client applications are using it over years, you have to think about backward compatibility. Most improvements and introductions of new features in your application may be easily done in a forward-compatible manner, adding new properties and elements in your API without breaking existing functionality.

However, from time to time you may have the need to introduce breaking changes. For this API versioning can be used. The Site API is using an URL-based versioning approach. This fits nicely into the AEM and Sling architecture and caching concepts.

Example for Site URL including version information:

```
/content/myapp/us/en.site.v1.api/index.json
                         |--|  Version
```

So, basically the fixed selector defined for the Site API is extended with a version suffix, using `site.v1` instead of `site` as selector. The idea behind this: The AEM application only implements the latest API version you serve. You do not want to keep multiple versions or branches our your application and Sling Models around and maintain multiple different versions of it at the same time.

Instead, older versions are implemented as a transformation layer. As soon as you introduce a new, breaking version `v2`, you add a transformation layer mapped to version `v1` which internally transforms the JSON response for `v2` to `v1` by using a set of declarative transformation rules e.g. using [Jolt][jolt]. With this, your code only implements the latest version and the previous versions' responses are derived from that. This implies, that `v2` contains all data that is needed to build the `v1` response, but probably in a completely different JSON structure.


### Multi Tenancy

The Site API is implemented with multi-tenancy in mind. You may want to deploy multiple applications to the same AEM instance, but each of them implementing a different type of API with different endpoint concepts and processors.

All OSGi interfaces that can be implemented by applications in the Site API are using the concept of [Context-Aware Services][wcmio-sling-context-aware-services]. With this concept, OSGi service implementations are only applied to "resource contexts", e.g. Sites/page hierarchy subtrees, that belong the the application they are build for. As a result, multiple different implementations of the same service and URL mapping can nicely co-exist on the same AEM instance.


### More general concepts

Additional general concepts for the Site API modules:

* [Site API Processor General Concepts][site-api-processor-general-concepts]
* [Site API Handler General Concepts][site-api-handler-general-concepts]


[aem-cf-graphql]: https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/headless/graphql-api/content-fragments.html
[aem-sling-models-exporter]: https://experienceleague.adobe.com/docs/experience-manager-learn/foundation/development/understand-sling-model-exporter.html?lang=en
[aem-spa-editor]: https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/implementing/developing/hybrid/editor-overview.html?lang=en
[aem-persisted-graphql-queries]: https://experienceleague.adobe.com/docs/experience-manager-cloud-service/content/headless/graphql-api/persisted-queries.html?lang=en
[site-api-processor]: processor/
[site-api-handler]: handler/
[site-api-processor-general-concepts]: processor/general-concepts.html
[site-api-handler-general-concepts]: processor/general-concepts.html
[wcmio-handler]: https://wcm.io/handler
[wcmio-sling-context-aware-services]: https://wcm.io/sling/commons/context-aware-services.html
[jolt]: https://github.com/bazaarvoice/jolt
