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

```sh
$ mvn compile
$ alias jsrlib="java -cp \"$PWD/target/classes:$(mvn dependency:build-classpath | grep -v '^\[')\" org.secnod.jsr.cli.Tool"
```

List JSRs that specify a given package:

```sh
$ jsrlib query package javax.ws.rs
JSR 370: JavaTM API for RESTful Web Services (JAX-RS 2.1) Specification
  Description: This JSR is to develop JAX-RS 2.1, the next release of Java API for RESTful Web Services.
  Link: https://www.jcp.org/en/jsr/detail?id=370
  Packages: javax.ws.rs javax.ws.rs.client
  Tags: JavaEE8
  Succeeds: JSR 339 JAX-RS 2.0: The Java API for RESTful Web Services

JSR 339: JAX-RS 2.0: The Java API for RESTful Web Services
  Description: This JSR will develop the next version of JAX-RS, the API for for RESTful (Representational State Transfer) Web Services in the Java Platform.
  Link: https://www.jcp.org/en/jsr/detail?id=339
  Packages: javax.ws.rs javax.ws.rs.client
  Tags: JavaEE7
  Succeeds: JSR 311 JAX-RS: The JavaTM API for RESTful Web Services

JSR 311: JAX-RS: The JavaTM API for RESTful Web Services
  Description: This JSR will develop an API for providing support for RESTful(Representational State Transfer) Web Services in the Java Platform.
  Link: https://www.jcp.org/en/jsr/detail?id=311
  Packages: javax.ws.rs
  Tags: JavaEE6 JavaEE5
```

Then download it:

```sh
$ jsrlib download jsr 370 .
./JSR-370-jaxrs2_1finalspec.pdf
```

List all variants of JSR 318:

```sh
$ jsrlib list jsr 318
JSR 318-ejb: Enterprise JavaBeansTM 3.1
  Description: Enterprise JavaBeans is an architecture for the development and deployment of component-based business applications.
  Link: https://www.jcp.org/en/jsr/detail?id=318
  Packages: javax.ejb
  Tags: JavaEE6
  Succeeds: JSR 220 Enterprise JavaBeansTM 3.0

JSR 318-interceptors: Interceptors 1.2
  Description: Enterprise JavaBeans is an architecture for the development and deployment of component-based business applications.
  Link: https://www.jcp.org/en/jsr/detail?id=318
  Packages: javax.interceptor
  Tags: JavaEE6
```

Then download the interceptors variant:

```sh
$ jsrlib download jsr 318-interceptors .
./JSR-318-interceptors-Intercept.pdf
```

For help on further usage:

```sh
$ jsrlib help
```


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
 * [JSR 919](https://jcp.org/en/jsr/detail?id=919) specifies
   [multiple versions](https://stackoverflow.com/questions/45723816/single-jsr-multiple-api-versions)
   of JavaMail™.

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

```sh
$ mvn -DskipITs=false verify
```
