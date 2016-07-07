# Contacts
The contacts repository consists of a ReST API that stubs out functionality for Auditing, Logging, and Searchlight services.

# Running
```
$ node app.js
```

**_NOTE:_** The repository is configured to run within the Docker container.  
If running locally the environment config file (e.g. config/default.json) will need to be modified to point to the location of your certs.

# Building Docker Image
1. Build Image

```
$ docker build -t docker.lab24.co/inabox/contacts . 
```
2. (If needed) Save image as a tar archive

```
$ docker save -o contacts.tar docker.lab24.co/inabox/contacts
```

# Running Docker Image
1. Load Docker Image into container
```
$ docker load -i contacts.tar
```
2. Run Mongo container (if not already running)
```
$ docker run -d --name inabox_mongodb mongo
```
3. Run Docker container
```
$ docker run -d --name contacts --link inabox_mongodb:mongo -v /home/user/.easycert/CAs/MyCa/servers/localhost:/testcerts -p 9443:9443 docker.lab24.co/inabox/contacts
```

A few important things need to happen when running the container.
* Map the directory location of your server key and crt pem files to the /testcerts directory within the container ( -v /path/to/certdirectory:/testcerts )
* Map the port in the container (9443) to a local port ( -p <local port>:9443)
* Link to the mongodb container which persists user information

# Settings when running locally
Default settings are in config/default.json.  To update for an environment, do either of the following:
- Adjust in an environment.json file (ala production.json), and run as NODE_ENV=[environment] npm start
- Apply a NODE_CONFIG environmental settings with json
``NODE_CONFIG={"Port": 9883}``

# Endpoints
## Searchlight
``GET /searchlight/[userid]?format=json``
  * Users to test with include foo, bar, baz, and jjdoe

e.g. ``https://dockervm:9443/searchlight/jjdoe?format=json``

``GET /searchlight/list=[usernames-commaseparated]?format=json``

e.g. ``https://dockervm:9443/searchlight/list=jjdoe,foo,bar?format=json``

``GET /searchlight?format=json``
  * Currently pulls some information from the client certificate and sparsely populates a response

---
## Auditing

Base URL: ``https://{host}/utm/jax-rs/{version}/utm``

The version url parameter is ignored so anything you put there will not be validated

``PUT /audit``

#### Required JSON keys
_All Requests_
* dn
* systemName
* userQueryIntent
* userSelectedAuthorities
* eventDate

_Parent Requests_
* justification
* requiresPostQueryReview
* missionId
* classification
* queryStartDate
* queryEndDate
* uspFlag
* spcma

_Child Requests_
* parentId

---
## Logging

``GET /log/status``

* Returns the status of the system.  Currently always returns up.

``POST /log``

##### Required Payload Parameters

* action
* project
* userDN
* eventTime

**_NOTE:_** action parameter value must be one of the following:
* createdBookmark
* deletedBookmark
* export
* feedback
* follow
* login
* searchedMultimedia
* searchedReports
* share
* subscribe
* subscriptiondelivery
* subscriptionSent
* subscriptionView
* unfollow
* unsubscribe
* viewedGSH
* viewedMultimedia
* viewedReport
* viewHistory
* visited
