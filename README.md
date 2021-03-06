[![Build Status](https://travis-ci.org/valery1707/lunch-voting-system.svg)](https://travis-ci.org/valery1707/lunch-voting-system)
[![Build status](https://ci.appveyor.com/api/projects/status/f1iixe94tbk038c0/branch/master?svg=true)](https://ci.appveyor.com/project/valery1707/lunch-voting-system/branch/master)
[![Build status](https://codeship.com/projects/7d03b270-a8ac-0133-5371-2a9c037ce78e/status?branch=master)](https://codeship.com/projects/130683)
[![Build status](https://circleci.com/gh/valery1707/lunch-voting-system.svg?style=svg)](https://circleci.com/gh/valery1707/lunch-voting-system)
[![Build Status](https://drone.io/github.com/valery1707/lunch-voting-system/status.png)](https://drone.io/github.com/valery1707/lunch-voting-system/latest)
[![Build status](https://app.wercker.com/status/19ff06a2e2da0ce7e080f1ebf858f948/s/master)](https://app.wercker.com/project/bykey/19ff06a2e2da0ce7e080f1ebf858f948)

[![Coverage Status](https://coveralls.io/repos/valery1707/lunch-voting-system/badge.svg?branch=master&service=github)](https://coveralls.io/github/valery1707/lunch-voting-system?branch=master)
[![codecov.io](https://codecov.io/github/valery1707/lunch-voting-system/coverage.svg?branch=master)](https://codecov.io/github/valery1707/lunch-voting-system)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/86f3e1ba12934be6af1e39ca5e61c917)](https://www.codacy.com/app/valery1707/lunch-voting-system)
[![Dependency Status](https://www.versioneye.com/user/projects/5634dd1836d0ab0019001c05/badge.svg?style=flat)](https://www.versioneye.com/user/projects/5634dd1836d0ab0019001c05)

Simple service with JSON API using Hibernate/Spring/SpringMVC **without frontend**.

Voting system for deciding where to have lunch.

### System description:
* 2 types of users: admin and regular users
* Admin can input a restaurant and it's lunch menu of the day (2-5 items usually, just a dish name and price)
* Users can vote on which restaurant they want to have lunch at
* Only one vote counted per user
* If user votes again the same day:
	* If it is before 11:00 we assume that he changed his mind.
	* If it is after 11:00 then it is too late, vote can't be changed
* Each restaurant provides new menu each day.

### Explanation of technology choice:
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
	I decide use Jackson for correctly marshall entity objects with cycles.
1. Database (select only from in-memory pure Java database)
	* Variants: H2, HSQLDB, Apache Derby
	* Selected: H2
	* Reason: H2 database is more active project (HSQLDB released 4 version in last 2 years, Derby released 5 versions in last 2 years, H2 released more than 15 versions in last year) and it have less footprint than Derby.
1. Database migration tool
	* Variants: Flyway, Liquibase
	* Selected: Flyway
	* Reason: Flyway support SQL and Java migrations, that allow to realise any migration what can be needed. Liquibase generate SQL from XML form, but this form very complex.
	SQL migration is simple and good decision.

### Useful gradle commands
1. Run project from console: `./gradlew bootRun`
1. Build executable jar: `./gradlew build`
1. Make code coverage report: `./gradlew clean build jacocoTestReport`

### CURL commands example
System after start have some build-in users:
1. login: `admin`, password: `admin`, role: `admin`
1. login: `user_1`, password: `password one`, role: `user`
1. login: `user_2`, password: `password two`, role: `user`

##### Some good things
1. Query first page of restaurants (default size: 20):
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant
```
1. Query second page of restaurants (size: 1):
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  'http://localhost:8080/api/restaurant?size=1&page=1'
```
1. Query first page of restaurants with filter by name (size: 1):
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  'http://localhost:8080/api/restaurant?filter=name;~;heLL'
```
1. Query first page of restaurants with two filters by name (size: 1):
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  'http://localhost:8080/api/restaurant?filter=name;~;heLL&filter=name;~;bar'
```
1. Query first page of restaurants with complex filter by name:
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  -X GET \
  -d '{
    "or": [
      {"field": "name", "operation": "~", "value": "heLL"},
      {"field": "name", "operation": "~", "value": "bar"}
    ]
  }' http://localhost:8080/api/restaurant
```
1. Get information about one Restaurant by id:
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/78a9353f-7e08-40a6-ad70-af2664a37a36 
```
1. Create new restaurant with some dishes:
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PUT \
  -d '{"name": "Created from CURL",
    "dishes": [
      {"name": "Curl dish 1", "price": 1}, 
      {"name": "Curl dish 2", "price": 2.5}
    ]
  }' http://localhost:8080/api/restaurant
```
1. Update restaurant: change only name
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PATCH \
  -d '{"name": "Updated from curl"}' \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
```
1. Update restaurant: change one dish price
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PATCH \
  -d '{
    "dishes": [
      {"id": "6b2edfa5-0894-4fca-aed0-511171f650f5", "price": 500}
    ]
  }' \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
```
1. Update restaurant: add new dish
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PATCH \
  -d '{
    "dishes": [
      {"name": "New dish", "price": 600}
    ]
  }' \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
```
1. Get vote score for current date:
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/vote
```
1. Get vote score for selected date:
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/vote?date=2015-10-31
```
1. Vote for restaurant:
```bash
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  -X POST \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86/vote
```
1. Vote for restaurant at specified dateTime (for test purpose):
```bash
curl -i -g \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  -X POST \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86/vote?datetime=2015-10-21T10:30:21.000Z[GMT]
curl -i \
  -H "Accept: application/json" \
  -u "user_1:password one" \
  http://localhost:8080/api/restaurant/vote?date=2015-10-21
```
##### Some evil things
1. Create restaurant with empty name
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PUT \
  -d '{"name": "",
    "dishes": [
    ]
  }' http://localhost:8080/api/restaurant
```
1. Add new dish without price to exists restaurant
```bash
curl -i \
  -H "Content-Type: application/json" \
  -u "admin:admin" \
  -X PATCH \
  -d '{
    "dishes": [
      {"name": "New dish"}
    ]
  }' \
  http://localhost:8080/api/restaurant/60d4f411-4cff-4f60-b392-46bed14c5f86
```

[![Analytics](https://ga-beacon.appspot.com/UA-70691593-1/lunch-voting-system/README.md)](https://github.com/igrigorik/ga-beacon)
