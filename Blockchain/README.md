# BA05 Privte Blockcchain

## Prerequisities:

- [Docker](https://www.docker.com/)
- [Maven](https://www.apache.org/)
- [Java](https://java.com/de/)

## How to run:

Install dependencies and build **jar-files**:

```bash
# command has to be executed in the root directory
2 # in order to build and install all dependencies
3 mvn install
4 # or this command to skip tests
5 mvn install - DskipTests
```

Start Blockchain:

```bash 
# change to BlockchainDemo directory
2 cd BlockchainDemo / target
3 # execute jar file
4 java - jar BLOCKCHAIN_DEMO . jar
```

Start ClientNode to interact with the Blockchain through the CLI

```bash 
# change to ClientNode directory
2 cd ClientNode / target
3 # execute jar file
4 java - jar ClientNode -0.0.1 - SNAPSHOT . jar
```

## CLI Commands:

```bash 
# This command will show all commands and how to use them
2 shell:> help
3
4 # Generates a key pair
5 shell:> generate-keys
6 # Register with the public key
7 shell:> register --register ChooseYourPasssword
8 # Login ( password is auto saved by the CLI )
9 shell:> login
10
11 # Now it is possible to use the private blockchain
12 shell:> post-tweet --tweet " This is a tweet "
13 shell:> get-my-tweets
14 shell:> send-invalid-tweet
15 shell:> get-tweet --hash someHashValue
```

## Testing:

The project uses `JUnit` in combination with `testontainers` for testing

```bash
# start all tests
mvn test
```