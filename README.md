# Debezium Outbox Pattern
The idea of this project is to show how to use the Outbox pattern via Debezium Change Data Capture (CDC).
Debezium is used as a connector between the database in our case Postgres and the Kafka topic.

### Story behind this project
A common case if we use several independent systems, such as a database, message broker, caching system, how to synchronize so that the data is the same everywhere.

One of the simplest ways was to use "ChainedTransactionManager".
ChainedTransactionManager works on the principle that it is necessary to list all transactions participating in the chain. 
For example, the first transaction can be a database via jpa (JpaTransactionManager) and the second transaction can be Kafka (KafkaTransactionManager).

Possible scenarios:
- The first transaction is successfully committed and the second transaction is moved on. 
  The second transaction also commits successfully.

- The first transaction commits unsuccessfully and a rollback occurs. 
  The second transaction is skipped.

- The first transaction is successfully committed and the second transaction is moved on. 
  Another transaction commits unsuccessfully.
  **(Here we have a problem because two independent systems have mismatched data)**

We can use the "ChainedTransactionManager" approach if we are sure that one of the transactions will always work, 
while for the other there is a possibility that it might be rollbacked.

If we are not sure for any transaction that it will be successful or unsuccessful, 
a better solution is to use the CDC mechanism through the outbox pattern.

![SynchronizationDataBetweenDifferentSystems.png](art/synchronization_data_between_different_systems.png)


## Stream changes from the database

![StreamChangesFromDb.png](art/stream_changes_from_db.png)


```java
    @Transactional
    public void placeOrder(OrderRequest orderRequest) {
        var order = OrderMapper.toOrderEntity(orderRequest);

        order.setCreatedAt(Timestamp.from(Instant.now()));
        order.setStatus(OrderStatus.PENDING);
        order.setId(UUID.randomUUID());

        orderRepository.save(order);
        outBoxRepository.save(OutBox.builder()
                .aggregateId(order.getId())
                .aggregateType(ORDER)
                .type(ORDER_CREATED)
                .payload(mapper.convertValue(order, JsonNode.class))
                .build());
    }
```

A safer way to successfully commit or rollback is to use only one system in the transaction, for example, in our case, the database.
When saving data in the order table, we additionally save the same data as payload in the outbox table.
A safer way to successfully commit or rollback is to use only one system in the transaction, for example, in our case, the database.

When saving data in the order table, we additionally save the same data as payload in the outbox table. 
When the data is successfully saved in the database, the log is also written in the WAL (Write-Ahead Logging) file. 
In Postgres, the log file is called WAL, while in other databases the log file is called differently but has the same purpose. 
Debezium CDC reads changes from the WAL file and sends them to the Kafka topic.


### What is Debezium (CDC)?
- Debezium CDC is an open source distributed platform that is triggered during insert, update, delete events in the database.
- Debezium CDC currently supports these databases or has created connectors for them
  - MongoDB, MySQL, PostgreSQL, SQL Server, Oracle, Db2, Cassandra, Spanner
- Data is read from transaction logs
- It is possible to use 3 variants of Debezium
  - Debezium library that is added inside maven or gradle.
  - Debezium standalone server
  - Debezium Connect (Kafka Connect + pre-installed connectors for databases)
    - In our project, we will use the Debezium Connect option

![DebeziumCDC.png](art/debezium_cdc.png)

## Outbox pattern (Debezium)
![OutboxPattern.png](art/outbox_pattern.png)






