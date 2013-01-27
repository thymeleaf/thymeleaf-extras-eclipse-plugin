
Thymeleaf Eclipse Plugin
========================

A plugin for the Eclipse IDE to add content assist features for the Thymeleaf
standard dialect processors and expression utility objects, using the Eclipse
Web Tools Platform HTML source editor.

 - Current version: 0.3.0-SNAPSHOT
 - Released: 27 Jan 2013

Note that this plugin is still being developed, so I welcome any feedback and/or
bug reports, either through the Thymeleaf forum (on this thread: http://forum.thymeleaf.org/Thymeleaf-content-assist-plugin-for-Eclipse-td4025498.html),
or as issue reports through the GitHub page.


Minimum Requirements
--------------------

 - Java 6
 - Eclipse Indigo SR2 (3.7.2) w/ Web Tools Platform 3.3.2 (ie: the Java EE
   Developer bundle)


Installation
------------

Download the ZIP file from the Thymeleaf forum (http://forum.thymeleaf.org/Thymeleaf-content-assist-plugin-for-Eclipse-td4025498.html).
Then, in Eclipse, go to Help >> Install New Software... and add the ZIP file as
an archive repository.  The Thymeleaf Eclipse Plugin item should appear.  Select
it, and follow the on-screen prompts to install the plugin.


Usage
-----

Content assist features are only available for dialects whose namespaces (with
the matching prefix) are defined in your HTML files, and if that dialect has
supplied some help files in their JARs.  Help files for Thymeleaf's standard
processors come bundled with this plugin, so all you have to do to get content
assist support is to include the Thymeleaf namespace and prefix in your HTML
file like so:

	<!DOCTYPE html>
	<html xmlns:th="http://www.thymeleaf.org">

You should now start getting content assistance for all of Thymeleaf's standard
processors: suggestions as you type and autocompletion of what you've entered so
far if it matches only one result (both of these can be invoked manually using
CTRL+SPACE), and help text when hovering over the text of a Thymeleaf processor.

Support for more of Thymeleaf's features, like expression utility objects, and
the Thymeleaf Extras modules, is in the works.

If you're developing a Thymeleaf dialect and would like to take advantage of
content assist for your own dialect, read the next section.


Adding content assist for your dialect
--------------------------------------

The content assist features and help content are driven by XML files containing
information about a dialect.  XML help files for the Thymeleaf Standard dialect
comes bundled with this plugin, and you can see how it's structured by
[taking a look at the XML file itself](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/master/bundles/thymeleaf-extras-eclipse-plugin.content-assist/dialects/Standard-Dialect.xml),
as well as [taking a look at the schema file it conforms to](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/master/bundles/thymeleaf-extras-eclipse-plugin.dialect/schemas/thymeleaf-dialect-help.xsd).

When content assist is invoked, this plugin will look for XML files in the
current project, or in the dependencies of the current project, whose XML
namespace is `http://www.thymeleaf.org/extras/dialect`.  If such a file is found,
it is loaded and the information in it becomes part of the plugin's content
assist.

Dialect developers can take advantage of this by including XML help files as
part of their dialect JARs.  All you need to do is create an XML file that
conforms to the schema above, then bundle that XML file with your JAR.

Some notes on where you put that file:

 - it cannot go in the default package
 - the directory it goes in must be a valid Java package name

These are just short-comings of the current dialect scanning method, which
itself is built upon Eclipse's own lookup mechanisms.

An example of a dialect bundled with an XML file in it's JAR: the [Thymeleaf Layout Dialect](https://github.com/ultraq/thymeleaf-layout-dialect/tree/dev)
(see the Java/nz/net/ultraq/web/thymeleaf directory).


Changelog
---------

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


