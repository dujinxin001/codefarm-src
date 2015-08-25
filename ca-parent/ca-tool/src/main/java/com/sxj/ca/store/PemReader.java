package com.sxj.ca.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.StringTokenizer;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERInteger;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.sec.ECPrivateKeyStructure;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.openssl.PEMException;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordException;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.x509.X509AttributeCertificate;
import org.bouncycastle.x509.X509V2AttributeCertificate;

import com.sxj.ca.key.SSH2_KEY_TYPE;
import com.sxj.ca.key.Ssh2PublicKeyImpl;

public class PemReader extends PEMReader
{
    private final PasswordFinder pFinder;
    
    private final String provider;
    
    public PemReader(Reader arg0, PasswordFinder arg1, String arg2)
    {
        super(arg0);
        this.pFinder = arg1;
        this.provider = arg2;
    }
    
    public PemReader(Reader arg0, PasswordFinder arg1)
    {
        this(arg0, arg1, "BC");
    }
    
    public PemReader(Reader arg0)
    {
        this(arg0, null, "BC");
    }
    
    private PublicKey readPublicKey(String endMarker) throws IOException
    {
        KeySpec keySpec = new X509EncodedKeySpec(readBytes(endMarker));
        String[] algorithms = { "DSA", "RSA" };
        for (int i = 0; i < algorithms.length; i++)
        {
            try
            {
                KeyFactory keyFact = KeyFactory.getInstance(algorithms[i],
                        provider);
                PublicKey pubKey = keyFact.generatePublic(keySpec);
                
                return pubKey;
            }
            catch (NoSuchAlgorithmException e)
            {
                // ignore
            }
            catch (InvalidKeySpecException e)
            {
                // ignore
            }
            catch (NoSuchProviderException e)
            {
                throw new RuntimeException("can't find provider " + provider);
            }
        }
        
        return null;
    }
    
    private PKCS10CertificationRequest readCertificateRequest(String endMarker)
            throws IOException
    {
        try
        {
            return new PKCS10CertificationRequest(readBytes(endMarker));
        }
        catch (Exception e)
        {
            throw new PEMException("problem parsing certrequest: "
                    + e.toString(), e);
        }
    }
    
