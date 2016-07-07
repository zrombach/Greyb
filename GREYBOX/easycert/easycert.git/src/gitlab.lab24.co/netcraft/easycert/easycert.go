package main

import (
	"crypto/rand"
	"crypto/rsa"
	"crypto/sha1"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/asn1"
	"encoding/pem"
	"errors"
	"fmt"
	"gopkg.in/alecthomas/kingpin.v2"
	"math/big"
	"os"
	"os/user"
	"time"
	"io/ioutil"
	"os/exec"
)

type (
	ca struct {
		name    string
		servers []string
		users   []string
	}

	listCaCmd struct {
		caList  []*ca
		verbose bool
		baseDir string
	}

	makeCaCmd struct {
		caList  []*ca
		caName  string
		baseDir string
	}

	removeCaCmd struct {
		caList  []*ca
		caName  string
		baseDir string
		force   bool
	}

	listServerCmd struct {
		caList  []*ca
		caName  string
		verbose bool
		baseDir string
	}

	makeServerCmd struct {
		caList     []*ca
		caName     string
		baseDir    string
		serverName string
		makeP12    bool
		makeJks    bool
		makeBundle bool
	}

	removeServerCmd struct {
		caList     []*ca
		caName     string
		serverName string
		baseDir    string
	}

	listUserCmd struct {
		caList  []*ca
		caName  string
		baseDir string
		verbose bool
	}

	makeUserCmd struct {
		caList     []*ca
		caName     string
		baseDir    string
		userName   string
		makeP12    bool
		makeBundle bool
	}

	removeUserCmd struct {
		caList []*ca
		caName string
		baseDir string
		userName string
	}
)

var (
	caNotFoundError     = errors.New("CA Not Found")
	serverNotFoundError = errors.New("Server Not Found")
	serverExistsError   = errors.New("Server Already Exists")
	caExistsError       = errors.New("CA Already Exists")
	userExistsError     = errors.New("User Already Exists")
	userNotFoundError   = errors.New("User Not Found")
)

const (
	pemBlockTypeCert = "CERTIFICATE"
	pemBlockTypeRSAPK = "RSA PRIVATE KEY"

	mainHelpText = `Easycert makes it simple to manage a public key infrastructure for your development environment.
It's main use case is managing a certificate authority that you want to use across multiple development projects.
Use Easycert to create multiple server and user certificates that are all signed by the same CA. By default it will
create public and private keys as PEM files, but has options for creating p12 (requires openssl) and JKS
(requires a JDK) files as well. Easycert is not for managing PKI in production environments.`
)

func (this *listCaCmd) run(pc *kingpin.ParseContext) error {
	for _, c := range this.caList {
		if this.verbose {
			fmt.Println(makeCaPath(this.baseDir, c.name))
		} else {
			fmt.Println(c.name)
		}
	}

	return nil
}

func (this *makeCaCmd) run(pc *kingpin.ParseContext) error {
	for _, val := range this.caList {
		if val.name == this.caName {
			return caExistsError
		}
	}

	caBasePath := makeCaPath(this.baseDir, this.caName)

	if err := os.MkdirAll(caBasePath, 0700); err != nil {
		return fmt.Errorf("Error creating CA directory: %s", err)
	}

	pk, err := rsa.GenerateKey(rand.Reader, 2048)
	if err != nil {
		return fmt.Errorf("Cannot Generate Private Key: %s", err)
	}

	subKeyId, err := generateSubjectKeyId(pk)
	if err != nil {
		return fmt.Errorf("Cannot Create Subject Key Id: %s", err)
	}

	caTemplate := &x509.Certificate{
		BasicConstraintsValid: true,
		IsCA:       true,
		MaxPathLen: 0,

		SerialNumber: big.NewInt(1),

		SubjectKeyId: subKeyId,

		Subject: pkix.Name{
			CommonName:         this.caName,
			Organization:       []string{this.caName + " Org"},
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

	certBytes, err := x509.CreateCertificate(rand.Reader, caTemplate, caTemplate, &pk.PublicKey, pk)
	if err != nil {
		return fmt.Errorf("Cannot create CA certificate: %s", err)
	}

	if err := saveCertAsPEM(makeCertPath(caBasePath), certBytes); err != nil {
		return fmt.Errorf("Cannot save CA certificate: %s", err)
	}

	if err := savePrivateKeyAsPEM(makeKeyPath(caBasePath), pk); err != nil {
		return fmt.Errorf("Cannot save CA private key: %s", err)
	}

	return createP12FromPEM(caBasePath)
}

func (this *removeCaCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		return os.RemoveAll(makeCaPath(this.baseDir, this.caName))
	})
}

