#!/bin/sh
# Compila con JDK 25 en Docker (Loom 1.18 lo necesita); el jar sale en build/libs/
cd "$(dirname "$0")"
exec docker run --rm -u "$(id -u):$(id -g)" -e HOME=/home/b -e GRADLE_USER_HOME=/home/b/.gradle \
  -v "$HOME/.gradle":/home/b/.gradle -v "$PWD":/w -w /w eclipse-temurin:25-jdk ./gradlew build --no-daemon -q "$@"