    private X509Certificate readCertificate(String endMarker)
            throws IOException
    {
        ByteArrayInputStream bIn = new ByteArrayInputStream(
                readBytes(endMarker));
        
        try
        {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509",
                    provider);
            
            return (X509Certificate) certFact.generateCertificate(bIn);
        }
        catch (Exception e)
        {
            throw new PEMException("problem parsing cert: " + e.toString(), e);
        }
    }
    
    private ContentInfo readPKCS7(String endMarker) throws IOException
    {
        String line;
        StringBuffer buf = new StringBuffer();
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        
        while ((line = readLine()) != null)
        {
            if (line.indexOf(endMarker) != -1)
            {
                break;
            }
            
            line = line.trim();
            
            buf.append(line.trim());
            
            Base64.decode(buf.substring(0, (buf.length() / 4) * 4), bOut);
            
            buf.delete(0, (buf.length() / 4) * 4);
        }
        
        if (buf.length() != 0)
        {
            throw new IOException("base64 data appears to be truncated");
        }
        
        if (line == null)
        {
            throw new IOException(endMarker + " not found");
        }
        
        try
        {
            ASN1InputStream aIn = new ASN1InputStream(bOut.toByteArray());
            
            return ContentInfo.getInstance(aIn.readObject());
        }
        catch (Exception e)
        {
            throw new PEMException("problem parsing PKCS7 object: "
                    + e.toString(), e);
        }
    }
    
    private X509CRL readCRL(String endMarker) throws IOException
    {
        ByteArrayInputStream bIn = new ByteArrayInputStream(
                readBytes(endMarker));
        
        try
        {
            CertificateFactory certFact = CertificateFactory.getInstance("X.509",
                    provider);
            
            return (X509CRL) certFact.generateCRL(bIn);
        }
        catch (Exception e)
        {
            throw new PEMException("problem parsing cert: " + e.toString(), e);
        }
    }
    
    private X509AttributeCertificate readAttributeCertificate(String endMarker)
            throws IOException
    {
        return new X509V2AttributeCertificate(readBytes(endMarker));
    }
    
    private KeyPair readKeyPair(String type, String endMarker) throws Exception
    {
        boolean isEncrypted = false;
        String line = null;
        String dekInfo = null;
        StringBuffer buf = new StringBuffer();
        
        while ((line = readLine()) != null)
        {
            if (line.startsWith("Proc-Type: 4,ENCRYPTED"))
            {
                isEncrypted = true;
            }
            else if (line.startsWith("DEK-Info:"))
            {
                dekInfo = line.substring(10);
            }
            else if (line.indexOf(endMarker) != -1)
            {
                break;
            }
            else
            {
                buf.append(line.trim());
            }
        }
        
        //
        // extract the key
        //
        byte[] keyBytes = Base64.decode(buf.toString());
        
        if (isEncrypted)
        {
            if (pFinder == null)
            {
                throw new PasswordException(
                        "No password finder specified, but a password is required");
            }
            
            char[] password = pFinder.getPassword();
            
            if (password == null)
            {
                throw new PasswordException(
                        "Password is null, but a password is required");
            }
            
            StringTokenizer tknz = new StringTokenizer(dekInfo, ",");
            String dekAlgName = tknz.nextToken();
            byte[] iv = Hex.decode(tknz.nextToken());
            
            keyBytes = PEMUtilities.crypt(false,
                    provider,
                    keyBytes,
                    password,
                    dekAlgName,
                    iv);
        }
        
        KeySpec pubSpec, privSpec;
        ASN1Sequence seq = (ASN1Sequence) ASN1Object.fromByteArray(keyBytes);
        
        if (type.equals("RSA"))
        {
            //                DERInteger              v = (DERInteger)seq.getObjectAt(0);
            DERInteger mod = (DERInteger) seq.getObjectAt(1);
            DERInteger pubExp = (DERInteger) seq.getObjectAt(2);
            DERInteger privExp = (DERInteger) seq.getObjectAt(3);
            DERInteger p1 = (DERInteger) seq.getObjectAt(4);
            DERInteger p2 = (DERInteger) seq.getObjectAt(5);
            DERInteger exp1 = (DERInteger) seq.getObjectAt(6);
            DERInteger exp2 = (DERInteger) seq.getObjectAt(7);
            DERInteger crtCoef = (DERInteger) seq.getObjectAt(8);
            
            pubSpec = new RSAPublicKeySpec(mod.getValue(), pubExp.getValue());
            privSpec = new RSAPrivateCrtKeySpec(mod.getValue(),
                    pubExp.getValue(), privExp.getValue(), p1.getValue(),
                    p2.getValue(), exp1.getValue(), exp2.getValue(),
                    crtCoef.getValue());
        }
        else if (type.equals("ECDSA"))
        {
            ECPrivateKeyStructure pKey = new ECPrivateKeyStructure(seq);
            AlgorithmIdentifier algId = new AlgorithmIdentifier(
                    X9ObjectIdentifiers.id_ecPublicKey, pKey.getParameters());
            PrivateKeyInfo privInfo = new PrivateKeyInfo(algId,
                    pKey.getDERObject());
            SubjectPublicKeyInfo pubInfo = new SubjectPublicKeyInfo(algId,
                    pKey.getPublicKey().getBytes());
            
            privSpec = new PKCS8EncodedKeySpec(privInfo.getEncoded());
            pubSpec = new X509EncodedKeySpec(pubInfo.getEncoded());
        }
        else
        // "DSA"
        {
            //                DERInteger              v = (DERInteger)seq.getObjectAt(0);
            DERInteger p = (DERInteger) seq.getObjectAt(1);
            DERInteger q = (DERInteger) seq.getObjectAt(2);
            DERInteger g = (DERInteger) seq.getObjectAt(3);
            DERInteger y = (DERInteger) seq.getObjectAt(4);
            DERInteger x = (DERInteger) seq.getObjectAt(5);
            
            privSpec = new DSAPrivateKeySpec(x.getValue(), p.getValue(),
                    q.getValue(), g.getValue());
            pubSpec = new DSAPublicKeySpec(y.getValue(), p.getValue(),
                    q.getValue(), g.getValue());
        }
        
        KeyFactory fact = KeyFactory.getInstance(type, provider);
        
        return new KeyPair(fact.generatePublic(pubSpec),
                fact.generatePrivate(privSpec));
    }
    
    @Override
    public Object readObject() throws IOException
    {
        
        String line;
        
        while ((line = readLine()) != null)
        {
            if (line.indexOf("-----BEGIN PUBLIC KEY") != -1)
            {
                return readPublicKey("-----END PUBLIC KEY");
            }
            if (line != null
                    && line.indexOf("---- BEGIN SSH2 PUBLIC KEY") != -1)
            {
                PublicKey readRSAPublicKey = readRSAPublicKey("---- END SSH2 PUBLIC KEY");
                return new Ssh2PublicKeyImpl((RSAPublicKey) readRSAPublicKey,
                        SSH2_KEY_TYPE.IETFSECSH);
            }
            if (line != null && line.indexOf("ssh-rsa") != -1)
            {
                line = line.substring("ssh-rsa".length(), line.length()).trim();
                PublicKey readOpensshPublicKey = readOpensshPublicKey(line);
                return new Ssh2PublicKeyImpl(
                        (RSAPublicKey) readOpensshPublicKey,
                        SSH2_KEY_TYPE.OPENSSH);
            }
            if (line.indexOf("-----BEGIN RSA PUBLIC KEY") != -1)
            {
                return readRSAPublicKey("-----END RSA PUBLIC KEY");
            }
            if (line.indexOf("-----BEGIN CERTIFICATE REQUEST") != -1)
            {
                return readCertificateRequest("-----END CERTIFICATE REQUEST");
            }
            if (line.indexOf("-----BEGIN NEW CERTIFICATE REQUEST") != -1)
            {
                return readCertificateRequest("-----END NEW CERTIFICATE REQUEST");
            }
            if (line.indexOf("-----BEGIN CERTIFICATE") != -1)
            {
                return readCertificate("-----END CERTIFICATE");
            }
            if (line.indexOf("-----BEGIN PKCS7") != -1)
            {
                return readPKCS7("-----END PKCS7");
            }
            if (line.indexOf("-----BEGIN X509 CERTIFICATE") != -1)
            {
                return readCertificate("-----END X509 CERTIFICATE");
            }
            if (line.indexOf("-----BEGIN X509 CRL") != -1)
            {
                return readCRL("-----END X509 CRL");
            }
            if (line.indexOf("-----BEGIN ATTRIBUTE CERTIFICATE") != -1)
            {
                return readAttributeCertificate("-----END ATTRIBUTE CERTIFICATE");
            }
            if (line.indexOf("-----BEGIN RSA PRIVATE KEY") != -1)
            {
                try
                {
                    return readKeyPair("RSA", "-----END RSA PRIVATE KEY");
                }
                catch (IOException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new PEMException("problem creating RSA private key: "
                            + e.toString(), e);
                }
            }
            if (line.indexOf("-----BEGIN DSA PRIVATE KEY") != -1)
            {
                try
                {
                    return readKeyPair("DSA", "-----END DSA PRIVATE KEY");
                }
                catch (IOException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new PEMException("problem creating DSA private key: "
                            + e.toString(), e);
                }
            }
            if (line.indexOf("-----BEGIN EC PARAMETERS-----") != -1)
            {
                return readECParameters("-----END EC PARAMETERS-----");
            }
            if (line.indexOf("-----BEGIN EC PRIVATE KEY-----") != -1)
            {
                try
                {
                    return readKeyPair("ECDSA", "-----END EC PRIVATE KEY-----");
                }
                catch (IOException e)
                {
                    throw e;
                }
                catch (Exception e)
                {
                    throw new PEMException(
                            "problem creating ECDSA private key: "
                                    + e.toString(), e);
                }
            }
        }
        
        return null;
    }
    
    private ECNamedCurveParameterSpec readECParameters(String endMarker)
            throws IOException
    {
        DERObjectIdentifier oid = (DERObjectIdentifier) ASN1Object.fromByteArray(readBytes(endMarker));
        
        return ECNamedCurveTable.getParameterSpec(oid.getId());
    }
    
    private byte[] readBytes(String endMarker) throws IOException
    {
        String line;
        StringBuffer buf = new StringBuffer();
        
        while ((line = readLine()) != null)
        {
            if (line.indexOf(endMarker) != -1)
            {
                break;
            }
            buf.append(line.trim());
        }
        
        if (line == null)
        {
            throw new IOException(endMarker + " not found");
        }
        
        return Base64.decode(buf.toString());
    }
    
    private PublicKey readRSAPublicKey(String endMarker) throws IOException
    {
        ASN1InputStream ais = new ASN1InputStream(readBytes(endMarker));
        Object asnObject = ais.readObject();
        ASN1Sequence sequence = (ASN1Sequence) asnObject;
        RSAPublicKeyStructure rsaPubStructure = new RSAPublicKeyStructure(
                sequence);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                rsaPubStructure.getModulus(),
                rsaPubStructure.getPublicExponent());
        
        try
        {
            KeyFactory keyFact = KeyFactory.getInstance("RSA", provider);
            
            return keyFact.generatePublic(keySpec);
        }
        catch (NoSuchProviderException e)
        {
            throw new IOException("can't find provider " + provider);
        }
        catch (Exception e)
        {
            throw new PEMException("problem extracting key: " + e.toString(), e);
        }
    }
    
    private PublicKey readOpensshPublicKey(String line) throws IOException
    {
        ASN1InputStream ais = new ASN1InputStream(Base64.decode(line));
        Object asnObject = ais.readObject();
        ASN1Sequence sequence = (ASN1Sequence) asnObject;
        RSAPublicKeyStructure rsaPubStructure = new RSAPublicKeyStructure(
                sequence);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(
                rsaPubStructure.getModulus(),
                rsaPubStructure.getPublicExponent());
        
        try
        {
            KeyFactory keyFact = KeyFactory.getInstance("RSA", provider);
            
            return keyFact.generatePublic(keySpec);
        }
        catch (NoSuchProviderException e)
        {
            throw new IOException("can't find provider " + provider);
        }
        catch (Exception e)
        {
            throw new PEMException("problem extracting key: " + e.toString(), e);
        }
    }
    
}
