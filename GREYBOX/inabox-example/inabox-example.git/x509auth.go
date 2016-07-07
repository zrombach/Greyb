package main

import (
	"crypto/tls"
	"crypto/x509"
	"crypto/x509/pkix"
	"errors"
	"io/ioutil"
	"time"
	"fmt"
	"strings"
)

type Authenticator struct {
	rootCerts *x509.CertPool
}

//Returns an initialized Authenticator. Can take an number of function arguments
//that are used to configure the resulting Authenticator (although, there is
//currently ony one such configuration function - SetCAPAth).
func NewAuthenticator(options ...func(*Authenticator) error) (*Authenticator, error) {
	authenticator := Authenticator{}

	for _, option := range options {
		if err := option(&authenticator); err != nil {
			return &authenticator, err
		}
	}

	return &authenticator, nil
}

//SetCAPath returns a setter that can be passed to NewAuthenticator in order to configure
//the CA used to authenticate client certificates. By default the authenticator uses
//the system root CAs to do client authentication
//Example: authenticator, err := NewAuthenticator(SetCAPath("path/to/root/ca"))
func SetCAPath(path string) func(*Authenticator) error {
	return func(authenticator *Authenticator) error {
		certPool := x509.NewCertPool()
		clientCa, err := ioutil.ReadFile(path)
		if err != nil {
			return err
		}
		if !certPool.AppendCertsFromPEM(clientCa) {
			return errors.New("Can't parse client certificate authority")
		}
		authenticator.rootCerts = certPool
		return nil
	}
}

//VerifyTLS performs basically the same task as the tls handshake in the standard
//library - the main difference is that this implementation does not enforce
//extended key usage checks - this is a requirement for the particular PK infrastructure.
//In use by the customer. If client authentication fails, then a non nil error
//is returned. If authentication is successful the certificates Subject is returned.
func (this *Authenticator) VerifyTLS(t *tls.ConnectionState) (*pkix.Name, error) {
	certs := t.PeerCertificates

	if len(certs) == 0 {
		return nil, errors.New("No Certificate Received From Client")
	}

	opts := x509.VerifyOptions{
		Roots:         this.rootCerts,
		CurrentTime:   time.Now(),
		Intermediates: x509.NewCertPool(),
		KeyUsages:     []x509.ExtKeyUsage{x509.ExtKeyUsageAny},
	}

	for _, cert := range certs[1:] {
		opts.Intermediates.AddCert(cert)
	}
	chains, err := certs[0].Verify(opts)
	if err != nil {
		return nil, err
	}
	t.VerifiedChains = chains

	return &certs[0].Subject, nil
}

func SubjectToDn(subject *pkix.Name) string {
	orgUnits := ""
	start := len(subject.OrganizationalUnit) - 1

	for i := start; i >= 0; i-- {
		orgUnits += fmt.Sprintf("ou=%s,", subject.OrganizationalUnit[i])
	}

	return fmt.Sprintf(
		"cn=%s,%so=%s,c=%s",
		subject.CommonName,
		orgUnits,
		strings.Join(subject.Organization, ",o="),
		strings.Join(subject.Country, ",c="),
	)
}