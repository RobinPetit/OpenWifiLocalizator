if [ -f "OWL-DB.db" ]; then
	rm "OWL-DB.db"
fi
sqlite3 "OWL-DB.db" < init_db.sql