func (this *listServerCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		for _, s := range c.servers {
			if this.verbose {
				fmt.Println(makeServerPath(this.baseDir, this.caName, s))
			} else {
				fmt.Println(s)
			}
		}
		return nil
	})
}

func (this *makeServerCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		if stringInArr(c.servers, this.serverName) {
			return serverExistsError
		}

		serverBasePath := makeServerPath(this.baseDir, this.caName, this.serverName)

		if err := os.MkdirAll(serverBasePath, 0700); err != nil {
			return fmt.Errorf("Cannot create the server directory: %s", err)
		}

		pk, err := rsa.GenerateKey(rand.Reader, 2048)
		if err != nil {
			return fmt.Errorf("Cannot Generate Private Key: %s", err)
		}

		subKeyId, err := generateSubjectKeyId(pk)
		if err != nil {
			return fmt.Errorf("Cannot Create Subject Key Id: %s", err)
		}

		certTemplate := &x509.Certificate{
			SerialNumber: big.NewInt(1),

			SubjectKeyId: subKeyId,

			Subject: pkix.Name{
				CommonName:         this.serverName,
				Organization:       []string{this.caName + " Org"},
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

		if err := signAndWrite(this.baseDir, serverBasePath, this.caName, certTemplate, pk, this.makeBundle); err != nil {
			return err
		}

		if this.makeP12 || this.makeJks {
			if err := createP12FromPEM(serverBasePath); err != nil {
				return err
			}
		}

		if this.makeJks {
			return createJavaKeyStore(serverBasePath, makeCertPath(makeCaPath(this.baseDir, this.caName)))
		}

		return nil
	})
}

func (this *removeServerCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		return findNameAndDo(this.serverName, c.servers, serverNotFoundError, func(s string) error {
			return os.RemoveAll(makeServerPath(this.baseDir, this.caName, this.serverName))
		})
	})
}

func (this *listUserCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		for _, u := range c.users {
			if this.verbose {
				fmt.Println(makeUserPath(this.baseDir, this.caName, u))
			} else {
				fmt.Println(u)
			}
		}
		return nil
	})
}

func (this *makeUserCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		if stringInArr(c.users, this.userName) {
			return userExistsError
		}

		userBasePath := makeUserPath(this.baseDir, this.caName, this.userName)

		if err := os.MkdirAll(userBasePath, 0700); err != nil {
			return fmt.Errorf("Cannot create the user directory: %s", err)
		}

		pk, err := rsa.GenerateKey(rand.Reader, 2048)
		if err != nil {
			return fmt.Errorf("Cannot Generate Private Key: %s", err)
		}

		subKeyId, err := generateSubjectKeyId(pk)
		if err != nil {
			return fmt.Errorf("Cannot Create Subject Key Id: %s", err)
		}

		certTemplate := &x509.Certificate{
			SerialNumber: big.NewInt(1),

			SubjectKeyId: subKeyId,

			Subject: pkix.Name{
				CommonName:         this.userName,
				Organization:       []string{this.caName + " Org"},
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

		if err := signAndWrite(this.baseDir, userBasePath, this.caName, certTemplate, pk, this.makeBundle); err != nil {
			return err
		}

		if this.makeP12 {
			return createP12FromPEM(userBasePath)
		}

		return nil
	})
}
func (this *removeUserCmd) run(pc *kingpin.ParseContext) error {
	return findCaAndDo(this.caName, this.caList, func(c *ca) error {
		return findNameAndDo(this.userName, c.servers, userNotFoundError, func(s string) error {
			return os.RemoveAll(makeServerPath(this.baseDir, this.caName, this.userName))
		})
	})
}


