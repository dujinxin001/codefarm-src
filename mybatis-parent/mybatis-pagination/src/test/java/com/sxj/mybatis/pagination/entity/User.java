package com.sxj.mybatis.pagination.entity;

import java.io.Serializable;

public class User implements Serializable {

	/** 
	 * 
	 */
	private static final long serialVersionUID = -7458068073273177502L;

	private Integer id;

	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
