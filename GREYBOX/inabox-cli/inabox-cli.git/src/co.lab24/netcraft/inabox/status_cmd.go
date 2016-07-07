package main

import (
	"github.com/alecthomas/kingpin"
	"github.com/fsouza/go-dockerclient"
	"os/user"
)

type StatusCmd struct {
	User         *user.User
	DockerClient *docker.Client
}

func (this *StatusCmd) Run(c *kingpin.ParseContext) error {
	printServices(this.DockerClient)
	return nil
}

func ConfigureStatusCmd(app *kingpin.Application, currentUser *user.User, dockerClient *docker.Client) {
	cmd := StatusCmd{User: currentUser, DockerClient: dockerClient}
	app.Command("status", "show status (running/not running/urls/etc) of inabox services").Action(cmd.Run)
}
