# SiMian TouPée

SiMian TouPée allows you to fully test the email functionality of your application without sending real email messages. All you need to do is set up your own instance of SiMian TouPée and point your application at it.

It is a self-contained jar (war also available); find a machine with Java 1.8+
and run it:

    java -jar simian-toupee.jar

Once it is running, visit the main page at http://localhost:8080 to complete
the rest of the configuration.

# Building

## Prerequisites

 * Maven 3.3+
 * Git
 * Java 8+
 * If not running embedded, one of the following:
   * Tomcat 8+
   * WildFly 10+

## Build

To build the project

    mvn clean install

