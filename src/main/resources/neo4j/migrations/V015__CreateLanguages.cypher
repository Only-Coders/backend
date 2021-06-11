CREATE (l:Language{code:"en", name:"English"}) with l MATCH (p:User) CREATE (p)-[:SPEAKS]->(l);
CREATE (:Language{code:"es", name:"Español"});


