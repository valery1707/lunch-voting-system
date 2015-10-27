Simple service with JSON API using Hibernate/Spring/SpringMVC **without frontend**.

Voting system for deciding where to have lunch.

System description:
* 2 types of users: admin and regular users
* Admin can input a restorant and it's lunch menu of the day (2-5 items usually, just a dish name and price)
* Users can vote on which restaurant they want to have lunch at
* Only one vote counted per user
* If user votes again the same day:
	* If it is before 11:00 we asume that he changed his mind.
	* If it is after 11:00 then it is too late, vote can't be changed
* Each restorant provides new menu each day.
