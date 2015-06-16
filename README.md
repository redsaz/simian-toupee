# Minimalist Embedded J2EE Web Application (MEJWA)

The goals of this project are:

 * To provide a J2EE Web Application which can act as a base for starting other J2EE projects
 * The components included within the project can be easily replaced or removed if not needed
 * Run without requiring separately installed Servlet containers such as Tomcat or Wildfly.
 * Be able to run using separately installed Servlet containers anyway.
 * Have a built-in database.
 * Have an interface for both REST clients and browsers.


# Using MEJWA

Right now, MEJWA is configured to be run from WildFly servlet container. It is able to be run as a standalone app, but no without first extracting the war yourself and then specifying the huge classpath and the main class, which is not optimal.

