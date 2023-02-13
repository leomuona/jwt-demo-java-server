# MaiBB server

REST API server for MaiBB

## Development setup

# SSL

Install localhost development TLS certificate:

```sh
mkcert -key-file key.pem -cert-file cert.pem localhost
```

# Startup

Check that you have Java 17 JDK in use (i.e. OpenJDK 17).

Start project direcly: `./gradlew runBoot`

Or you can build and start:
```sh
./gradlew build
java -jar build/libs/server-0.0.1-SNAPSHOT.jar
```

# Authentication token

Couple curls for your needs:
```sh
curl -v -X POST -k -H "Content-Type: application/json" -d '{"username":"namehere","password":"passwordhere"}' https://localhost:8443/auth/login
curl -v -X GET -k -H "Authorization: Bearer tokenhere" https://localhost:8443/hello
```

## License
This project is licensed under MIT License. See LICENSE file in the root directory of this project for details.

