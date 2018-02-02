package run.wallet.common;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;


public abstract class Db {

    //private static final Db=new Db();
	private static final String DATABASE_FILE="runlock.db";
	
	protected SQLiteDatabase db;
	protected Context context;
	public final String TABLE_NAME;  // "TestingData.db"
	public final DbField[] TABLE_FIELDS;
	private static String[] TABLE_FIELD_NAMES;


	public Db(String TABLE_NAME, DbField[] TABLE_FIELDS, Context context) {
		this.TABLE_NAME=TABLE_NAME;
		this.TABLE_FIELDS=TABLE_FIELDS;
		this.context=context;
		open();
		ensureTable(context);
	}
	
	public void close() {
		if(db!=null)
			db.close();
		
	}
    private DatabaseErrorHandler bderrorhandler = new DatabaseErrorHandler() {
        @Override
        public void onCorruption(SQLiteDatabase sqLiteDatabase) {
            //Log.e("DBERROR","returns db errorrrrrrrrrrrrrrrrrrrrrr");
        }
    };
    public long getSizeOnDisk() {
        return new File(db.getPath()).length();
    }
	public boolean open() {
		if(db==null || !db.isOpen()) {
			try {
		        db = context.openOrCreateDatabase(
		        		DATABASE_FILE
		       		, Context.MODE_PRIVATE
		       		, null
                        ,bderrorhandler
		      		);
			} catch(Exception e) {
				//Log.add("DB open() error, cannot open or create DB."+DATABASE_FILE);
			}
		}
		if(db!=null && db.isOpen()) {
			return true;
		} else {
			return false;
		}
	}
	//public abstract String createTable();
	
	/*
	 *  EnsureTable
	 *  confirms table exists, if not create it
	 *  confirms all table columns exist, if not modify table to include new columns (for updates that involve adding a new column to the dataset)
	 * 
	 */
	public synchronized boolean ensureTable(Context context) {
		boolean ensured=false;
		//Log.e("DBT", "-"+TABLE_NAME);
		if(open()) {
		
        //db.setVersion(1);
	        if(TABLE_NAME!=null) {


                boolean hasTable=false;
                Cursor checkcursor = db.rawQuery("SELECT * FROM sqlite_master WHERE name=\""+TABLE_NAME+"\"",null);//+TABLE_NAME+" LIMIT 1", null);;
                if(checkcursor!=null) {
                    checkcursor.moveToFirst();
                    if (checkcursor.getCount() > 0) {
                        do {
                            hasTable=true;
                            //Log.e("DBO",checkcursor.getString(checkcursor.getColumnIndex("name")));
                        } while (checkcursor.moveToNext());
                    }
                }

                // below is sql to select all tables, use this to check for table and create  if not there, want to do silent style.
                // SELECT * FROM sqlite_master;
                if(!hasTable) {
                    createTable();
                }

                Cursor cursor = null;



	        	if(hasTable) {
                    try {
                        cursor = db.rawQuery("SELECT * FROM "+TABLE_NAME+" LIMIT 1", null);
                    } catch(Exception e) {
                        // no table exists, create it and done.
                        //createTable();
                        //ensured=true;
                    }
		        	if(cursor!=null) {
		            	// test columns
		            	ArrayList<DbField> noncolumns=new ArrayList<DbField>();
		            	
		            	String[] cols = cursor.getColumnNames();
		            	for(int i=0; i<TABLE_FIELDS.length; i++) {
		            		boolean found=false;
		            		DbField f= TABLE_FIELDS[i];
		            		for(int j=0; j<cols.length; j++) {
		            			if(cols[j].equals(f.getName())) {
		            				found=true; 
		            				break;
		            			}
		            		}
		            		// if it gets here no field found
		            		if(!found)
		            			noncolumns.add(f);
		            	}
		            	if(!noncolumns.isEmpty())
		            		modifyTableAddColums(noncolumns);
		        	} else {
		        		//Log.e("DB","DB error ensureTable() "+TABLE_NAME+" cursor returned as NULL");
		        	}
	        	}
	        	if(cursor!=null)
	        		cursor.close();
	        } else {
				//Log.e("DB","DB error ensureTable() NULL TABLE_NAME");
	        }
		}
		return ensured;
	}
	
	private void modifyTableAddColums(ArrayList<DbField> addFields) {
		
		if(addFields!=null && !addFields.isEmpty()) {
			for(DbField field: addFields) {
				StringBuilder sb = new StringBuilder("ALTER TABLE ");
				sb.append(TABLE_NAME);
				sb.append(" ADD COLUMN ");
				sb.append(field.getName());
				sb.append(getSqlFieldType(field));
				
				//Log.e("DB-ALTER",sb.toString());
				db.execSQL(sb.toString());
			}
		}
	}
	
	private void createTable() {
		
		StringBuilder sb = new StringBuilder("CREATE TABLE ");
		sb.append(TABLE_NAME);
		sb.append(" (");
		for(int i=0; i<TABLE_FIELDS.length; i++) {
			if(i!=0)
				sb.append(", ");
			sb.append(TABLE_FIELDS[i].getName());
			sb.append(getSqlFieldType(TABLE_FIELDS[i]));
		}
		sb.append(")");
		//Log.e("DBCREATETABLE",sb.toString());
		db.execSQL(sb.toString());
		for(int i=0; i<TABLE_FIELDS.length; i++) {
			DbField f = TABLE_FIELDS[i];
			if(f.hasIndex()) {
				String index="CREATE INDEX "+TABLE_NAME+"_"+f.getName()+" ON "+TABLE_NAME+"("+f.getName()+")";
				db.execSQL(index);
				//Log.e("DB_INDEX_TABLE",index);
			}
		}
	}
	
	protected String getSqlFieldType(DbField field) {
		StringBuilder sb = new StringBuilder(" ");
		switch(field.getFieldType()) {
			case DbField.FIELD_TYPE_TEXT:
				sb.append("TEXT");
				break;
			case DbField.FIELD_TYPE_INT:
				sb.append("INTEGER");
				if(field.isPrimary())
					sb.append(" PRIMARY KEY AUTOINCREMENT");
				break;
			case DbField.FIELD_TYPE_FLOAT:
				sb.append("REAL");
				break;
			case DbField.FIELD_TYPE_BLOB:
				sb.append("BLOB");
				break;
			default:
				break;
		}
		return sb.toString();
	}
	
	public String[] getFieldNames() {

			TABLE_FIELD_NAMES=new String[TABLE_FIELDS.length];
			for(int i=0; i<TABLE_FIELDS.length; i++) {
				TABLE_FIELD_NAMES[i]=TABLE_FIELDS[i].getName();
			}

		return TABLE_FIELD_NAMES;
	}

	
}
