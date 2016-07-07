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

type CreateHostCmd struct {
	User         *user.User
	Hostname     string
	DockerClient *docker.Client
}

func (this *CreateHostCmd) Run(c *kingpin.ParseContext) error {
	if this.User == nil {
		return errors.New("CreateHostCmd.User cannot be nil")
	}

	if _, err := os.Stat(getInaboxCaDir(this.User)); os.IsNotExist(err) {
		return errors.New("Cannot find the inabox certificate authority. Have you run `inabox up`?")
	}

	if err := os.MkdirAll(getInaboxServersDir(this.User), 0700); err != nil {
		return err
	}

	if err := os.Mkdir(getServerPathF(this.Hostname)(this.User), 0700); os.IsExist(err) {
		return fmt.Errorf("The user %s already exists", this.Hostname)
	}

	if err := copyFile(getCrtPath(this.User, getInaboxCaDir), getServerPathF(this.Hostname)(this.User) + "/" + CaCertFileName); err != nil {
		return err
	}

	serverCrtPath := getCrtPath(this.User, getServerPathF(this.Hostname))
	serverKeyPath := getKeyPath(this.User, getServerPathF(this.Hostname))

	hostPk, err := parseOrCreatePk(serverKeyPath)
	if err != nil {
		return fmt.Errorf("Error creating private key for the %s", this.Hostname)
	}

	serial, err := getNextSerialNumber(this.User)
	if err != nil {
		return err
	}

	template := makeHostTemplate(this.Hostname, InaboxCaName, serial)

	if err := signAndWritePem(hostPk, template, this.Hostname, serverCrtPath, getCrtPath(this.User, getInaboxCaDir), getKeyPath(this.User, getInaboxCaDir)); err != nil {
		return err
	}

	serverPath := getServerPathF(this.Hostname)(this.User)
	if err := createP12FromPem(serverPath, this.DockerClient); err != nil {
		return err
	}

	if err := createJavaKeyStore(serverPath, this.DockerClient); err != nil {
		return err
	}

	rc := NewRestClient()
	rc.Host = getDockerAddress()
	rc.Port = findHostPort(AuthPortAdminPort, AuthportContainerName, this.DockerClient)
	if err := rc.PutUser(template); err != nil {
		return fmt.Errorf("Unable to add %s to authport: %s", this.Hostname, err)
	}

	log.Printf("%s has been added to authport.", this.Hostname)
	log.Printf("Certificates for %s can be found at %s. All cert bundles (.p12 and .jks files use the password '%s')", this.Hostname, serverPath, CertPassword)

	return nil
}

func ConfigCreateHostCmd(createCmd *kingpin.CmdClause, currentUser *user.User, dockerClient *docker.Client) {
	cmd := CreateHostCmd{User: currentUser, DockerClient: dockerClient}
	createHost := createCmd.Command("host", "create an inabox host").Action(cmd.Run)
	createHost.Arg("hostname", "The hostname for the server (e.g. 'localhost', '192.168.100.1', 'www.mydomain.com', etc)").StringVar(&cmd.Hostname)
}