func signAndWrite(baseDir, subDir, caName string, template *x509.Certificate, pk *rsa.PrivateKey, makeBundle bool) error {

	serial, err := rand.Int(rand.Reader, big.NewInt(999999999999)) //TODO: does this matter?
	if err != nil {
		return err
	}

	template.SerialNumber = serial

	caPath := makeCaPath(baseDir, caName)

	caCert, err := getCertFromPath(makeCertPath(caPath))
	if err != nil {
		return fmt.Errorf("Could not parse CA file for %s", caName)
	}

	caPk, err := getPkFromPath(makeKeyPath(caPath))
	if err != nil {
		return fmt.Errorf("Could not parse CA private key for %s", caName)
	}

	certBytes, err := x509.CreateCertificate(rand.Reader, template, caCert, &pk.PublicKey, caPk)
	if err != nil {
		return fmt.Errorf("Error Creating Certificate: %s", err)
	}

	if err := saveCertAsPEM(makeCertPath(subDir), certBytes); err != nil {
		return fmt.Errorf("Cannot save certificate: %s", err)
	}

	if err := savePrivateKeyAsPEM(makeKeyPath(subDir), pk); err != nil {
		return fmt.Errorf("Cannot save private key: %s", err)
	}

	if makeBundle {
		caBytes, err := ioutil.ReadFile(makeCertPath(caPath))
		if err != nil {
			return err
		}

		return saveBundleAsPEM(makeBundlePath(subDir), certBytes, caBytes, pk);
	}

	return nil
}

func createP12FromPEM(basePath string) error {
	certPath := makeCertPath(basePath)
	keyPath := makeKeyPath(basePath)
	p12Path := makeP12Path(basePath)
	return exec.Command("openssl", "pkcs12", "-export", "-in", certPath, "-inkey", keyPath, "-passout", "pass:secret", "-out", p12Path).Run()
}

func createJavaKeyStore(basePath, caPath string) error {
	err := exec.Command("keytool", "-importkeystore", "-srckeystore", makeP12Path(basePath), "-srcstoretype", "PKCS12", "-srcstorepass", "secret", "-destkeystore", basePath + "/keystore.jks", "-deststorepass",  "secret", "-destkeypass", "secret").Run()
	if err != nil {
		fmt.Println(err)
		return err
	}

	return exec.Command("keytool", "-importcert", "-file", caPath, "-storepass", "secret", "-noprompt", "-keystore", basePath + "/keystore.jks").Run()
}

func findCaAndDo(caName string, caList []*ca, cb func(*ca) error) error {
	for _, c := range caList {
		if c.name == c.name {
			return cb(c)
		}
	}
	return caNotFoundError
}

func findNameAndDo(name string, names []string, notFound error, cb func(string) error) error {
	for _, n := range names {
		if name == n {
			return cb(name)
		}
	}
	return notFound
}

func stringInArr(stuff []string, term string) bool {
	for _, val := range stuff {
		if val == term {
			return true
		}
	}
	return false
}

func makeServerPath(baseDir, caName, serverName string) string {
	return makeCaPath(baseDir, caName) + "/servers/" + serverName
}

func makeUserPath(baseDir, caName, userName string) string {
	return makeCaPath(baseDir, caName) + "/users/" + userName
}

func makeCaPath(baseDir, caName string) string {
	return baseDir + "/CAs/" + caName
}

func makeCertPath(basePath string) string {
	return basePath + "/crt.pem"
}

func makeBundlePath(basePath string) string {
	return basePath + "/bundle.pem"
}

func makeKeyPath(basePath string) string {
	return basePath + "/key.pem"
}

func makeP12Path(basePath string) string {
	return basePath + "/bundle.p12"
}

func savePem(path string, pemBytes []byte, pemType string) error {
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()
	return pem.Encode(f, &pem.Block{
		Type: pemType,
		Bytes: pemBytes,
	})
}

