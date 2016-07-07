package main

import (
	"github.com/fsouza/go-dockerclient"
	"log"
	"os"
	"strings"
)

const (
	MongoRepoName      = "mongo"
	MongoTag           = "3.0.6"
	MongoImageName     = MongoRepoName + ":" + MongoTag
	MongoContainerName = "inabox_mongo"

	AuthportImageName     = "docker.lab24.co/netcraft/authviarest"
	AuthportContainerName = "inabox_authport"

	WebAPIsImageName     = "docker.lab24.co/inabox/webapis"
	WebAPIsContainerName = "inabox_webapis"

	OpenSslRepoName  = "golang"
	OpenSslTag       = "1.5.1"
	OpenSslImageName = OpenSslRepoName + ":" + OpenSslTag

	JavaRepoName  = "java"
	JavaTag       = "8u66"
	JavaImageName = JavaRepoName + ":" + JavaTag

	DockerTcpPrefix = "tcp://"

	AuthPortAdminPort = "8080/tcp"

	HostPortTemplate = "{{(index (index .NetworkSettings.Ports %d/tcp) 0).HostPort}}"
)

var (
	ImageDependencies []docker.PullImageOptions = []docker.PullImageOptions{
		docker.PullImageOptions{
			Repository: JavaRepoName,
			Tag:        JavaTag,
		},
		docker.PullImageOptions{
			Repository: OpenSslRepoName,
			Tag:        OpenSslTag,
		},
		docker.PullImageOptions{
			Repository: MongoRepoName,
			Tag:        MongoTag,
		},
	}
)

func getDockerClient() (*docker.Client, error) {
	client, err := docker.NewClientFromEnv()
	if err != nil {
		return docker.NewClient("unix:///var/run/docker.sock")
	}
	return client, nil
}

func containerIsRunning(name string, client *docker.Client) (bool, error) {
	containers, err := client.ListContainers(docker.ListContainersOptions{})
	if err != nil {
		return false, err
	}
	return containerInList(name, containers), nil
}

func containerIsCreated(name string, client *docker.Client) (bool, error) {
	containers, err := client.ListContainers(docker.ListContainersOptions{
		All: true,
	})
	if err != nil {
		return false, err
	}
	return containerInList(name, containers), nil
}

func containerInList(name string, containers []docker.APIContainers) bool {
	for i := 0; i < len(containers); i++ {
		for j := 0; j < len(containers[i].Names); j++ {
			if containers[i].Names[j] == "/"+name {
				return true
			}
		}
	}
	return false
}

func getDockerAddress() string {
	address := "localhost"
	if dockerHostEnv, ok := os.LookupEnv("DOCKER_HOST"); ok {
		if strings.HasPrefix(dockerHostEnv, DockerTcpPrefix) {
			address = strings.Split(strings.TrimPrefix(dockerHostEnv, DockerTcpPrefix), ":")[0]
		}
	}
	return address
}

func findHostPort(port docker.Port, id string, client *docker.Client) string {
	result, err := client.InspectContainer(id)
	if err == nil {
		if mappings, ok := result.NetworkSettings.Ports[port]; ok {
			return mappings[0].HostPort
		}
	}
	return ""
}

func printServices(client *docker.Client) {
	isAuthRunning, _ := containerIsRunning(AuthportContainerName, client)
	if isAuthRunning {
		log.Printf("The Authport API is located at https://%s:%s", getDockerAddress(), findHostPort("8443/tcp", AuthportContainerName, client))
		log.Printf("The Authport Admin Interface is located at http://%s:%s/admin", getDockerAddress(), findHostPort(AuthPortAdminPort, AuthportContainerName, client))
	} else {
		log.Printf("Authport is not running. Use `inabox up` to start it.")
	}
	isAPIsRunning, _ := containerIsRunning(WebAPIsContainerName, client)
	if isAPIsRunning {
		log.Printf("The WebAPIs are located at https://%s:%s", getDockerAddress(), findHostPort("9443/tcp", WebAPIsContainerName, client))
	} else {
		log.Printf("WebAPIs is not running. Use `inabox up` to start it.")
	}
}
