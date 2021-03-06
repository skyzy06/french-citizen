# French Citizen API

## Definition

This is API that allows to register, edit and delete adult French citizens.
The database is embedded, to provide a turnkey API. It's a H2 database with a persistence file store at `~/fcdb.mv.db`.

The information about a citizen are the following:
* **username (required)**
* **birthdate (required)**
* **country of residence (required)**
* **password (required)**
* firstname
* lastname
* phone number
* gender

## How to use it
*Precondition: java 11 and maven 3 available as environment variables*

All the commands have to be launch for the root folder of the project.
### Run from the sources
``mvn spring-boot:run``
### Run all the tests
``mvn test`` or ``mvn verify``
### Package the project
``mvn package``
It will produce a `.jar` in the *target* folder. Run the following command to run the API:
``java -jar target/french-citizen-{{version}}.jar``
### Clean compiled code and packages
``mvn clean``

## Properties
The [application.properties](src/main/resources/application.properties) file contains the default value for some properties of the API.
They can be overridden by environment variables:
* *encryption.password.key* is the password encryption key. Set ``ENCRYPTION_PASSWORD_KEY`` in the environment variables to override it.
* *token.validity.duration* is the duration while the token is valid. Set ``TOKEN_VALIDITY_DURATION`` in the environment variables to override it.
* *token.encryption.secret* is the secret used to encrypt the token. Set ``TOKEN_ENCRYPTION_SECRET`` in the environment variables to override it.

## Specifications
### Input format
* *birthdate format:* YYYY-MM-DD, only adult(18+ years old) can be registered
* *password format:* only alpha-numeric, minimum 8 characters, at least: 1 majuscule, 1 minuscule, 1 number
* *gender format:* only M (male), F (female) or U (unknown) are accepted when provided
* *country of residence:* only French resident can be registered
* *username:* cannot be longer than 32 characters

### Not updatable
The following information about a citizen cannot be change after its registration:
* birthdate
* country of residence

## API
Here are some template of requests. A more detailed collection can be found here: [Postman collection](postman/french-citizen.postman_collection.json). You can also try it with its swagger at http://localhost:8080/swager-ui/ when the server is running.

Only the creation and retrieve token endpoints are open to anyone, otherwise you need a token that match with the requested citizen.
### Register / creation
```
POST {{host}}/citizen
Content-Type: application/json

{
    "username": "{{username}}",
    "residenceCountry": "{{residenceCountry}}",
    "birthdate": "{{birthdate}}",
    "password": "{{password}}",
    "gender": "{{gender}}"
}
```
### Authentication/Retrieve token
```
POST {{host}}/citizen/token
Content-Type: application/json

{
    "username": "{{username}}",
    "password": "{{password}}"
}
```
### Search / find
```
GET {{host}}/citizen/{{citizen_id}}
Authorization: Bearer {{token_bearer}}
```
### Update citizen information
```
PATCH {{host}}/citizen/{{citizen_id}}
Authorization: Bearer {{token_bearer}}
Content-Type: application/json

{
    "username": "{{username}}",
    "firstname": "{{firstname}}",
    "gender": "{{gender}}"
}
```
### Update citizen password
```
PATCH {{host}}/citizen/{{citizen_id}}/password
Authorization: Bearer {{token_bearer}}
Content-Type: application/json

{
    "password": "{{new_password}}",
    "oldPassword": "{{old_password}}"
}
```
### Delete
```
DELETE {{host}}/citizen/{{citizen_id}}
Authorization: Bearer {{token_bearer}}
```