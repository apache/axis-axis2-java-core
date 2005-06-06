/*
 * Created on Apr 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.util;

import java.util.Random;

/**
 * Code borrowed from AuthenticatorBase.java for generating a secure id's.
 */
public class SessionUtils {

    /**
     * The number of random bytes to include when generating a
     * session identifier.
     */
    protected static final int SESSION_ID_BYTES = 16;

    /**
     * A random number generator to use when generating session identifiers.
     */
    protected static Random random = null;

    /**
     * The Java class name of the random number generator class to be used
     * when generating session identifiers.
     */
    protected static String randomClass = "java.security.SecureRandom";

    /**
     * Host name/ip.
     */
    private static String thisHost = null;

    /**
     * Generate and return a new session identifier.
     *
     * @return a new session id
     */
    public static synchronized String generateSessionId() {
        // Generate a byte array containing a session identifier
        byte bytes[] = new byte[SESSION_ID_BYTES];

        getRandom().nextBytes(bytes);

        // Render the result as a String of hexadecimal digits
        StringBuffer result = new StringBuffer();

        for (int i = 0; i < bytes.length; i++) {
            byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
            byte b2 = (byte) (bytes[i] & 0x0f);

            if (b1 < 10) {
                result.append((char) ('0' + b1));
            } else {
                result.append((char) ('A' + (b1 - 10)));
            }
            if (b2 < 10) {
                result.append((char) ('0' + b2));
            } else {
                result.append((char) ('A' + (b2 - 10)));
            }
        }
        return (result.toString());
    }

    /**
     * Generate and return a new session identifier.
     *
     * @return a new session.
     */
    public static synchronized Long generateSession() {
        return new Long(getRandom().nextLong());
    }

    /**
     * Return the random number generator instance we should use for
     * generating session identifiers.  If there is no such generator
     * currently defined, construct and seed a new one.
     *
     * @return Random object
     */
    private static synchronized Random getRandom() {
        if (random == null) {
            try {
                Class clazz = Class.forName(randomClass);
                random = (Random) clazz.newInstance();
            } catch (Exception e) {
                random = new java.util.Random();
            }
        }
        return (random);
    }

}

