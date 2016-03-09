package com.codefarm.mybatis.shard.configuration.node;

public class ShardRuleCfg {

	private String tableName;

	private String column;

	
	
	@Override
	public String toString() {
		return "ShardRule [tableName=" + tableName + ", column=" + column + "]";
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumn() {
		return column;
	}

	public void setColumn(String column) {
		this.column = column;
	}

}
