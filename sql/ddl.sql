-- Create the cd schema
CREATE SCHEMA IF NOT EXISTS cd;

-- Create cd.members table
CREATE TABLE IF NOT EXISTS cd.members (
    memid SERIAL PRIMARY KEY,
    surname VARCHAR(200),
    firstname VARCHAR(200),
    address VARCHAR(300),
    zipcode INTEGER,
    telephone VARCHAR(20),
    recommendedby INTEGER,
    joindate TIMESTAMP,
    FOREIGN KEY (recommendedby) REFERENCES cd.members(memid)
);

-- Create cd.facilities table
CREATE TABLE IF NOT EXISTS cd.facilities (
    facid SERIAL PRIMARY KEY,
    name VARCHAR(100),
    membercost NUMERIC,
    guestcost NUMERIC,
    initialoutlay NUMERIC,
    monthlymaintenance NUMERIC
);

-- Create cd.bookings table
CREATE TABLE IF NOT EXISTS cd.bookings (
    bookid SERIAL PRIMARY KEY,
    facid INTEGER,
    memid INTEGER,
    starttime TIMESTAMP,
    slots INTEGER,
    FOREIGN KEY (facid) REFERENCES cd.facilities(facid),
    FOREIGN KEY (memid) REFERENCES cd.members(memid)
);