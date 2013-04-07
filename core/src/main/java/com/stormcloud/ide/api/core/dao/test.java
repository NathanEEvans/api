package com.stormcloud.ide.api.core.dao;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author martijn
 */
public class test {

    public static void main(String[] args) {


        String original = "costea";

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");

            md.update(original.getBytes());

            byte[] digest = md.digest();

            StringBuilder sb = new StringBuilder("");

            for (byte b : digest) {
                sb.append(Integer.toHexString(b & 0xff));
            }

            System.out.println(sb.toString());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
