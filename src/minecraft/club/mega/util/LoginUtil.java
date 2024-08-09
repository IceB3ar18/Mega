package club.mega.util;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public class LoginUtil {
    public static void loginMicrosoft(String email, String pass) {
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        MicrosoftAuthResult result = null;
        try {
            result = authenticator.loginWithCredentials(email,pass);
            System.out.printf("Logged in with '%s'%n", result.getProfile().getName());
            Minecraft.getMinecraft().session = new Session(result.getProfile().getName(),result.getProfile().getId(), result.getAccessToken(),"legacy");


        } catch (MicrosoftAuthenticationException e) {
            e.printStackTrace();
        }
    }
}
