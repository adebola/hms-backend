#!/bin/bash
# Repair Flyway schema after failed migration

echo "Checking Flyway configuration..."

# Run Flyway repair command
mvn flyway:repair -Dflyway.configFiles=src/main/resources/application.yml

echo ""
echo "Flyway repair completed!"
echo "You can now start the application."
