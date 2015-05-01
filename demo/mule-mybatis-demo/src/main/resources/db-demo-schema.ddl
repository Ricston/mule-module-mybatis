DROP TABLE address;
DROP TABLE person;

CREATE TABLE person (
	id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	name varchar(255),
	surname varchar(255),
	age int
);

CREATE TABLE address (
	id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
	address varchar(255),
	personId int,

	CONSTRAINT address_person_fk FOREIGN KEY (personId) REFERENCES person(id)
);
