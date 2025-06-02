# Camunda Hello World Template

- [Description](#description)
- [Prerequisites](#prerequisites)
- [Build](#build)
- [Kubernetes](#kubernetes)

## Description

### URLS

Use 8080 when started locally or 30080 in Kubernetes

- Camunda Cockpit: http://localhost:8080/camunda/app/welcome/default/#!/welcome
- CIB-7 Cockpit: http://localhost:8080/webapp
- Actuator: http://localhost:8080/actuator
- Openapi:
  - http://localhost:8080/swagger/v3/api-docs
  - http://localhost:8080/swagger/v3/api-docs.yaml
  
  - http://localhost:8080/swagger/v3/api-docs/camunda-engine-rest-api
  - http://localhost:8080/swagger/v3/api-docs/camunda-engine-rest-api.yaml
  
  - http://localhost:8080/swagger/v3/api-docs/actuator
  - http://localhost:8080/swagger/v3/api-docs/actuator.yaml
  
  - http://localhost:8080/swagger/v3/api-docs/restapi
  - http://localhost:8080/swagger/v3/api-docs/restapi.yaml
  
  - http://localhost:8080/swagger-ui/index.html
  
- H2 Console: http://localhost:8080/h2-console (in the connection jdbc url use: jdbc:h2:mem:workflow-hello-world)
- Rest Api:
  - http://localhost:8080/restapi/camunda
  - http://localhost:8080/restapi/ping
  - http://localhost:8080/restapi/workflow

### Accessing Services

#### LDAP Database

- URL: ldap://localhost:30388 or ldap://localhost:388
- User: cn=admin,dc=example,dc=ch
- Password: password


All rest services can be executed via the httprequest folder using the k8s environment setting

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

To run maven filtering for destination target/k8s and destination target/helm run:
```bash
mvn clean install -DskipTests 
```

### Deployment with Kubernetes

Deployment goes into the default namespace.

To deploy all resources:
```bash
kubectl apply -f target/k8s/
```

To remove all resources:
```bash
kubectl delete -f target/k8s/
```

Check
```bash
kubectl get deployments -o wide
kubectl get pods -o wide
```

You can use the actuator rest call to verify via port 30080