package main

import (
	"bytes"
	"crypto/tls"
	"crypto/x509"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"net/url"
	"strings"
)

type Client struct {
	httpClient *http.Client
	url        *url.URL
}

func NewAuthClient(u string, opts ...func(*Client) error) (*Client, error) {
	url, err := url.Parse(u)
	if err != nil {
		return nil, fmt.Errorf("Could not parse the server url: %s", err)
	}

	c := &Client{
		httpClient: &http.Client{},
		url:        url,
	}

	for _, opt := range opts {
		if err := opt(c); err != nil {
			return nil, err
		}
	}

	return c, nil
}

func getOrCreateTransport(c *Client) *http.Transport {
	if c.httpClient.Transport == nil {
		c.httpClient.Transport = &http.Transport{
			TLSClientConfig: &tls.Config{
				ServerName: strings.Split(c.url.Host, ":")[0],
				MaxVersion: tls.VersionTLS10,
			},
		}
	}
	return c.httpClient.Transport.(*http.Transport)
}

func SetX509(cert, key string) func(*Client) error {
	return func(c *Client) error {
		t := getOrCreateTransport(c)
		clientCert, err := tls.LoadX509KeyPair(cert, key)
		if err != nil {
			return fmt.Errorf("Could not load client cert/key pair: %s", err)
		}

		t.TLSClientConfig.Certificates = []tls.Certificate{clientCert}
		return nil
	}
}

func SetRootCA(ca string) func(*Client) error {
	return func(c *Client) error {
		t := getOrCreateTransport(c)
		caCertPool := x509.NewCertPool()
		caCert, err := ioutil.ReadFile(ca)
		if err != nil {
			return fmt.Errorf("Could not read the CA cert file: %s", err)
		}
		if !caCertPool.AppendCertsFromPEM(caCert) {
			return fmt.Errorf("Could not load the CA certificate")
		}
		t.TLSClientConfig.RootCAs = caCertPool
		return nil
	}
}

func (c *Client) GetUserInfo(id string, info interface{}) error {
	req, err := http.NewRequest("GET", c.url.String()+"/users/"+id, nil)
	if err != nil {
		return err
	}
	req.Header.Add("Accept", "application/json")

	response, err := c.httpClient.Do(req)
	if err != nil {
		return err
	}
	defer response.Body.Close()

	data, err := ioutil.ReadAll(response.Body)
	if err != nil {
		return err
	}

	//Trim leading /* and trailing */ if present
	userData := bytes.TrimSuffix(bytes.TrimPrefix(data, []byte{47, 42}), []byte{42, 47})

	if err := json.Unmarshal(userData, info); err != nil {
		return err
	}

	return nil
}
