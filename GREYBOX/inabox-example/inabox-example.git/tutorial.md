## InABox Tutorial
This project demonstrates a simple test of the InABox authorization services and PKI management tools. This tutorial assumes you have already installed [Docker](https://docs.docker.com/installation/) and that you are connected to the Internet.

### Step 1: Load the InABox Docker Images
From your terminal, run the following commands:
```
$ docker load /$PATH_TO_CD/docker-images/authviarest.tar

$ docker load /$PATH_TO_CD/docker-images/webapis.tar
```

### Step 3: Initialize InABox
From your terminal, run the following commands:

```
$ /$PATH_TO_CD/inabox-cli/macos/inabox up

$ /$PATH_TO_CD/inabox-cli/macos/inabox create host localhost

$ /$PATH_TO_CD/inabox-cli/macos/inabox create user testUser

```

Take note of the URL for the authport service and the path to the server and user certificates.

### Step 4: Start the example App
From your terminal, run the following command. Use the certificate path for the server and URL for authport as obtained in Step 3. For example in the following command the authport URL was `https://servicehost:32770` and the path to the server certificates was `$HOME/inabox/pki/servers/localhost/`:
```
$ /$PATH_TO_CD/inabox-example/macos/inabox-example \
    --cert $HOME/inabox/pki/servers/localhost/crt.pem \
    --key $HOME/inabox/pki/servers/localhost/key.pem \
    --ca $HOME/inabox/pki/servers/localhost/cacrt.pem \
    --auth-url https://servicehost:32770
```

### Step 5: Install User Certificate
Install the User certificate (bundle.p12 under the path for user certificates as obtained in Step 3) generated in Step 3 in your browser. On Mac OS X this can be done by opening up the `Keychain Access` tool and installing the certificate under `Certificates` of the `login` chain. The password for the certificate bundle is `changeme`.

### Step 6.
Visit the site in your browser. The URL is `https://localhost:8000`. You may need to bypass/ignore warnings about an insecure site. If everything worked you will see a hello message.