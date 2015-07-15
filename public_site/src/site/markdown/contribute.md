## Contribute

wcm.io is an open-source project and all contributions are welcome to assist with its
development and maintenance. wcm.io can be used freely and is released under
[Apache license][apache-license].

[apache-license]: http://www.apache.org/licenses/LICENSE-2.0


### Collaboration tools (Issues, Wiki, Mailing Lists)

Please report any bugs found, feature requests or other issues on [JIRA][jira]. You can signup [here][jira-signup].

When creating a new issue, try following [necolas's guidelines][issue-guidelines].

The project's wiki is hosted in [Confluence][confluence].

Overview over the project's [mailing lists][mailing-lists].


[jira]: https://wcm-io.atlassian.net/
[jira-signup]: https://wcm-io.atlassian.net/admin/users/sign-up
[confluence]: https://wcm-io.atlassian.net/wiki/
[issue-guidelines]: http://github.com/necolas/issue-guidelines/#readme
[mailing-lists]: http://wcm.io/mailing-lists.html


### Fork, patch and contribute code

Feel free to fork wcm.io's [Git repository at GitHub][wcm-io-github] for your own use and
updates.

Contribute your fixes and new features back to the main codebase using
[GitHub pull requests][github-pull-req].

[wcm-io-github]: http://github.com/wcm-io
[github-pull-req]: http://help.github.com/articles/using-pull-requests



### Build from sources

If you want to build wcm.io from sources make sure you have configured all [Maven Repositories](maven.html) in your settings.xml.

See [Travis Maven settings.xml](https://github.com/wcm-io/wcm-io/blob/master/.travis.maven-settings.xml) for an example with a full configuration.

Then you can build using

```
mvn clean install
```


### Acknowledgements

wcm.io was initially contributed and is sponsered by [pro!vision GmbH][pro-vision]

AEM stands for [Adobe Experience Manager][aem] and is a registered Trademark from Adobe

The project's websites uses the [Reflow Maven Skin][reflow] by [Andrius Velykis][andrius-velykis].

The "parametrized" JUnit rule from the JUnit Commons project is based on work by [Jens Schauder]([schauderhaft]).

[pro-vision]: http://www.pro-vision.de
[aem]: http://www.adobe.com/solutions/web-experience-management.html
[reflow]: http://andriusvelykis.github.io/reflow-maven-skin/
[andrius-velykis]: http://andrius.velykis.lt/
[schauderhaft]: http://blog.schauderhaft.de/



### Copyright and license

Copyright 2014 wcm.io

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
