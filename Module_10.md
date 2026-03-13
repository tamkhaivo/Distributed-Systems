# Module 10: Coordination

> **Operator's Log**: "Time is an illusion. Lunchtime doubly so. But clock drift in a distributed system? That's what sets off pager storms at 3 AM. This module gets to the heart of distributed coordination — a space full of elegant algorithms trying to hide the inherent, messy chaos of a network where absolute time doesn't really exist."

Coordination in distributed systems is about ensuring that independent processes, which may not share memory or a common physical clock, can agree on the ordering of events, access to resources, and leadership.

---

## 5.1 Clock Synchronization

In centralized systems, time is unambiguous. In distributed systems, relying on physical time is a recipe for disaster unless carefully synchronized.

### 5.1.1 Physical Clocks
Each computer has its own timer circuit (usually a quartz crystal). Due to slight differences in frequency, crystals oscillate at different rates. This phenomenon is called **clock drift**, leading to clock skew where identical machines show different times. If you check `time()` on three different servers in the same rack, you'll rarely get three identical answers.

### 5.1.2 Clock Synchronization Algorithms
When absolute time matters (e.g., meeting external real-world deadlines), we need synchronization:

*   **NTP (Network Time Protocol)**: The industry standard. It factors in network latency by exchanging timestamps (`T1, T2, T3, T4`) between client and server, calculating offset and delay. *Operator Note: NTP can keep you within a few milliseconds, but leap seconds and asymmetric routing delays can still ruin your day.*
*   **Berkeley Algorithm**: An active time server polls a set of machines, computes the average, and tells the others how to adjust their clocks. It's an internal synchronization without referring to an external UTC source.
*   **PTP (Precision Time Protocol)**: Used in LAN environments requiring microsecond accuracy (e.g., high-frequency trading networks). Requires hardware support.

> **Battle-Hardened Axiom**: True external consistency is elusive. If you rely heavily on timestamps for correctness instead of conflict resolution (like LWW - Last Write Wins), you must bound the clock uncertainty, similar to Google Spanner's TrueTime API which explicitly exposes `[earliest, latest]`.

---

## 5.2 Logical Clocks

When physical clocks fail us, we care less about *when* an event happened and more about the *order* of events (who happened before whom).

### 5.2.1 Lamport’s Logical Clocks
Leslie Lamport established the "happens-before" relation (`->`). 
*   If `a` and `b` are events in the same process, and `a` occurs before `b`, then `a -> b`.
*   If `a` is sending a message and `b` is receiving it, `a -> b`.

Each process maintains a counter `L_i`.
1.  Before executing an event, `L_i = L_i + 1`.
2.  When sending a message `m`, append `L_i`.
3.  Upon receiving `(m, t)`, process `j` sets `L_j = max(L_j, t) + 1` and passes the message to the application.

*Limitation*: If `L(a) < L(b)`, we *cannot* conclude that `a -> b`. They might be concurrent.

### 5.2.2 Vector Clocks
To capture strict causality, vector clocks were introduced. Instead of a single counter, each process keeps a vector `V` where `V[i]` is the logical time at process `i`.
*   When a process `i` executes an event, it increments `V_i[i]`.
*   A message carries the sender's entire vector.
*   Upon receiving `V_msg`, the receiver `j` updates its own vector: `V_j[k] = max(V_j[k], V_msg[k])` for all `k`, and then increments `V_j[j]`.

*Operator Note*: Vector clocks give us causality (if `V(a) < V(b)`, then `a -> b`), but they scale poorly with the number of nodes. Dynamo-style databases like Riak use vector clocks to detect concurrent writes (conflicts) so the application can resolve them.

---

## 5.3 Mutual Exclusion

Coordinating access to shared resources without a shared memory.

### 5.3.1 Overview
We need to ensure safety (only one process holds the lock), liveness (no deadlocks/starvation), and fairness (requests are handled in order).

### 5.3.2 A Centralized Algorithm
A single coordinator manages a queue for a lock.
*   *Pros*: Simple, guarantees fairness.
*   *Cons*: Single point of failure, performance bottleneck. "The coordinator is down" vs. "The lock is held for a long time" can be indistinguishable without timeouts.

### 5.3.3 A Distributed Algorithm (Ricart-Agrawala)
Requires total ordering of events (using Lamport clocks). A process broadcast a request `(timestamp, process_id)` and waits for `OK` from *all* other nodes.
*   *Cons*: `N` points of failure instead of 1. A single crashed node halts the entire system. It requires `2(N-1)` messages.

### 5.3.4 A Token-Ring Algorithm
Nodes form a logical ring. A token rotates around the ring. You can only enter the critical section if you hold the token.
*   *Pros*: Fair, no starvation.
*   *Cons*: If a token is lost (node crashes while holding it), it must be regenerated—a difficult task. Node failures require ring reconfiguration.

### 5.3.5 A Decentralized Algorithm
Uses a voting system (majority/quorum) across multiple coordinators. A client needs permission from `m > N/2` coordinators. It provides fault tolerance and handles crash failures well, but can suffer from starvation if multiple nodes request the lock simultaneously and split the votes.

### 5.3.6 Example: Simple Locking with ZooKeeper
ZooKeeper provides an elegant centralized, fault-tolerant approach using ephemeral sequential znodes.
1. Client creates an ephemeral sequential node `/lock/request-000x`.
2. Client checks children of `/lock`.
3. If its node has the lowest sequence number, it has the lock.
4. If not, it sets a watch on the node with the next lowest sequence number and waits for it to be deleted.

