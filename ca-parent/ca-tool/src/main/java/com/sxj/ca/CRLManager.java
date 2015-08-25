package com.sxj.ca;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.x509.X509V2CRLGenerator;
import org.bouncycastle.x509.extension.AuthorityKeyIdentifierStructure;

import com.sxj.ca.exception.CRLException;
import com.sxj.ca.exception.StorageException;
import com.sxj.ca.store.PEMFileStore;

/**
 * @author Administrator
 * 证书撤销列表
 *
 */
public class CRLManager
{
    static
    {
        // Load BouncyCastle security provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    public X509CRL newEmptyCRL(X509Certificate issuerCert, KeyPair issuerKeyPair)
            throws CRLException
    {
        try
        {
            X509V2CRLGenerator crlGen = new X509V2CRLGenerator();
            Date now = new Date();
            crlGen.setIssuerDN(issuerCert.getSubjectX500Principal());
            crlGen.setThisUpdate(now);
            crlGen.setNextUpdate(new Date(now.getTime() + 100000));
            crlGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
            crlGen.addExtension(X509Extensions.AuthorityKeyIdentifier,
                    false,
                    new AuthorityKeyIdentifierStructure(issuerCert));
            crlGen.addExtension(X509Extensions.CRLNumber, false, new CRLNumber(
                    BigInteger.valueOf(1)));
            return crlGen.generate(issuerKeyPair.getPrivate(), "BC");
        }
        catch (Exception e)
        {
            throw new CRLException(e);
        }
    }
    
    public X509CRL newCRL(X509Certificate issuerCert, KeyPair issuerKeyPair,
            BigInteger serialNumber) throws CRLException
    {
        try
        {
            X509V2CRLGenerator crlgenerator = new X509V2CRLGenerator();
            Date now = new Date();
            crlgenerator.setIssuerDN(issuerCert.getSubjectX500Principal());
            crlgenerator.setThisUpdate(now);
            crlgenerator.setNextUpdate(new Date(now.getTime() + 100000));
            crlgenerator.setSignatureAlgorithm("SHA256WithRSAEncryption");
            crlgenerator.addCRLEntry(serialNumber,
                    now,
                    CRLReason.privilegeWithdrawn);
            crlgenerator.addExtension(X509Extensions.AuthorityKeyIdentifier,
                    false,
                    new AuthorityKeyIdentifierStructure(issuerCert));
            crlgenerator.addExtension(X509Extensions.CRLNumber,
                    false,
                    new CRLNumber(BigInteger.valueOf(1)));
            return crlgenerator.generate(issuerKeyPair.getPrivate(), "BC");
        }
        catch (Exception e)
        {
            throw new CRLException(e);
        }
    }
    
    public X509CRL revoke(X509CRL oldCRL, X509Certificate issuerCert,
            KeyPair issuerKeyPair, BigInteger serialNumber) throws CRLException
    {
        try
        {
            
            X509V2CRLGenerator crlgenerator = new X509V2CRLGenerator();
            Date now = new Date();
            crlgenerator.addCRL(oldCRL);
            crlgenerator.setIssuerDN(issuerCert.getSubjectX500Principal());
            crlgenerator.setThisUpdate(now);
            crlgenerator.setNextUpdate(new Date(now.getTime() + 100000));
            crlgenerator.setSignatureAlgorithm("SHA256WithRSAEncryption");
            crlgenerator.addCRLEntry(serialNumber,
                    now,
                    CRLReason.privilegeWithdrawn);
            crlgenerator.addExtension(X509Extensions.AuthorityKeyIdentifier,
                    false,
                    new AuthorityKeyIdentifierStructure(issuerCert));
            byte[] extensionValue = oldCRL.getExtensionValue(X509Extensions.CRLNumber.getId());
            crlgenerator.addExtension(X509Extensions.CRLNumber,
                    false,
                    new CRLNumber(
                            new BigInteger(extensionValue).add(BigInteger.valueOf(1))));
            return crlgenerator.generate(issuerKeyPair.getPrivate(), "BC");
        }
        catch (Exception e)
        {
            throw new CRLException(e);
        }
    }
    
    public boolean verify(X509CRL crl, X509Certificate cert)
    {
        return crl.isRevoked(cert);
    }
    
    public boolean isValidCRL(X509CRL crl, X509Certificate issuerCert)
    {
        try
        {
            crl.verify(issuerCert.getPublicKey(), "BC");
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    
    public static void main(String... args)
    {
        PEMFileStore<KeyPair> keystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\ca.key");
        
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\ca.crt");
        PEMFileStore<X509Certificate> employeecertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\employee.crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\client.crt");
        CRLManager crlmanager = new CRLManager();
        try
        {
            X509CRL crl = crlmanager.newEmptyCRL(certstore.read(),
                    keystore.read());
            X509Certificate employeecert = employeecertstore.read();
            X509Certificate clientcert = clientcertstore.read();
            
            crl = crlmanager.revoke(crl,
                    certstore.read(),
                    keystore.read(),
                    employeecert.getSerialNumber());
            System.out.println(crlmanager.verify(crl, employeecert));
            System.out.println(crlmanager.verify(crl, clientcert));
        }
        catch (CRLException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (StorageException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
