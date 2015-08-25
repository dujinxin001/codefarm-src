package com.sxj.ca;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.junit.Test;

import com.sxj.ca.exception.CertificateException;
import com.sxj.ca.exception.KeyPairException;
import com.sxj.ca.exception.StorageException;
import com.sxj.ca.store.PEMFileStore;
import com.sxj.ca.store.PfxStore;

public class CAManagerTest
{
    PEMFileStore<KeyPair> keystore = new PEMFileStore<KeyPair>(
            "D:\\certs\\ca.key");
    
    PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
            "D:\\certs\\ca.crt");
    
    CAManager ca = null;
    
    /**Step 1，创建CA
     * @throws KeyPairException
     * @throws CertificateException
     */
    public void initCA() throws KeyPairException, CertificateException
    {
        ca = new CAManager();
        X509Attrs principals = new X509Attrs();
        principals.setCommonName("私享家CA根证书");
        principals.setCountryCode("AU");
        ca.init(keystore, certstore, principals);
    }
    
    /**
     * Step 3，生成证书请求
     * @throws KeyPairException 
     * @throws CertificateException 
     * @throws StorageException 
     */
    public void createClientCSR() throws KeyPairException,
            CertificateException, StorageException
    {
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\client.key");
        PEMFileStore<PKCS10CertificationRequest> clientrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\client.req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        clientkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName("CRM平台根证书");
        principals.setCountryCode("AU");
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        clientrequeststore.save(csr, null);
    }
    
    /**Step 4，利用中间证书签发客户证书
     * @throws StorageException
     * @throws CertificateException
     */
    public void createClientCert() throws StorageException,
            CertificateException
    {
        PEMFileStore<PKCS10CertificationRequest> clientrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\client.req");
        PEMFileStore<KeyPair> serverkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\server.key");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\server.crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\client.crt");
        PKCS10CertificationRequest request = clientrequeststore.read();
        X509Certificate parentcert = servercertstore.read();
        KeyPair parentkey = serverkeystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                365,
                parentcert,
                parentkey,
                true);
        clientcertstore.save(certificate, null);
    }
    
    /**Step 5，生成PKCS12
     * @throws StorageException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     * @throws IOException
     */
    public void createClientPfx() throws CertificateException, StorageException
    {
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\inter.crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\server.crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\client.crt");
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\client.key");
        PfxStore pfxstore = new PfxStore("D:\\certs\\client.pfx");
        X509Certificate cacert = certstore.read();
        X509Certificate intercert = intercertstore.read();
        X509Certificate servercert = servercertstore.read();
        X509Certificate clientcert = clientcertstore.read();
        X509Certificate[] chain = new X509Certificate[4];
        chain[0] = (clientcert);
        chain[1] = (servercert);
        chain[2] = (intercert);
        chain[3] = (cacert);
        KeyPair clientkey = clientkeystore.read();
        KeyStore pkcs12 = ca.generatePKCS12(chain, clientkey);
        pfxstore.save(pkcs12, "123456");
    }
    
    /**
     * Step 6，生成证书请求
     * @throws KeyPairException 
     * @throws CertificateException 
     * @throws StorageException 
     */
    public void createServerCSR() throws KeyPairException,
            CertificateException, StorageException
    {
        PEMFileStore<KeyPair> serverkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\herongwangserver.key");
        PEMFileStore<PKCS10CertificationRequest> serverrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\herongwangserver.req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        serverkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName("*.herongwangnt.com");
        principals.setCountryCode("CN");
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        serverrequeststore.save(csr, null);
    }
    
    /**Step 7，利用中间证书签发服务器证书
     * @throws StorageException
     * @throws CertificateException
     */
    public void createServerCert() throws StorageException,
            CertificateException
    {
        PEMFileStore<PKCS10CertificationRequest> serverrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\herongwangserver.req");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\inter.crt");
        PEMFileStore<KeyPair> interkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\inter.key");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\herongwangserver.crt");
        
        PKCS10CertificationRequest request = serverrequeststore.read();
        X509Certificate parentcert = intercertstore.read();
        KeyPair parentkey = interkeystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                365,
                parentcert,
                parentkey,
                true);
        servercertstore.save(certificate, null);
    }
    
    public void createIntermediateCSR() throws KeyPairException,
            StorageException, CertificateException
    {
        PEMFileStore<KeyPair> interkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\inter.key");
        PEMFileStore<PKCS10CertificationRequest> interrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\inter.req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        interkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName("一级子CA证书");
        principals.setCountryCode("AU");
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        
        interrequeststore.save(csr, null);
    }
    
    public void createIntermediateCert() throws StorageException,
            CertificateException
    {
        PEMFileStore<PKCS10CertificationRequest> interrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\inter.req");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\inter.crt");
        PKCS10CertificationRequest request = interrequeststore.read();
        X509Certificate parentcert = certstore.read();
        KeyPair parentkey = keystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                365,
                parentcert,
                parentkey,
                true);
        intercertstore.save(certificate, null);
    }
    
    public void createEmployeeCSR() throws KeyPairException, StorageException,
            CertificateException
    {
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        PublicKey public1 = keypair.getPublic();
        PEMFileStore<PublicKey> publicstore = new PEMFileStore<PublicKey>(
                "D:\\certs\\employee.pub");
        publicstore.save(public1, null);
        PEMFileStore<KeyPair> employeekeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\employee.key");
        employeekeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName("CRM测试员工");
        principals.setCountryCode("AU");
        principals.setGiveName("E00001");
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        PEMFileStore<PKCS10CertificationRequest> employeerequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\employee.req");
        employeerequeststore.save(csr, null);
    }
    
    public void createEmployeeCert() throws StorageException,
            CertificateException
    {
        PEMFileStore<PKCS10CertificationRequest> employeerequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                "D:\\certs\\employee.req");
        PEMFileStore<X509Certificate> employeecertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\employee.crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\client.crt");
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                "D:\\certs\\client.key");
        PKCS10CertificationRequest request = employeerequeststore.read();
        X509Certificate parentcert = clientcertstore.read();
        KeyPair parentkey = clientkeystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                365,
                parentcert,
                parentkey,
                false);
        
        employeecertstore.save(certificate, null);
    }
    
    public void createEmployeePfx() throws StorageException,
            CertificateException
    {
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\inter.crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\server.crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\client.crt");
        PEMFileStore<X509Certificate> employeecertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\employee.crt");
        X509Certificate cacert = certstore.read();
        X509Certificate intercert = intercertstore.read();
        X509Certificate servercert = servercertstore.read();
        X509Certificate clientcert = clientcertstore.read();
        X509Certificate employeecert = employeecertstore.read();
        X509Certificate[] chain = new X509Certificate[5];
        chain[0] = (employeecert);
        chain[1] = (clientcert);
        chain[2] = (servercert);
        chain[3] = (intercert);
        chain[4] = (cacert);
        KeyPair employeekey = new PEMFileStore<KeyPair>(
                "D:\\certs\\employee.key").read();
        KeyStore pkcs12 = ca.generatePKCS12(chain, employeekey);
        new PfxStore("D://certs//employee.pfx").save(pkcs12, "123456");
    }
    
    public void createServerPfx() throws StorageException, CertificateException
    {
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\inter.crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                "D:\\certs\\herongwangserver.crt");
        X509Certificate cacert = certstore.read();
        X509Certificate intercert = intercertstore.read();
        X509Certificate servercert = servercertstore.read();
        X509Certificate[] chain = new X509Certificate[3];
        chain[0] = (servercert);
        chain[1] = (intercert);
        chain[2] = (cacert);
        KeyPair serverkey = new PEMFileStore<KeyPair>(
                "D:\\certs\\herongwangserver.key").read();
        KeyStore pkcs12 = ca.generatePKCS12(chain, serverkey);
        new PfxStore("D://certs//herongwangserver.pfx").save(pkcs12, "123456");
    }
    
    @Test
    public void testProcess() throws KeyPairException, CertificateException,
            StorageException, KeyStoreException, NoSuchProviderException,
            NoSuchAlgorithmException, java.security.cert.CertificateException,
            IOException
    {
        initCA();
        //createIntermediateCSR();
        // createIntermediateCert();
        
        createServerCSR();
        createServerCert();
        createServerPfx();
        
        //createClientCSR();
        // createClientCert();
        //createClientPfx();
        
        //createEmployeeCSR();
        //createEmployeeCert();
        //createEmployeePfx();
        //        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        //        PublicKey public1 = keypair.getPublic();
        //        public1.getEncoded();
        //        PEMFileStore<PublicKey> publicstore = new PEMFileStore<PublicKey>(
        //                "D:\\certs\\ssh_employee.pub");
        //        PEMFileStore<KeyPair> employeekeystore = new PEMFileStore<KeyPair>(
        //                "D:\\certs\\ssh_employee.key");
        //        employeekeystore.save(keypair, null);
        //        publicstore.save(public1, null);
    }
}
