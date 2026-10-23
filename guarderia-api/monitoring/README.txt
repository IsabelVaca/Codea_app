The following steps *must* be carried out before the API can be ran with monitoring.

A file must be created at /guarderia-api/monitoring/secrets/metrics_token with a suitable random token. For example:
02f1TFP7kfxeQEd_el8ffdzct9DFdfmRcYTO5vm323o

The Grafana user has the username "admin" by default, and the password must be set in the .env at /guarderia-api . Write GRAFANA_ADMIN_PASSWORD=your_password there.

To run the API with monitoring, use this command:
docker compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d --build

Access URLs:
* Grafana: http://localhost:3000
* Prometheus: http://localhost:9090

The Grafana container doesn't have a dashboard by default. After connecting it to Prometheus with the server URL http://prometheus:9090 you can import the desired dashboard from /guarderia-api/monitoring/grafana/dashboards via Dashboards > New > Import dashboard.