#!/bin/bash

# HW4 AWS Deployment Script
# This script builds the application and synthesizes/deploys the AWS infrastructure.

set -e

echo "🚀 Starting HW4 Deployment Process..."

# 1. Build the Java Application
echo "📦 Building Java Application (Fat JAR)..."
mvn clean package -DskipTests

# 2. Change to CDK directory
cd cdk

# 3. Authenticate Docker with ECR (Assuming AWS credentials are set)
echo "🔍 Synthesizing AWS Infrastructure..."
npx aws-cdk synth

echo "✨ Synthesis complete!"
echo "To deploy, run: npx aws-cdk deploy"
echo "To destroy resources later: npx aws-cdk destroy"
