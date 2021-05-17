CREATE INDEX workposition_id IF NOT exists FOR (p:WorkPosition) ON (p.id);
CREATE INDEX workplace_id IF NOT exists FOR (p:Workplace) ON (p.id);