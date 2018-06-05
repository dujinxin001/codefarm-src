package com.codefarm.ca;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPublicKey;

import com.codefarm.ca.exception.KeyPairException;
import com.codefarm.ca.exception.StorageException;
import com.codefarm.ca.key.SSH2_KEY_TYPE;
import com.codefarm.ca.key.Ssh2PublicKeyImpl;
import com.codefarm.ca.store.PEMFileStore;

public class SSHKeyManager
{
    static
    {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }
    
    public byte[] encodePublicKey(RSAPublicKey key) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        /* encode the "ssh-rsa" string */
        byte[] sshrsa = new byte[] { 0, 0, 0, 7, 's', 's', 'h', '-', 'r', 's',
                'a' };
        out.write(sshrsa);
        /* Encode the public exponent */
        BigInteger e = key.getPublicExponent();
        byte[] data = e.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        /* Encode the modulus */
        BigInteger m = key.getModulus();
        data = m.toByteArray();
        encodeUInt32(data.length, out);
        out.write(data);
        return out.toByteArray();
    }
    
    public void encodeUInt32(int value, OutputStream out) throws IOException
    {
        byte[] tmp = new byte[4];
        tmp[0] = (byte) ((value >>> 24) & 0xff);
        tmp[1] = (byte) ((value >>> 16) & 0xff);
        tmp[2] = (byte) ((value >>> 8) & 0xff);
        tmp[3] = (byte) (value & 0xff);
        out.write(tmp);
    }
    
    public static void main(String... args) throws StorageException,
            IOException, KeyPairException
    {
        KeyPair keypair = KeyPairManager.generateRSAKeyPair();
        PEMFileStore<KeyPair> id_rsa = new PEMFileStore<KeyPair>(
                "D:\\certs\\ssh\\id_rsa");
        id_rsa.save(keypair, null);
        PublicKey publicKey = keypair.getPublic();
        Ssh2PublicKeyImpl opensshPK = new Ssh2PublicKeyImpl(
                (RSAPublicKey) publicKey, SSH2_KEY_TYPE.OPENSSH,
                "tomcat@localhost.localdomain");
        PEMFileStore<Ssh2PublicKeyImpl> opensshstore = new PEMFileStore<Ssh2PublicKeyImpl>(
                "d:\\certs\\ssh\\id_rsa.pub");
        opensshstore.save(opensshPK, null);
    }
}
