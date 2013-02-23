
Thymeleaf - Eclipse Plugin module
=================================

A plugin for the Eclipse IDE to add content assist features for the Thymeleaf
standard dialect processors and expression utility objects, using the Eclipse
Web Tools Platform HTML source editor.

 - Current version: 2.0.0
 - Released: 23 Feb 2013


Minimum Requirements
--------------------

 - Java 6
 - Eclipse Indigo SR2 (3.7.2) w/ Web Tools Platform 3.3.2 (ie: the Java EE
   Developer bundle)


Installation
------------

Download the Eclipse plugin ZIP file from the [Thymeleaf Eclipse Plugin SourceForge page](https://sourceforge.net/projects/thymeleaf/files/thymeleaf-extras-eclipse-plugin/).
Then, in Eclipse, go to Help >> Install New Software... and add the ZIP file as
an archive repository.  The Thymeleaf Eclipse Plugin item should appear.  Select
it, and follow the on-screen prompts to install the plugin.


Usage
-----

Content assist features are available for dialects whose namespaces (with the
matching prefix) are defined in your HTML files, and if that dialect has
supplied some help files in their JARs.  Help files for Thymeleaf's standard and
Spring standard dialects, as well as the Thymeleaf Extras modules (Spring
Security 3 and Tiles 2), come bundled with this plugin, so all you have to do to
get content assist support is to include the Thymeleaf namespace and prefix in
your HTML file like so:

	<!DOCTYPE html>
	<html xmlns:th="http://www.thymeleaf.org">

You should now start getting content assistance for all of the processors and
expression objects in Thymeleaf's standard dialect (as well as the Spring
dialect if you've included it in your project).  This includes suggestions as
you type and autocompletion of what you've entered so far if it matches only one
result (both of these can be invoked manually using CTRL+SPACE), and help text
when hovering the cursoer over a Thymeleaf processor.

If you're developing a Thymeleaf dialect and would like to take advantage of
content assist for your own dialect, read the next section.


Adding content assist for your dialect
--------------------------------------

The content assist features and help content are driven by meta data about a
dialect, currently done using XML files.  XML files for the Thymeleaf Standard
dialect comes bundled with this plugin, and you can see how it's structured by
taking a look at [the XML file itself](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/master/bundles/thymeleaf-extras-eclipse-plugin.content-assist/dialects/Standard-Dialect.xml),
as well as taking a look at [the schema file it conforms to](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/master/bundles/thymeleaf-extras-eclipse-plugin.dialect/schemas/thymeleaf-dialect-help.xsd).

When content assist is invoked, this plugin will look for XML files in the
current project, or in the dependencies of the current project, whose XML
namespace is `http://www.thymeleaf.org/extras/dialect`.  If such a file is
found, it is loaded and the information in it becomes part of the plugin's
content assist.

Dialect developers can take advantage of this by including XML help files as
part of their dialect JARs.  All you need to do is create an XML file that
conforms to the schema above, then bundle that XML file with your JAR.

When writing documentation to appear in the content assist, you can either:
 - refer to an existing class file, using its Javadocs as the help text
 - or write your own documentation in a `<documentation>` element in the XML

When including the XML file with your JAR, some notes on where you put that
file:

 - it cannot go in the default package
 - the directory it goes in must be a valid Java package name

These are just short-comings of the current dialect scanning method, which
itself is built upon Eclipse's own lookup mechanisms.

An example of a dialect bundled with an XML file in it's JAR: the [Thymeleaf Layout Dialect](https://github.com/ultraq/thymeleaf-layout-dialect/tree/dev)
(see the Java/nz/net/ultraq/web/thymeleaf/Layout-Dialect.xml file).


Changelog
---------

### 2.0.0
 - Moved to a 'Thymeleaf extras' branching and versioning scheme.
 - Added a source code feature to the generated repository file so that you have
   the option of installing the source code of this plugin.
 - Fixed an issue where negative restrictions (ie: tags that the processor
   cannot appear on) were not being proposed at all.

### 0.4.0
 - Resolved [Issue #8](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/8),
   adding support for the Spring standard dialect.
 - Added the ability to use a processor class' Javadoc content as the help
   content that would appear with the content assist.
 - Added autocomplete/suggestion support for attribute processors with a limited
   value set (eg: `th:inline` can accept only `text`, `javascript`, or `dart`).
 - Resolved [Issue #10](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/10),
   so attribute processors already in use in the same element will not be
   suggested in content assist.

### 0.3.0
 - Moved to become a Thymeleaf Extras project.
 - Added a dialect scanning feature, contributed by [Thibault Duchateau](https://github.com/tduchateau),
   so that content assist can extend to dialects other than the standard
   Thymeleaf ones bundled with the plugin.  See the [Adding content assist for
   your dialect](adding-content-assist-for-your-dialect) section to find out how
   dialect developers can take advantage of this plugin.
 - Added showing the basic help/documtation appear when hovering over a
   processor.
 - Added autocomplete/suggestion support for element processors.
 - Added autocomplete/suggestion support for Thymeleaf's expression utility
   objects.
 - Added help files for the Thymeleaf Extras modules: Spring Security 3 and
   Tiles 2.

### 0.2.0
 - Added Eclipse API baseline support to work towards other versions of Eclipse.
 - Resolved [Issue #1](https://github.com/ultraq/thymeleaf-eclipse-plugin/issues/1)
   so the plugin can now work on Java 6, and Eclipse 3.7.2 w/ WTP 3.3.2.
 - Fixed some spelling mistakes in the standard attribute processor suggestions,
   which would insert misspelled processors into your code!  Whoops!
 - Started work on resolving [Issue #2](https://github.com/ultraq/thymeleaf-eclipse-plugin/issues/1)
   by adding some documentation to many of the standard attribute processors.
   
### 0.1.0
 - Initial release
