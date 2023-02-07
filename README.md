# MaiBB server

REST API server for MaiBB

## Development setup

Check that you have Java 17 JDK in use (i.e. OpenJDK 17).

Start project direcly: `./gradlew runBoot`

Or you can build and start:
```
./gradlew build
java -jar build/libs/server-0.0.1-SNAPSHOT.jar
```

# Authentication token

Couple curls for your needs:
```
curl -v -X POST -H "Content-Type: application/json" -d '{"username":"namehere","password":"passwordhere"}' http://localhost:8080/authenticate
curl -v -X GET -H "Authorization: Bearer tokenhere" http://localhost:8080/hello
```

## License
This project is licensed under MIT License. See LICENSE file in the root directory of this project for details.

