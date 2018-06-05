package com.codefarm.ca.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DEROutputStream;

import com.codefarm.ca.exception.StorageException;
import com.codefarm.ca.interf.Store;

public class DERFileStore<T> implements Store<T>
{
    private String path;
    
    public DERFileStore(String path)
    {
        super();
        this.path = path;
    }
    
    public void save(T obj, String password) throws StorageException
    {
        ASN1InputStream input = null;
        DEROutputStream out = null;
        try
        {
            if (obj instanceof X509Certificate)
            {
                
                input = new ASN1InputStream(
                        ((X509Certificate) obj).getEncoded());
                out = new DEROutputStream(new FileOutputStream(new File(path)));
                out.writeObject(input.readObject());
            }
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
        finally
        {
            if (out != null)
                try
                {
                    out.close();
                }
                catch (IOException e)
                {
                    throw new StorageException(e);
                }
            if (input != null)
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    throw new StorageException(e);
                }
            
        }
    }
    
    public T read() throws StorageException
    {
        DERInputStream input = null;
        try
        {
            input = new DERInputStream(new FileInputStream(new File(path)));
            return (T) input.readObject();
        }
        catch (Exception e)
        {
            throw new StorageException(e);
        }
        finally
        {
            if (input != null)
                try
                {
                    input.close();
                }
                catch (IOException e)
                {
                    throw new StorageException(e);
                }
        }
    }
    
}
