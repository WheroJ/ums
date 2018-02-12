package com.zetavision.panda.ums.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by shopping on 2018/1/17 9:07.
 * https://github.com/wheroj
 */

public class AESTool {

    private static final byte[] pass_key = new byte[]{17, 68, -29, -41, 3, -116, 83, 12, -28, 105, -47, -63, -16, -35, -23, 3};

    /**
     * <AES加密>
     * <功能详细描述>
     *
     * @param content 需要加密的内容
     * @return String 加密结果
     */
    private static String encrypt1(String content) {
        KeyGenerator kgen;
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

            secureRandom.setSeed(new String().getBytes());

            kgen = KeyGenerator.getInstance("AES");

            kgen.init(128, secureRandom);

            SecretKey secretKey = kgen.generateKey();

            byte[] enCodeFormat = secretKey.getEncoded();

            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");

            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            byte[] byteContent = content.getBytes("utf-8");

            // 初始化
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] result = cipher.doFinal(byteContent);

            // 加密
            return parseByte2HexStr(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <AES加密>
     * <功能详细描述>
     *
     * @param content 需要加密的内容
     * @return String 加密结果
     */
    private static String encrypt(String content) {
        try {
            SecretKeySpec key = new SecretKeySpec(pass_key, "AES");

            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            byte[] byteContent = content.getBytes("utf-8");

            // 初始化
            cipher.init(Cipher.ENCRYPT_MODE, key);//ivParameterSpec

            byte[] result = cipher.doFinal(byteContent);

            // 加密
            return parseByte2HexStr(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <AES解密>
     * <功能详细描述>
     *
     * @param content 待解密内容
     * @return String 解密结果
     */
    private static String decrypt1(String content) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");

            secureRandom.setSeed(new String().getBytes());

            byte[] decryptFrom = parseHexStr2Byte(content);

            KeyGenerator kgen = KeyGenerator.getInstance("AES");

            kgen.init(128, secureRandom);

            SecretKey secretKey = kgen.generateKey();

            byte[] enCodeFormat = secretKey.getEncoded();

            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");

            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] result = cipher.doFinal(decryptFrom);

            // 解密
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <AES解密>
     * <功能详细描述>
     *
     * @param content  待解密内容
     * @return String 解密结果
     */
    private static String decrypt(String content) {
        try {
            byte[] decryptFrom = parseHexStr2Byte(content);
            SecretKeySpec key = new SecretKeySpec(pass_key, "AES");

            // 创建密码器
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            // 初始化
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(decryptFrom);

            // 解密
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } /*catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } *//*catch (NoSuchProviderException e) {
            e.printStackTrace();
        }*/
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            try {
                String hex = Integer.toHexString(buf[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            try {
                int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1),
                        16);
                int low = Integer.parseInt(hexStr.substring(i * 2 + 1,
                        i * 2 + 2), 16);
                result[i] = (byte) (high * 16 + low);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    public static void main(String[] args) {
        String pw = "app123456";
        //密钥
        System.out.println("===========加密================");
        System.out.println("密码明文：" + pw);
        String encryptResultStr = AESTool.combineEncrypt(pw);
        System.out.println("ASE加密后：" + encryptResultStr);
        System.out.println("===========加密================");

        System.out.println("===========解密================");
        String decryptResult = AESTool.combineDecrypt(encryptResultStr);
        System.out.println("aes解密后：" + decryptResult);
        System.out.println("===========解密================");
    }

    /**
     * 加密
     * @param content
     * @return
     */
    public static String combineEncrypt(String content) {
        String encryptResultStr = AESTool.encrypt(content);
        return Base64Util.encode(AESTool.parseHexStr2Byte(encryptResultStr));
//        return Base64Util.encrypt(encryptResultStr);
    }


    /**
     * 解密
     * @param content
     * @return
     */
    public static String combineDecrypt(String content) {
        String str = AESTool.parseByte2HexStr(Base64Util.decode(content));
        return decrypt(str);
//        return decrypt(Base64Util.decrypt(content));
    }
}
