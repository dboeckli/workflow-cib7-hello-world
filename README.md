# Camunda Hello World Template

- [Description](#description)
- [Prerequisites](#prerequisites)
    - [Eclipse specific](#eclipse-specific)
- [Build](#build)
- [Usage](#usage)

## Description

Write your own description here

### URLS

- Camunda: http://localhost:8081/bpm/camunda/app/welcome/default/#!/welcome
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

Start all: kubectl apply -f k8s/
Stop all: kubectl delete all --all

Camunda ist reachable on port: 30081
http://localhost:30081/bpm/camunda/app/welcome/default/#!/welcome

