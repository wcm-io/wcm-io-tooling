About JSON Dialog Converter Maven Plugin
========================================

Converts AEM Dialog Definitions in JSON Format with Rules from [Adobe AEM Dialog Conversion Tool](https://github.com/Adobe-Marketing-Cloud/aem-dialog-conversion).

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/json-dialog-conversion-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.wcm.maven.plugins/json-dialog-conversion-plugin)


### Documentation

* [Plugin Documentation][plugindocs]
* [Changelog][changelog]


### Overview

The [Adobe AEM Dialog Conversion Tool][aem-dialog-conversion] is deployed to a AEM instance and is able to convert dialogs from installed AEM application in the repository. This is not helpful for AEM projects with `Sling-Initial-Content` in JSON format, because it's tedious do get the converted dialogs back to the project in JSON format.

This Maven plugin is a wrapper around the conversion tool and applies the conversion directly to the local Maven bundle project.

From the rules included in [Adobe AEM Dialog Conversion Tool][aem-dialog-conversion] only the "Node-based rewrite rules" are applied. Via configuration it is possible to choose between:

* `/libs/cq/dialogconversion/rules/coral2` (default): Migration Coral UI 2 to Coral UI 3
* `/libs/cq/dialogconversion/rules/classic`: Migrate Dialogs from Classic UI

This plugin is primary targeted for the `coral2` ruleset, the `classic` ruleset was never tested. From the rule implementation of the [Adobe AEM Dialog Conversion Tool][aem-dialog-conversion] only the "node-based" rewrite rules are applied; the special "CQ Dialog", "Include" and "Multifield" rewrite rules are not used (but thy are primary relevant for the Classic UI conversion).


### Usage

Execute on a maven bundle project with Sling-Initial-Content in JSON format

```
mvn io.wcm.maven.plugins:json-dialog-conversion-plugin:convert
```

This scans all JSON files and converts `cq:dialog` and `cq:design_dialog` definitions. Make sure you have a backup / SCM commit before executing the tool! You have to review the conversion in detail, it may need further manual adjustments. The conversion removes comments from the JSON files and applies a default JSON indentation formatting.

If you want to get an exact diff of the conversion in your SCM without all the reformatting noise, you can execute a reformat-only run first, commit it, and then start the conversion:

```
mvn io.wcm.maven.plugins:json-dialog-conversion-plugin:convert -Dconvert.formatOnly=true
```


[aem-dialog-conversion]: https://github.com/Adobe-Marketing-Cloud/aem-dialog-conversion
[plugindocs]: plugin-info.html
[changelog]: changes-report.html
