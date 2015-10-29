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
	* Reason: Jetty has less footprint (~ 1.4 MiB, ~ 1.6 MiB with websocket implementation) than Tomcat (~ 2.9 MiB, 3.1 MiB with websocket implementation)
1. JSON library
	* Variants: Jackson, GSON, JSON.simple, JSONP
	* Selected: GSON
	* Reason: In some tests (see [1](http://blog.takipi.com/the-ultimate-json-library-json-simple-vs-gson-vs-jackson-vs-json/) and [2](http://ruedigermoeller.github.io/fast-serialization/json_bench.html))
	GSON have less or comparable performance than Jackson, but its have very small footprint (~ 200 KiB) than Jackson (1.4 MiB).
	If this project will need more performance in JSON processing, we can migrate to Jackson in few lines of code
1. Database (select only from in-memory pure Java database)
	* Variants: H2, HSQLDB, Apache Derby
	* Selected: H2
	* Reason: H2 database is more active project (HSQLDB released 4 version in last 2 years, Derby released 5 versions in last 2 years, H2 released more than 15 versions in last year) and it have less footprint than Derby.
