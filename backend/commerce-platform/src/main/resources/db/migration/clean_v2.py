with open("V1_raw.sql", "r", encoding="utf-8") as f:
    lines = f.readlines()

out = []
for line in lines:
    s = line.strip()
    # Skip pg_dump session markers
    if s.startswith('\\restrict') or s.startswith('\\unrestrict'):
        continue
    # Skip SET, SELECT pg_catalog
    if s.startswith('SET ') or s.startswith('SELECT pg_catalog'):
        continue
    # Skip CREATE SEQUENCE blocks (BIGSERIAL creates them automatically)
    if s.startswith('CREATE SEQUENCE') or s.startswith('ALTER SEQUENCE'):
        continue
    # Skip empty lines
    if s == '':
        continue
    # Skip comment-only lines that reference SEQUENCE
    if s == '--' or s.startswith('-- Name:') or s.startswith('-- Dumped'):
        continue
    if 'SEQUENCE' in s and s.startswith('--'):
        continue
    # Remove public. prefix
    line = line.replace('public.', '')
    out.append(line.rstrip())

result = '\n'.join(out)

# collapse multiple blank lines
import re
result = re.sub(r'\n{3,}', '\n\n', result)

header = """-- ============================================================
-- V1__init_schema.sql
-- AI Commerce Platform - Complete schema from Hibernate
-- ============================================================

"""

with open("V1__init_schema.sql", "w", encoding="utf-8") as f:
    f.write(header + result + '\n')

print("OK")