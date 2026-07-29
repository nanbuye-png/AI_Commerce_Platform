#!/bin/bash
# CI Flyway Migration Version Conflict Check
# Exit with 1 if duplicate versions found

MIGRATION_DIR="backend/commerce-platform/src/main/resources/db/migration"

echo "========================================"
echo "Flyway Migration Version Uniqueness Check"
echo "========================================"

duplicates=$(ls "$MIGRATION_DIR"/V*.sql 2>/dev/null | \
  sed 's/.*\/V\([0-9]*\)__.*/\1/' | \
  sort | uniq -d)

if [ -n "$duplicates" ]; then
  echo ""
  echo "[ERROR] Duplicate Flyway migration versions found:"
  for v in $duplicates; do
    echo ""
    echo "  Version V$v:"
    ls "$MIGRATION_DIR"/V${v}__*.sql 2>/dev/null | while read f; do
      echo "    - $(basename "$f")"
    done
  done
  echo ""
  echo "Each migration version must be unique."
  echo "Rename one of the conflicting files to the next available version."
  echo ""
  exit 1
else
  echo ""
  echo "[OK] All Flyway migration versions are unique."
  echo ""
  exit 0
fi