*Operator Note*: This is the modern, production-ready answer. Stop implementing distributed mutual exclusion from scratch. Let ZooKeeper or etcd handle it.

---

## 5.4 Election Algorithms

When the coordinator fails, someone needs to step up.

### 5.4.1 The Bully Algorithm
When a process notices the leader is dead, it initiates an election.
*   It sends an ELECTION message to all processes with *higher* IDs.
*   If no one responds, it wins and sends a COORDINATOR message to all lower-ID processes.
*   If a higher ID responds, the higher ID takes over the election process.
*   "The biggest bully always wins."

### 5.4.2 A Ring Algorithm
Similar to the token ring. An election message travels around the ring, accumulating the IDs of all active nodes. When the message completes the circuit, the initiator picks the highest ID in the list as the leader and passes a COORDINATOR message around the ring.

### 5.4.3 Example: Leader Election in ZooKeeper
Analogous to the locking mechanism. Nodes create sequential ephemeral znodes `/election/node-seq`. The node with the lowest sequence number becomes the leader. If the leader crashes, its ephemeral node vanishes, triggering a watch event for the next node in line, which smoothly transitions to leadership.

### 5.4.4 Example: Leader Election in Raft
Raft simplifies consensus. Nodes are Followers, Candidates, or Leaders.
1. A Follower whose heartbeat timer expires becomes a Candidate.
2. It requests votes from others.
3. If it secures a majority for its "term", it becomes Leader and starts sending heartbeats.
*Operator Note*: Raft’s election is beautiful because it’s deeply integrated with log replication, making it much easier to reason about than Paxos.

### 5.4.5 Elections in Large-Scale Systems
In highly decentralized environments (like P2P networks), electing a single global leader is impractical. Instead, we use superpeers (nodes with high uptime and bandwidth). Elections are localized, dynamically organizing nodes into a hierarchy.

### 5.4.6 Elections in Wireless Environments
Wireless constraints (adhoc structures, unreliable links, battery life) require specialized election algorithms spanning spanning trees, ensuring that the selected 'leader' nodes (sinks or gateways) are optimally placed to minimize network hops and conserve energy.

---

## 5.5 Gossip-Based Coordination

Inspired by epidemiology, nodes spread state by randomly selecting peers to "gossip" with. Extremely resilient to failure and highly scalable.

### 5.5.1 Aggregation
Computing global properties (e.g., average system load, network size). Every node starts with a value. Gossip exchanges average the values between two nodes. Eventually, all nodes converge exactly on the network-wide average.

### 5.5.2 A Peer-Sampling Service
How do you randomly pick a peer in an unstructured network? Nodes maintain partial views (a small list of known peers) and periodically explicitly gossip to exchange and shuffle these views, ensuring the topology graph remains highly connected and random.

### 5.5.3 Gossip-Based Overlay Construction
You can bias the peer-sampling service. Instead of purely random shuffling, nodes prefer peers based on proximity or semantic similarity, organically constructing structured topologies (like a ring or a tree) entirely through local randomized interactions.

### 5.5.4 Secure Gossiping
Gossip assumes nodes aren't malicious. In untrusted environments, rapid dissemination means bad data propagates quickly. Protocols require cryptographic signatures or validation steps to isolate Byzantine (malicious) peers trying to poison the aggregation or overlay.

---

## 5.6 Distributed Event Matching

In publish-subscribe systems, producers generate events, and consumers subscribe to them. Coordination is required to match and route them.

### 5.6.1 Centralized Implementations
A single broker or a clustered broker (like a Kafka cluster or RabbitMQ) receives all events, matches them against subscriptions (e.g., matching routing keys or content-based rules), and forwards them.

### 5.6.2 Secure Publish-Subscribe Solutions
How to ensure that only authorized publishers can send to a topic, and only authorized subscribers can read it? You need decoupled key management. The brokers shouldn't necessarily read the contents (end-to-end encryption) but must still route events accurately.

---

## 5.7 Location Systems

How nodes figure out where they are, physically or logically.

### 5.7.1 GPS: Global Positioning System
Relies on highly synchronized atomic clocks in satellites broadcasting their timestamps. A receiver calculates its distance from at least four satellites to triangulate 3D position and precise time.

### 5.7.2 When GPS is not an option
Indoor environments or IoT sensor networks use beacons, WiFi access point fingerprinting, or ultra-wideband (UWB) measuring Time of Flight (ToF) and signal strength to derive relative localization.

### 5.7.3 Logical Positioning of Nodes
Algorithms like Vivaldi map network latency coordinates onto an N-dimensional geometric space. If Node A wants to know its latency to Node B, it computes the Cartesian distance between their assigned coordinates without executing a live ping. Useful for building CDNs or routing overlay networks.

---

## 5.8 Summary

Coordination fundamentally dictates how parts of a scattered system act as one coherent entity. Whether it's agreeing on exactly what time it is (which is almost impossible), agreeing on who holds the lock (difficult but solved via ZooKeeper/etcd), or agreeing on who is in charge (Raft/Paxos), it's the core of making a distributed system predictable.

*Operator's Final Thought: Stop trying to reinvent consensus. Use established, verified primitives. Your future on-call self will thank you.*
