# Camunda Hello World Template

- [Description](#description)
- [Prerequisites](#prerequisites)
- [Build](#build)
- [Kubernetes](#kubernetes)

## Description

### URLS

Use 8080 when started locally or 30080 in Kubernetes

- Camunda Cockpit: 
  - http://localhost:8080/camunda/app/welcome/default/#!/welcome
  - http://localhost:30080/camunda/app/welcome/default/#!/welcome
- CIB-7 Cockpit: 
  - http://localhost:8080/webapp
  - http://localhost:30080/webapp
- Actuator: 
  - http://localhost:8080/actuator
  - http://localhost:30080/actuator
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
  - http://localhost:8080/restapi/camunda or http://localhost:30080/restapi/camunda
  - http://localhost:8080/restapi/ping or http://localhost:30080/restapi/ping
  - http://localhost:8080/restapi/workflow or http://localhost:30080/restapi/workflow

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

### Deployment with Helm

To run maven filtering for destination target/helm run:
```bash
mvn clean install -DskipTests 
```

Be aware that we are using a different namespace here (not default).

Go to the directory where the tgz file has been created after 'mvn install'
```powershell
cd target/helm/repo
```

unpack
```powershell
$file = Get-ChildItem -Filter workflow-cib7-hello-world-v*.tgz | Select-Object -First 1
tar -xvf $file.Name
```

install
```powershell
$APPLICATION_NAME = Get-ChildItem -Directory | Where-Object { $_.LastWriteTime -ge $file.LastWriteTime } | Select-Object -ExpandProperty Name
helm upgrade --install $APPLICATION_NAME ./$APPLICATION_NAME --namespace workflow-cib7-hello-world --create-namespace --wait --timeout 5m --debug
```

show logs and show event
```powershell
kubectl get pods -n workflow-cib7-hello-world
```
replace $POD with pods from the command above
```powershell
kubectl logs $POD -n workflow-cib7-hello-world --all-containers
```

Show Details and Event

$POD_NAME can be: workflow-cib7-hello-world-mongodb, workflow-cib7-hello-world
```powershell
kubectl describe pod $POD_NAME -n workflow-cib7-hello-world
```

Show Endpoints
```powershell
kubectl get endpoints -n workflow-cib7-hello-world
```

uninstall
```powershell
helm uninstall $APPLICATION_NAME --namespace workflow-cib7-hello-world
```

delete all
```powershell
kubectl delete all --all -n workflow-cib7-hello-world
```

create busybox sidecar
```powershell
kubectl run busybox-test --rm -it --image=busybox:1.36 --namespace=workflow-cib7-hello-world --command -- sh
```

You can use the actuator rest call to verify via port 30080