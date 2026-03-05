# Distributed DB Sharding: Horizontal Scaling & Zero-Downtime Migration

## 🚀 Overview

This repository demonstrates a production-grade **Database Sharding** architecture using **Apache ShardingSphere**. As data grows into the millions, single-instance databases become a bottleneck. This project solves that by horizontally partitioning data across multiple physical MySQL nodes while keeping the application code clean and transparent.

### Key Architecture Features:

* **Transparent Sharding**: Using ShardingSphere JDBC to intercept SQL at the driver level.
* **Horizontal Scalability**: Physically splitting a single logical `t_order` table into `db_0` and `db_1`.
* **Zero-Downtime Strategy**: Implementation of the **Dual-Write Pattern** to allow for live migrations when adding new shards.

---

## 🏗 Sharding Strategy: Virtual Slots

To avoid the "Rehash Problem" (where adding a shard requires moving 100% of data), this project follows the **Virtual Slot** mental model:

1. **The Math**: `user_id % 2` currently maps to 2 shards.
2. **The Growth Path**: In a production environment, we map to **1024 Virtual Slots**.
3. **The Advantage**: When adding a 3rd shard, we only move specific "Slots" from the old shards to the new one, rather than recalculating the entire dataset.

---

## 🛡 Zero-Downtime Migration (Live Demo)

One of the hardest problems in Sharding is adding a new node while the app is live. This project implements a **Dual-Write Simulation** in the `OrderService`:

```java
if (isMigrationActive) {
    simulateMigrationLog(savedOrder); 
    // Captures real-time changes to sync with the new Shard (ds2)
}

```

**How it works:**

1. **Catch-up Phase**: While the app is running, we copy old data to the new shard.
2. **Dual-Write Phase**: Every *new* order is written to the current shard AND logged for the new shard.
3. **Cutover**: Once the new shard is synced, we update the `application.yml` and point to the 3rd node with **Zero Downtime**.

---

## 📂 Project Structure

```text
├── src/main/java/com/example/sharding/
│   ├── controller/    # REST API Layer
│   ├── entity/        # Logical JPA Entity
│   ├── repository/    # Standard JpaRepository
│   └── service/       # Business logic with Dual-Write Migration logic
├── src/main/resources/application.yml  # Sharding rules & algorithms
├── docker-compose.yml # 2 Physical MySQL Shards (Port 3307, 3308)
└── pom.xml            # ShardingSphere JDBC Starter

```

---

## ⚙️ Setup & Verification

### 1. Start Infrastructure

```bash
docker-compose up -d

```

### 2. Run Application

```bash
mvn clean install -U
mvn spring-boot:run

```

### 3. Verify Sharding Logic

* **Request (User 10 - Even):**
  `POST /api/orders` -> `{"userId": 10, "productDescription": "Laptop", "amount": 1200}`
  *Check Port 3307 (ds_0): Row exists.*
* **Request (User 11 - Odd):**
  `POST /api/orders` -> `{"userId": 11, "productDescription": "Phone", "amount": 800}`
  *Check Port 3308 (ds_1): Row exists.*
* **Migration Logs:**
  Check the application console. You will see:
  `🚀 [MIGRATION LOG]: Dual-writing Order ID ... to New Shard (ds2)`

---

## 💡 Interview Talking Points

* **Why ShardingSphere?** "It decouples sharding logic from business logic. The code doesn't know the DB is sharded."
* **How to handle growth?** "We use Virtual Slots. Adding a node only requires moving a fraction of the data."
* **Why Dual-Writes?** "To ensure data consistency during live migrations without putting the site into 'Maintenance Mode'."

---

**Author:** *Akash Giri* **Keywords:** *Database Sharding, Apache ShardingSphere, Spring Boot, Distributed Systems, MySQL, Horizontal Scaling*

---