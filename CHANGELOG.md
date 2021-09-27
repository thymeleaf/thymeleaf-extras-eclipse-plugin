
Changelog
=========

### 3.1.0
 - Upgrade JAXB Utilities to 2.1.0 to clear reflective access warnings in more
   recent versions of Java, which have since become errors in Java 16+

### 3.0.1
 - Bundle all JARs to fix plugin startup issues around missing
   `javax.activation` dependency.  That JAR is normally included in the
   "Eclipse IDE for Enterprise and Java Web Developers" bundle, but other
   installations might not have it despite meeting other prerequisites (like
   Spring Tool Suite)
   ([#99](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/99),
   [#100](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/100))

### 3.0.0
 - Update plugin dependencies to fix an incompatibility with Java 9+.  Plugin is
   now built and run against all Java LTS versions as of this release (Java 8,
   11, and 14)
   ([#84](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/91),
   [#91](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/91))
 - Minimum required Java version is now 8
 - Minimum required Eclipse version is now 2019-06

### 2.1.2
 - Fixed content assist not working over a list of tags in the restrictions
   element, ([#68](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/68)),
   with [Alexandre Araújo](https://github.com/alexandrearaujo) providing the
   necessary fix ([#69](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/pull/69))
 - Fixed this bug where the Thymeleaf menu would show up in context menus when
   nothing was selected ([#56](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/56))

### 2.1.1
 - Focused so much on getting Thymeleaf Natures _added_ to a project, that we
   forgot to test _removing_ it, which suffered from the same bugs as adding it
   did.  Fixed. ([#45](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/45))
 - Fixed a non-blocking error that occurs when the XML namespace checking tries
   to resolve external resources ([#47](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/47))
 - Enhanced the attribute restriction feature so that an attribute value can
   also be a part of the restriction ([#48](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/48))

### 2.1.0
 - Support for Thymeleaf 2.1's new features! ([#33](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/33),
   [#34](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/34),
   [#35](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/35),
   [#36](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/36),
   [#37](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/37),
   [#41](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/41))
 - Dialect metadata file is now on a public URL: http://www.thymeleaf.org/xsd/thymeleaf-extras-dialect-2.1.xsd
   This should make it easier to create conformant XML files since you can now
   just point your XML editor to the schema ([#23](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/23))
 - Fix for interoperability with other plugins that may hijack the standard HTML
   page editor, or plugins that make use of the standard HTML page editor
   ([#28](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/28),
   [#43](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/43))

### 2.0.4
 - Fix for some Eclipse installations that were failing to add the Thymeleaf
   nature, introduced in 2.0.3 ([#40](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/40),
   [#42](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/42),
   with a big thanks to [Thibault Duchateau](https://github.com/tduchateau) for
   finding and providing the fix)

### 2.0.3
 - Added support for a wider range of workspace refresh types, so if the
   dialects that your project is using change, the plugin is now better equipped
   to reflect those changes in the content assist.
 - A Thymeleaf Nature that can be added to your projects.  See the Features
   section of the readme for more details
   ([#20](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/issues/20))

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
