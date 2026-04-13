package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.Collections;
import java.util.Map;

public class CdkStack extends Stack {
    public CdkStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        // 1. VPC - Public subnets for direct access (as per Assignment.md)
        Vpc vpc = Vpc.Builder.create(this, "DS-Vpc-HW4")
                .maxAzs(2)
                .subnetConfiguration(Collections.singletonList(
                        SubnetConfiguration.builder()
                                .name("Public")
                                .subnetType(SubnetType.PUBLIC)
                                .build()
                ))
                .build();

        // 2. ECS Cluster
        Cluster cluster = Cluster.Builder.create(this, "DS-Cluster-HW4")
                .vpc(vpc)
                .build();

        // 3. Server Component
        FargateTaskDefinition serverTaskDef = FargateTaskDefinition.Builder.create(this, "ServerTaskDef")
                .memoryLimitMiB(512)
                .cpu(256)
                .build();

        serverTaskDef.addContainer("ServerContainer", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromAsset("../", AssetImageProps.builder()
                        .file("Dockerfile.server")
                        .build()))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("Server")
                        .logRetention(RetentionDays.ONE_DAY)
                        .build()))
                .build())
                .addPortMappings(PortMapping.builder()
                        .containerPort(8080)
                        .build());

        FargateService serverService = FargateService.Builder.create(this, "ServerService")
                .cluster(cluster)
                .taskDefinition(serverTaskDef)
                .assignPublicIp(true)
                .desiredCount(1)
                .build();

        // Allow traffic on 8080
        serverService.getConnections().allowFromAnyIpv4(Port.tcp(8080), "Allow inbound traffic on port 8080");

        // 4. Client Component (One-off Task or Service)
        FargateTaskDefinition clientTaskDef = FargateTaskDefinition.Builder.create(this, "ClientTaskDef")
                .memoryLimitMiB(512)
                .cpu(256)
                .build();

        clientTaskDef.addContainer("ClientContainer", ContainerDefinitionOptions.builder()
                .image(ContainerImage.fromAsset("../", AssetImageProps.builder()
                        .file("Dockerfile.client")
                        .build()))
                .environment(Map.of(
                        "SERVER_HOST", "REPLACE_WITH_SERVER_PUBLIC_IP"
                ))
                .logging(LogDriver.awsLogs(AwsLogDriverProps.builder()
                        .streamPrefix("Client")
                        .logRetention(RetentionDays.ONE_DAY)
                        .build()))
                .build());
    }
}
