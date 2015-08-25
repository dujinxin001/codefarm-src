package com.sxj.ca.store;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import org.bouncycastle.jce.PKCS10CertificationRequest;

import com.sxj.ca.CAManager;
import com.sxj.ca.CSRManager;
import com.sxj.ca.KeyPairManager;
import com.sxj.ca.X509Attrs;
import com.sxj.ca.exception.CertificateException;
import com.sxj.ca.exception.KeyPairException;
import com.sxj.ca.exception.StorageException;

public class CaTool
{
    /**
     * 生成CA根证书
     * @param caName
     * @param caPath
     * @param commonName
     * @param au
     * @throws KeyPairException
     * @throws CertificateException
     */
    public static void initCA(String caName, String caPath, String commonName,
            String au) throws KeyPairException, CertificateException
    {
        PEMFileStore<KeyPair> keystore = new PEMFileStore<KeyPair>(caPath
                + caName + ".key");
        
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                caPath + caName + ".crt");
        CAManager ca = new CAManager();
        X509Attrs principals = new X509Attrs();
        principals.setCommonName(commonName);
        principals.setCountryCode(au);
        ca.init(keystore, certstore, principals);
    }
    
    /**
     * 生成一级子根证书
     * @param interName
     * @param interPath
     * @param commonName
     * @param au
     * @throws KeyPairException
     * @throws StorageException
     * @throws CertificateException
     */
    public static void createIntermediateCSR(String interName,
            String interPath, String commonName, String au)
            throws KeyPairException, StorageException, CertificateException
    {
        PEMFileStore<KeyPair> interkeystore = new PEMFileStore<KeyPair>(
                interPath + interName + ".key");
        PEMFileStore<PKCS10CertificationRequest> interrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                interPath + interName + ".req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        interkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName(commonName);
        principals.setCountryCode(au);
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        interrequeststore.save(csr, null);
    }
    
    public static void createIntermediateCert(String interName,
            String interPath, String caName, String caPath)
            throws StorageException, CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<KeyPair> keystore = new PEMFileStore<KeyPair>(caPath
                + caName + ".key");
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                caPath + caName + ".crt");
        
        PEMFileStore<PKCS10CertificationRequest> interrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                interPath + interName + ".req");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                interPath + interName + ".crt");
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
    
    /**
     * 生成服务器证书请求
     * @throws KeyPairException 
     * @throws CertificateException 
     * @throws StorageException 
     */
    public static void createServerCSR(String serverName, String serverPath,
            String commonName, String cn) throws KeyPairException,
            CertificateException, StorageException
    {
        PEMFileStore<KeyPair> serverkeystore = new PEMFileStore<KeyPair>(
                serverPath + serverName + ".key");
        PEMFileStore<PKCS10CertificationRequest> serverrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                serverPath + serverName + ".req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        serverkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName(commonName);
        principals.setCountryCode(cn);
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        serverrequeststore.save(csr, null);
    }
    
    /**Step 7，利用中间证书签发服务器证书
     * @throws StorageException
     * @throws CertificateException
     */
    public static void createServerCert(String serverName, String serverPath,
            String interName, String interpath) throws StorageException,
            CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<PKCS10CertificationRequest> serverrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                serverPath + serverName + ".req");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                interpath + interName + ".crt");
        PEMFileStore<KeyPair> interkeystore = new PEMFileStore<KeyPair>(
                interpath + interName + ".key");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                serverPath + serverName + ".crt");
        
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
    
    public static void createServerPfx(String caName, String caPath,
            String interName, String interPath, String serverName,
            String serverPath, String password) throws StorageException,
            CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                caPath + caName + ".crt");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                interPath + interName + ".crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                serverPath + serverName + ".crt");
        X509Certificate cacert = certstore.read();
        X509Certificate intercert = intercertstore.read();
        X509Certificate servercert = servercertstore.read();
        X509Certificate[] chain = new X509Certificate[3];
        chain[0] = (servercert);
        chain[1] = (intercert);
        chain[2] = (cacert);
        KeyPair serverkey = new PEMFileStore<KeyPair>(serverPath + serverName
                + ".key").read();
        KeyStore pkcs12 = ca.generatePKCS12(chain, serverkey);
        new PfxStore(serverPath + serverName + ".pfx").save(pkcs12, password);
    }
    
    /**
     * Step 3，生成证书请求
     * @throws KeyPairException 
     * @throws CertificateException 
     * @throws StorageException 
     */
    public static void createClientCSR(String cilentName, String cilentPath,
            String commonName, String au) throws KeyPairException,
            CertificateException, StorageException
    {
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                cilentPath + cilentName + ".key");
        PEMFileStore<PKCS10CertificationRequest> clientrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                cilentPath + cilentName + ".req");
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        clientkeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName(commonName);
        principals.setCountryCode(au);
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        clientrequeststore.save(csr, null);
    }
    
    /**Step 4，利用中间证书签发客户证书
     * @throws StorageException
     * @throws CertificateException
     */
    public static void createClientCert(String cilentName, String clientPath,
            String serverName, String serverPath, int days)
            throws StorageException, CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<PKCS10CertificationRequest> clientrequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                clientPath + cilentName + ".req");
        PEMFileStore<KeyPair> serverkeystore = new PEMFileStore<KeyPair>(
                serverPath + serverName + ".key");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                serverPath + serverName + ".crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                clientPath + cilentName + ".crt");
        PKCS10CertificationRequest request = clientrequeststore.read();
        X509Certificate parentcert = servercertstore.read();
        KeyPair parentkey = serverkeystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                days,
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
    public static void createClientPfx(String caName, String caPath,
            String interName, String interPath, String cilentName,
            String cilentPath, String serverName, String serverPath,
            String password) throws CertificateException, StorageException
    {
        CAManager ca = new CAManager();
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                caPath + caName + ".crt");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                interPath + interName + ".crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                serverPath + serverName + ".crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                cilentPath + cilentName + ".crt");
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                cilentPath + cilentName + ".key");
        PfxStore pfxstore = new PfxStore(cilentPath + cilentName + ".pfx");
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
        pfxstore.save(pkcs12, password);
    }
    
    public static void createEmployeeCSR(String name, String path,
            String commonName, String au, String giveName)
            throws KeyPairException, StorageException, CertificateException
    {
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        PublicKey public1 = keypair.getPublic();
        PEMFileStore<PublicKey> publicstore = new PEMFileStore<PublicKey>(path
                + name + ".pub");
        publicstore.save(public1, null);
        PEMFileStore<KeyPair> employeekeystore = new PEMFileStore<KeyPair>(path
                + name + ".key");
        employeekeystore.save(keypair, null);
        X509Attrs principals = new X509Attrs();
        principals.setCommonName(commonName);
        principals.setCountryCode(au);
        principals.setGiveName(giveName);
        PKCS10CertificationRequest csr = CSRManager.generateCSR(keypair,
                principals);
        PEMFileStore<PKCS10CertificationRequest> employeerequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                path + name + ".req");
        employeerequeststore.save(csr, null);
    }
    
    public static void createEmployeeCert(String name, String path,
            String cilentName, String cilentPath, int days)
            throws StorageException, CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<PKCS10CertificationRequest> employeerequeststore = new PEMFileStore<PKCS10CertificationRequest>(
                path + name + ".req");
        PEMFileStore<X509Certificate> employeecertstore = new PEMFileStore<X509Certificate>(
                path + name + ".crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                cilentPath + cilentName + ".crt");
        PEMFileStore<KeyPair> clientkeystore = new PEMFileStore<KeyPair>(
                cilentPath + cilentName + ".key");
        PKCS10CertificationRequest request = employeerequeststore.read();
        X509Certificate parentcert = clientcertstore.read();
        KeyPair parentkey = clientkeystore.read();
        X509Certificate certificate = ca.issueCertificate(request,
                days,
                parentcert,
                parentkey,
                false);
        
        employeecertstore.save(certificate, null);
    }
    
    public static void createEmployeePfx(String caName, String caPath,
            String interName, String interPath, String serverName,
            String serverPath, String cilentName, String cilentPath,
            String employeeName, String employeePath, String password)
            throws StorageException, CertificateException
    {
        CAManager ca = new CAManager();
        PEMFileStore<X509Certificate> certstore = new PEMFileStore<X509Certificate>(
                caPath + caName + ".crt");
        PEMFileStore<X509Certificate> intercertstore = new PEMFileStore<X509Certificate>(
                interPath + interName + ".crt");
        PEMFileStore<X509Certificate> servercertstore = new PEMFileStore<X509Certificate>(
                serverPath + serverName + ".crt");
        PEMFileStore<X509Certificate> clientcertstore = new PEMFileStore<X509Certificate>(
                cilentPath + cilentName + ".crt");
        PEMFileStore<X509Certificate> employeecertstore = new PEMFileStore<X509Certificate>(
                employeePath + employeeName + ".crt");
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
        KeyPair employeekey = new PEMFileStore<KeyPair>(employeePath
                + employeeName + ".key").read();
        KeyStore pkcs12 = ca.generatePKCS12(chain, employeekey);
        new PfxStore(employeePath + employeeName + ".pfx").save(pkcs12,
                password);
    }
}
