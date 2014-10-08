## wcm.io Netbeans Sightly Extension - Usage

The following examples who how to use the plugin.

### data-sly-\* commands

If you type in the start of a data-sly-\* command, the plugin will automatically give you a list with all available completions:

![data-sly](images/data-sly-.png)

The list displays only completions which are currently available (e.g. data-sly-u will not display data-sly-list).


### Properties of AEM classes

If you type in the the begining of one of the following variables, the plugin will automatically give you a list with the matching completion:

* currentPage
* wcmmode


![Build in](images/buildIn.png)


If the variable has been completed, the autocompletion will display a list with all members/methods of the variable:


![Build in](images/buildIn02.png)


### Variable-names defined by data-sly-use and data-sly-list

If you define a variable using data-sly-use or data-sly-list, the plugin will suggest it for further autocompletions.


![Variables](images/variable.png)


### Class Lookup

If you define a variable using data-sly-use, the plugin will suggest all available classes from the current project:

![ClassLookup](images/classlookup.png)


### Members of variables

If you define a variable using data-sly-use or data-sly-list, the plugin will suggest it's members/methods as autocompletion:


![Members](images/member01.png)


will display a list with all members/methods which are public and have a return-value (not void).

This also works in cascades:

![Members](images/member02.png)

