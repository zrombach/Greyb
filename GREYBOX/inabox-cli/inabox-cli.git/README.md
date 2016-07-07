## InABox Command Line Interface
The InABox CLI is a developer tool that provides a much easier and less error-prone way to get up and running with the the InABox services. What would otherwise take several long and tedious commands typed into a terminal can now be accomplished with a single easy to remember invocation: `inabox up`. The InABox CLI also integrates management of various entities (Users, Projects, Groups, etc) across services as well as combining it with management of PKI. For example, with a single `inabox create user` command, you can add a user to all InABox services as well as create the neccessary certificates for that user.

## Getting Started

### Docker
InABox CLI requires that [Docker](https://www.docker.com/) be installed on your system. If you are running it on Windows or Mac OS X, it is recomended that you install Docker using [Docker Toolbox](https://www.docker.com/toolbox).

### InABox Docker Containers
Currently there is no publicly accessible docker repository where the InABox Docker images can be hosted. Therefore they must be obtained from Lab 24 or built locally. If you have obtained the images on a disk, they will be in tar archive and can be loaded into Docker via the Docker [load](https://docs.docker.com/reference/commandline/load/) command. If you wish to build the projects yourself, please consult the project specific documentation. The following images are needed to run the InABox CLI:
    1. [docker.lab24.co/netcraft/authviarest](https://gitlab.lab24.com/inabox/auth-service)
    2. [docker.lab24.co/inabox/webapis](https://gitlab.lab24.com/inabox/webapis)

### Installing the InABox CLI
The InABox CLI is a single executable file. Once you have obtained/built it for your OS, simply copy it somewhere on your path.

### Running the InABox CLI
The InABox CLI includes an interactive help system that will guide through using what few commands there are to learn. Invoke it with `inabox help`. You can also type out any command and use `--help` to see instructions for that command. E.g. `inabox create server --help`.

To get up and running, simply type `inabox up`. The utility will create and start the neccessary docker containers, as well as create a certificate authority and the needed certificates for those containers. It will also give you feedback as to where you can access the InABox components (urls, ports, etc).

## Building the InABox CLI
The InABox CLI is written and [go](https://golang.org/). It uses [gb](http://getgb.io/docs/install/) as a build tool. You must install them both in order to build the project. The project can then be built with `gb build all`, invoked from anywhere within the projects directory tree.
