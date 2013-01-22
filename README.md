
Thymeleaf Eclipse Plugin
========================

A plugin for the Eclipse IDE to add auto-completion support for the Thymeleaf
standard dialect processors in the Eclipse Web Tools Platform HTML source
editor.

 - Current version: 0.3.0-SNAPSHOT
 - Released: ?? ??? 2013

Note that this plugin is still _very_ beta, so I welcome any feedback and/or bug
reports, either through the Thymeleaf forum (on this thread: http://forum.thymeleaf.org/Thymeleaf-content-assist-plugin-for-Eclipse-td4025498.html),
or as issue reports through the GitHub page.


Minimum Requirements
--------------------

 - Java 6
 - Eclipse Indigo SR2 (3.7.2) w/ Web Tools Platform 3.3.2 (ie: the Java EE
   Developer bundle)


Installation
------------

Download the JAR from the Thymeleaf forum thread (http://forum.thymeleaf.org/Thymeleaf-content-assist-plugin-for-Eclipse-td4025498.html)
and place it in your Eclipse /dropins folder.


Usage
-----

Add the namespace for the Thymeleaf standard dialect to your HTML file, like so:

	<!DOCTYPE html>
	<html xmlns:th="http://www.thymeleaf.org">

You should now start getting content assistance for all of Thymeleaf's standard
processors: suggestions as you type and autocompletion if what you've entered so
far if it matches only one result (both of these can be invoked manually using
CTRL+SPACE), and help text when hovering over the text of a Thymeleaf processor.

Content assist features are only available for dialects whose namespaces (with
the matching prefix) are defined in your HTML files, and for any dialects that
supply help files with their JARs.  (Thymeleaf ones are already bundled with
this plugin.)

If you're developing a Thymeleaf dialect and would like to take advantage of
Eclipse content assist for your own dialect, read the next section.


Adding content assist for your dialect
--------------------------------------




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


