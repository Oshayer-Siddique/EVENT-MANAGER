#!/usr/bin/env bash
set -euo pipefail

# Path where the git repository lives
REPO_DIR="/home/ubuntu/TicketifyDev/BACKEND"
# Systemd service name that runs the Spring Boot jar
SERVICE_NAME="event-manager"
# Location where the runnable jar + .env live
DEPLOY_DIR="/opt/event-manager"
JAR_NAME="EVENT_MANAGER-0.0.1-SNAPSHOT.jar"

log() {
  printf '[%s] %s\n' "$(date -u +'%Y-%m-%dT%H:%M:%SZ')" "$*"
}

log "Switching to repo at $REPO_DIR"
cd "$REPO_DIR"

git fetch --all --prune
log "Pulling latest changes"
git pull --ff-only

log "Building jar"
./mvnw clean package -DskipTests

log "Stopping $SERVICE_NAME"
sudo systemctl stop "$SERVICE_NAME" || true

log "Deploying new jar to $DEPLOY_DIR"
sudo install -d -m 755 "$DEPLOY_DIR"
sudo install -m 644 "target/$JAR_NAME" "$DEPLOY_DIR/$JAR_NAME"

log "Starting $SERVICE_NAME"
sudo systemctl start "$SERVICE_NAME"

log "Service status"
sudo systemctl status "$SERVICE_NAME" --no-pager
