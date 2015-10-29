Simple service with JSON API using Hibernate/Spring/SpringMVC **without frontend**.

Voting system for deciding where to have lunch.

System description:
* 2 types of users: admin and regular users
* Admin can input a restaurant and it's lunch menu of the day (2-5 items usually, just a dish name and price)
* Users can vote on which restaurant they want to have lunch at
* Only one vote counted per user
* If user votes again the same day:
	* If it is before 11:00 we assume that he changed his mind.
	* If it is after 11:00 then it is too late, vote can't be changed
* Each restaurant provides new menu each day.

Explanation of technology choice:
1. Build system
	* Alternatives: Maven, Gradle, ANT, SBT
	* Selected: Gradle
	* Reason: Gradle is declarative build system for Java project, that designed to build not only Java projects, and have many possibilities for change behavior of build process
1. Embedded servlet container
	* Alternatives: Tomcat embedded, Jetty
	* Selected: Jetty
	* Reason: Jetty has less footprint (~ 1.4 MiB, ~ 1.6 MiB with websocket implementation) than Tomcat (~ 3.1 MiB, 3.3 MiB with websocket implementation)
