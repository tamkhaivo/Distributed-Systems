# Module 9: Naming in Distributed Systems

> *Notes from the trenches: Naming things is one of the two hard problems in computer science, but in distributed systems, the thing you named might have died, moved, or cloned itself while you were looking up its name.*

In a distributed system, you aren't dealing with a static list of 10 servers anymore. You are dealing with thousands to millions of ephemeral entities (pods, lambda functions, actors, event streams) that need to communicate.

## 1. The Core Concepts: Names, Identifiers, and Addresses
To survive, you have to decouple *what* a thing is from *where* it is.
*   **Name**: A logical, often human-readable string (`user-profile-service`, `us-east-db-primary`). It refers to the *role* or *entity*.
*   **Identifier (ID)**: A persistently unique, machine-readable reference to a specific instance or record (`UUIDv7`, `01H05Z...`). It doesn't change, even if the entity moves.
*   **Address**: The physical or network location (`10.42.1.55:8080`, `MAC address`). This changes constantly in cloud-native environments.

**The Naming Problem**: Continuously and reliably maintaining the mapping between Names/Identifiers and Addresses in a system where addresses are ephemeral and network partitions happen.

## 2. The Challenges (Why this breaks in production)

### A. Decentralized ID Generation
How do thousands of concurrent nodes generate unique IDs without talking to a central database (which would become a massive bottleneck and Single Point of Failure)?
*   *Solution*: UUIDv4 (randomness) or, better yet, time-sorted decentralized IDs like **Twitter Snowflake**, **ULID**, or **UUIDv7**.
*   *Battle Scar*: Relying on an auto-incrementing `PRIMARY KEY` in a single SQL database for your event-sourcing IDs until you hit 50,000 writes/second and the DB melts down.

### B. The Perils of Caching & Stale Data
To map a Name to an Address, you need a registry (like DNS or Consul). To make it fast, clients cache the resolution.
*   *The Trap*: Node A crashes. The registry updates. Client B still has Node A's old IP in its cache and keeps sending traffic to a dead server (or worse, a new, unrelated server that was just assigned that IP).
*   *Rule of Thumb*: If you rely on DNS TTLs for fast failover, you have already failed.

### C. Naming Collisions & Context
In a massive system, names easily collide if namespaces aren't strictly enforced. Two teams deploy a service named `auth-worker`. Chaos ensues. What context (dev, prod, region) does the name belong to?

## 3. Resolving Identifiers to Addresses
*(The mechanics of actually finding the IP/MAC when all you have is a UUID or a Name)*

Finding an entity in a constantly shifting topology requires specialized techniques, each with distinct trade-offs in scalability, latency, and fault tolerance.

### A. Multicasting and Broadcasting
*   **The Theory**: The client sends a message to everyone on the network: *"Who has identifier X?"* The entity holding X replies: *"I do, my address is Y."* (e.g., ARP for translating IP to MAC addresses).
*   **The Reality**: Works beautifully on a local subnet. In a massive cloud network, it creates catastrophic broadcast storms. You will saturate your network switches instantly.

### B. Forwarding Pointers
*   **The Theory**: When an entity moves from Address 1 to Address 2, it leaves behind a "forwarding pointer" at Address 1 saying *"I moved to Address 2."* Any client that queries Address 1 is redirected.
*   **The Reality**: Extremely simple to implement for highly mobile entities (like actor models migrating between nodes). **The downside?** The chain gets endlessly long. If an entity moves 50 times, you follow 50 network hops to find it. If just *one* node in that chain crashes, the entity is permanently lost. This requires aggressive path compression (where clients cache the final destination) and periodic garbage collection of old pointers.

### C. Peer-to-Peer (P2P) & Distributed Hash Tables (DHT) (Flat Naming)
*   **The Theory**: Instead of a central registry, rely on math. Identifiers are hashed, and nodes themselves are arranged in a logical overlay network (like a Chord ring or Kademlia tree). Requests are routed algorithmically through the network (usually taking $O(\log N)$ hops) until they land on the node responsible for that hash space.
*   **The Reality**: The ultimate in fault tolerance—there is no single point of failure. BitTorrent and Dynamo-style databases (Cassandra) rely on this distributed design. **The downside?** Unpredictable latency. Your logical "next hop" in the ring might be physically located across the globe. You trade predictable lookup speeds for decentralized survival.

### D. Hierarchical Search Trees (Structured Naming)
*   **The Theory**: The namespace is divided into domains, zones, and sub-zones (like DNS: `service.datacenter.corp.com`). A client queries a root node, which points to a Top-Level Domain (TLD) node, which points to an authoritative nameserver.
*   **The Reality**: Incredible read scalability because it allows massive, multi-layered caching at every level of the tree. **The downside?** Extremely slow update propagation. When an IP changes, it takes time for the Time-To-Live (TTL) caches to expire globally. If you need sub-second convergence for a microservice that just crashed, standard DNS will get you paged.

### E. Dynamic Service Discovery (The Modern Standard)
*   **The Theory**: Services register their current addresses in a highly available, strongly consistent key-value store (**etcd, ZooKeeper, Consul**). Clients query the registry and keep long-polling or listening for push updates (watches) when endpoints change.
*   **The Reality**: This is how modern infrastructure survives. However, it pushes the entire reliability of your routing layer onto your consensus algorithm (Paxos/Raft). If your etcd cluster loses quorum, your microservices go completely blind.

## 4. Real-World Examples & Tooling

*   **Kubernetes Services / CoreDNS**: K8s provides a stable Name (e.g., `my-service`) and a stable virtual IP (ClusterIP). `kube-proxy` routes packets from the virtual IP to the constantly changing underlying Pod Addresses. It abstracts the churn away from the client.
*   **Amazon DynamoDB Partition Keys**: Using a well-distributed Identifier as your partition key so data is evenly spread across storage nodes. A bad naming strategy here (like using a timestamp as a key) leads to "hot partitions" and throttling.
*   **The "Thundering Herd" DNS Outage 💀**: A microservices cache expires simultaneously across 10,000 containers. They all query the DNS server at the exact same millisecond. The DNS server drops packets. The clients retry (without jitter or backoff). The DNS server dies. The entire datacenter is blind.
