## Prerequisites

* You need to know how to start a Confluence plugin,
* Use Java 11
* Use Maven 3.8.6 (for example),
* Use Atlassian's Maven repositories configured in Maven
  (or use the Atlassian SDK instead of Maven).

## How to show the bug

* Start Confluence[1]
* Install this plugin
* Install Scaffolding 8.29.10
* Create a page with Scaffolding:
  * Create a page,
  * Put the macro "Text Data" (by Scaffolding) on it,
  * In "Field Name", type "text-1",
  * Save the page,
  * Click "Edit Contents" to add contents to the Scaffolding macro,
* While you are viewing the page, click the "CLICK HERE TO SHOW THE BUG" link in the top bar,
* A white page with the (correct) rendering appears,
* Bug: Look at the logs, there is a large stacktrace in it.

Why is this stacktrace considered a bug?

* Other plugins render pages many times with Scaffolding on it,
* Scaffolding will then fill the logs and provoke the disk to fill up and crash Confluence,
* There is no way to prevent Scaffolding from displaying this stacktrace,

What is happening technically?

* The rendering is called in a separate thread, so there is no current "HttpRequest" or
  currently-logged-in user,
* Scaffolding seems to call a method from ServletActionContextCompatManager.getRequest(),
  in an old version, which triggers an exception because it is not in the context of
  an HttpRequest,
* Then, instead of ignoring the absence of "HttpRequest" as should be, Scaffolding seems to
  log the exception without any way for anyone to prevent it.

## Video demonstration

See [demo-how-to-run.mp4](demo-how-to-run.mp4)

## Tracking

This is tracked by https://appfire.atlassian.net/servicedesk/customer/portal/11/SUPPORT-174435

## [1] How to start Confluence?

* Go to the folder of the project where the pom.xml is,
* Ensure you have Java 11: `java -version` will certainly tell you,
* Run `mvn amps:debug`,
* Wait for Confluence 8.5.4 to start,
* The login is admin/admin,
* In a separate window, ensure you have Java 11, then recompile the plugin
  and it will install automatically: `mvn install`.
