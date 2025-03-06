#!/bin/bash

set -o errexit  # abort on nonzero exit status
set -o nounset  # abort on unbound variable
set -o pipefail # don't hide errors within pipes
set -o errtrace # Ensure the error trap handler is inherited

readonly ACTION_RUNNER_DEPLOYMENT_WORKING_DIR="/opt/actions-runner/deployment-$(date +'%Y-%m-%d_%T')"
readonly BACKUP_DIR="backup"

if [ -z "${FORCE_DEPLOY}" ]; then
  FORCE_DEPLOY="false"
fi

readonly RUNTIME_DIR RUNTIME_USER BRANCH_MVN_VERSION BRANCH_NAME CAMUNDA_DB_SECRET LDAP_SECRET ENVIRONMENT ARTIFACT_ID ARTIFACTORY_URL GROUP_ID

function verifyParameter() {
  echo "Environment Variables are:"
  echo "Variable FORCE_DEPLOY: ${FORCE_DEPLOY}"

  if [[ ${ARTIFACTORY_URL} && ${ARTIFACTORY_URL-x} ]]; then
    echo "[INFO] ARTIFACTORY_URL: ${ARTIFACTORY_URL}"
  else
    echo "[ERROR] Missing mandatory environment variable. ARTIFACTORY_URL was empty"
    exit 1
  fi

  if [[ ${ARTIFACT_ID} && ${ARTIFACT_ID-x} ]]; then
    echo "[INFO] ARTIFACTORY_URL: ${ARTIFACT_ID}"
  else
    echo "[ERROR] Missing mandatory environment variable. ARTIFACT_ID was empty"
    exit 1
  fi

  if [[ ${GROUP_ID} && ${GROUP_ID-x} ]]; then
    echo "[INFO] ARTIFACTORY_URL: ${GROUP_ID}"
  else
    echo "[ERROR] Missing mandatory environment variable. GROUP_ID was empty"
    exit 1
  fi

  if [[ ${BRANCH_NAME} && ${BRANCH_NAME-x} ]]; then
    echo "[INFO] BRANCH_NAME: ${BRANCH_NAME}"
  else
    echo "[ERROR] Missing mandatory environment variable. BRANCH_NAME was empty"
    exit 1
  fi

  if [[ ${BRANCH_MVN_VERSION} && ${BRANCH_MVN_VERSION-x} ]]; then
    echo "[INFO] BRANCH_MVN_VERSION: ${BRANCH_MVN_VERSION}"
  else
    echo "[ERROR] Missing mandatory environment variable. BRANCH_MVN_VERSION was empty"
    exit 1
  fi

  if [[ ${RUNTIME_DIR} && ${RUNTIME_DIR-x} ]]; then
    echo "[INFO] RUNTIME_DIR: ${RUNTIME_DIR}"
  else
    echo "[ERROR] Missing mandatory environment variable. RUNTIME_DIR was empty"
    exit 1
  fi

  if [[ ${RUNTIME_USER} && ${RUNTIME_USER-x} ]]; then
    echo "[INFO] RUNTIME_DIR: ${RUNTIME_USER}"
  else
    echo "[ERROR] Missing mandatory environment variable. RUNTIME_USER was empty"
    exit 1
  fi

  if [[ ${CAMUNDA_DB_SECRET} && ${CAMUNDA_DB_SECRET-x} ]]; then
    echo "[INFO] CAMUNDA_DB_SECRET has been set"
  else
    echo "[ERROR] Missing mandatory environment variable. CAMUNDA_DB_SECRET was empty"
    exit 1
  fi

  if [[ ${LDAP_SECRET} && ${LDAP_SECRET-x} ]]; then
    echo "[INFO] LDAP_SECRET has been set"
  else
    echo "[ERROR] Missing mandatory environment variable. LDAP_SECRET was empty"
    exit 1
  fi

  if [[ ${ENVIRONMENT} && ${ENVIRONMENT-x} ]]; then
    echo "[INFO] TOMCAT_NAME: ${ENVIRONMENT}"
  else
    echo "[ERROR] Missing mandatory environment variable. ENVIRONMENT was empty"
    exit 1
  fi
}

