package main

import (
	"errors"
	"github.com/alecthomas/kingpin"
	"github.com/fsouza/go-dockerclient"
	"os"
	"os/user"
)

type ResetCmd struct {
	User         *user.User
	DockerClient *docker.Client
}

func (this *ResetCmd) Run(c *kingpin.ParseContext) error {
	if this.User == nil {
		return errors.New("ResetCmd.User cannot be nil")
	}

	containers := []string{MongoContainerName, AuthportContainerName}
	for i := 0; i < len(containers); i++ {
		if exists, _ := containerIsCreated(containers[i], this.DockerClient); exists {
			this.DockerClient.RemoveContainer(docker.RemoveContainerOptions{
				RemoveVolumes: true,
				Force:         true,
				ID:            containers[i],
			})
		}
	}

	return os.RemoveAll(getInaboxPkiDir(this.User))
}

func ConfigureResetCmd(app *kingpin.Application, currentUser *user.User, dockerClient *docker.Client) {
	cmd := ResetCmd{User: currentUser, DockerClient: dockerClient}
	app.Command("reset", "Removes all inabox services, databases and certificates").Action(cmd.Run)
}
