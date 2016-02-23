# Minimalist Embedded J2EE Web Application (MEJWA)

The goals of this project are:

 * To provide a J2EE Web Application which can act as a base for starting
   other J2EE projects
 * The components included within the project can be easily replaced or
   removed if not needed
 * Be run standalone or in a Servlet container such as Tomcat or Wildfly.
 * Have a built-in database.
 * Have an interface for both REST clients and browsers.


# Prerequisites

 * Maven 3.3+
 * Git
 * Java 8+
 * If not running embedded, one of the following:
   * Tomcat 8+
   * WildFly 10+

# Using deciservice

deciservice can be run as a standalone app or as a war that runs within a
servlet container such as Tomcat or WildFly. It can be run from an
IDE-maintained servlet container when coding and debugging.

## Build and run standalone

To build the project and run it standalone:

    mvn clean install

Once built, the standalone jar can be found in the deciservice/target/
directory. Run it:

    cd deciservice/target
    java -jar deciservice-swarm.jar

It will create a new directory in the working directory labelled "deciservice"
and will contain the database. Once the application has initialized, you can
visit the app with your browser by going to http://localhost:8080/notes .
Once finished, close the app with Ctrl-C in the terminal.

## Build and run in WildFly servlet container

This is the same process as building the standalone:

    mvn clean install

Now, to run it in a WildFly servlet container, copy the
deciservice/target/deciservice.war into your WildFly standalone/deployments
directory, and run the WildFly bin/standalone script.

## Build and run in Tomcat (or other) servlet container

Use the "war" profile:

    mvn clean install -Pwar

Once built, copy the resulting deciservice/target/deciservice.war into your
Tomcat webapps directory and start Tomcat.

## Running in IDE

Having the application run via an IDE like Eclipse, Netbeans, or IntelliJ
can make development quicker if the changes are applied immediately upon
saving your edited files. Since this is like any other war file, you can
run it as you would for normal war files. If using Tomcat, follow the
instructions above for Tomcat, and if using WildFly, make sure you're using
the "without-embedded" profile. Your favorite IDE should have instructions
on how to do this.


# REST calls

The REST calls are made to the same endpoints you visited earlier, only we
change the mediatypes that we accept. For example, if you were running
standalone, you would vist "http://localhost:8080/notes" with your browser.
We'll visit the same location, but via REST:

    curl -X GET -h "Accept: application/x-deciservice-v1-notes+json" http://localhost:8080/notes

You'll notice that it didn't return the HTML content, but JSON instead. We
*could* get the webpage form if we wanted:

curl -X GET -h "Accept: text/html" http://localhost:8080/notes

(or, we could leave off the header entirely and produce the same result.)

For more REST endpoints, look at docs/endpoints.md (it will also explain
reasoning and other information too.)


# Using for different projects

This project is designed to be a prototype for other projects. You should be
able to change this project and add to it for any simple web app project.
Do the following:

 * Search for "deciservice" and change the results to the name of your project
 * Change the com.redsaz.deciservice packagename to your own package name
 * Anything involving notes (Note, NotesResource, etc) are for example purposes
   only and can be deleted or transformed
 * HSQLDB, JOOQ, and Bootstrap are easily replaced

