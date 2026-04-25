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

*   **NTP (Network Time Protocol)**: The industry standard. It factors in network latency by exchanging timestamps (`T1, T2, T3, T4`).
    **Failure Analysis by Step:**
    1.  **Step: Message Exchange.** If the **NTP Server crashes**, the client receives no response. Clients typically query multiple servers (stratum 1, 2) to mitigate this.
    2.  **Step: Latency Calculation.** If there is **Asymmetric Network Delay**, NTP's assumption that $T_{delay} = (T4-T1) - (T3-T2) / 2$ fails, leading to an incorrect clock offset.
    3.  **Step: Update Clock.** If the **Clock Offset is too large** (e.g., > 128ms), NTP might "step" the clock (jump). "Slewing" (gradual adjustment) is preferred for safety.
*   **Berkeley Algorithm**: An active time server polls a set of machines, computes the average, and tells others how to adjust.
    **Failure Analysis by Step:**
    1.  **Step: Master Polls Workers.** If a **Worker crashes**, it doesn't respond. The Master calculates the average based on the surviving nodes.
    2.  **Step: Master computes average.** If the **Master crashes**, the entire process stops. A leader election must be triggered to promote a new Master.
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
*   *Cons*: Single point of failure, performance bottleneck. 

**Failure Analysis by Step:**
1.  **Step: Client requests lock.** If the **Coordinator crashes** here, the client's request is lost. The client must timeout and initiate a leader election for a new coordinator.
2.  **Step: Client holds lock.** If the **Client crashes** here, the lock is never released. The coordinator's queue is blocked forever. 
    *   *Solution*: Use **Leases**. The lock is only granted for a specific time. If the client doesn't renew or release, the coordinator forcibly reclaims it.
3.  **Step: Coordinator grants lock.** If the **Coordinator crashes** after granting but before the client receives it, the client waits forever.

### 5.3.3 A Distributed Algorithm (Ricart-Agrawala)
Requires total ordering of events (using Lamport clocks). A process broadcast a request `(timestamp, process_id)` and waits for `OK` from *all* other nodes.

**Failure Analysis by Step:**
1.  **Step: Broadcast Request.** If the **Sender crashes** halfway through, some nodes receive the request and some don't. Since the sender is dead, those who received it will never get a release, but they aren't blocked because they only reply to *future* requests.
2.  **Step: Wait for OKs.** If a **Recipient crashes** and never replies, the Sender is blocked indefinitely. The algorithm *cannot tolerate a single node failure* without a perfect failure detector to remove the dead node from the set of required responses.

### 5.3.4 A Token-Ring Algorithm
Nodes form a logical ring. A token rotates around the ring. 

**Failure Analysis by Step:**
1.  **Step: Pass Token.** If the **Sender crashes** while passing the token, or the **Receiver crashes** before acknowledging, the **Token is Lost**. No one can enter the critical section.
    *   *Solution*: Monitor the ring. If a node doesn't see the token for a full rotation time, a complex election is needed to regenerate exactly one token.
2.  **Step: Hold Token.** If a **Node crashes while in the critical section**, the token is lost and the resource might be in an inconsistent state.
3.  **Step: Link failure.** if a **Neighbor crashes**, the ring is broken. Nodes must maintain a list of all participants to "skip" the dead node and close the ring.

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

## 5.4 Consensus and Election Algorithms

When the coordinator fails, or when a distributed cluster needs to agree on a single source of truth (like who owns a shard, or which transaction commits next), the system relies on Consensus Algorithms.

### 5.4.1 The "Split-Brain" Problem
If a network partition occurs, a cluster might divide into two isolated halves. Without consensus, both halves might elect their own leader, resulting in a "Split-Brain" scenario where both sides accept conflicting writes, permanently corrupting the data. Consensus algorithms prevent this by requiring a strict **Majority Quorum** (e.g., $N/2 + 1$ nodes) to elect a leader or commit a write. The half of the network without a quorum simply halts, preserving safety over availability.

### 5.4.2 Paxos: The Academic Foundation
Created by Leslie Lamport, Paxos is the grandfather of all consensus algorithms.

**Failure Analysis by Step:**
1.  **Phase 1 (Prepare):** If the **Proposer crashes**, another proposer can simply start with a higher proposal number. No harm done.
2.  **Phase 2 (Accept):** If the **Proposer crashes** after sending `Accept` to only a minority of nodes, the value is not committed. A future proposer will see these values, but since they aren't a majority, it can choose its own value.
3.  **Phase 2 (Accept - Majority):** If the **Proposer crashes** *after* a majority has accepted but *before* notifying everyone, the value is **safely chosen**. Any future proposer is *guaranteed* to see this value in Phase 1 and will be forced to use it, ensuring consensus is maintained even across proposer deaths.
4.  **Acceptor Failure:** If a **Majority of Acceptors crash**, the system deadlocks. It cannot reach consensus until enough acceptors recover their state from stable storage.

### 5.4.3 Raft: Consensus Designed for Humans
Created as a direct response to Paxos's complexity, Raft prioritizes understandability.

**Failure Analysis by Step:**
1.  **Step: Leader Election.** If the **Leader crashes**, Followers stop receiving heartbeats. After a randomized timeout, they start a new election. 
    *   *Edge Case*: If the **Candidate crashes** during election, the term eventually times out and another node tries.
