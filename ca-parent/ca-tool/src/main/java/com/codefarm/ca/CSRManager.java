package com.codefarm.ca;

import java.security.KeyPair;
import java.security.Security;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.X509Principal;

import com.codefarm.ca.exception.CertificateException;

/**
 * @author Administrator
 * 证书申请
 *
 */
public class CSRManager
{
    static
    {
        // Load BouncyCastle security provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    public static PKCS10CertificationRequest generateCSR(KeyPair pair,
            X509Attrs attrs) throws CertificateException
    {
        try
        {
            X509Principal x509Principal = new X509Principal(
                    attrs.getOrdering(), attrs.getAttrs());
            return new PKCS10CertificationRequest("SHA256withRSA",
                    new X500Principal(x509Principal.getEncoded()),
                    pair.getPublic(), null, pair.getPrivate());
        }
        catch (Exception e)
        {
            throw new CertificateException(e);
        }
    }
}
