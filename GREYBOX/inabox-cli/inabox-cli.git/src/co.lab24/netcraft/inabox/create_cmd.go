package main

import (
	"github.com/alecthomas/kingpin"
	"github.com/fsouza/go-dockerclient"
	"os/user"
)

func ConfigureCreateCmd(app *kingpin.Application, currentUser *user.User, dockerClient *docker.Client) {
	create := app.Command("create", "Create inabox users and hosts")
	ConfigCreateHostCmd(create, currentUser, dockerClient)
	ConfigureCreateUserCmd(create, currentUser, dockerClient)
}