2.  **Step: Log Replication (Pre-Quorum).** If the **Leader crashes** after receiving a client request but before it reaches a majority, the write is lost. The next leader (who doesn't have it) will overwrite this entry in the followers' logs.
3.  **Step: Log Replication (Post-Quorum).** If the **Leader crashes** *after* a majority has replicated the entry but *before* the leader can broadcast the "commit" message, the entry is **committed but the client doesn't know it**. The new leader is guaranteed to have this entry (because it needs a majority of votes, and at least one node in that majority must have the committed entry) and will eventually notify everyone of the commit.

### 5.4.4 Raft vs. Paxos: A Comparative Deep Dive

While both solve the same fundamental problem, their operational characteristics differ significantly:

| Feature | Raft | Paxos (Multi-Paxos) |
| :--- | :--- | :--- |
| **Understandability** | High (Designed for education/implementation). | Low (Academic, complex state space). |
| **Consensus Mechanism** | **Strong Leader**: All log entries flow from Leader to Followers. Decoupled sub-problems. | **Quorum-Based Proposers**: Any node can propose. Multi-Paxos uses a stable leader optimization. |
| **Implementation** | **Straightforward**: Explicit specification for log compaction, membership changes. | **Difficult**: Basic Paxos is simple; Multi-Paxos is under-specified in practice (custom implementations). |
| **Testing/Verification** | **Easier**: Reduced state space and explicit RPCs make deterministic testing simpler. | **Harder**: Large state space and peer-to-peer collisions make edge cases harder to catch. |

#### Failure Scenarios: Side-by-Side

| Scenario | Raft's Response | Paxos's Response |
| :--- | :--- | :--- |
| **Network Partition** | The minority side halts immediately (no leader heartbeats). The majority side elects a new leader and continues. | The minority side cannot reach a quorum and stalls. The majority side continues to reach consensus using its quorum. |
| **Leader/Proposer Crash during Write** | If majority haven't ACKed, entry is lost. If majority ACKed, the new leader *must* have the entry and will finish the commit. | If majority haven't accepted, value is lost. If majority accepted, any future proposer *must* discover and propose that same value. |
| **Duplicate Messages** | Handled via **Terms and Indices**. A follower ignores an `AppendEntries` for a term/index it has already processed. | Handled via **Proposal Numbers**. An acceptor ignores a `Prepare` or `Accept` for a lower proposal number. |
| **Client Retries** | **At-least-once** by default. To achieve **Exactly-once**, the leader must track client IDs and sequence numbers to detect duplicates. | Similar to Raft; usually implemented as a layer on top of the consensus state machine. |

*   **The "Suffer" Factor**: In Paxos, you are often testing your *derivation* of the protocol because the papers leave production details (log compaction, membership) as an exercise. In Raft, these are part of the core specification.

### 5.4.5 Flooding-Based Consensus
In environments where establishing a stable leader is too fragile, systems can rely on peer-to-peer **Flooding-Based Consensus**. Instead of funneling decisions through a coordinator, every node independently broadcasts (floods) its current state or proposal to *every other node* in the network across synchronized rounds. After receiving everyone's data, each node independently applies the exact same deterministic voting function to reach an identical conclusion.
*   **Advantages**: 
    *   **Simplicity**: Functionally straightforward to implement without managing complex leader/follower state transitions.
    *   **Extreme Fault Tolerance**: Because there is no single leader, there is no "failover" downtime. If a node crashes, the surrounding architecture inherently masks the failure; the surviving network simply continues their flooding rounds and votes without the dead node.
*   **Disadvantages**: 
    *   **Massive Network Overhead**: Generates $O(N^2)$ message complexity. Every node must actively ping every other node. 
    *   **Poor Scalability**: While it works beautifully for a 5-node constrained cluster, a flooding algorithm will completely saturate the network bandwidth and CPU of a 500-node cluster.

### 5.4.6 Traditional Election: The Bully Algorithm
When a process notices the leader is dead, it initiates an election by sending an ELECTION message to all processes with *higher* IDs. "The biggest bully always wins." 

**Failure Analysis by Step:**
1.  **Step: Send ELECTION.** If the **Initiator crashes** after sending to some but not all higher-ID nodes, those who received it will start their own elections. The process continues.
2.  **Step: Higher-ID node responds OK.** If a **Higher-ID node crashes** *after* sending OK but *before* announcing itself as leader, the initiator waits for a timeout. When no COORDINATOR message arrives, the initiator (or another node) must restart the election.
3.  **Step: Announce COORDINATOR.** If the **New Leader crashes** immediately after winning, the system detects the failure and the entire process repeats.
4.  **Network Partition:** If the network splits, both sides might elect the "biggest bully" in their respective partitions, leading to **Split-Brain**.

### 5.4.7 Example: Leader Election in ZooKeeper
Analogous to its locking mechanism, nodes create sequential ephemeral znodes `/election/node-seq`. The node with the lowest sequence number becomes the leader. If the leader crashes, its ephemeral node vanishes, triggering a watch event for the next node in line, which smoothly transitions to leadership (powered internally by the ZAB, ZooKeeper Atomic Broadcast, protocol).

### 5.4.8 Elections in Large-Scale Systems
In highly decentralized environments (like P2P networks), electing a single global leader is impractical. Instead, we use superpeers (nodes with high uptime and bandwidth). Elections are localized, dynamically organizing nodes into a hierarchy.

### 5.4.9 Elections in Wireless Environments
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
