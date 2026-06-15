provider "grafana" {
  url             = var.grafana_url
  auth            = var.grafana_auth
  sm_url          = var.grafana_sm_url
  sm_access_token = var.grafana_sm_token
}

# Fetch the list of available Synthetic Monitoring probes
data "grafana_synthetic_monitoring_probes" "main" {
  # Only run if SM URL and Token are provided
  count = var.grafana_sm_url != "" && var.grafana_sm_token != "" ? 1 : 0
}

# Create the HTTP Check for our Nemologic endpoint
resource "grafana_synthetic_monitoring_check" "nemologic_http" {
  count = var.grafana_sm_url != "" && var.grafana_sm_token != "" ? 1 : 0

  job       = "nemologic-api-health"
  target    = "https://rogic.io/api/stages"
  enabled   = true
  frequency = 60000
  timeout   = 5000

  # Enable checks in Tokyo, Singapore, and Sydney Probes
  probes = [
    data.grafana_synthetic_monitoring_probes.main[0].probes.Tokyo,
    data.grafana_synthetic_monitoring_probes.main[0].probes.Singapore,
    data.grafana_synthetic_monitoring_probes.main[0].probes.Sydney,
  ]

  settings {
    http {
      method = "GET"
      tls_config {
        insecure_skip_verify = false
      }
    }
  }
}

# Look up the default Grafana Cloud Prometheus data source
data "grafana_data_source" "prometheus" {
  count = var.grafana_url != "" && var.grafana_auth != "" ? 1 : 0
  name  = var.grafana_prometheus_datasource_name
}

# Create a folder for Nemologic Alerts if auth is configured
resource "grafana_folder" "nemologic_folder" {
  count = var.grafana_url != "" && var.grafana_auth != "" ? 1 : 0
  title = "Nemologic Monitoring"
}

# Configure the Alert Rule Group for service availability
resource "grafana_rule_group" "nemologic_alerts" {
  count = var.grafana_url != "" && var.grafana_auth != "" ? 1 : 0

  name             = "nemologic-alerts"
  folder_uid       = grafana_folder.nemologic_folder[0].uid
  interval_seconds = 60

  rule {
    name      = "Nemologic-Service-Down-Alert"
    for       = "2m"
    condition = "C"

    # Query A: PromQL to check if active probes count is 0
    data {
      ref_id = "A"
      relative_time_range {
        from = 300
        to   = 0
      }
      datasource_uid = data.grafana_data_source.prometheus[0].uid
      model = jsonencode({
        expr          = "sum(sm_check_status{job=\"nemologic-api-health\"})"
        intervalMs    = 15000
        maxDataPoints = 43200
        refId         = "A"
      })
    }

    # Expression B: Reduce query A to a single value (minimum)
    data {
      ref_id = "B"
      relative_time_range {
        from = 0
        to   = 0
      }
      datasource_uid = "-100" # Special UID for Grafana built-in expressions
      model = jsonencode({
        conditions = []
        datasource = {
          name = "Expression"
          type = "__expr__"
          uid  = "-100"
        }
        expression = "A"
        hide       = false
        reducer    = "min"
        type       = "reduce"
      })
    }

    # Expression C: Threshold check (alert if min is below 1)
    data {
      ref_id = "C"
      relative_time_range {
        from = 0
        to   = 0
      }
      datasource_uid = "-100"
      model = jsonencode({
        conditions = [
          {
            evaluator = {
              params = [1]
              type   = "lt"
            }
            operator = {
              type = "and"
            }
            query = {
              params = []
            }
            reducer = {
              params = []
              type   = "avg"
            }
            type = "query"
          }
        ]
        datasource = {
          name = "Expression"
          type = "__expr__"
          uid  = "-100"
        }
        expression = "B"
        hide       = false
        type       = "threshold"
      })
    }

    annotations = {
      summary     = "Nemologic service is down"
      description = "The HTTP health check for https://rogic.io/api/stages has failed across all probes for more than 2 minutes."
    }

    labels = {
      severity = "critical"
    }
  }
}

# Create the developer email contact point dynamically
resource "grafana_contact_point" "email_alerts" {
  count = var.grafana_url != "" && var.grafana_auth != "" ? 1 : 0

  name = "Developer-Email-Alerts"
  email {
    addresses = [var.alert_email]
  }
}

# Create and deploy the SLA & Availability dashboard dynamically
resource "grafana_dashboard" "sla_dashboard" {
  count = var.grafana_url != "" && var.grafana_auth != "" ? 1 : 0

  folder      = grafana_folder.nemologic_folder[0].uid
  config_json = replace(
    file("${path.module}/../../docs/sla_dashboard.json"),
    "\"$${DS_PROMETHEUS}\"",
    "\"${data.grafana_data_source.prometheus[0].uid}\""
  )
}


