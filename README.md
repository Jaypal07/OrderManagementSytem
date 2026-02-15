# OMS - Order Management System

This repository contains a sample modular Spring Boot application.

Quick start

- Build and run tests:

```powershell
./mvnw clean test package
```

- Run the packaged jar:

```powershell
java -jar target/oms-0.0.1-SNAPSHOT.jar
```

Profiles
- application.yaml contains default configuration. Use `--spring.profiles.active=dev` or `prod` for environment overrides.

Docker
- Build image: `docker build -t oms:local .`
- Run: `docker run -p 8080:8080 oms:local`

CI
- A GitHub Actions workflow at `.github/workflows/ci.yml` runs the test and package steps on push/PR.

Observability
- Actuator endpoints are available when enabled; a basic health indicator is included.


