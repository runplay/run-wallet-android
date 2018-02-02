package run.wallet.common;

public class DbField {
	private String NAME;
	private int FIELD_TYPE;
	private boolean PRIMARY_KEY;
	private boolean HAS_INDEX;
	
	public static final int FIELD_TYPE_INT=0;
	public static final int FIELD_TYPE_TEXT=1;
	public static final int FIELD_TYPE_FLOAT=2;
	public static final int FIELD_TYPE_BLOB=3;
	
	public DbField(String name, int FIELD_TYPE, boolean isPrimaryKey, boolean indexIfNotPrimary){
		this.NAME=name;
		this.FIELD_TYPE=FIELD_TYPE;
		this.PRIMARY_KEY=isPrimaryKey;
		this.HAS_INDEX=indexIfNotPrimary;
	}
	public DbField(String name, int FIELD_TYPE){
		this.NAME=name;
		this.FIELD_TYPE=FIELD_TYPE;
		this.PRIMARY_KEY=false;
	}

	public String getName() {
		return this.NAME;
	}
	public int getFieldType() {
		return this.FIELD_TYPE;
	}
	public boolean isPrimary() {
		return this.PRIMARY_KEY;
	}
	public boolean hasIndex() {
		return HAS_INDEX;
	}
}