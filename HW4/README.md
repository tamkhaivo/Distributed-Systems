# HW4 - AWS Orchestration with Java

This project demonstrates the containerization and orchestration of a Distributed Systems application using **AWS ECS Fargate** and **AWS CDK (Java)**.

## Project Structure
- `src/`: Java application source code (Server & Client).
- `Dockerfile.server`: Optimized multi-stage build for the Server.
- `Dockerfile.client`: Optimized multi-stage build for the Client.
- `docker-compose.yml`: For local testing and build definitions.
- `cdk/`: AWS Cloud Development Kit project in Java for infrastructure orchestration.
- `deploy.sh`: Helper script to build and synthesize the infrastructure.

## Prerequisites
- AWS CLI configured with appropriate permissions.
- Docker installed and running.
- Node.js (for `npx aws-cdk`).
- Java 17+ and Maven.

## Getting Started

### 1. Local Testing
To verify the application locally:
```bash
docker-compose up --build
```

### 2. AWS Orchestration
The orchestration is managed via AWS CDK in Java. To prepare for deployment:
```bash
./deploy.sh
```

### 3. Deployment
To deploy the stack to your AWS account:
```bash
cd cdk
npx aws-cdk deploy
```
*Note: This will provision a VPC, ECS Cluster, and Fargate Services.*

### 4. Verification
Once deployed:
1. Go to the AWS Console -> ECS -> Clusters -> `DS-Cluster-HW4`.
2. Find the `ServerService` and get the **Public IP** of the running task.
3. Access the server at `http://<PUBLIC_IP>:8080`.

## Design Decisions
- **Multi-stage Docker Builds**: Reduces image size significantly by using a JRE-only final stage.
- **Fat JAR**: The Maven Shade plugin is used to create a single executable JAR, simplifying container deployment.
- **Serverless (Fargate)**: Chosen for cost-effectiveness and zero infrastructure management, as per the assignment requirements.
- **VPC Infrastructure**: Customized to use public subnets only, avoiding NAT Gateway costs while allowing direct access to the application.