function getArtefacts() {
  if [[ "${FORCE_DEPLOY}" == "true" ]] || [[ "${BRANCH_NAME}" == "main" ]]; then
    set -x
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.6.1:purge-local-repository \
      -DmanualInclude=${GROUP_ID}:"${ARTIFACT_ID}" \
      -DreResolve=false \
      -X \
      -s "${GITHUB_WORKSPACE}/.github/.m2/artifactory/settings.xml" \
      -Partifactory \
      -e
    mvn org.apache.maven.plugins:maven-dependency-plugin:2.10:get \
      -X \
      -s "${GITHUB_WORKSPACE}/.github/.m2/artifactory/settings.xml" \
      -Partifactory \
      -e \
      -U \
      -DgroupId="${GROUP_ID}" \
      -DartifactId="${ARTIFACT_ID}" \
      -Dversion="${BRANCH_MVN_VERSION}" \
      -Dpackaging=jar \
      -Dtransitive=false \
      -DrepositoryId=artifactory \
      -Ddest="${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar"
    set +x

    if [ -f "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar" ]; then
      echo "[INFO] artifact successfully downloaded: ${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar"
    else
      echo "[ERROR] Missing deployment artifact: ${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar"
      exit 1
    fi
  else
    echo "[INFO] No need to retrieve artifacts when no deployment on the way."
  fi
}

function deploy() {
  if [[ "${FORCE_DEPLOY}" == "true" ]] || [[ "${BRANCH_NAME}" == "main" ]]; then

    if [[ -f "${RUNTIME_DIR}/${ARTIFACT_ID}.jar" ]]; then
      mkdir -p "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}"/"$BACKUP_DIR"
      sudo cp "${RUNTIME_DIR}/${ARTIFACT_ID}.jar" "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${BACKUP_DIR}/"
      sudo cp "${RUNTIME_DIR}/${ARTIFACT_ID}".conf "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${BACKUP_DIR}/"
    fi

    if [[ -f "${RUNTIME_DIR}/${ARTIFACT_ID}.conf" ]]; then
      mkdir -p "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}"/"$BACKUP_DIR"
      sudo cp "${RUNTIME_DIR}/${ARTIFACT_ID}".conf "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${BACKUP_DIR}/"
    fi

    sudo service workflow-hello-world stop
    sleep 5
    echo "[INFO] ### deploying ${ARTIFACT_ID}.conf to ${RUNTIME_DIR}/${ARTIFACT_ID}.conf"
    sudo cp "${GITHUB_WORKSPACE}/src/main/conf/${ARTIFACT_ID}.conf" "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"
    echo "[INFO] ### deploying ${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar to ${RUNTIME_DIR}/${ARTIFACT_ID}.jar"
    sudo cp "${ACTION_RUNNER_DEPLOYMENT_WORKING_DIR}/${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar" "${RUNTIME_DIR}/${ARTIFACT_ID}.jar"

    sudo chown "${RUNTIME_USER}:${RUNTIME_USER}" "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"
    sudo chmod 664 "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"

    sudo sed -i "s/@@profile@@/${ENVIRONMENT}/g" "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"
    sudo sed -i "s/@@db_secret@@/${CAMUNDA_DB_SECRET}/g" "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"
    sudo sed -i "s/@@ldap_secret@@/${LDAP_SECRET}/g" "${RUNTIME_DIR}/${ARTIFACT_ID}.conf"

    sudo chown "${RUNTIME_USER}:${RUNTIME_USER}" "${RUNTIME_DIR}/${ARTIFACT_ID}.jar"
    sudo chmod 770 "${RUNTIME_DIR}/${ARTIFACT_ID}.jar"

    sudo systemctl daemon-reload

    sudo service workflow-hello-world start
    sleep 15
  else
    echo "::notice:: ### Deployment skipped for ${ARTIFACT_ID}-${BRANCH_MVN_VERSION}.jar"
  fi
}

function main() {
  verifyParameter

  echo "[INFO] All good. Starting deployment with user: $(whoami) and version ${BRANCH_MVN_VERSION}. Deployment enforced: ${FORCE_DEPLOY}"

  getArtefacts

  deploy
}

main "$@"
