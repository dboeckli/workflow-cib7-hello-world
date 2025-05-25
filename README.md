# Camunda Hello World Template

- [Description](#description)
- [Prerequisites](#prerequisites)
- [Build](#build)
- [Kubernetes](#kubernetes)

## Description

### URLS

- Camunda: http://localhost:8081/bpm/camunda/app/welcome/default/#!/welcome
- Cib7: http://localhost:8081/bpm/webapp
- Actuator: http://localhost:8081/bpm/actuator
- Openapi:
  - http://localhost:8081/bpm/swagger/v3/api-docs
  - http://localhost:8081/bpm/swagger/v3/api-docs.yaml
  - http://localhost:8081/bpm/swagger-ui/index.html
  
- H2 Console: http://localhost:8081/bpm/h2-console (in the connection jdbc url use: jdbc:h2:mem:workflow-hello-world)
- Rest Api:
  - http://localhost:8081/bpm/restapi/camunda
  - http://localhost:8081/bpm/restapi/ping
  - http://localhost:8081/bpm/restapi/workflow

### Servers

- local: localhost, database on h2 locally


## Prerequisites

- Java 21
- Camunda 7.21
- Spring Boot 3.3.1
- Maven 3.6.3 (Older versions might cause build problems)
- *_/home/$username/.m2/settings.xml_* is set
  up [Help](https://swp-confluence.atlassian.net/wiki/spaces/SWPIT/pages/411173348/How+to+Install+and+setup+maven#Setting-up-the-maven-settings)
- there are two run configs which requires passwords (ldap, postgres). therefore you need to edit/rename in the src/main/conf folder the changeme-local.conf and changeme-ci.conf to ci.conf and local.conf.

## Build

| Usage         | Action      |
|---------------|-------------|
| Clean project | mvn clean   |
| Build project | mvn package |
| Test project  | mvn test    |

## Kubernetes

### Deployment

To deploy all resources:
```bash
kubectl apply -f k8s/
```

To remove all resources:
```bash
kubectl delete -f k8s/
```

Check
```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

### Accessing Services

#### LDAP Database

- URL: ldap://localhost:30389
- User: cn=admin,dc=example,dc=ch
- Password: password

#### Camunda

- URL: http://localhost:30081/bpm/camunda/app/welcome/default/#!/welcome

All rest services can be executed via the httprequest folder using the k8s environment setting
