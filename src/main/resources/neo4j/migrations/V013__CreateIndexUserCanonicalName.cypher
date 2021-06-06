CREATE CONSTRAINT person_index_canonicalname IF NOT exists ON (p:Person) ASSERT p.canonicalName IS UNIQUE;
