package main

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha1"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/asn1"
	"encoding/pem"
	"fmt"
	"github.com/fsouza/go-dockerclient"
	"io/ioutil"
	"math/big"
	"net"
	"os"
	"time"
	"io"
	"sync"
	"os/user"
	"strconv"
)

const (
	PemBlockTypeCert  = "CERTIFICATE"
	PemBlockTypeRSAPK = "RSA PRIVATE KEY"
	InaboxCaName      = "inabox"
	CertPassword      = "changeme"
)

var serialLock = new(sync.Mutex)

func getCertFromPath(path string) (*x509.Certificate, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(data)
	if block.Type != PemBlockTypeCert {
		return nil, fmt.Errorf("Invalid PEM block type; expected %s, got %s", PemBlockTypeCert, block.Type)
	}
	return x509.ParseCertificate(block.Bytes)
}

func getPkFromPath(path string) (*rsa.PrivateKey, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(data)
	if block.Type != PemBlockTypeRSAPK {
		return nil, fmt.Errorf("Invalid PEM block type; expected %s, got %s", PemBlockTypeRSAPK, block.Type)
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}

func parseOrCreatePk(path string) (*rsa.PrivateKey, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		if os.IsNotExist(err) {
			pk, err := rsa.GenerateKey(rand.Reader, 2048)
			if err != nil {
				return nil, err
			}
			return pk, savePrivateKeyAsPEM(path, pk)
		}

		return nil, err
	}

	block, _ := pem.Decode(data)
	if block.Type != PemBlockTypeRSAPK {
		return nil, fmt.Errorf("Invalid PEM block type; expected %s, got %s", PemBlockTypeRSAPK, block.Type)
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}

func savePem(path string, pemBytes []byte, pemType string) error {
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()
	return pem.Encode(f, &pem.Block{
		Type:  pemType,
		Bytes: pemBytes,
	})
}

func saveCertAsPEM(path string, certBytes []byte) error {
	return savePem(path, certBytes, PemBlockTypeCert)
}

func savePrivateKeyAsPEM(path string, pk *rsa.PrivateKey) error {
	return savePem(path, x509.MarshalPKCS1PrivateKey(pk), PemBlockTypeRSAPK)
}

func generateSubjectKeyId(pk *rsa.PrivateKey) ([]byte, error) {
	subKeyId, err := asn1.Marshal(pk.PublicKey)
	if err != nil {
		return nil, fmt.Errorf("Cannot Create Subject Key Id: %s", err)
	}

	idHash := sha1.Sum(subKeyId)

	return idHash[:], nil
}

func createP12FromPem(path string, client *docker.Client) error {
	mountPoint := "/inabox"
	container, err := client.CreateContainer(docker.CreateContainerOptions{
		HostConfig: &docker.HostConfig{Binds: []string{path + ":" + mountPoint}},
		Config: &docker.Config{
			Mounts: []docker.Mount{docker.Mount{
				Source:      path,
				Destination: mountPoint,
				RW:          true,
			}},
			Image:      OpenSslImageName,
			Entrypoint: []string{"openssl"},
			Cmd: []string{
				"pkcs12",
				"-export",
				"-in",
				mountPoint + "/" + CertFileName,
				"-inkey",
				mountPoint + "/" + KeyFileName,
				"-passout",
				"pass:" + CertPassword,
				"-out",
				mountPoint + "/" + P12FileName,
			},
		},
	})
	if err != nil {
		return err
	}

	return startWaitRemove(container, client)
}

func createJavaKeyStore(path string, client *docker.Client) error {
	mountPoint := "/inabox"
	container, err := client.CreateContainer(docker.CreateContainerOptions{
		HostConfig: &docker.HostConfig{Binds: []string{path + ":" + mountPoint}},
		Config: &docker.Config{
			Mounts: []docker.Mount{docker.Mount{
				Source:      path,
				Destination: mountPoint,
				RW:          true,
			}},
			Image:      JavaImageName,
			Entrypoint: []string{"keytool"},
			Cmd: []string{
				"-importkeystore",
				"-srckeystore",
				mountPoint + "/" + P12FileName,
				"-srcstoretype",
				"PKCS12",
				"-srcstorepass",
				CertPassword,
				"-destkeystore",
				mountPoint + "/" + JksFileName,
				"-deststorepass",
				CertPassword,
				"-destkeypass",
				CertPassword,
			},
		},
	})
	if err != nil {
		return err
	}

	if err := startWaitRemove(container, client); err != nil {
		return err
	}

	container, err = client.CreateContainer(docker.CreateContainerOptions{
		HostConfig: &docker.HostConfig{Binds: []string{path + ":" + mountPoint}},
		Config: &docker.Config{
			Mounts: []docker.Mount{docker.Mount{
				Source: path,
				Destination: mountPoint,
				RW: true,
			}},
			Image: JavaImageName,
			Entrypoint: []string{"keytool"},
			Cmd: []string{
				"-importcert",
				"-file",
				mountPoint + "/" + CaCertFileName,
				"-storepass",
				CertPassword,
				"-noprompt",
				"-keystore",
				mountPoint + "/" + JksFileName,
			},
		},
	})
	if err != nil {
		return err
	}

	return startWaitRemove(container, client)
}

func startWaitRemove(container *docker.Container, client *docker.Client) error {
	if err := client.StartContainer(container.ID, nil); err != nil {
		return err
	}

	client.WaitContainer(container.ID)

	return client.RemoveContainer(docker.RemoveContainerOptions{
		Force:         true,
		RemoveVolumes: true,
		ID:            container.ID,
	})
}

func makeUserTemplate(cn string, caName string, serial int64) *x509.Certificate {
	return &x509.Certificate{
		SerialNumber: big.NewInt(serial),

		SubjectKeyId: nil,

		Subject: pkix.Name{
			CommonName:   cn,
			Organization: []string{caName + " Org"},
			Country:      []string{"US"},
		},

		NotBefore: time.Now().Add(-24 * time.Hour).UTC(),
		NotAfter:  time.Now().AddDate(5, 0, 0).UTC(),

		KeyUsage:                    0,
		ExtKeyUsage:                 nil,
		UnknownExtKeyUsage:          nil,
		DNSNames:                    nil,
		PermittedDNSDomainsCritical: false,
		PermittedDNSDomains:         nil,
	}
}

func makeHostTemplate(cn string, caName string, serial int64) *x509.Certificate {
	certTemplate := &x509.Certificate{
		SerialNumber: big.NewInt(serial),

		SubjectKeyId: nil,

		Subject: pkix.Name{
			CommonName:         cn,
			Organization:       []string{caName + " Org"},
			OrganizationalUnit: []string{"D00X"},
			Country:            []string{"US"},
		},

		NotBefore: time.Now().Add(-24 * time.Hour).UTC(),
		NotAfter:  time.Now().AddDate(5, 0, 0).UTC(),

		KeyUsage:                    0,
		ExtKeyUsage:                 nil,
		UnknownExtKeyUsage:          nil,
		DNSNames:                    nil,
		PermittedDNSDomainsCritical: false,
		PermittedDNSDomains:         nil,
	}

	if net.ParseIP(cn) != nil {
		certTemplate.IPAddresses = []net.IP{net.ParseIP(cn)}
	}

	return certTemplate
}

func signAndWritePem(pk *rsa.PrivateKey, template *x509.Certificate, cn string, certPath string, caCertPath string, caKeyPath string) error {
	subKeyId, err := generateSubjectKeyId(pk)
	if err != nil {
		return fmt.Errorf("Error creating certificate - cannot create subject key id: %s", err)
	}

	template.SubjectKeyId = subKeyId

	caCert, err := getCertFromPath(caCertPath)
	if err != nil {
		return err
	}

	caPk, err := getPkFromPath(caKeyPath)
	if err != nil {
		return err
	}

	certBytes, err := x509.CreateCertificate(
		rand.Reader,
		template,
		caCert,
		&pk.PublicKey,
		caPk,
	)
	if err != nil {
		return fmt.Errorf("Error Creating Certificate: %s", err)
	}

	return saveCertAsPEM(certPath, certBytes)
}

func copyFile(src, dest string) (err error) {
	in, err := os.Open(src)
	if err != nil {
		return err
	}
	defer in.Close()
	out, err := os.Create(dest)
	if err != nil {
		return
	}
	defer func() {
		if closeErr := out.Close(); closeErr != nil {
			err = closeErr
		}
	}()
	if _, err = io.Copy(out, in); err != nil {
		return
	}
	err = out.Sync()
	return
}

func getNextSerialNumber(user *user.User) (serial int64, err error) {
	serialLock.Lock()
	defer serialLock.Unlock()

	b, err := ioutil.ReadFile(getSerialFilePath(user))
	if err != nil {
		return
	}

	serial, err = strconv.ParseInt(string(b), 10, 64)
	serial += 1

	err = ioutil.WriteFile(getSerialFilePath(user), []byte(strconv.FormatInt(serial, 10)), 0666)

	return
}