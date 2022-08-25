# Thesis Proposal

## Goals:
- Implementation of a private blockchain system, that allows users to post tweets
- Hash Algorithm: SHA-256
- **Nodes**:
    - **Authority node**:
        - provides the blockchain api: `register`, `login`, `post-tweet`, `get-tweets`
        - implement the selection of the primary node that creates a the next block
    - **Validation nodes**: 
        - store the full ledger 
        - mine, create and validate blocks
        - calculate and validate merkle trees
        - validate tweets
        - use proof-of-authority consensus algorithm
    - **Client nodes**: 
        - provide CLI applciation to interact with the blockchain

## Target Architecture:
![](./assets/demo_architecture.png)

---

## Milestones:
- Data Model:
    - Blocks
    - Tweets
    - MerklNodes
- Merkle Tree:
    - Creation
    - Validation
- Tweet Validation:
    - check signatures
- P2P Network:
    - communication between validation nodes
    - commuincation between validation nodes and authority node
    - define payloads and message types
- Validation Nodes:
    - Block mining
    - Block validation
    - Integrate into P2P Network
- Authority Node:
    - Authentication: `/login`, `/register`
    - Endpoint for posting tweet
    - Ednpoitn for fetching tweets
- Client Node
    - CLI that makes requests to the different endpoints

--- 

## Proposed Deadlines:
```mermaid
    gantt
        title Gant-Chart BA05 SS2022
        dateFormat  YYYY-MM-DD
        section Data Model
        Define Data Model for Blocks           :a1, 2022-03-30, 1d
        Define Data Model for Tweets           :2022-03-30, 1d
        Define Data Model for MerkleNodes      :2022-03-30, 1d
        Draw Class-Diagramm                    :a2, 2022-03-30, 1d
        Implement Classes                      :a3, 2022-03-30, 1d
        section Merkle Tree
        Draw Class-Diagramm                    :merkle2, after a2, 1d
        Implementation of Merkle Tree          :merkle3, after a2, 4d
        Implementation of Merkle Tree Validation :merkle4, after a2, 4d
        section P2P Network
        Define how communication happens        :p2p1, after merkle4, 2d
        Draw Component diagram                  :, after merkle4, 2d
        Define P2P Protocol                    :p2p2, after p2p1, 1d
        Draw Class Digramm                      :p2p4, after p2p2, 1d
        Implement P2P Communication             :p2p5, after p2p4, 2d
        section Validation-nodes
        Draw Class-Diagramm                     :valN1, after p2p5, 1d
        Implement Tweet Validation              :valN2, after valN1, 1d
        Implement Block creation                :valN3, after valN2, 1d
        Implement Block validation.             :valN4, after valN3, 1d
        section Authority Node
        Implement Authentication                :an1, after valN4, 2d
        Implement primary node selection        :an2, after an1, 1d
        section Client Node
        Implement CLI Application               :cn1, after an2, 2d
```
