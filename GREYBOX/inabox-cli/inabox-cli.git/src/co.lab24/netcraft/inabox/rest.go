package main

import (
	"bytes"
	"crypto/x509"
	"crypto/x509/pkix"
	"encoding/json"
	"fmt"
	"net/http"
	"strings"
)

const (
	AuthPortUserPath = "/extras/users"
)

type User struct {
	Dn             string   `json:"dn"`
	LastName       string   `json:"lastName"`
	FullName       string   `json:"fullName"`
	Uid            string   `json:"uid"`
	Classification []string `json:"classification"`
}

type RestClient struct {
	client *http.Client
	Host   string
	Port   string
}

func NewRestClient() *RestClient {
	return &RestClient{
		client: &http.Client{},
	}
}

func (this *RestClient) PutUser(cert *x509.Certificate) error {
	dnString := SubjectToDn(&cert.Subject)
	data, err := json.Marshal(&User{
		Dn:             dnString,
		LastName:       cert.Subject.CommonName,
		FullName:       cert.Subject.CommonName,
		Uid:            cert.Subject.CommonName,
		Classification: []string{},
	})
	if err != nil {
		return err
	}

	req, err := http.NewRequest("PUT", "http://"+this.Host+":"+this.Port+AuthPortUserPath+"/"+dnString, bytes.NewBuffer(data))
	if err != nil {
		return err
	}
	req.Header.Add("Content-Type", "application/json")
	_, err = this.client.Do(req)
	return err
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
