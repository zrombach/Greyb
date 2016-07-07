## Easycert
Easycert makes it simple to manage a public key infrastructure for your development environment. Its main use case is managing a certificate authority that you want to use across multiple development projects. Use Easycert to create multiple server and user certificates that are all signed by the same Certificate Authority (CA). By default it will create public and private keys as PEM files, but has options for creating p12 (requires openssl) and JKS (requires a JDK) files as well. Easycert is not for managing PKI in production environments.

## Building the project

1. [Install go](https://golang.org/dl/)
2. [Install gb](http://getgb.io/)
3. `git clone git@gitlab.lab24.com:netcraft/easycert.git`
4. Run `gb build all`
5. The `easycert` binary will be in `./bin`

## Getting Started
1. Run `easycert` to see usage and help text

```
$ easycert

usage: easycert [<flags>] <command> [<args> ...]

Easycert makes it simple to manage a public key infrastructure for your development environment. Its main use case is managing a certificate authority that you want to use across multiple development
projects. Use Easycert to create multiple server and user certificates that are all signed by the same Certificate Authority (CA). By default it will create public and private keys as PEM files, but has options for creating p12
(requires openssl) and JKS (requires a JDK) files as well. Easycert is not for managing PKI in production environments.

Flags:
  --help  Show help (also see --help-long and --help-man).

Commands:
  help [<command>...]
    Show help.

  ca list [<flags>]
    List existing easycert certificate authorities

  ca create <CA Name>
    Create a new certificate authority

  ca remove [<flags>] [<CA Name>]
    Remove an existing certificate authority

  server list [<flags>] <CA Name>
    List existing easycert servers

  server create [<flags>] <CA Name> <Server Name>
    Create a server certifcate/key pair

  server remove <CA Name> <Server Name>
    Remove a server from a certificate authority

  user list [<flags>] <CA Name>
    List existing easycert users

  user create [<flags>] <CA Name> <Sid>
    Create a user certificate/key pair

  user remove <CA Name> <User Name>
    Remove a user
```

### Ca

```
$ easycert ca --help

usage: easycert ca <command> [<args> ...]

Manage easycert certificate authorities

Flags:
  --help  Show help (also see --help-long and --help-man).

Subcommands:
  ca list [<flags>]
    List existing easycert certificate authorities

  ca create <CA Name>
    Create a new certificate authority

  ca remove [<flags>] [<CA Name>]
    Remove an existing certificate authority
```

### Server

```
$ easycert server --help

usage: easycert server <command> [<args> ...]

Manage easycert server certificates

Flags:
  --help  Show help (also see --help-long and --help-man).

Subcommands:
  server list [<flags>] <CA Name>
    List existing easycert servers

  server create [<flags>] <CA Name> <Server Name>
    Create a server certifcate/key pair

  server remove <CA Name> <Server Name>
    Remove a server from a certificate authority
```

### User
```
$ easycert user --help

usage: easycert user <command> [<args> ...]

Manage easycert user certificates

Flags:
  --help  Show help (also see --help-long and --help-man).

Subcommands:
  user list [<flags>] <CA Name>
    List existing easycert users

  user create [<flags>] <CA Name> <Sid>
    Create a user certificate/key pair

  user remove <CA Name> <User Name>
    Remove a user
```

## Common Usages

* Create a Certificate Authority:
```
easycert ca create MyCa
```

* Create a server certificate/key with hostname `localhost` signed by MyCa:
```
easycert server create MyCa localhost
```

* Create a server ceritifcate/key and jks with hostname `dockervm` signed by MyCa:
```
easycert server create --jks MyCa dockervm
```

* Create a user certificate/key as a p12 bundle signed by MyCa:
```
easycert user create --p12 MyCa "My User Common Name"
```



## Where are My Certs?
Easycert stores all certificates and keys under `$HOME/.easycert`. You can see the paths to folders that contain the files for a particular CA, server, or use by using the list sub command along with the --verbose flag. Run the online help for more info.
