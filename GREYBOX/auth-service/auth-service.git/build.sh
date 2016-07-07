#!/bin/bash

# This script builds the auth service docker image and runs
# a trivial integration test to make sure we're able to start
# the service. After running this script, if successful the auth service
# docker container will have been built and tagged as latest
#
# TODO: integrate the postman tests into this script.
#

set -e

GIT_HASH="`git log -n 1 --pretty=format:%h`"
ADMIN_BUILD_DATA="admin_build_$GIT_HASH"
SERVICE_BUILD_DATA="service_build_$GIT_HASH"
MONGO_CONTAINER="mongo_test_$GIT_HASH"
TEMP_BUILD_DIR="`pwd`/build_output"
SERVICE_HOST="localhost"
CONTAINER_NAMESPACE="docker.lab24.co/netcraft/authviarest"
CURRENT_BUILD_TAG="$CONTAINER_NAMESPACE:$GIT_HASH"
SERVICE_TEST_CONTAINER="service_test_$GIT_HASH"
UTIL_IMAGE="busybox"

[ -n "$DOCKER_HOST" ] && SERVICE_HOST=`echo $DOCKER_HOST | sed -e 's/tcp:\/\/\(.*\):.*/\1/'`

function cleanup() {
    for container in $ADMIN_BUILD_DATA $SERVICE_BUILD_DATA $MONGO_CONTAINER $SERVICE_TEST_CONTAINER
    do
        if [ -n "`docker ps | grep $container`" ]
        then
            docker stop $container
        fi

        if [ -n "`docker ps -a | grep $container`" ]
        then
            docker rm -v $container
        fi
    done

    rm -rf "$TEMP_BUILD_DIR"
}

trap cleanup EXIT INT TERM

#Create a data volume only container for the admin client build
docker run --name "$ADMIN_BUILD_DATA" -v /buildadmin "$UTIL_IMAGE" true

#Load the admin client code into the volume
tar -c -C ./admin-client --exclude=node_modules --exclude=dist . \
    | docker run -i --rm --volumes-from "$ADMIN_BUILD_DATA" "$UTIL_IMAGE" tar -x -C /buildadmin

#Run the client build
NPM_CMD="docker run --rm --volumes-from $ADMIN_BUILD_DATA -w /buildadmin node:0.10 npm"
$NPM_CMD install
$NPM_CMD run build

#start mongo and give it some time to start up
docker run -d --name "$MONGO_CONTAINER" mongo --noprealloc --smallfiles
sleep 10

#Create a data volume only container for the admin service build
docker run --name "$SERVICE_BUILD_DATA" -v /buildservice "$UTIL_IMAGE" true

#Load the service code into the volume
tar -c -C ./authViaREST . \
    | docker run -i --rm --volumes-from "$SERVICE_BUILD_DATA" "$UTIL_IMAGE" tar -x -C /buildservice

# Load the admin client artifacts into the service build volume
docker run -i --rm --volumes-from "$ADMIN_BUILD_DATA" "$UTIL_IMAGE" tar -c -C /buildadmin/dist . \
    | docker run -i --rm --volumes-from "$SERVICE_BUILD_DATA" "$UTIL_IMAGE" tar -x -C /buildservice/src/main/resources/assets/adminClient

#Run the service build
docker run --rm --volumes-from "$SERVICE_BUILD_DATA" -w /buildservice --link "$MONGO_CONTAINER":mongo -e MONGODB_DB_HOST=mongo -e MONGODB_DB_PORT=27017 maven:3 mvn clean package

mkdir "$TEMP_BUILD_DIR"

docker run -i --rm --volumes-from "$SERVICE_BUILD_DATA" "$UTIL_IMAGE" tar -c -C /buildservice/target . \
    | tar -x -C "$TEMP_BUILD_DIR"

docker build -t "$CURRENT_BUILD_TAG" .

#start a container using the just built image
docker run -d -P --name "$SERVICE_TEST_CONTAINER" --link "$MONGO_CONTAINER":mongo "$CURRENT_BUILD_TAG"
sleep 10

SERVICE_PORT=`docker inspect --format='{{(index (index .NetworkSettings.Ports "8081/tcp") 0).HostPort}}' "$SERVICE_TEST_CONTAINER"`

if [ "`curl -v http://$SERVICE_HOST:$SERVICE_PORT/ping`" == 'pong' ]
then
    docker tag -f "$CURRENT_BUILD_TAG" "$CONTAINER_NAMESPACE:latest"
else
    exit 1
fi
