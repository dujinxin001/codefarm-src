package com.sxj.ca.interf;

import com.sxj.ca.exception.StorageException;

public interface Store<T>
{
    public void save(T obj, String password) throws StorageException;
    
    public T read() throws StorageException;
}
