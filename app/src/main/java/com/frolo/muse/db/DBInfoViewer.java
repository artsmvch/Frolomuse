package com.frolo.muse.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.PrintStream;


final class DBInfoViewer {
    private DBInfoViewer() {
    }

    static void dump(SQLiteDatabase database, PrintStream stream) {
        final StringBuilder sb = new StringBuilder("\nDatabase v" + database.getVersion() + ":");
        Cursor dbQuery = database.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);
        if (dbQuery.moveToFirst()) {
            do {
                String tableName = dbQuery.getString(dbQuery.getColumnIndex("name"));
                sb.append('\n').append(tableName).append('(');
                Cursor tableQuery = database.query(tableName, null, null, null, null, null, null);
                if (tableQuery != null) {
                    String[] columnNames = tableQuery.getColumnNames();
                    int columnCount = columnNames.length;
                    for (int i = 0; i < columnCount; i++) {
                        sb.append(columnNames[i]);
                        if (i != columnCount - 1) {
                            sb.append(',');
                        }
                    }
                    tableQuery.close();
                }
                sb.append(')');
            } while (dbQuery.moveToNext());
            dbQuery.close();
        }
        stream.print(sb.toString());
    }
}
