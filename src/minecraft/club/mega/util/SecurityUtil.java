package club.mega.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.sql.*;

public final class SecurityUtil {

    public static String getHWID() {
        try{
            String toEncrypt = getSerialNumber() + System.getenv("COMPUTERNAME") + System.getProperty("user.name") + System.getenv("PROCESSOR_IDENTIFIER") + System.getenv("PROCESSOR_LEVEL");
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(toEncrypt.getBytes());
            StringBuffer hexString = new StringBuffer();

            byte byteData[] = md.digest();

            for (byte aByteData : byteData) {
                String hex = Integer.toHexString(0xff & aByteData);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private static String getSerialNumber() {
        try {
            ProcessBuilder pb = new ProcessBuilder("wmic", "baseboard", "get", "serialnumber");
            Process process = pb.start();
            process.waitFor();
            String serialNumber = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                for (String line = br.readLine(); line != null; line = br.readLine())
                {
                    if (line.length() < 1 || line.startsWith("SerialNumber")) continue;

                    serialNumber = line;
                    break;
                }
            }
            return serialNumber;
        } catch (Exception exception) {
            return null;
        }
    }

}