func saveBundleAsPEM(path string, certBytes []byte, caBytes []byte, pk *rsa.PrivateKey) error {
	f, err := os.Create(path)
	if err != nil {
		return err
	}
	defer f.Close()

	if err := pem.Encode(f, &pem.Block{
		Type: pemBlockTypeCert,
		Bytes: certBytes,
	}); err != nil {
		return err
	}

	if err := pem.Encode(f, &pem.Block{
		Type: pemBlockTypeRSAPK,
		Bytes: x509.MarshalPKCS1PrivateKey(pk),
	}); err != nil {
		return err
	}

	return pem.Encode(f, &pem.Block{
		Type: pemBlockTypeCert,
		Bytes: caBytes,
	})
}

func saveCertAsPEM(path string, certBytes []byte) error {
	return savePem(path, certBytes, pemBlockTypeCert)
}

func savePrivateKeyAsPEM(path string, pk *rsa.PrivateKey) error {
	return savePem(path, x509.MarshalPKCS1PrivateKey(pk), pemBlockTypeRSAPK)
}

func generateSubjectKeyId(pk *rsa.PrivateKey) ([]byte ,error) {
	subKeyId, err := asn1.Marshal(pk.PublicKey)
	if err != nil {
		return nil, fmt.Errorf("Cannot Create Subject Key Id: %s", err)
	}

	idHash := sha1.Sum(subKeyId)

	return idHash[:], nil
}

func getPkFromPath(path string) (*rsa.PrivateKey, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(data)
	if block.Type != pemBlockTypeRSAPK {
		return nil, fmt.Errorf("Invalid PEM block type; expected %s, got %s", pemBlockTypeRSAPK, block.Type)
	}
	return x509.ParsePKCS1PrivateKey(block.Bytes)
}

func getCertFromPath(path string) (*x509.Certificate, error) {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		return nil, err
	}

	block, _ := pem.Decode(data)
	if block.Type != pemBlockTypeCert {
		return nil, fmt.Errorf("Invalid PEM block type; expected %s, got %s", pemBlockTypeCert, block.Type)
	}
	return x509.ParseCertificate(block.Bytes)
}

