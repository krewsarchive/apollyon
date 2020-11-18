package org.krews.apollyon.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class PngSignatureChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(PngSignatureChecker.class);
    private static byte[] signature = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10 };

    public static boolean isPngFile(byte[] file) {
        if (Arrays.equals(Arrays.copyOfRange(file, 0, 8), signature)) {
            return true;
        } else {
            LOGGER.warn("[Apollyon] Someone tried to exploit the camera by uploading a malicious file!");
            return false;
        }
    }
}
