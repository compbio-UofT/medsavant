/*
 *    Copyright 2011 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.ut.biolab.medsavant.mail;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Wrappers to make using javax.crypto.Cipher a little less unfriendly.
 *
 * @author tarkvara
 */
@SuppressWarnings("NonFinalStaticVariableUsedInClassInitialization")
public class CryptoUtils {
    private static final byte[] salt = {
        (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
    };

    private static Cipher ENCRYPTOR;
    private static Cipher DECRYPTOR;

    static {
        try {
            KeySpec keySpec = new PBEKeySpec("brucrew".toCharArray(), salt, 19);
            SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            ENCRYPTOR = Cipher.getInstance(key.getAlgorithm());
            DECRYPTOR = Cipher.getInstance(key.getAlgorithm());
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, 19);
            ENCRYPTOR.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            DECRYPTOR.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (Exception x) {
        }
    }
    
    /**
     * Decrypt a BASE64 encoded encrypted string.
     * 
     * @param str BASE64 representation of encrypted string
     * @return plain-text
     */
    public static String decrypt(String str) {
        try {
            return new String(DECRYPTOR.doFinal(new BASE64Decoder().decodeBuffer(str)));
        } catch (Exception x) {
        }
        return null;
    }

    
    /**
     * Encrypt the string and return a BASE64 representation suitable for framing or wrapping fish.
     *
     * @param str the plaintext string to be encrypted
     * @return BASE64 encoding of encrypted string
     */
    public static String encrypt(String str) {
        try {
            // Encode bytes to base64 to get a string
            return new BASE64Encoder().encode(ENCRYPTOR.doFinal(str.getBytes("UTF-8")));
        } catch (Exception x) {
        }
        return null;
    }

    public static void main(String[] argv) {
    }
}
