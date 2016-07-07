package main

import (
	"github.com/alecthomas/kingpin"
	"log"
	"os"
	"os/user"
)

func main() {
	currentUser, err := user.Current()
	if err != nil {
		log.Fatalf("Could not determine current user: %s", err)
	}

	dockerClient, err := getDockerClient()
	if err != nil {
		log.Fatalf("Could not initialize docker client: %s", err)
	}

	app := kingpin.New("inabox", "")

	ConfigureUpCmd(app, currentUser, dockerClient)
	ConfigureStatusCmd(app, currentUser, dockerClient)
	ConfigureResetCmd(app, currentUser, dockerClient)
	ConfigureCreateCmd(app, currentUser, dockerClient)

	kingpin.MustParse(app.Parse(os.Args[1:]))
}
