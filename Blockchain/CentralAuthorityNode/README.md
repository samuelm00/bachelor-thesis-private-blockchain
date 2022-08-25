# Authority Node

## Configs:

- **application.properties:**

```properties

# jpa configs to access databse
spring.jpa.hibernate.ddl-auto=update
spring.datasource.url=jdbc:postgresql://localhost:5432/ledger
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.datasource.username=postgres
spring.datasource.password=root
server.port=8080
# The port that is used inside the P2PNetwork
p2p.port=10000
# The host that is used inside the P2PNetwork
p2p.host=localhost
# Secret string that is used for the JWT generation 
jwt.secret=secret
```

- **validation-nodes.yml**

```yaml
# All the validators have to specified in this file otherwise they will never be selected as primary node
validation-nodes:
  connections:
    - ip: localhost
      # The port that the validation listens to in the P2PServer 
      port: 9002
      # Unique Id
      publicKey: 1
      # The port of the webserver (Spring boot)
      serverPort: 9001
    - ip: localhost
      port: 9003
      publicKey: 2
      serverPort: 9004
    - ip: localhost
      port: 9005
      publicKey: 3
      serverPort: 9006
```
