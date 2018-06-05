package com.codefarm.ca.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import com.codefarm.ca.exception.StorageException;
import com.codefarm.ca.interf.Store;

public class PfxStore implements Store<KeyStore>
{
    private String path;
    
    public PfxStore(String path)
    {
        super();
        this.path = path;
    }
    
    public void save(KeyStore obj, String password) throws StorageException
    {
        try
        {
            obj.load(null, null);
            obj.store(new FileOutputStream(new File(path)),
                    password == null ? "".toCharArray()
                            : password.toCharArray());
        }
        catch (KeyStoreException e)
        {
            throw new StorageException(e);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new StorageException(e);
        }
        catch (CertificateException e)
        {
            throw new StorageException(e);
        }
        catch (FileNotFoundException e)
        {
            throw new StorageException(e);
        }
        catch (IOException e)
        {
            throw new StorageException(e);
        }
    }
    
    public KeyStore read() throws StorageException
    {
        try
        {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");
            keyStore.load(new FileInputStream(new File(path)), null);
            return keyStore;
        }
        catch (Exception e)
        {
            throw new StorageException(e);
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
    
}
