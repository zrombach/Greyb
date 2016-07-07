package main

import (
	"errors"
	"fmt"
	"github.com/alecthomas/kingpin"
	"github.com/fsouza/go-dockerclient"
	"log"
	"os"
	"os/user"
)

type CreateUserCmd struct {
	User         *user.User
	CommonName   string
	DockerClient *docker.Client
}

func (this *CreateUserCmd) Run(c *kingpin.ParseContext) error {
	if this.User == nil {
		return errors.New("CreateUserCmd.User cannot be nil")
	}

	if _, err := os.Stat(getInaboxCaDir(this.User)); os.IsNotExist(err) {
		return errors.New("Cannot find the inabox certificate authority. Have you run `inabox up`?")
	}

	if err := os.MkdirAll(getInaboxUsersDir(this.User), 0700); err != nil {
		return err
	}

	if err := os.Mkdir(getUserPathF(this.CommonName)(this.User), 0700); os.IsExist(err) {
		return fmt.Errorf("The user %s already exists", this.CommonName)
	}

	userCrtPath := getCrtPath(this.User, getUserPathF(this.CommonName))
	userKeyPath := getKeyPath(this.User, getUserPathF(this.CommonName))

	userPk, err := parseOrCreatePk(userKeyPath)
	if err != nil {
		return fmt.Errorf("Unable to create private key for user %s: %s", this.CommonName, err)
	}

	serial, err := getNextSerialNumber(this.User)
	if err != nil {
		return err
	}

	template := makeUserTemplate(this.CommonName, InaboxCaName, serial)
	if err := signAndWritePem(userPk, template, this.CommonName, userCrtPath, getCrtPath(this.User, getInaboxCaDir), getKeyPath(this.User, getInaboxCaDir)); err != nil {
		return fmt.Errorf("Unable to create certificate for user %s: %s", this.CommonName, err)
	}

	userPath := getUserPathF(this.CommonName)(this.User)
	if err := createP12FromPem(userPath, this.DockerClient); err != nil {
		return fmt.Errorf("Unable to create p12 bundle for user %s: %s", this.CommonName, err)
	}

	rc := NewRestClient()
	rc.Host = getDockerAddress()
	rc.Port = findHostPort(AuthPortAdminPort, AuthportContainerName, this.DockerClient)

	if err := rc.PutUser(template); err != nil {
		return fmt.Errorf("Unable to add %s to authport: %s", this.CommonName, err)
	}

	log.Printf("%s has been added to authport.", this.CommonName)
	log.Printf("Certificates for %s can be found at %s. All cert bundles (.p12 and .jks files use the password '%s'", this.CommonName, userPath, CertPassword)

	return nil
}

func ConfigureCreateUserCmd(createCmd *kingpin.CmdClause, currentUser *user.User, dockerClient *docker.Client) {
	cmd := CreateUserCmd{User: currentUser, DockerClient: dockerClient}
	createUser := createCmd.Command("user", "Create an inabox user").Action(cmd.Run)
	createUser.Arg("common name", "Will be used to construct the user's DN. Usually the cn should be the user's Sid").StringVar(&cmd.CommonName)
}
