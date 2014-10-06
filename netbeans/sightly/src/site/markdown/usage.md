## wcm.io Netbeans Sightly Extension - Usage

The following examples who how to use the plugin.

### data-sly-\* commands

If you type in the start of a data-sly-\* command, the plugin will automatically give you a list with all available completions:

```html
<div da
```

The list displays only completions which are currently available (e.g. data-sly-u will not display data-sly-list).


### Properties of AEM classes

If you type in the the begining of one of the following variables, the plugin will automatically give you a list with the matching completion:

* currentPage
* wcmmode

e.g.
```html
${curr
```
will display **currentPage** as suggestion.

If the variable has been completed, the autocompletion will display a list with all members/methods of the variable:
```html
${wcmmode.
```
will display a list with **edit**, **preview**, **design** and **disabled**.

### Variable-names defined by data-sly-use and data-sly-list

If you define a variable using data-sly-use or data-sly-list, the plugin will suggest it for further autocompletions.
```html
<div data-sly-use.foobar="${myClass}">
${fo
```
will display "foobar" as suggestion.

### Members of variables

If you define a variable using data-sly-use or data-sly-list, the plugin will suggest it's members/methods as autocompletion:
```html
<div data-sly-use.foobar="${myClass}">
${foobar.
```
will display a list with all members/methods which are public and have a return-value (not void).

This also works in cascades:
```html
<div data-sly-use.foobar="${myClass}">
<ul data-sly-list.items="${foobar.items}">
${items.
```
will display a list with all members/methods of the return-type defined in myClass.getItems()
