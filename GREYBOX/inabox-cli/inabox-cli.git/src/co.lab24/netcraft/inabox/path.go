package main

import (
	"os/user"
	"path"
)

const (
	KeyFileName  = "key.pem"
	CertFileName = "crt.pem"
	P12FileName  = "bundle.p12"
	JksFileName  = "keystore.jks"
	CaCertFileName = "cacrt.pem"
	serialFileName = "serial.txt"
)

func getInaboxDir(user *user.User) string {
	return user.HomeDir + "/inabox"
}

func getSerialFilePath(user *user.User) string {
	return path.Join(getInaboxCaDir(user), serialFileName)
}

func getInaboxPkiDir(user *user.User) string {
	return getInaboxDir(user) + "/pki"
}

func getInaboxCaDir(user *user.User) string {
	return getInaboxPkiDir(user) + "/ca"
}

func getInaboxServersDir(user *user.User) string {
	return getInaboxPkiDir(user) + "/servers"
}

func getInaboxUsersDir(user *user.User) string {
	return getInaboxPkiDir(user) + "/users"
}

func getUserPathF(cn string) func(*user.User) string {
	return func(user *user.User) string {
		return getInaboxUsersDir(user) + "/" + cn
	}
}

func getServerPathF(cn string) func(*user.User) string {
	return func(user *user.User) string {
		return getInaboxServersDir(user) + "/" + cn
	}
}

func getKeyPath(user *user.User, pathF func(user *user.User) string) string {
	return pathF(user) + "/" + KeyFileName
}

func getCrtPath(user *user.User, pathF func(user *user.User) string) string {
	return pathF(user) + "/" + CertFileName
}

func getJksPath(user *user.User, pathF func(user *user.User) string) string {
	return pathF(user) + "/" + JksFileName
}

func getP12Path(user *user.User, pathF func(user *user.User) string) string {
	return pathF(user) + "/" + P12FileName
}

func getCertsDirectory(user *user.User, pathF func(user *user.User) string) string {
	return pathF(user)
}
