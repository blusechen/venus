package com.meidusa.venus.client;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;

public class ServiceFactoryBean<T> implements FactoryBean<T> {
	private T obj;
	private Class<T> type;
	public ServiceFactoryBean(T obj,Class<T> type) {
		this.obj = obj;
		this.type = type;
	}

	@Override
	public T getObject() throws BeansException {
		return obj;
	}

	@Override
	public Class<T> getObjectType() {
		return type;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}