func main() {
	app := kingpin.New("easycert", mainHelpText)

	baseDir, err := initBaseDir()
	app.FatalIfError(err, "Could not initialize base directory: %s", err)

	caList, err := readCAs(baseDir)
	app.FatalIfError(err, "Could not initialize application data: %s", err)

	cmdCa := app.Command("ca", "Manage easycert certificate authorities")
	cmdSvr := app.Command("server", "Manage easycert server certificates")
	cmdUsr := app.Command("user", "Manage easycert user certificates")

	lsCa := &listCaCmd{caList: caList, baseDir: baseDir}
	cmdLsCa := cmdCa.Command("list", "List existing easycert certificate authorities").Action(lsCa.run)
	cmdLsCa.Flag("verbose", "List the path to the folder for the CA instead of the name").BoolVar(&lsCa.verbose)

	mkCa := &makeCaCmd{caList: caList, baseDir: baseDir}
	cmdMkCa := cmdCa.Command("create", "Create a new certificate authority").Action(mkCa.run)
	cmdMkCa.Arg("CA Name", "Name of the certificate authority to create").Required().StringVar(&mkCa.caName)

	rmCa := &removeCaCmd{caList: caList, baseDir: baseDir}
	cmdRmCa := cmdCa.Command("remove", "Remove an existing certificate authority").Action(rmCa.run)
	cmdRmCa.Arg("CA Name", "Name of the certificate authority to remove").StringVar(&rmCa.caName)
	cmdRmCa.Flag("force", "Force removal of a certificate authority with existing servers/users").BoolVar(&rmCa.force)

	lsSvr := &listServerCmd{caList: caList, baseDir: baseDir}
	cmdLsSvr := cmdSvr.Command("list", "List existing easycert servers").Action(lsSvr.run)
	cmdLsSvr.Arg("CA Name", "Name of the certificate authority to list").Required().StringVar(&lsSvr.caName)
	cmdLsSvr.Flag("verbose", "List the path to the folder for the server certs instead of the server name").BoolVar(&lsSvr.verbose)

	mkSvr := &makeServerCmd{caList: caList, baseDir: baseDir}
	cmdMkSvr := cmdSvr.Command("create", "Create a server certifcate/key pair").Action(mkSvr.run)
	cmdMkSvr.Arg("CA Name", "Name of the certifcate authority to use").Required().StringVar(&mkSvr.caName)
	cmdMkSvr.Arg("Server Name", "Name of the server to create").Required().StringVar(&mkSvr.serverName)
	cmdMkSvr.Flag("p12", "Create a PKCS12 bundle along with PEM files - requires openssl to be installed").BoolVar(&mkSvr.makeP12)
	cmdMkSvr.Flag("jks", "Create a java keystore in jks format - requires a Java JDK and keytoo to be installed").BoolVar(&mkSvr.makeJks)
	cmdMkSvr.Flag("bundle", "Create a PEM bundle with public and private key and CA cert").BoolVar(&mkSvr.makeBundle)

	rmSvr := &removeServerCmd{caList: caList, baseDir: baseDir}
	cmdRmSvr := cmdSvr.Command("remove", "Remove a server from a certificate authority").Action(rmSvr.run)
	cmdRmSvr.Arg("CA Name", "Name of the certificate authority").Required().StringVar(&rmSvr.caName)
	cmdRmSvr.Arg("Server Name", "Name of the server to remove").Required().StringVar(&rmSvr.serverName)

	lsUsr := &listUserCmd{caList: caList, baseDir: baseDir}
	cmdLsUsr := cmdUsr.Command("list", "List existing easycert users").Action(lsUsr.run)
	cmdLsUsr.Arg("CA Name", "Name of the certificate authority to list").Required().StringVar(&lsUsr.caName)
	cmdLsUsr.Flag("verbose", "List the path to the folder containing the user certs").BoolVar(&lsUsr.verbose)

	mkUsr := &makeUserCmd{caList: caList, baseDir: baseDir}
	cmdMkUsr := cmdUsr.Command("create", "Create a user certificate/key pair").Action(mkUsr.run)
	cmdMkUsr.Arg("CA Name", "Name of the certificate authority").Required().StringVar(&mkUsr.caName)
	cmdMkUsr.Arg("Sid", "A unique id for the user").Required().StringVar(&mkUsr.userName)
	cmdMkUsr.Flag("p12", "Create a PKCS12 bundle along with PEM files - requires openssl to be installed").BoolVar(&mkUsr.makeP12)
	cmdMkUsr.Flag("bundle", "Create a PEM bundle with public and private key and CA cert").BoolVar(&mkUsr.makeBundle)

	rmUsr := &removeUserCmd{caList: caList, baseDir: baseDir}
	cmdRmUsr := cmdUsr.Command("remove", "Remove a user").Action(rmUsr.run)
	cmdRmUsr.Arg("CA Name", "Name of the certificate authority").Required().StringVar(&rmUsr.caName)
	cmdRmUsr.Arg("User Name", "Name of the user to remove").Required().StringVar(&rmUsr.userName)

	kingpin.MustParse(app.Parse(os.Args[1:]))
}

func initBaseDir() (string, error) {
	currentUser, err := user.Current()
	if err != nil {
		return "", err
	}

	baseDir := currentUser.HomeDir + "/.easycert"

	return baseDir, os.MkdirAll(baseDir+"/CAs", 0700)
}

func readCAs(baseDir string) ([]*ca, error) {
	caList := []*ca{}

	caNames, err := getDirNames(baseDir + "/CAs")
	if err != nil {
		return caList, err
	}

	for _, val := range caNames {
		caPath := makeCaPath(baseDir, val)

		userNames, err := getDirNames(caPath + "/users")
		serverNames, err := getDirNames(caPath + "/servers")
		if err != nil {
			return caList, err
		}

		caList = append(caList, &ca{val, serverNames, userNames})
	}

	return caList, nil
}

func getDirNames(path string) ([]string, error) {
	names := []string{}

	dir, err := os.Open(path)
	if err != nil {
		if os.IsNotExist(err) {
			return names, nil
		}
		return names, err
	}
	defer dir.Close()

	infoList, err := dir.Readdir(0)
	if err != nil {
		return names, err
	}

	for _, val := range infoList {
		if val.IsDir() {
			names = append(names, val.Name())
		}
	}

	return names, nil
}
