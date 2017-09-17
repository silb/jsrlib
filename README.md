Jsrlib is a software library that acts as a librarian for Java Specification
Requests (JSRs). It can answer questions like:

* which JSRs specify a given Java package or class?
* which Java package names are specified by JSRs?
* which JSRs are part of Java EE 7?

Jsrlib runs queries against local data
([JsrData.json](src/main/resources/org/secnod/jsr/store/JsrData.json),
[JsrMetadata.json](src/main/resources/org/secnod/jsr/store/JsrMetadata.json))
and performs downloads by screen scraping <https://www.jcp.org>.


# Command line usage

    mvn package
    export CLASSPATH="target/classes:target/test-classes:$(mvn dependency:build-classpath | grep -v '^\[')"

List JSRs that specify a given package:

    java org.secnod.jsr.cli.Tool query package javax.ws.rs

Then download it:

    java org.secnod.jsr.cli.Tool download jsr 370 .
    ./JSR-370-jaxrs2_1finalspec.pdf

List all variants of JSR 318:

    java org.secnod.jsr.cli.Tool list jsr 318

Then download the interceptors variant:

    java org.secnod.jsr.cli.Tool download jsr 318-interceptors
    /tmp/Intercept.pdf

For help on further usage:

    java org.secnod.jsr.cli.Tool


# Legal

The screen scraper downloads JSRs licensed for evalutation. It does not display the license agreement for the JSRs at <https://www.jcp.org>.

It is the responsibility of users to familarize themselves with the licensing options of the JSRs they download using this software.


# Limitations

## Incomplete dataset

Only a subset of [all JSRs](https://www.jcp.org/en/jsr/all) have data in
[JsrData.json](src/main/resources/org/secnod/jsr/store/JsrData.json).

## JSRs are identified by only number and variant

JSRs are identified by (number, variant?) where the variant is optional.

Examples:

  * [JSR 53](https://www.jcp.org/en/jsr/detail?id=53) has a "servlet" and "jsp"
    variant.
  * [JSR 154](https://www.jcp.org/en/jsr/detail?id=154) has no variant.

But a JSR should be indentified by (number, variant?, release?) where variant
and release are optional.

Examples:

  * [JSR 154](https://www.jcp.org/en/jsr/detail?id=154) is specifies both
    Servlet API 2.4 for J2EE 1.4 in its Final Release and Servlet API 2.5 for
    JavaEE 5 in its Maintenance Release.
  * [JSR 907](https://www.jcp.org/en/jsr/detail?id=907) specifies multiple
    versions of Java Transaction API:
    * 1.2 for Java EE 7 and Java EE 8
    * 1.1 for Java EE 5 and Java EE 6
    * [1.0.1B, 1.0.1 and 1.0.1a](http://www.oracle.com/technetwork/java/javaee/tech/jta-138684.html)
      for J2EE

Currently Jsrlib picks the latest final or maintenance release of a JSR.

## JavaEE profiles are not supported

Jsrlib has no knowledge of JavaEE profiles. For instance, in JavaEE 6, the web
profile only includes a subset of the EJB functionality specified in
[JSR 318](https://www.jcp.org/en/jsr/detail?id=318) as "EJB 3.1 Lite".

See
<https://en.wikipedia.org/wiki/Java_Platform%2C_Enterprise_Edition#Web_profile>
for an overview of the web profiles.


# Development

## Running the integration tests

    mvn -DskipITs=false verify

