package com.liferay.devtool.bundles;

public class DbSchemaEntry {
	private String schemaName;
	private int tableCount;
	private String schemaVersion;
	private boolean atDeployed = false;
	
	public String getSchemaName() {
		return schemaName;
	}
	
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	
	public int getTableCount() {
		return tableCount;
	}
	
	public void setTableCount(int tableCount) {
		this.tableCount = tableCount;
	}
	
	public String getSchemaVersion() {
		return schemaVersion;
	}
	
	public void setSchemaVersion(String schemaVersion) {
		this.schemaVersion = schemaVersion;
	}

	public boolean isAtDeployed() {
		return atDeployed;
	}

	public void setAtDeployed(boolean atDeployed) {
		this.atDeployed = atDeployed;
	}
}
