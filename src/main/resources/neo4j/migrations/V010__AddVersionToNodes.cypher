MATCH (n)
  WHERE NOT n:`__Neo4jMigration` AND NOT n:`__Neo4jMigrationsLock`
SET n += {version: 1};