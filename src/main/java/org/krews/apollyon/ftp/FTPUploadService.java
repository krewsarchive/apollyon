package org.krews.apollyon.ftp;

import com.eu.habbo.Emulator;
import org.krews.apollyon.utils.PngSignatureChecker;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;

public class FTPUploadService {
    private static final String ftpUrl = "ftp://%s:%s@%s/%s;type=i";

    public static void uploadImage(byte[] image, String uploadPath) throws IOException {
        if (PngSignatureChecker.isPngFile(image)) {
            String host = Emulator.getConfig().getValue("ftp.host");
            String user = Emulator.getConfig().getValue("ftp.user");
            String pass = Emulator.getConfig().getValue("ftp.password");

            String uploadURL = String.format(ftpUrl, URLEncoder.encode(user, "UTF-8"), URLEncoder.encode(pass, "UTF-8"), host, uploadPath);

            URL url = new URL(uploadURL);
            URLConnection conn = url.openConnection();
            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(image, 0, image.length);
            outputStream.close();
        }
    }
}
