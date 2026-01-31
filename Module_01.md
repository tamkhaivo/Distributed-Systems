# Module 1: Introduction & Architectures

#### Definition: What is a Distributed System?
> "A collection of multiple independent computers operating concurrently that appears to its users as a single coherent system, coordinating activities among components by exchanging messages and able to tolerate failures among individual components." (Van Steen 2017)
>
> "A distributed system is a collection of autonomous computing elements that appears to its users as a single coherent system." (Tanenbaum & Van Steen 2020)

*   **Operator's Take**:
    *   *"Single coherent system"*: The grand illusion. We spend 90% of our time trying to maintain this facade against the reality of CAP theorem.
    *   *"Tolerate failures"*: This distinguishes a distributed system from a distributed *breakdown*. If one node failing takes down the cluster, you've just invented a very expensive single point of failure.

*   *Observation*: The definition of "Distributed System" often glosses over the administrative complexity.
*   *Critique*: Middleware covers a multitude of sins.


### Architectural Spectrum: Centralized vs. Decentralized vs. Distributed

Comparing along three axes: **State**, **Control**, and **Failure Mode**.

#### 1. Centralized Systems (The "Monolith")
*   **Concept**: Single node (or tightly coupled cluster) holds all state and logic. Clients are thin.
*   **Example**: Classic Mainframe, or a simple LAMP stack (Single Apache + Single MySQL).
*   **Operator's View**:
    *   *Pros*: ACID is free. Debugging is linear (grep the logs). Latency is effectively zero (local bus).
    *   *Cons*: Vertical scaling hits a physics wall (you can only buy so much RAM). If the PSU blows, the business stops.
    *   *Reality*: We all secretly miss this simplicity. "Just put it on one big box" is a valid pattern until it isn't.

#### 2. Decentralized Systems (The "Wild West")
*   **Concept**: No single source of truth. Every node is equal (peer). Consensus is hard, expensive, or probabilistic.
*   **Example**: BitTorrent, Bitcoin (Blockchain), Gnutella.
*   **Operator's View**:
    *   *Pros*: Unkillable. Take down half the network, the swarms reconfigure. Censorship-resistant.
    *   *Cons*: Latency is awful. "Eventual consistency" can mean minutes or hours. Debugging is a nightmare of trace logs across random IPs.
    *   *Reality*: Great for resilience and file sharing, terrible for high-throughput transactional systems.

