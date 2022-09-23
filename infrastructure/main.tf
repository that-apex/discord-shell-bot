terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "4.37.0"
    }
  }

  backend "gcs" {
    bucket = "discord-shell-bot-tf-state-prod"
    prefix = "terraform/state"
  }
}

provider "google" {
  project = "discord-shell-bot"
  region  = "europe-west3"
  zone    = "europe-west3-a"
}

resource "google_project_service" "compute_engine_service" {
  service = "compute.googleapis.com"
}

resource "google_project_service" "container_service" {
  service = "container.googleapis.com"
}

resource "google_project_service" "cloud_build_service" {
  service = "cloudbuild.googleapis.com"
}

resource "google_project_service" "artifact_registry_service" {
  service = "artifactregistry.googleapis.com"
}

resource "google_artifact_registry_repository" "artifact_registry" {
  repository_id = "shell-bot"
  location      = "europe-west3"
  format        = "DOCKER"
  depends_on    = [
    google_project_service.cloud_build_service,
    google_project_service.artifact_registry_service,
  ]
}

resource "google_compute_network" "vpc_network" {
  name       = "shell-bot"
  depends_on = [google_project_service.compute_engine_service]
}

resource "google_compute_firewall" "default" {
  name    = "shell-bot-firewall"
  network = google_compute_network.vpc_network.name

  allow {
    protocol = "tcp"
    ports    = ["0-65535"]
  }

  allow {
    protocol = "udp"
    ports    = ["0-65535"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["shell-bot-instance"]
}

resource "google_container_cluster" "cluster" {
  name               = "shell-bot"
  location           = "europe-west3"
  depends_on         = [google_project_service.container_service]
  network            = google_compute_network.vpc_network.name
  initial_node_count = 1
  enable_autopilot   = true
  ip_allocation_policy {}
}
