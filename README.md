# Minimalist Embedded J2EE Web Application (MEJWA)

The goals of this project are:

 * To provide a J2EE Web Application which can act as a base for starting other J2EE projects
 * The components included within the project can be easily replaced or removed if not needed
 * Run without requiring separately installed Servlet containers such as Tomcat or Wildfly.
 * Be able to run using separately installed Servlet containers anyway.
 * Have a built-in database.
 * Have an interface for both REST clients and browsers.


# Prerequisites

 * Maven 3.x+
 * Git
 * Java 8+
 * If not running embedded, one of the following:
   * Tomcat 8+
   * WildFly 8.1+

# Using MEJWA

MEJWA can be run as a standalone app or as a war that runs within a servlet container such as Tomcat or WildFly. It can be run from an IDE-maintained servlet container when coding and debugging.

To build the project:

  mvn clean install

## Running standalone

Once built, the war can be found in the war/target/ directory. Run it:

  cd war/target
  java -jar embeddedrest.war

It will create a new directory in the user's home directory named ".embeddedrest", and then run from there. It will then spawn a new java instance and run from that location. It can be visited by going to "http://localhost:8080/notes", once finished, in order to fully shutdown:

  ps aux | embeddedrest
  kill embeddedrest_pid

## Running in Servlet Container

If choosing to run it in a servlet container instead of standalone, it depends on which servlet container you use. In any case, a directory named ".embeddedrest" will be created in your user's home folder. Unlike the embedded version, this will not extract out the war subdirectory which allows it to run standalone.

After following the steps below to start the servlet, usually it can be visited by going to "http://localhost:8080/embeddedrest/notes"

### Tomcat or anything that isn't WildFly

Take war/target/embeddedrest.war and drop it into Tomcat's webapps/ directory, and start tomcat.

### Wildfly only

Wildfly already contains many of the components that MEJWA includes for itself (in fact, much of the project is based on Wildfly, like undertow and the CDI implementation). Because of this, we need to NOT include the various components which would otherwise cause conflicts and make our project fail to run. To do this, we'll build using maven with a different profile called "without-embedded". From the project base directory:

  mvn clean install -P without-embedded

Once built, take the resulting war from war/target/embeddedrest.war and drop it into standalone/deployments directory, and start WildFly.

## Running in IDE

Having the application run via an IDE like Eclipse, Netbeans, or IntelliJ can make development quicker if the changes are applied immediately upon saving your edited files. Since this is like any other war file, you can run it as you would for normal war files. If using Tomcat, follow the instructions above for Tomcat, and if using WildFly, make sure you're using the "without-embedded" profile. Your favorite IDE should have instructions on how to do this.

# REST calls

The REST calls are made to the same endpoints you visited earlier, only we change the mediatypes that we accept. For example, if you were running standalone, you would vist "http://localhost:8080/notes" with your browser. We'll visit the same location, but via REST:

curl -X GET -h "Accept: application/x-embeddedrest-v1+json" http://localhost:8080/notes

You'll notice that it didn't return the HTML content, but JSON instead. We *could* get the webpage form if we wanted:

curl -X GET -h "Accept: text/html" http://localhost:8080/notes

(or, we could leave off the header entirely and produce the same result.)

For more REST endpoints, look at docs/endpoints.md (it will also explain reasoning and other information too.)

