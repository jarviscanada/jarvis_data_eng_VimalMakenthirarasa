# Introduction
The SQL project was designed to enhance my knowledge of SQL so that I can interact with and manipulate data to solve complex problems. This project uses PostgreSQL for data storage, Docker for containerizing the PostgreSQL instance, and Git for version control. The database tables were created in the ddl.sql file, after which the sample data in the clubdata.sql file was loaded using Bash. The dataset is made up of various information on a set of facilities, which have members and booking details. This data was stored in 3 separate tables; cd.members, cd.bookings, and cd.facilities. I created custom queries to provide detailed information on various data points that are useful to the user. These queries include modifying data (INSERT, UPDATE, DELETE), basic data manipulation (SELECT, WHERE, FROM, UNION, comparing dates), joins (INNER, OUTER), aggregation, and string formatting.

# SQL Quries

###### Table Setup (DDL)
```sql
INSERT into cd.facilities 
	(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
	VALUES (9, 'Spa', 20, 30, 100000, 800);
```

###### Question 1: Insert some data into a table

```sql
SELECT *
FROM cd.members
```

###### Question 2: Insert calculated data into a table

```sql
INSERT into cd.facilities 
	(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
	SELECT (SELECT max(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800;
```

###### Question 3: Update some existing data

```sql
UPDATE cd.facilities
	SET initialoutlay = 10000
	WHERE name = 'Tennis Court 2';
```

###### Question 4: Update a row based on the contents of another row

```sql
UPDATE cd.facilities
	SET 
		membercost = (SELECT membercost FROM cd.facilities 
					  WHERE name = 'Tennis Court 1') * 1.1,
		guestcost = (SELECT guestcost FROM cd.facilities 
					  WHERE name = 'Tennis Court 1') * 1.1			  
	WHERE name = 'Tennis Court 2';
```

###### Question 5: Delete all bookings

```sql
DELETE from cd.bookings;
```

###### Question 6: Delete a member from the cd.members table

```sql
DELETE from cd.members 
	WHERE memid = 37;
```

###### Question 7: Control which rows are retrieved

```sql
SELECT facid, name, membercost, monthlymaintenance 
    FROM cd.facilities
	WHERE 
		membercost > 0 AND
		membercost < (monthlymaintenance / 50); 
```

###### Question 8: Basic string searches

```sql
SELECT * 
    FROM cd.facilities
	WHERE name LIKE '%Tennis%';
```

###### Question 9: Matching against multiple possible values

```sql
SELECT * 
    FROM cd.facilities 
	WHERE facid in (1,5);
```

###### Question 10: Working with dates

```sql
SELECT memid, surname, firstname, joindate 
	FROM cd.members
	WHERE joindate > '2012-09-01';
```

###### Question 11: Combining results from multiple queries

```sql
SELECT surname
	FROM cd.members
UNION
SELECT name 
	FROM cd.facilities;
```

###### Question 12: Retrieve the start times of members' bookings

```sql
SELECT starttime
	FROM cd.bookings
		LEFT JOIN cd.members ON cd.bookings.memid = cd.members.memid
	WHERE
		cd.members.firstname = 'David' AND
		cd.members.surname = 'Farrell';
```

###### Question 13: Work out the start times of bookings for tennis courts

```sql
SELECT cd.bookings.starttime, cd.facilities.name
	FROM cd.bookings
		INNER JOIN cd.facilities ON cd.bookings.facid = cd.facilities.facid
	WHERE
		cd.facilities.name in ('Tennis Court 1', 'Tennis Court 2') AND
		cd.bookings.starttime >= '2012-09-21' AND
		cd.bookings.starttime < '2012-09-22'
ORDER BY cd.bookings.starttime;
```

###### Question 14: Produce a list of all members, along with their recommender

```sql
SELECT mems.firstname AS memfname, mems.surname AS memsname, recs.firstname AS recfname, recs.surname AS recsname
	FROM 
		cd.members mems
		LEFT OUTER JOIN cd.members recs
			ON recs.memid = mems.recommendedby
ORDER BY memsname, memfname;    
```

###### Question 15: Produce a list of all members who have recommended another member

```sql
SELECT DISTINCT recs.firstname AS firstname, recs.surname AS surname
	FROM 
		cd.members mems
		INNER JOIN cd.members recs
			ON recs.memid = mems.recommendedby
ORDER BY surname, firstname; 
```

###### Question 16: Produce a list of all members, along with their recommender, using no joins.

```sql
SELECT DISTINCT CONCAT(mems.firstname, ' ', mems.surname) as member,
	(SELECT CONCAT(recs.firstname, ' ', recs.surname) as recommender
	 	FROM cd.members recs
	 	WHERE recs.memid = mems.recommendedBY
	 )
	 FROM
	 	cd.members mems
ORDER BY member;
```

###### Question 17: Count the number of recommendations each member makes

```sql
SELECT recommendedby, COUNT(*)
	FROM cd.members
	WHERE recommendedby IS NOT NULL
	GROUP BY recommendedby
ORDER BY recommendedby;
```

###### Question 18: List the total slots booked per facility

```sql
SELECT facid, sum(slots)
	FROM cd.bookings
	GROUP BY facid
ORDER BY facid;
```

###### Question 19: List the total slots booked per facility in a given month

```sql
SELECT facid, sum(slots)
	FROM cd.bookings
	WHERE
		starttime >= '2012-09-01'
		AND starttime < '2012-10-01'
	GROUP BY facid
ORDER BY sum(slots);
```
###### Question 20: List the total slots booked per facility per month

```sql
SELECT facid, EXTRACT(month FROM starttime) AS month, sum(slots)
	FROM cd.bookings
	WHERE EXTRACT(year FROM starttime) = 2012
	GROUP BY facid, month
ORDER BY facid, month;
```

###### Question 21: Find the count of members who have made at least one booking

```sql
SELECT COUNT(DISTINCT memid) 
	FROM cd.bookings;
```

###### Question 22: List each member's first booking after September 1st 2012

```sql
SELECT surname, firstname, cd.members.memid, min(cd.bookings.starttime) AS starttimes
	FROM cd.bookings
	INNER JOIN cd.members ON cd.members.memid = cd.bookings.memid
	WHERE starttime >= '2012-09-01'
	GROUP BY surname, firstname, cd.members.memid
ORDER BY cd.members.memid;
```

###### Question 23: Produce a list of member names, with each row containing the total member count

```sql
SELECT (SELECT COUNT(*) FROM cd.members) as count, firstname, surname
	FROM cd.members
ORDER BY joindate;
```

###### Question 24: Produce a numbered list of members

```sql
SELECT COUNT(*) OVER(ORDER BY joindate), firstname, surname 
	FROM cd.members
ORDER BY joindate;
```

###### Question 25: Output the facility id that has the highest number of slots booked, again

```sql
SELECT facid, total FROM (
  	SELECT facid, sum(slots) total, rank() OVER (ORDER BY sum(slots) desc) rank
  	FROM cd.bookings
  	GROUP BY facid
  ) AS ranked
  WHERE rank = 1;
```

###### Question 26: Format the names of members

```sql
SELECT CONCAT(surname, ', ', firstname) as name from cd.members;
```

###### Question 27: Find telephone numbers with parentheses

```sql
SELECT memid, telephone 
	FROM cd.members
		WHERE telephone SIMILAR TO '%[()]%';
```

###### Question 28: Count the number of members whose surname starts with each letter of the alphabet

```sql
SELECT substr(cd.members.surname, 1,1) as letter, count(*) as count
	FROM cd.members
	GROUP BY letter
	ORDER BY letter;
```
