# Employee Performance Tracker

A Spring Boot REST API for tracking employee performance reviews and goals across review cycles.

## Tech Stack
- Java 21, Spring Boot 3.x
- PostgreSQL + Flyway migrations
- Spring Data JPA / Hibernate
- Lombok, Maven

## Running Locally

**Prerequisites:** Java 21, PostgreSQL

1. Create the database:
```sql
CREATE DATABASE performance_tracker;
```

2. Update credentials in `src/main/resources/application.yml`

3. Run the application:
```bash
./mvnw spring-boot:run
```

Flyway will automatically apply migrations on startup.

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/employees` | Create an employee |
| POST | `/reviews` | Submit a performance review |
| GET | `/employees/{id}/reviews` | Get all reviews for an employee |
| GET | `/cycles/{id}/summary` | Get cycle summary with stats |
| GET | `/employees?department={d}&minRating={x}` | Filter employees |

## Assumptions Made
- Reviewers are themselves employees in the system (managers)
- Multiple reviewers can review the same employee in one cycle
- One reviewer cannot submit duplicate reviews for the same employee in the same cycle
- Goals are informational — they can be created directly via DB for now; no POST /goals endpoint was specified

---

## System Design

### Scaling to 500 Concurrent Managers

The Spring Boot application is fully stateless — no session state is held in memory — which means horizontal scaling requires zero code changes. Multiple instances can run behind a load balancer (e.g. AWS ALB) and handle requests independently.

Reporting endpoints like `/cycles/{id}/summary` are pure reads. These should be routed to a PostgreSQL read replica, keeping write load on the primary. Spring's `@Transactional(readOnly = true)` is already applied on all read service methods, making this routing straightforward via `AbstractRoutingDataSource`.

Connection pool tuning matters more than most people expect. HikariCP (Spring Boot's default) is configured with a pool size of ~10-20 per instance. Without this, a traffic spike exhausts DB connections and queues requests. 500 concurrent users does not mean 500 DB connections.

For expensive reports that risk timeout under load, an async pattern works well — the client submits a request and receives a job ID, then polls for the result. This frees threads during peak season.

### Fixing a Slow `/cycles/{id}/summary` at 100k+ Reviews

The first step is always `EXPLAIN ANALYZE` on the actual query — not guessing. The composite indexes on `(review_cycle_id, employee_id)` for both `performance_reviews` and `goals` should keep aggregation fast at moderate scale.

If the query is still slow, a PostgreSQL **materialized view** pre-aggregates the summary per cycle:
```sql
CREATE MATERIALIZED VIEW cycle_summary_mv AS
SELECT review_cycle_id, AVG(rating) AS avg_rating, COUNT(*) AS review_count
FROM performance_reviews
GROUP BY review_cycle_id;
```

This can be refreshed concurrently after new reviews are submitted, making the summary endpoint a simple lookup instead of a full aggregation scan.

### Caching Strategy

Cycle summaries are the best cache candidate — they are expensive to compute but stable within a cycle. Once a cycle ends, the summary never changes.
```java
@Cacheable(value = "cycleSummary", key = "#cycleId")
public CycleSummaryResponse getCycleSummary(Long cycleId) { ... }

@CacheEvict(value = "cycleSummary", key = "#review.reviewCycleId")
public void submitReview(ReviewRequest review) { ... }
```

Redis is the right cache store for production — it's shared across all app instances, unlike in-memory caches like Caffeine which break the moment you scale horizontally.

Employee lists and individual reviews are not good cache candidates — they change frequently and cache invalidation complexity outweighs the benefit.