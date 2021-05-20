CREATE CONSTRAINT person_unique_email IF NOT exists ON (p:Person) ASSERT p.email IS UNIQUE;
CREATE INDEX person_id IF NOT exists FOR (p:Person) ON (p.id);
CREATE INDEX role_name IF NOT exists FOR (r:Role) ON (r.name);
CREATE INDEX skill_cn IF NOT exists FOR (s:Skill) ON (s.canonicalName);
CREATE INDEX country_code IF NOT exists FOR (c:Country) ON (c.code);
CREATE INDEX report_type_id IF NOT exists FOR (r:ReportType) ON (r.id);
CREATE INDEX tag_cn IF NOT exists FOR (t:Tag) ON (t.canonicalName);
CREATE INDEX post_id IF NOT exists FOR (p:Post) ON (p.id);
CREATE INDEX reaction_id IF NOT exists FOR (r:Reaction) ON (r.id);
CREATE INDEX git_platform_id IF NOT exists FOR (g:GitPlatform) ON (g.id);
CREATE INDEX workposition_id IF NOT exists FOR (p:WorkPosition) ON (p.id);
CREATE INDEX workplace_id IF NOT exists FOR (p:Workplace) ON (p.id);
CREATE INDEX institute_id IF NOT exists FOR (p:Institute) ON (p.id);
CREATE INDEX degree_id IF NOT exists FOR (p:Degree) ON (p.id);