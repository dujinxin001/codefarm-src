package com.sxj.mybatis.shard.mapper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.FactoryBean;

import com.sxj.mybatis.shard.session.ShardMapperProxy;


public class ShardMapperFactoryBean<T> implements FactoryBean<T> {

	private Class<T> mapperInterface;

	public void setMapperInterface(Class<T> mapperInterface) {
		this.mapperInterface = mapperInterface;
	}

	@SuppressWarnings("rawtypes")
	private Map<Class, Object> mappers = new HashMap<Class, Object>();

	@SuppressWarnings("unchecked")
	public T getObject() throws Exception {
		T obj = (T) mappers.get(mapperInterface);
		if (obj != null) {
			return obj;
		}
		obj = ShardMapperProxy.newMapperProxy(mapperInterface);
		mappers.put(mapperInterface, obj);
		return obj;
	}

	public Class<T> getObjectType() {
		return this.mapperInterface;
	}

	public boolean isSingleton() {
		return true;
	}

}
