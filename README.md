Thymeleaf - Eclipse Plugin module
=================================

A plugin for the Eclipse IDE to add content assist features for the Thymeleaf
standard dialect processors and expression utility objects, using the Eclipse
Web Tools Platform HTML source editor.

 - Current version: 2.0.5
 - Released: ?? ??? 2014

The 2.0.x versions of the Eclipse plugin are for Thymeleaf 2.0.  Check out the
[2.1-master branch](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/tree/2.1-master)
for a version that supports Thymeleaf 2.1.


Minimum Requirements
--------------------

 - Java 6
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
special help files in their JARs.  This plugin comes bundled with help files for
The Thymeleaf Standard and Spring Standard dialects (`th` prefix), the Spring
Security dialect (`sec`), and the Tiles dialect (`tiles`), so you don't need to
worry about those ones.

(Other dialects however are up to the discretion of the developer.  If you're
developing a Thymeleaf dialect and would like to take advantage of content
assist for your own dialect, read the section on
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
reusable HTML fragments without a common root element.

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

The content assist features are driven by meta data about a dialect, currently
done using XML files.  XML files for the Thymeleaf Standard dialect comes
bundled with this plugin, and you can see how it's structured by taking a look
at [the XML file itself](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/2.0-master/bundles/thymeleaf-extras-eclipse-plugin.dialect-files/dialects/Standard-Dialect.xml),
as well as taking a look at [the schema file it conforms to](https://github.com/thymeleaf/thymeleaf-extras-eclipse-plugin/blob/2.0-master/bundles/thymeleaf-extras-eclipse-plugin.core/schemas/thymeleaf-dialect-help.xsd).

When content assist is invoked, this plugin will look for XML files in the
current project, or in the dependencies of the current project, whose XML
namespace is `http://www.thymeleaf.org/extras/dialect`.  If such a file is
found, it is loaded and the information in it becomes part of the plugin's
content assist.

Dialect developers can take advantage of this by including XML help files as
part of their dialect JARs.  All you need to do is create an XML file that
conforms to the schema above, then bundle that XML file with your JAR.

Some notes on where you put that file in the JAR:

 - it cannot go in the default package
 - the directory it goes in must be a valid Java package name

These are just short-comings of the current dialect scanning method, which
itself is built upon Eclipse's own lookup mechanisms.

For an example of a dialect bundled with an XML file, see the [Thymeleaf Layout Dialect](https://github.com/ultraq/thymeleaf-layout-dialect)
and its [LayoutDialect.xml file](https://github.com/ultraq/thymeleaf-layout-dialect/blob/master/Java/nz/net/ultraq/thymeleaf/LayoutDialect.xml).

An explanation of the XML schema:


### `<dialect>`

The root element of the XML file, contains information about a dialect, its
processors, and its expression objects.

#### Attributes
 - `prefix`        - required, the prefix used by this dialect.
 - `namespace-uri` - required, the namespace used by this dialect.
 - `class`         - required, the dialect class.

#### Elements
The following elements can appear 0-or-more times, in any order:
 - [`<attribute-processor>`](#attribute-processor)           - optional.
 - [`<element-processor>`](#element-processor)               - optional.
 - [`<expression-object>`](#expression-object)               - optional.
 - [`<expression-object-method>`](#expression-object-method) - optional.

#### Example
```xml
<dialect xmlns="http://www.thymeleaf.org/extras/dialect"
	prefix="th" namespace-uri="http://www.thymeleaf.org"
	class="org.thymeleaf.standard.StandardDialect">
```


### `<attribute-processor>`

An attribute processor, includes an extra set of restrictions to help with
deciding where the processor can go and what values it can take.

#### Attributes
 - `name`  - required, the name of the attribute, minus the prefix part.
 - `class` - optional, points to the attribute processor class.  If you specify
             this attribute, then the Javadocs on the class are used to generate
             the documentation that appears in content assist. 

#### Elements
 - [`<documentation>`](#documentation) - optional, contains the text that
                                         appears in content assist.
 - [`<restrictions>`](#restrictions)   - optional, lists certain restrictions
                                         on the use of the attribute, such as
                                         what values it can take, in which HTML
                                         tags it can appear, and so on.

#### Example
```xml
<attribute-processor name="inline">
	<documentation
		reference="Using Thymeleaf section 11 on Inlining">
		<![CDATA[
		Lets you use expressions directly in your template.<br/>
		<br/>
		If this attribute's value is <b>text</b>, then you can use the [[...]]
		syntax to put expressions within your text without having to use the
		th:text attribute processor, eg:<br/>
		&lt;p th:inline="text"&gt;Hello [[${session.user.name}]]!&lt;p&gt;<br/>
		]]>
	</documentation>
	<restrictions values="text javascript dart"/>
</attribute-processor>
```


### `<restrictions>`

A set of restrictions on attribute processor use, used to help the content
assist decide where attribute suggestions should be made.

#### Attributes
 - `tags`       - optional, a list of tags that this processor can or cannot
                  appear in.  To list a tag that it can't appear in, prefix that
                  tag name with a minus symbol, eg: -head
 - `attributes` - optional, a list of attributes that must or must not be
                  present in the same tag as this processor.  To list an
                  attribute that must not be present, prefix that attribute name
                  with a minus symbol, eg: -style
 - `values`     - optional, a list of values this processor can take.


### `<element-processor>`

An element processor.

#### Attributes
 - `name`  - required, the name of the attribute, minus the prefix part.
 - `class` - optional, points to the attribute processor class.  If you specify
             this attribute, then the Javadocs on the class are used to generate
             the documentation that appears in content assist. 

#### Elements
 - [`<documentation>`](#documentation) - optional, contains the text that
                                         appears in content assist.


### `<expression-object>`

An object added to the processing context to be used by processors.

#### Attributes
 - `name`  - required, the name of the attribute, minus the prefix part.
 - `class` - optional, points to the attribute processor class.  If you specify
             this attribute, then the Javadocs on the class are used to generate
             the documentation that appears in content assist. 

#### Elements
 - [`<documentation>`](#documentation) - optional, contains the text that
                                         appears in content assist.

#### Example
```xml
<expression-object name="dates" class="org.thymeleaf.expression.Dates"/>
```


### `<expression-object-method>`

A method in an expression object.

#### Attributes
 - `name`  - required, the name of the attribute, minus the prefix part.
 - `class` - optional, points to the attribute processor class.  If you specify
             this attribute, then the Javadocs on the class are used to generate
             the documentation that appears in content assist. 

#### Elements
 - [`<documentation>`](#documentation) - optional, contains the text that
                                         appears in content assist.

#### Example
```xml
<expression-object-method name="fields.errors">
	<documentation see-also="errors"
		reference="Thymeleaf + Spring 3 section 7 on Validation and Error Messages">
		<![CDATA[
		Returns a list of error messages for the given field name.
		]]>
	</documentation>
</expression-object-method>
```


### `<documentation>`

Notes to help generate some documentation about a processor.

#### Attributes
 - `see-also`  - optional, a space-separated list of other dialect item names
                 related to this one, suggesting where else the user can go to
                 get more information or understanding.
 - `reference` - optional, names an 'official' document and the section/page
                 within it to get more information.
