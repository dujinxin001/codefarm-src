package com.codefarm.ca.store;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter;

import com.codefarm.ca.exception.StorageException;
import com.codefarm.ca.interf.Store;

public class PEMFileStore<T> implements Store<T>
{
    private String path;
    
    public PEMFileStore(String path)
    {
        super();
        this.path = path;
    }
    
    public PEMFileStore()
    {
        super();
    }
    
    public void save(T obj, String password) throws StorageException
    {
        PEMWriter writer = null;
        try
        {
            writer = new PemWriter(new FileWriter(path));
            if (password != null)
                writer.writeObject(obj,
                        "DESEDE",
                        password.toCharArray(),
                        new SecureRandom());
            else
                writer.writeObject(obj);
            
        }
        catch (IOException e)
        {
            throw new StorageException(e);
        }
        finally
        {
            if (writer != null)
                try
                {
                    writer.close();
                }
                catch (IOException e)
                {
                    throw new StorageException(e);
                }
        }
    }
    
    public String getPath()
    {
        return path;
    }
    
    public void setPath(String path)
    {
        this.path = path;
    }
    
    public T read() throws StorageException
    {
        PEMReader reader = null;
        try
        {
            reader = new PemReader(new FileReader(path));
            
            return (T) reader.readObject();
            
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
        finally
        {
            if (reader != null)
                try
                {
                    reader.close();
                }
                catch (IOException e)
                {
                    throw new StorageException(e);
                }
        }
    }
}
