package main

import (
	"crypto/rand"
	"crypto/x509"
	"crypto/x509/pkix"
	"errors"
	"fmt"
	"github.com/alecthomas/kingpin"
	"github.com/fsouza/go-dockerclient"
	"log"
	"math/big"
	"os"
	"os/user"
	"time"
	"path"
	"strconv"
)

type UpCmd struct {
	User         *user.User
	DockerClient *docker.Client
}

func (this *UpCmd) Run(c *kingpin.ParseContext) error {
	if this.User == nil {
		return errors.New("UpCmd.User cannot be nil")
	}

	log.Println("Pulling needed docker images. This could take a few minutes on the first run.")
	for i := 0; i < len(ImageDependencies); i++ {
		log.Printf("...Pulling %s:%s", ImageDependencies[i].Repository, ImageDependencies[i].Tag)
		if err := this.DockerClient.PullImage(ImageDependencies[i], docker.AuthConfiguration{}); err != nil {
			return err
		}
	}
	log.Println("...Done Pulling images")

	if err := os.MkdirAll(getInaboxCaDir(this.User), 0700); err != nil {
		return err
	}


	file, err := os.Create(path.Join(getSerialFilePath(this.User)))
	if err != nil {
		return fmt.Errorf("Could not create the inabox ca serial file")
	}

	serialLock.Lock()
	if _, err = file.Write([]byte(strconv.FormatInt(2, 10))); err != nil {
		serialLock.Unlock()
		return err
	}
	serialLock.Unlock()
	file.Close()

	caPk, err := parseOrCreatePk(getKeyPath(this.User, getInaboxCaDir))
	if err != nil {
		return fmt.Errorf("Error loading/creating private key for the inabox certificate authority")
	}

	if _, err = os.Stat(getCrtPath(this.User, getInaboxCaDir)); os.IsNotExist(err) {
		subKeyId, err := generateSubjectKeyId(caPk)
		if err != nil {
			return fmt.Errorf("Error creating CA certificate - cannot create subject key id: %s", err)
		}

		caTemplate := &x509.Certificate{
			BasicConstraintsValid: true,
			IsCA:       true,
			MaxPathLen: 0,

			SerialNumber: big.NewInt(1),

			SubjectKeyId: subKeyId,

			Subject: pkix.Name{
				CommonName:         InaboxCaName,
				Organization:       []string{InaboxCaName + " Org"},
				OrganizationalUnit: []string{"CA"},
				Country:            []string{"US"},
			},

			NotBefore: time.Now().Add(-24 * time.Hour).UTC(),
			NotAfter:  time.Now().AddDate(5, 0, 0).UTC(),

			KeyUsage:                    x509.KeyUsageCertSign,
			ExtKeyUsage:                 nil,
			UnknownExtKeyUsage:          nil,
			DNSNames:                    nil,
			PermittedDNSDomainsCritical: false,
			PermittedDNSDomains:         nil,
		}

		certBytes, err := x509.CreateCertificate(rand.Reader, caTemplate, caTemplate, &caPk.PublicKey, caPk)
		if err != nil {
			return fmt.Errorf("Cannot create CA certificate: %s", err)
		}

		if err := saveCertAsPEM(getCrtPath(this.User, getInaboxCaDir), certBytes); err != nil {
			return fmt.Errorf("Cannot save CA certificate: %s", err)
		}
	}

	if err = os.MkdirAll(getServerPathF(getDockerAddress())(this.User), 0700); err != nil {
		return fmt.Errorf("Error creating directory: %s", err)
	}

	servicePk, err := parseOrCreatePk(getKeyPath(this.User, getServerPathF(getDockerAddress())))
	if err != nil {
		return fmt.Errorf("Error loading/creating private key for the inabox services")
	}

	serverCrtPath := getCrtPath(this.User, getServerPathF(getDockerAddress()))

	if err := copyFile(getCrtPath(this.User, getInaboxCaDir), getServerPathF(getDockerAddress())(this.User) + "/" + CaCertFileName); err != nil {
		return err
	}

	if _, err := os.Stat(serverCrtPath); os.IsNotExist(err) {
		template := makeHostTemplate(getDockerAddress(), InaboxCaName, 2)
		if err := signAndWritePem(servicePk, template, getDockerAddress(), serverCrtPath, getCrtPath(this.User, getInaboxCaDir), getKeyPath(this.User, getInaboxCaDir)); err != nil {
			return fmt.Errorf("Error creating inabox service certificate: %s", err)
		}
	}

	serverPath := getServerPathF(getDockerAddress())(this.User)
	if _, err := os.Stat(getP12Path(this.User, getServerPathF(getDockerAddress()))); os.IsNotExist(err) {
		if err := createP12FromPem(serverPath, this.DockerClient); err != nil {
			return fmt.Errorf("Error creating p12 bundle for inabox services")
		}
	}

	if _, err := os.Stat(getJksPath(this.User, getServerPathF(getDockerAddress()))); os.IsNotExist(err) {
		if err := createJavaKeyStore(serverPath, this.DockerClient); err != nil {
			return fmt.Errorf("Error creating Java keystore file for inabox services")
		}
	}

	log.Println("Checking for existing mongo container.")
	created, err := containerIsCreated(MongoContainerName, this.DockerClient)
	if err != nil {
		return err
	}
	if !created {
		log.Println("...Not found. Creating a mongo container.")
		_, err := this.DockerClient.CreateContainer(docker.CreateContainerOptions{
			Name: MongoContainerName,
			Config: &docker.Config{
				Image: MongoImageName,
				Cmd:   []string{"--noprealloc", "--smallfiles"},
			},
		})
		if err != nil {
			return err
		}
	} else {
		log.Println("...Existing mongo container found")
	}

	log.Println("Checking that the mongo container is running")
	started, err := containerIsRunning(MongoContainerName, this.DockerClient)
	if err != nil {
		return err
	}
	if !started {
		log.Println("...Starting the mongo container.")
		if err := this.DockerClient.StartContainer(MongoContainerName, nil); err != nil {
			return err
		}
		time.Sleep(10 * time.Second)
	}
	log.Println("...Mongo is running.")

	log.Println("Checking for existing authport container")
	created, err = containerIsCreated(AuthportContainerName, this.DockerClient)
	if err != nil {
		return err
	}
	if !created {
		ksMountPoint := "/usr/share/authport/testcerts/keystore.jks"
		log.Println("...Not found. Creating the authport container.")
		_, err := this.DockerClient.CreateContainer(docker.CreateContainerOptions{
			Name: AuthportContainerName,
			HostConfig: &docker.HostConfig{
				Binds:           []string{getJksPath(this.User, getServerPathF(getDockerAddress())) + ":" + ksMountPoint},
				PublishAllPorts: true,
				Links:           []string{MongoContainerName + ":mongo"},
			},
			Config: &docker.Config{
				Image: AuthportImageName,
				Mounts: []docker.Mount{docker.Mount{
					Source:      getJksPath(this.User, getServerPathF(getDockerAddress())),
					Destination: ksMountPoint,
					RW:          true,
				}},
			},
		})
		if err != nil {
			return err
		}
	} else {
		log.Println("...Existing authport container found")
	}

	log.Println("Checking that the authport container is running")
	started, err = containerIsRunning(AuthportContainerName, this.DockerClient)
	if err != nil {
		return err
	}
	if !started {
		log.Println("...Starting the authport container.")
		if err := this.DockerClient.StartContainer(AuthportContainerName, nil); err != nil {
			return err
		}
		time.Sleep(10 * time.Second)
	}
	log.Println("...Authport is running.")

	log.Println("Checking for existing webapis container")
	log.Println(WebAPIsContainerName)
	created, err = containerIsCreated(WebAPIsContainerName, this.DockerClient)
	if err != nil {
		return err
	}

	if !created {
		certsMountPoint := "/testcerts"
		log.Println("...Not found. Creating the webapis container.")
		_, err := this.DockerClient.CreateContainer(docker.CreateContainerOptions{
			Name: WebAPIsContainerName,
			HostConfig: &docker.HostConfig{
				Binds:           []string{getCertsDirectory(this.User, getServerPathF(getDockerAddress())) + ":" + certsMountPoint},
				PublishAllPorts: true,
				Links:           []string{MongoContainerName + ":mongo"},
			},
			Config: &docker.Config{
				Image: WebAPIsImageName,
				Mounts: []docker.Mount{docker.Mount{
					Source:      getCertsDirectory(this.User, getServerPathF(getDockerAddress())),
					Destination: certsMountPoint,
					RW:          true,
				}},
			},
		})
		if err != nil {
			return err
		}
	} else {
		log.Println("...Existing webapis container found")
	}

	log.Println("Checking that the webapis container is running")
	started, err = containerIsRunning(WebAPIsContainerName, this.DockerClient)
	if err != nil {
		return err
	}
	if !started {
		log.Println("...Starting the webapis container.")
		if err := this.DockerClient.StartContainer(WebAPIsContainerName, nil); err != nil {
			return err
		}
		time.Sleep(10 * time.Second)
	}
	log.Println("...WebAPIs is running.")

	log.Println("Inabox is up and running!")
	printServices(this.DockerClient)
	return nil
}

func ConfigureUpCmd(app *kingpin.Application, currentUser *user.User, dockerClient *docker.Client) {
	cmd := &UpCmd{User: currentUser, DockerClient: dockerClient}
	app.Command("up", "Initialize and start the inabox services").Action(cmd.Run)
}