#### 3. Distributed Systems (The "Modern Enterprise Hybrid")
*   **Concept**: **Logically centralized** (looks like one app to the user), **physically distributed** (runs on commodity hardware everywhere). Uses middleware to hide the mess.
*   **Example**: Google Spanner, Kubernetes Clusters, Netflix Microservices, Amazon DynamoDB.
*   **Operator's View**:
    *   *Key Difference*: Unlike decentralized systems, we *do* have authorities (Raft Leaders, Master nodes), but they are elected dynamically.
    *   *The Trade-off*: We gain the scale of decentralized systems with the (seeming) coherence of centralized ones. The cost is extreme complexity.
    *   *Failure Mode*: **Partial Failure**. A system where "the website is slow because a disk failed in Virginia" is the definition of a distributed systems problem (Lamport's curse).

---

### Deep Dive: Distributed vs. Centralized Architecture

#### Characteristics Breakdown
| Feature | Distributed Architecture | Centralized Architecture |
| :--- | :--- | :--- |
| **Processes** | **Independent Elements**: Nodes run autonomously. | **Centric**: Tightly coupled, often sharing memory/OS. |
| **Scalability** | **Scalable**: Horizontal scaling (just add more nodes). | **Difficult to Scale**: Vertical scaling only (buy bigger hardware). |
| **Agility** | **Agile**: Teams can update microservices independently. | **Monolithic**: Update requiring full system redeploy. |
| **Reliability** | **Reliable**: No single point of failure (ideally). | **Fragile**: Single Point of Failure (SPOF). |
| **Failure Scope** | **Partial**: System degrades but stays up. | **Total**: "If one part fails, the whole system fails." |

#### The Challenges (The "Hidden Costs")

**Centralized Challenges:**
1.  **The "Bus Factor"**: If the one person who knows the mainframe config retires, you are doomed.
2.  **Resource Caps**: You eventually run out of CPU slots on the motherboard.
3.  **Deployment Fear**: Deploying a monolith takes 2 hours and everyone holds their breath. One bug rolls back everything.

**Distributed Challenges:**
1.  **Network Fallacies**: The network is NOT reliable. Latency is NOT zero. Bandwidth is NOT infinite.
2.  **Data Consistency**: Keeping data synced across 3 zones is a hard physics problem (CAP theorem).
3.  **Observability Hell**: "Where is the error?" requires distributed tracing (Jaeger/Zipkin), not just `tail -f`.

#### Transitioning: The Migration Pattern
*How do you move from Centralized to Distributed without going offline?*

1.  **The Strangler Fig Pattern**:
    *   Don't rewrite the monolith.
    *   Build *new* features as microservices around the edges.
    *   Slowly route traffic away from the monolith until it's just a hollow shell.
    *   *Operator Note*: This takes years. Be patient.

2.  **Database Decomposition (The Hard Part)**:
    *   Code is easy to split; Data is hard.
    *   Breaking foreign keys across network boundaries destroys performance.
    *   *Strategy*: Dual-write (write to both old and new DBs) before cutting over.


### Common Failures in These Architectures

The way things break defines how you fix them.

#### 1. Centralized Failures (The "Hard Stop")
*   **Crash Failure (Halting)**: The server just stops. Someone pulled the plug. The process segfaulted. It's dead.
    *   *Result*: Total system unavailability.
    *   *Fix*: Reboot it. Hope you have a backup.
*   **Resource Starvation**: The disk fills up. The CPU hits 100%. The system is technically "up" but unresponsive.
    *   *Result*: Latency spikes to infinity.
    *   *Fix*: Log rotation, bigger hardware, or optimization.

#### 2. Distributed Failures (The "Silent Killers")
*   **Omission Failure (The "Ghost")**: A server sends a message, but it never arrives. Or the other side receives it but crashes before replying.
    *   *Why it hurts*: You don't know if the action happened or not. Did the payment go through? Who knows. Timeouts are your only tool.
*   **Timing Failure (The "Lag")**: The answer comes back, but 10 seconds too late.
    *   *Why it hurts*: In synchronous systems, this creates a pile-up. In asynchronous systems, it leads to stale data.
*   **Byzantine Failure (The "Traitor")**: A node sends *wrong* or *malicious* data.
    *   *Context*: Rare in trusted data centers, common in decentralized (crypto/blockchain) systems.
    *   *Fix*: Requires expensive consensus protocols (PBFT) or checksums.
*   **Network Partitions (Split-Brain)**: The network cuts in half. Both sides think they are the "leader" and accept writes.
    *   *Result*: Data corruption when they merge back. The ultimate nightmare.

---

### Real-World Distributed System Examples by Category

It helps to see where these concepts live in the wild.

#### 1. Distributed Computing (Heavy Lifting)
*   **Hadoop / MapReduce**: Breaking big data into tiny chunks, processing them in parallel on cheap disks, and gluing the answers back together.
*   **Apache Spark**: Like Hadoop, but in memory. Fast, expensive, and prone to OOM (Out of Memory) errors.
*   **Folding@home**: Using idle PS5s and laptops around the world to simulate protein folding.

#### 2. Distributed Storage & Databases (The Vaults)
*   **Google Spanner**: The holy grail of distributed databases. Uses atomic clocks (TrueTime) to achieve external consistency globally.
*   **Apache Cassandra**: The "write-anything-anywhere" database. eventually consistent, unkillable, but reading data back can be tricky.
*   **Amazon S3**: Object storage. A key-value store so big users treat it like an infinite hard drive.

#### 3. Distributed Messaging (The Nervous System)
*   **Apache Kafka**: The firehose. Decouples producers (logging services) from consumers (analytics pipelines).
*   **RabbitMQ**: The mailman. Routes messages with complex logic to ensure delivery.

#### 4. Distributed Web Systems (The Face)
*   **World Wide Web (WWW)**: The largest distributed system in existence. A graph of documents linked across millions of independent servers.
*   **Content Delivery Networks (CDNs)**: Cloudflare/Akamai. Caching static assets (images, CSS) on servers physically close to the user to cheat the speed of light.

#### 5. Distributed Ledgers (The Trustless)
*   **Bitcoin / Ethereum**: Maintaining a shared ledger without a central bank. Solves the Byzantine Generals Problem using Proof of Work/Stake.

---

### Middleware: The Glue

> "A middleware is a software layer logically placed between OS and distributed applications. It includes protocols for communication, transactions, service composition, and reliability."

#### Dissecting the Definition (Operator's View)

1.  **"Software layer logically placed between OS and distributed applications"**
    *   **The Problem**: Writing raw socket code (TCP/IP) for every microservice is madness. You don't want to handle packet fragmentation or endianness manually.
    *   **The Abstraction**: Middleware sits on top of the OS network stack and presents a cleaner interface. Instead of `send_packet(IP, Port)`, you call `stub.GetUser(ID)`. It makes networked calls *look* like local function calls.

2.  **"Protocols for communication"**
    *   **What it means**: How services talk.
    *   **The Tech**:
        *   **RPC (Remote Procedure Call)**: gRPC, Apache Thrift.
        *   **Message Queuing**: Kafka, RabbitMQ, SQS.
        *   **REST/JSON**: The universal language of the web (not technically middleware, but acts like it).

3.  **"Transactions"**
    *   **What it means**: Ensuring data integrity across multiple databases.
    *   **The Nightmare**: Two-Phase Commit (2PC) or Sagas. Middleware attempts to coordinate this so you don't end up with money deducted from Account A but not added to Account B.

4.  **"Service Composition"**
    *   **What it means**: Combining multiple small services into a useful workflow.
    *   **The Tech**: Service Mesh (Istio, Linkerd). It handles "Who talks to whom?" and "Is Service A allowed to talk to Service B?"

5.  **"Reliability"**
    *   **What it means**: Hiding the fact that the network is terrible.
    *   **Feature Set**:
        *   **Retries**: Automatic retry with exponential backoff.
        *   **Circuit Breakers**: "Service X is down, stop hammering it so it can recover."
        *   **Service Discovery**: "Where is the Login Service living today? IP 10.0.0.5 or 10.0.0.98?"

---

### Transparency: The Art of Invisibility

> **Objective Point of View**: The ultimate goal of a distributed system is to achieve a **Single System Image (SSI)**. The user should not know (or care) that the calculator app on their phone is actually talking to 500 servers in a data center. Transparency is the measure of how well we hide that complexity.

#### Types of Transparency (The "Cloaking Device")

| Transparency | Goal (What is hidden?) | The Mechanism (How we do it?) |
| :--- | :--- | :--- |
| **Access** | Hides *differences in data representation* and invocation. | **IDL (Interface Definition Languages)** like Protobuf/Thrift. They serialize data into a standard binary format so a Python client can talk to a C++ server without worrying about big-endian vs. little-endian. |
| **Location** | Hides *where* an object is located. | **Naming Services (DNS)** and **Service Discovery** (Consul/Zookeeper). You connect to `db-primary`, not `192.168.1.55`. |
| **Relocation** | Hides that an object is *moved while in use*. | **Mobile IPs** and **Handover protocols**. Example: Walking from one WiFi tower to another while on a VoIP call without the call dropping. |
| **Migration** | Hides that an object may *move to another location*. | **VM Migration (vMotion)** or **Container Scheduling (K8s)**. A pod moves from Node A to Node B because Node A is full. |
| **Replication** | Hides that an object is *replicated* (copied). | **Load Balancers** and **Consensus**. You write to "The Database". You don't know that "The Database" is actually 5 nodes, and 3 of them just agreed to your write. |
| **Concurrency** | Hides that an object is potentially *shared by several independent users*. | **Locking**, **Transactions**, and **MVCC**. Two people edit a Google Doc. The system merges changes so it looks like a single cohesive stream, not a race condition. |
| **Failure** | Hides the *failure and recovery* of an object. | **Retries** and **Redundancy**. A node crashes mid-request. The load balancer silently routes your retry to a healthy node. You perceive a 50ms delay, not a 500 Error. |

#### *Operator's Critique on Transparency*
*   **Total Transparency is a Lie**: You usually *can't* hide failure completely. If the network cable is cut, no amount of abstraction will make the packet arrive.
*   **Leaky Abstractions**: Trying to hide network latency (Access Transparency) often leads to terrible performance. Treating a remote call like a local call is the root of all evil in distributed programming (see: *The Failings of CORBA*).
