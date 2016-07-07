# AuthService
This component provides a RESTful service to mimic Tier7 v2 for the authorization service.  Other tiers are not supported at this time.  This implementation also does not support the Morse format otherwise supported by certain calls of the authorization service.

## Usage
The system requires two-way certificate handshake, as does the authorization service it mimics. Testing applications with this service will involve managing a PKI infrastructure that closely matches what is used in production environments. In order to make this easier across multiple applications, it is reccomended that you use [Easycert](https://gitlab.lab24.com/netcraft/easycert). Follow the Easycert online help to create a jks keystore for your auth service.

The lastest version of the authorization service is available as a docker image in the Lab 24 Docker registry. You will need to install [docker](https://docs.docker.com/installation/) and configure it to use the [Lab 24 Docker Registry](http://docker.lab24.co). If the Lab24 Docker registry is unavailable to you, you can use the `docker load` command to import the image if you have it, or build it using the instructions below.

### Getting Started
1. Start a mongodb docker container:
```
docker run -d --name mongodb mongo
```

2. Start an authservice docker container that mounts your keystore and is linked to the mongodb container. The following command does this and also forwards port 8443 and 8080 on the host to the same port in the docker container. It also attaches to standard out so that you can watch progress of the container startup. Once it completes you safely detach from the container with `ctrl+c`:
```
docker run -d \
  --name authort \
  -p 8080:8080 \
  -p 8443:8443 \
  --link mongodb:mongo \
  -v /local/path/to/keystore.jks:/usr/share/authport/testcerts/keystore.jks \
  docker.lab24.co/netcraft/authviarest:latest \
&& docker attach --sig-proxy=false
```

3. Once the authport service completes its startup the API will be be available at `https://$DOCKER_HOST:8443` and the admin interface can be accessed at `http://$DOCKER_HOST:8080/admin`. The admin interface provides a simple way to manage the users/groups/projects that are in the system. It is not part of the actual authorization service. Consult the Enterprise Web Service API (tier 7) documentation for more information on the simulated endpoints.

## Build Instructions
The build script found in this repo builds the authport docker container. It requires bash, tar, curl and docker. To run it clone the repo and invoke the script:
```
git clone git@gitlab.lab24.com:inabox/auth-service.git \
&& cd auth-service \
&& ./build.sh
```
