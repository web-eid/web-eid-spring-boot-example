# Web eID Spring Boot example

![European Regional Development Fund](https://github.com/open-eid/DigiDoc4-Client/blob/master/client/images/EL_Regionaalarengu_Fond.png)

This project is an example Spring Boot web application that shows how to implement strong authentication
and digital signing with electronic ID smart cards using Web eID.

More information about the Web eID project is available on the project [website](https://web-eid.eu/), which is served by this application.

## Quickstart

Complete the steps below to run the example application in order to test authentication and digital signing with Web eID.

### 1. Setup HTTPS

Web eID only works over a HTTPS connection with a trusted HTTPS certificate.
You can either setup a reverse HTTPS proxy during development or, alternatively, configure
HTTPS support directly in the bundled web server. HTTPS configuration is described in more detail in section _[HTTPS support](#https-support)_ below.

You can use, for example, [_ngrok_](https://ngrok.com/) to get a reverse HTTPS proxy. Download _ngrok_ and run it in a terminal window by providing the protocol and Spring Boot application port arguments as follows:

    ngrok http 8080

### 2. Configure the origin URL

One crucial step of the Web eID authentication token validation algorithm is verifying the token signature. The value that is signed contains the site origin URL (the URL serving the web application) to protect against man-in-the-middle attacks. Hence the site origin URL must be configured in application settings.

To configure the origin URL, copy and paste the HTTPS URL from _ngrok_ output in step 1 into the `local-origin` field in the profile-specific settings file `src/main/resources/application-{dev,prod}.yaml` as follows:

```yaml
web-eid-auth-token:
    validation:
        local-origin: "https://<<NGROK HOSTNAME HERE>>"
```

### 3. Configure the trusted certificate authority certificates

The algorithm, which performs the validation of the Web eID authentication token, needs to know which intermediate certificate authorities (CA) are trusted to issue the eID authentication certificates. CA certificates are loaded either from `.cer` files in the profile-specific subdirectory of the [`certs`resource directory](src/main/resources/certs) or the [truststore file](src/main/resources/certs/prod/trusted_certificates.jks). By default, Estonian eID test CA certificates are included in the `dev` profile and production CA certificates in the `prod` profile.

In case you need to provide your own CA certificates, either add the `.cer` files to the `src/main/resources/certs/{dev,prod}` profile-specific directory or add the certificates to the truststore file.

### 4. Choose either the `dev` or `prod` profile

If you have a test eID card, use the `dev` profile. In this case access to paid services is not required, but you need to upload the authentication and signing certificates of the test card to the test OCSP responder database as described in section _[Using DigiDoc4j in test mode with the `dev` profile](#using-digidoc4j-in-test-mode-with-the-dev-profile)_ below. The `dev` profile is activated by default.

If you only have a production eID card, use the `prod` profile. You can still test authentication without further configuration; however, for digital signing to work, you need access to a paid timestamping service as described in section [_Using DigiDoc4j in production mode with the `prod` profile_](#using-digidoc4j-in-production-mode-with-the-prod-profile) below.

You can specify the profile as a command-line argument to the Maven wrapper command `./mvnw`, for example `./mvnw -Pprod ...`.

### 5. Run the application

Spring Boot web applications can be run from the command-line. You need to have the Java Development Kit 17 installed for building the application package and running the application.

Build and run the application with the following command in a terminal window:

```sh
./mvnw spring-boot:run
```

This will activate the default `dev` profile and launch the built-in Tomcat web server on port 8080 that was forwarded using _ngrok_ in step 1.

If you want to use the `prod` profile, build and run the application with the following command:

```sh
./mvnw -Pprod -Dspring-boot.run.profiles=prod spring-boot:run
```

When the application has started, open the _ngrok_ HTTPS URL in your preferred web browser and follow instructions on the front page.

## Table of contents

* [Quickstart](#quickstart)
* [Overview of the project](#overview-of-the-project)
  + [Overview of the source code](#overview-of-the-source-code)
  + [Configuration](#configuration)
  + [Integration with Web eID components](#integration-with-web-eid-components)
  + [Integration with DigiDoc4j components](#integration-with-digidoc4j-components)
    - [Using the Certificates' *Authority Information Access* (AIA) extension in DigiDoc4j](#using-the-certificates-authority-information-access-aia-extension-in-digidoc4j)
    - [Using DigiDoc4j in test mode with the `dev` profile](#using-digidoc4j-in-test-mode-with-the-dev-profile)
    - [Using DigiDoc4j in production mode with the `prod` profile](#using-digidoc4j-in-production-mode-with-the-prod-profile)
  + [Stateful and stateless authentication](#stateful-and-stateless-authentication)
  + [Assuring that the signing and authentication certificate subjects match](#assuring-that-the-signing-and-authentication-certificate-subjects-match)
* [HTTPS support](#https-support)
  + [How to verify that HTTPS is configured properly](#how-to-verify-that-https-is-configured-properly)
* [Deployment](#deployment)
* [Frequently asked questions](#frequently-asked-questions)
  + [Why do I sometimes get the 403 Forbidden response during authentication?](#why-do-i-sometimes-get-the-403-forbidden-response-during-authentication)
  + [Why do I get the "Access denied to TSP service" error?](#why-do-i-get-the-access-denied-to-tsp-service-error)
  + [Why can I not use my test ID card?](#why-can-i-not-use-my-test-id-card)
  + [Why do I get the 401 Unauthorized "Authentication failed: Web eID token validation failed" response during authentication?](#why-do-i-get-the-401-unauthorized-authentication-failed-web-eid-token-validation-failed-response-during-authentication)

## Overview of the project

This repository contains the code of a minimal Spring Boot web application that demonstrates how to use Web eID for strong authentication and digital signing. It makes use of the following technologies:

-   Spring Web MVC with REST support,
-   the Thymeleaf template engine,
-   Spring Security,
-   the Web eID authentication token validation library [_web-eid-authtoken-validation-java_](https://github.com/web-eid/web-eid-authtoken-validation-java),
-   the Web eID JavaScript library [_web-eid.js_](https://github.com/web-eid/web-eid.js),
-   the digital signing library [_DigiDoc4j_](https://github.com/open-eid/digidoc4j).

The project uses Maven for managing the dependencies and building the application. Maven project configuration file `pom.xml` is in the root of the project.

There is also a Docker Compose configuration file `docker-compose.yml` in the root of the project for building the application Docker image as described in section [_Deployment_](#deployment) below.

### Overview of the source code

The source code folder `src` contains the application source code and resources in the `main` subdirectory and tests in the `test` subdirectory.

The `src/main/java/org/webeid/example` directory contains the Spring Boot application Java class and the following subdirectories:

-   `config`: Spring and HTTP security configuration, Web eID authentication token validation library configuration, trusted CA certificates loading etc,
-   `security`: Web eID authentication token validation library integration with Spring Security via an `AuthenticationProvider` and `AuthenticationProcessingFilter`,
-   `service`: Web eID signing service implementation that uses DigiDoc4j, and DigiDoc4j runtime configuration,
-   `web`: Spring Web MVC controller for the welcome page and Spring Web REST controllers that provide endpoints
    -   for getting the challenge nonce used by the authentication token validation library,
    -   for digital signing.

The `src/resources` directory contains the resources used by the application:

-   `application.yaml`: main configuration file that is shared by all profiles,
-   `application-{dev,prod}.yaml`: profile-specific configuration files,
-   `application.properties`: activates the `dev` profile by default,
-   `certs`: CA certificates in profile-specific subdirectories,
-   `static`: web server static content, including CSS and JavaScript files,
-   `templates`: Thymeleaf templates.

The `src/tests` directory contains the application test suite. The most important test is the `WebApplicationTest.testHappyFlow_LoginPrepareSignDownload()` method that tests the full authentication and digital signing workflow.

### Configuration

As described in section [_4. Choose either the `dev` or `prod` profile_](#4-choose-either-the-dev-or-prod-profile) above, the application has two different configuration profiles: `dev` profile for running the application in development mode and `prod` profile for production mode. The `dev` profile is activated by default.

The profile-specific configuration files `src/main/resources/application-{dev,prod}.yaml` contain the `web-eid-auth-token.validation.use-digidoc4j-prod-configuration` setting that configures DigiDoc4j either in test or production mode, and a setting for configuring the origin URL as described in section [_2. Configure the origin URL_](#2-configure-the-origin-url) above. Additionally, the `web-eid-auth-token.validation.truststore-password` setting specifies the truststore password used in the `prod` profile.

The main configuration file `src/main/resources/application.yaml` is shared by all profiles and contains logging configuration and settings that make the session cookie secure behind a reverse proxy as described in section [_HTTPS support_](#https-support) below.

Besides configuration settings, the trusted certificate authority certificates may need to be configured as described in section [_3. Configure the trusted certificate authority certificates_](#3-configure-the-trusted-certificate-authority-certificates) above.

Spring Security has CSRF protection enabled by default. Web eID requires CSRF protection.

### Integration with Web eID components

Detailed overview of Java code changes required for integrating Web eID authentication token validation is available in the [_web-eid-authtoken-validation-java_ library README](https://github.com/web-eid/web-eid-authtoken-validation-java/blob/main/README.md). There are instructions for configuring the nonce generator, trusted certificate authority certificates, authentication token validator, Spring Security authentication integration and REST endpoints. The corresponding Java code is in the `src/main/java/org/webeid/example/{config,security,web/rest}` directories.

A similar overview of JavaScript and HTML code changes required for authentication and digital signing with Web eID is available in the [web-eid.js library README](https://github.com/web-eid/web-eid.js/blob/main/README.md). The corresponding JavaScript and HTML code is in the `src/resources/{static,templates}` directories.

### Integration with DigiDoc4j components

Java code examples that show how to create and sign data containers that hold signed file objects and digital signatures is available in the [DigiDoc4j wiki](https://github.com/open-eid/digidoc4j/wiki/Examples-of-using-it). Further information and links to the API documentation is available in the project [README](https://github.com/open-eid/digidoc4j/blob/master/README.md). The corresponding Java code is in the `src/main/java/org/webeid/example/{service,web/rest}` directories.

#### Using the Certificates' _Authority Information Access_ (AIA) extension in DigiDoc4j

In the `SigningService` constructor we have configured DigiDoc4j to use the AIA extension that contains the certificates’ OCSP service location with `signingConfiguration.setPreferAiaOcsp(true)`. Note that there may be limitations to using AIA URLs during signing as the services behind these URLs provide different security and SLA guarantees than dedicated OCSP services, so you should consider using a dedicated OCSP service instead. See the instructions in DigiDoc4j documentation and also the [corresponding section in _web-eid-authtoken-validation-java_ README](https://github.com/web-eid/web-eid-authtoken-validation-java/blob/main/README.md#certificates-authority-information-access-aia-extension).

#### Using DigiDoc4j in test mode with the `dev` profile

When using DigiDoc4j in test mode with a test eID card, you need to upload the corresponding authentication and signing certificates to the _demo.sk.ee_ OCSP responder database at https://demo.sk.ee/upload_cert/index.php, according to instructions on the webpage. You can choose the certificate status during upload, so you can also test bad statuses.

#### Using DigiDoc4j in production mode with the `prod` profile

When using DigiDoc4j in production mode, you need access to a paid timestamping service by registering your server's IP address with the service provider. Besides that, you may want to use a paid OCSP service for legal and reliability reasons as described above.

### Stateful and stateless authentication

In the current example we use the classical stateful Spring Security session cookie-based authentication mechanism, where a cookie that contains the user session ID is set during successful login and session data is stored at sever side. Cookie-based authentication must be protected against cross-site request forgery (CSRF) attacks and extra measures must be taken to secure the cookies by serving them only over HTTPS and setting the _HttpOnly_, _Secure_ and _SameSite_ attributes.

A common alternative to stateful authentication is stateless authentication with JSON Web Tokens (JWT) or secure cookie sessions where the session data resides at client side browser and is either signed or encrypted. Secure cookie sessions are described in [RFC 6896](https://datatracker.ietf.org/doc/html/rfc6896) and in the following [article about secure cookie-based Spring Security sessions](https://www.innoq.com/en/blog/cookie-based-spring-security-session/). Usage of both an anonymous session and a cache is required to store the challenge nonce and the time it was issued before the user is authenticated. The anonymous session must be used for protection against [forged login attacks](https://en.wikipedia.org/wiki/Cross-site_request_forgery#Forging_login_requests) by guaranteeing that the authentication token is received from the same browser to which the corresponding challenge nonce was issued. The cache must be used for protection against replay attacks by guaranteeing that each authentication token can be used exactly once.

### Assuring that the signing and authentication certificate subjects match

It is usually required to verify that the signing certificate subject matches the authentication certificate subject by assuring that both ID codes match. This check is implemented at the beginning of the `SigningService.prepareContainer()` method.

## HTTPS support

There are two ways of adding HTTPS support to a Spring Boot application:

1. enable HTTPS support directly in the bundled Tomcat server by configuring
   the `server.ssl.*` properties,

2. use a reverse proxy server that handles TLS termination and communicates
   with the Spring Boot application over a local HTTP socket.

The first approach is straightforward and documented in the [official documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#howto.webserver.configure-ssl).

The second approach, running behind a reverse proxy, is common in enterprise
deployments and the configuration is more complex. We assume this setup in the
current project and document it in this section to facilitate production deployment.

Enabling HTTPS support when running behind a reverse proxy server requires
configuration of both the proxy server and the Spring Boot application.

The proxy server must pass the `Host:` line from the incoming request to the
proxied application and set the `X-Forwarded-*` headers to inform the
application that it runs behind a reverse proxy. Here is example configuration
for the Apache web server:

    <Location />
        ProxyPreserveHost On
        ProxyPass http://localhost:8380/
        ProxyPassReverse http://localhost:8380/
        RequestHeader set X-Forwarded-Proto https
        RequestHeader set X-Forwarded-Port 443
    </Location>

The Spring Boot application turns on proxied HTTPS support in the bundled
Tomcat web server automatically if it detects the presence of the
`X-Forwarded-*` headers and the following settings are configured in
`application.properties`:

    server.forward-headers-strategy=native
    server.tomcat.remote-ip-header=x-forwarded-for
    server.tomcat.protocol-header=x-forwarded-proto

These settings are already enabled in the main configuration file `application.yaml`. See chapter
[9.3.12](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#howto-use-behind-a-proxy-server)
and
[9.14.3](https://docs.spring.io/spring-boot/docs/2.2.5.RELEASE/reference/htmlsingle/#howto-enable-https)
in the official documentation for further details.

### How to verify that HTTPS is configured properly

When HTTPS support is configured properly, all responses will have the HTTP
Strict Transport Security (HSTS) header and the `JSESSIONID` session cookie has the
`Secure` attribute set.

## Deployment

A Docker Compose configuration file `docker-compose.yml` is available in the root of the project for packaging the application in a Docker image so that it can be deployed with a container enginge.

Build the Docker image with [Jib](https://github.com/GoogleContainerTools/jib) as follows:

```sh
./mvnw -Pprod package com.google.cloud.tools:jib-maven-plugin:dockerBuild
```

and deploy with Docker Compose as follows:

```sh
docker-compose up -d
```

The application will run in production mode with `prod` profile settings.

## Frequently asked questions

### Why do I sometimes get the `403 Forbidden` response during authentication?

This is most likely caused by an expired CSRF token. Please refresh the page and try again.

### Why do I get the `"Access denied to TSP service"` error?

This error means that access was denied to the timestamping service. You are running in production mode with the `prod` profile and need to register for paid access to the timestamping service as described in section [_Using DigiDoc4j in production mode with the `prod` profile_](#using-digidoc4j-in-production-mode-with-the-prod-profile) above.

### Why can I not use my test ID card?

When running the application with the `dev` profile in test mode, you need to upload the corresponding authentication and signing certificates to the _demo.sk.ee_ OCSP responder database at <https://demo.sk.ee/upload_cert/index.php>, according to instructions on the webpage.

### Why do I get the `401 Unauthorized "Authentication failed: Web eID token validation failed"` response during authentication?

One possible reason is that you are using the test ID card on a site that is running in production mode or, vice-versa, a real ID card on a site that is running in test mode; or any other ID card whose certificate authority has not been added to the list of trusted certificate authorities. There will be a `CertificateNotTrustedException` in the logs in this case.

