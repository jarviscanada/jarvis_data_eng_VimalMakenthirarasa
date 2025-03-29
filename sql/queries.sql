-- Modifying Data

-- Q1: https://pgexercises.com/questions/updates/insert.html
INSERT into cd.facilities 
	(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
	VALUES (9, 'Spa', 20, 30, 100000, 800);

-- Q2: https://pgexercises.com/questions/updates/insert3.html
INSERT into cd.facilities 
	(facid, name, membercost, guestcost, initialoutlay, monthlymaintenance)
	SELECT (SELECT max(facid) FROM cd.facilities) + 1, 'Spa', 20, 30, 100000, 800;

-- Q3: https://pgexercises.com/questions/updates/update.html
UPDATE cd.facilities
	SET initialoutlay = 10000
	WHERE name = 'Tennis Court 2';

-- Q4: https://pgexercises.com/questions/updates/updatecalculated.html
UPDATE cd.facilities
	SET 
		membercost = (SELECT membercost FROM cd.facilities 
					  WHERE name = 'Tennis Court 1') * 1.1,
		guestcost = (SELECT guestcost FROM cd.facilities 
					  WHERE name = 'Tennis Court 1') * 1.1			  
	WHERE name = 'Tennis Court 2';

-- Q5: https://pgexercises.com/questions/updates/delete.html
DELETE from cd.bookings;

-- Q6: https://pgexercises.com/questions/updates/deletewh.html
DELETE from cd.members 
	WHERE memid = 37;



-- Basics
-- Q1: https://pgexercises.com/questions/basic/where2.html
SELECT facid, name, membercost, monthlymaintenance 
    FROM cd.facilities
	WHERE 
		membercost > 0 AND
		membercost < (monthlymaintenance / 50); 

-- Q2: https://pgexercises.com/questions/basic/where3.html
SELECT * 
    FROM cd.facilities
	WHERE name LIKE '%Tennis%';

-- Q3: https://pgexercises.com/questions/basic/where4.html
SELECT * 
    FROM cd.facilities 
	WHERE facid in (1,5);

-- Q4: https://pgexercises.com/questions/basic/date.html
SELECT memid, surname, firstname, joindate 
	FROM cd.members
	WHERE joindate > '2012-09-01';

-- Q5: https://pgexercises.com/questions/basic/union.html
SELECT surname
	FROM cd.members
UNION
SELECT name 
	FROM cd.facilities;



-- Join

-- Q1: https://pgexercises.com/questions/joins/simplejoin.html
SELECT starttime
	FROM cd.bookings
		LEFT JOIN cd.members ON cd.bookings.memid = cd.members.memid
	WHERE
		cd.members.firstname = 'David' AND
		cd.members.surname = 'Farrell';


-- Q2: https://pgexercises.com/questions/joins/simplejoin2.html
SELECT cd.bookings.starttime, cd.facilities.name
	FROM cd.bookings
		INNER JOIN cd.facilities ON cd.bookings.facid = cd.facilities.facid
	WHERE
		cd.facilities.name in ('Tennis Court 1', 'Tennis Court 2') AND
		cd.bookings.starttime >= '2012-09-21' AND
		cd.bookings.starttime < '2012-09-22'
ORDER BY cd.bookings.starttime;

-- Q3: https://pgexercises.com/questions/joins/self2.html (three joins)
SELECT mems.firstname AS memfname, mems.surname AS memsname, recs.firstname AS recfname, recs.surname AS recsname
	FROM 
		cd.members mems
		LEFT OUTER JOIN cd.members recs
			ON recs.memid = mems.recommendedby
ORDER BY memsname, memfname;    

-- Q4: https://pgexercises.com/questions/joins/self.html three joins
SELECT DISTINCT recs.firstname AS firstname, recs.surname AS surname
	FROM 
		cd.members mems
		INNER JOIN cd.members recs
			ON recs.memid = mems.recommendedby
ORDER BY surname, firstname; 

-- Q5: https://pgexercises.com/questions/joins/sub.html (subquery and join)
SELECT DISTINCT CONCAT(mems.firstname, ' ', mems.surname) as member,
	(SELECT CONCAT(recs.firstname, ' ', recs.surname) as recommender
	 	FROM cd.members recs
	 	WHERE recs.memid = mems.recommendedBY
	 )
	 FROM
	 	cd.members mems
ORDER BY member;

-- Aggregation

-- Q1: easy - https://pgexercises.com/questions/aggregates/count3.html Group by order by
SELECT recommendedby, COUNT(*)
	FROM cd.members
	WHERE recommendedby IS NOT NULL
	GROUP BY recommendedby
ORDER BY recommendedby;

-- Q2: easy - https://pgexercises.com/questions/aggregates/fachours.html group by order by
SELECT facid, sum(slots)
	FROM cd.bookings
	GROUP BY facid
ORDER BY facid;

-- Q3: easy - https://pgexercises.com/questions/aggregates/fachoursbymonth.html group by with condition 
SELECT facid, sum(slots)
	FROM cd.bookings
	WHERE
		starttime >= '2012-09-01'
		AND starttime < '2012-10-01'
	GROUP BY facid
ORDER BY sum(slots);

-- Q4: easy - https://pgexercises.com/questions/aggregates/fachoursbymonth2.html group by multi col
SELECT facid, EXTRACT(month FROM starttime) AS month, sum(slots)
	FROM cd.bookings
	WHERE EXTRACT(year FROM starttime) = 2012
	GROUP BY facid, month
ORDER BY facid, month;

-- Q5: easy - https://pgexercises.com/questions/aggregates/members1.html count distinct
SELECT COUNT(DISTINCT memid) 
	FROM cd.bookings;

-- Q6: med - https://pgexercises.com/questions/aggregates/nbooking.html group by multiple cols, join
SELECT surname, firstname, cd.members.memid, min(cd.bookings.starttime) AS starttimes
	FROM cd.bookings
	INNER JOIN cd.members ON cd.members.memid = cd.bookings.memid
	WHERE starttime >= '2012-09-01'
	GROUP BY surname, firstname, cd.members.memid
ORDER BY cd.members.memid;

-- Q7: hard - https://pgexercises.com/questions/aggregates/countmembers.html window function
SELECT (SELECT COUNT(*) FROM cd.members) as count, firstname, surname
	FROM cd.members
ORDER BY joindate;

-- Q8: hard - https://pgexercises.com/questions/aggregates/nummembers.html window function
SELECT COUNT(*) OVER(ORDER BY joindate), firstname, surname 
	FROM cd.members
ORDER BY joindate;

-- Q9: hard - https://pgexercises.com/questions/aggregates/fachours4.html window function, subquery, group by
SELECT facid, total FROM (
  	SELECT facid, sum(slots) total, rank() OVER (ORDER BY sum(slots) desc) rank
  	FROM cd.bookings
  	GROUP BY facid
  ) AS ranked
  WHERE rank = 1;

-- String

-- Q1: easy - https://pgexercises.com/questions/string/concat.html format string
SELECT CONCAT(surname, ', ', firstname) as name from cd.members;

-- Q2: easy - https://pgexercises.com/questions/string/reg.html WHERE + string function
SELECT memid, telephone 
	FROM cd.members
		WHERE telephone SIMILAR TO '%[()]%';

-- Q3: easy - https://pgexercises.com/questions/string/substr.html group by, substr
SELECT substr(cd.members.surname, 1,1) as letter, count(*) as count
	FROM cd.members
	GROUP BY letter
	ORDER BY letter;