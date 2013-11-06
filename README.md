
Thymeleaf - Eclipse Plugin module
=================================

A plugin for the Eclipse IDE to add content assist features for the Thymeleaf
standard dialect processors and expression utility objects, using the Eclipse
Web Tools Platform HTML source editor.

 - Current version: 2.1.0
 - Released: ?? ??? 2013


Minimum Requirements
--------------------

 - Java 6
 - Thymeleaf 2.1.0
 - Eclipse Indigo SR2 (3.7.2) w/ Web Tools Platform 3.3.2 (ie: the Java EE
   Developer bundle)


Installation
------------

In Eclipse, go to Help >> Install New Software... then either use the update
site URL, or download a ZIP archive of the plugin from SourceForge:

 - Update site URL: [http://www.thymeleaf.org/eclipse-plugin-update-site/](http://www.thymeleaf.org/eclipse-plugin-update-site/)
 - ZIP file downloads: [https://sourceforge.net/projects/thymeleaf/files/thymeleaf-extras-eclipse-plugin/](https://sourceforge.net/projects/thymeleaf/files/thymeleaf-extras-eclipse-plugin/)


Features
--------

### Content Assist

Content assist features are only available for dialects which have supplied
special dialect metadata files in their JARs.  thymeleaf-core, thymeleaf-spring3,
thymeleaf-extras-springsecurity3, and thymeleaf-extras-tiles2 projects already
have such files.  Other dialects, however, are up to the discretion of their
developer(s).  If you're developing a Thymeleaf dialect and would like to take
advantage of content assist for your own dialect, read the section on
[adding content assist for your dialect](#adding-content-assist-for-your-dialect).

Once those help files are available, you can make content assist available in
your HTML files through 1 of 2 ways:

#### 1. Declaring the dialect namespace and prefix in your HTML files

This is the easiest method and you may have already done this to keep the XML
validator happy:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
```

#### 2. Applying the Thymeleaf project nature to your project

This method will make content assist available to _all_ of the HTML files in
your project, and is ideal for when you've organized your code to have plenty of
reusable HTML fragments without a common root element on which to put the XML
namespace.

To add the Thymeleaf nature to your project: right-click a project >> Thymeleaf >>
Add Thymeleaf Nature.

Using either method, you should now start getting content assist for any dialect
whose namespace is explicitly declared in your HTML files (method 1), or for
every dialect in your project's classpath (method 2).  This applies to
suggestions as you type, autocompletion of what you've entered so far if it
matches only one result (both of these can be invoked manually using CTRL+SPACE),
and help text when hovering the cursor over a Thymeleaf processor.


Adding content assist for your dialect
--------------------------------------

The content assist features are driven by metadata about a dialect, currently
done using XML files, conforming to a schema that lives at
[http://www.thymeleaf.org/xsd/thymeleaf-extras-dialect-2.1.xsd](http://www.thymeleaf.org/xsd/thymeleaf-extras-dialect-2.1.xsd).

When content assist is invoked, this plugin will look for XML files in the
classpath of the current project whose XML namespace is `http://www.thymeleaf.org/extras/dialect`.
If such a file is found, it is loaded and the information in it is used to form
the content assist data that the Eclipse plugin uses.

Dialect developers can take advantage of this by including XML help files as
part of their dialect JARs.  All you need to do is create an XML file that
conforms to the schema above, then bundle that XML file with your JAR.

Some notes on where you put that file in the JAR:

 - it cannot go in the default package
 - the directory it goes in must be a valid Java package name

These are just short-comings of the current dialect scanning method, which
itself is built upon Eclipse's own lookup mechanisms.


Changelog
---------

### 2.1.0
 - Support for Thymeleaf 2.1.0's new features!
 - Dialect metadata file is now on a public URL: http://www.thymeleaf.org/xsd/thymeleaf-extras-dialect-2.1.xsd
   This should make it easier to create conformant XML files since you can now
   just point your XML editor to the schema ([#23](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/23))

### 2.0.3
 - Added support for a wider range of workspace refresh types, so if the
   dialects that your project is using change, the plugin is now better equipped
   to reflect those changes in the content assist.
 - A Thymeleaf Nature that can be added to your projects.  See the [Features](#features)
   section for more details ([#20](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/20))

### 2.0.2
 - Dialect files in dependent projects weren't being picked up, either
   through Eclipse or dependency-management containers (Maven, Gradle) ([#15](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/15))
 - Added a basic refresh mechanism which tracks changes in scanned dialect files
   and reflects those changes in the plugin.  This is an ongoing work, which can
   be tracked against [#21](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/21).
 - Added a new attribute to the `<restrictions>` element called `attributes`,
   which lists other attributes that must or must not appear in the same tag for
   the attribute processor to be suggested ([#17](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/17))
 - We now have an update site! :) ([#13](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/13))

### 2.0.1
 - `th:inline` is now part of the suggested attribute processors list ([#12](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/12))
 - Added support for the upcoming Thymeleaf-Spring3 `#themes.code(...)`
   expression object and method, which is the Thymeleaf equivalent of the Spring
   `<spring:theme code=''/>` JSP tag.
 - Updated Tycho to 0.17.0, which now generates source features automatically
   (explicit source feature sub-project deleted).

### 2.0.0
 - Moved to a 'Thymeleaf extras' branching and versioning scheme.
 - Added a source code feature to the generated repository file so that you have
   the option of installing the source code of this plugin.
 - Fixed an issue where negative restrictions (ie: tags that the processor
   cannot appear on) were not being proposed at all.

### 0.4.0
 - Added support for the Spring standard dialect ([#8](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/8))
 - Added the ability to use a processor class' Javadoc content as the help
   content that would appear with the content assist.
 - Added autocomplete/suggestion support for attribute processors with a limited
   value set (eg: `th:inline` can accept only `text`, `javascript`, or `dart`).
 - Made it so attribute processors already in use in the same element will not
   be suggested in content assist ([#10](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/10))

### 0.3.0
 - Moved to become a Thymeleaf Extras project.
 - Added a dialect scanning feature, contributed by [Thibault Duchateau](https://github.com/tduchateau),
   so that content assist can extend to dialects other than the standard
   Thymeleaf ones bundled with the plugin.  See the [Adding content assist for
   your dialect](#adding-content-assist-for-your-dialect) section to find out how
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
 - Relaxed the plugin requirements so it can now work in Java 6 and Eclipse
   3.7.2 w/ WTP 3.3.2.
 - Fixed some spelling mistakes in the standard attribute processor suggestions,
   which would insert misspelled processors into your code!  Whoops!
 - Added help/documentation for many more of the standard attribute processors.
   
### 0.1.0
 - Initial release
