Berkeley Identity Management Suite (BIDMS)
==========================================

This repository contains the software for the *Berkeley Identity Management
Suite* (BIDMS).

The web applications that comprise BIDMS are [Spring
Boot](http://spring.io/projects/spring-boot) applications with a Java 11
baseline.

BIDMS source code is licensed under the [BSD two-clause license](LICENSE.txt).

## Quick-start: Running in developer mode

`./gradlew :bidms-boot:bootRun`

## Quick-start: Building a WAR file

`./gradlew :bidms-boot:war`

The WAR file ends up at
`./bidms-boot/build/lib/bidms-VERSION.war`.  This WAR file is
suitable for deployment to external application servers and this is the
recommended method for production deployments.

Spring Boot supports self-executable WAR files with an embedded application
server.  If this is your preference, you can do the following:<br/>
`./gradlew :bidms-boot:bootWar`

The WAR file ends up at
`./bidms-boot/build/lib/bidms-VERSION-boot.war` and this can
then be executed as a shell script.

## Quick-start: Generate aggregate Javadocs

`./gradlew :bidms-aggregate-javadocs:javadoc`
