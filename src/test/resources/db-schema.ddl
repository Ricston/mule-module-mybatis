DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS address;

CREATE TABLE person (
	id int auto_increment PRIMARY KEY,
	name varchar(255),
	surname varchar(255),
	age int
);

CREATE TABLE address (
	id int auto_increment PRIMARY KEY,
	address varchar(255),
	personId int
);

ALTER TABLE  address
 ADD CONSTRAINT address_person_fk 
 FOREIGN KEY (personId) 
 REFERENCES person (id);