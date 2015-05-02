DEMO
=======

Import the demo as an Anypoint Studio project after successfully installing the plugin. The project will create an in-memory Derby database to support the data. MyBatis will be using this database to insert and retrieve data.

The demo exposes 2 HTTP calls:

  - POST http://localhost:8081/insert
  - GET http://localhost:8081/select

The insert call accepts a JSON as the following example:

```json
{
  "name" : "Joe",
  "surname" : "Bloggs",
  "age" : 30,
	"addresses" : [
		{
			"address" : "Some address"
		}
	]
}
```

This will insert the above Person into the database, and return back the same object with the automatically generated keys populated. The insert will also perform the inserts in a transaction since first the person object has to be inserted, then the list of address has to be inserted using the newly generated key for person as the foreign key for the address.

The select call will select all the persons from the database and return back a list of JSON objects.

