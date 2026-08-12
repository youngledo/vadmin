#!/usr/bin/env bash
set -euo pipefail

project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
consumer_pom="${project_root}/verification/standalone-consumer/pom.xml"

"${project_root}/mvnw" -B -ntp -f "${project_root}/pom.xml" install -DskipTests
"${project_root}/mvnw" -B -ntp -f "${consumer_pom}" verify
"${project_root}/mvnw" -B -ntp -f "${consumer_pom}" -Pproduction package -DskipTests
