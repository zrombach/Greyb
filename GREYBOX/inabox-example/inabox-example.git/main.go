package main

import (
	"net/http"
	"fmt"
	"log"
	"crypto/tls"
	"flag"
)

var (
	certPath string
	keyPath string
	caPath string
	authUrl string

	htmlSuccess = "<!doctype html><h1>Hello %s</h1>"
	htmlNotAuth = "<!doctype html><h1>Not Authorized</h1>"
)

type AuthportResponse struct {
	Dn string `json:"dn"`
	FullName string `json:"fullName"`
}

func init() {
	flag.StringVar(&certPath, "cert", "", "Path to the ssl certificate")
	flag.StringVar(&keyPath, "key", "", "Path to the ssl server")
	flag.StringVar(&caPath, "ca", "", "Path to the signing certificate used to verify client certificates")
	flag.StringVar(&authUrl, "auth-url", "", "Url for the authorization service")
	flag.Parse()
}

func main() {
	certVerifier, err := NewAuthenticator(SetCAPath(caPath))
	if err != nil {
		log.Fatalf("Could not initialize certificate verifier: %s", err)
	}

	authClient, err := NewAuthClient(authUrl, SetRootCA(caPath), SetX509(certPath, keyPath))
	if err != nil {
		log.Fatalf("Could not initialize authport client: %s", err)
	}

	mux := http.NewServeMux()
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {

		//Check Certificate
		subj, err := certVerifier.VerifyTLS(r.TLS)
		if err != nil {
			log.Printf("Certificate verification failed: %s", err)
			w.WriteHeader(401)
			fmt.Fprint(w, htmlNotAuth)
			return
		}
		userDn := SubjectToDn(subj)
		var userData AuthportResponse

		//Authport Check
		if err := authClient.GetUserInfo(userDn, &userData); err != nil {
			log.Printf("Authport check failed: %s", err)
			w.WriteHeader(401)
			fmt.Fprintf(w, htmlNotAuth)
			return
		}

		log.Printf("Authentication and Authorization was successful for user %s", userData.Dn)

		//Proceed with the rest of the request/response handling
		if r.Method != "GET" {
			log.Println("Invalid request method: Only GET is supported")
			w.WriteHeader(405)
			return
		}

		fmt.Fprintf(w, htmlSuccess, userData.FullName)
	})

	server := &http.Server{
		TLSConfig: &tls.Config{
			ClientAuth: tls.RequestClientCert,
		},
		Addr: ":8000",
		Handler: mux,
	}

	log.Fatal(server.ListenAndServeTLS(certPath, keyPath))
}
