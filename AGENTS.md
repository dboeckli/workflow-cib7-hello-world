# AGENTS.md

## Projekt

`workflow-cib7-hello-world` — CIB-seven-BPM (Camunda-7-Nachfolger) Hello-World-Spring-Boot-App (`ch.bpm.workflow.cib7`).
Eigenes Helm-Chart unter `helm-charts/workflow-cib7-hello-world/` inkl. lokalem LDAP-Subchart (`charts/ldap`).
Siehe README.md für Build/Kubernetes/Helm-Anleitung.

## Kommandos

| Zweck | Befehl |
|---|---|
| Format/Spotless prüfen | `./mvnw validate` |
| Build (ohne Docker/Start) | `./mvnw package -Dskip.docker.build=true -Dskip.start.stop.springboot=true` |
| Voll-Build inkl. Helm (lint/template/package) | `./mvnw clean install -Dskip.start.stop.springboot=true` |
| Tests | `./mvnw test` |

## Sandbox

- Kit: opencode-sandbox-kit (README → Sandbox). Sandbox-Quirk: vor jedem `./mvnw`
  `export npm_config_bin_links=false` (Spotless/prettier → EPERM im Mount).
- Maven-Auflösung nutzt bei Mount `C:\development\maven-repo:ro` den Host-Cache; GitHub-Packages-Zugriff
  (`github-maven`-Secret) läuft über den Kit-Proxy. Nur echte Maven-Builds sind repräsentativ
  (`mvn dependency:get` ignoriert settings-`<proxies>`).
- Formatting: shfmt `3.13.1` (Spotless `<shfmt>` + CI `mfinelli/setup-shfmt@v4`).

## Hinweise

- Registry-/Migrations-Entscheidungen: opencode-sandbox-kit Buchhaltung #44.
- Onboarding-Drehbuch: opencode-sandbox-kit #45 (dieses Projekt: Issue #188).
