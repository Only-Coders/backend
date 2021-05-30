MERGE (:Admin:Person {createdAt: timestamp(), updatedAt: timestamp(), firstName: "Admin", lastName: "Admin", email: "onlycoders.tech@gmail.com", canonicalName: "admin-onlycoders", id: randomUUID()});
MATCH (a:Admin{canonicalName: "admin-onlycoders"}) with a MATCH (r:Role{name: 'ADMIN'}) MERGE (a)-[:HAS]->(r);
