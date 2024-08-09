package club.mega.gui.altmanager;

import club.mega.Mega;
import club.mega.gui.altmanager.alt.Alt;
import club.mega.gui.altmanager.alt.AltType;
import club.mega.gui.altmanager.alt.LoginThread;
import club.mega.gui.clientManager.ClientBackground;
import club.mega.gui.clientManager.ClientColor;
import club.mega.module.impl.player.MCF;
import club.mega.util.*;
import club.mega.util.animation.AnimationUtil;
import club.mega.util.glsl.BGShaderUtil;
import club.mega.util.glsl.GLSLSandboxShader;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.thealtening.auth.TheAlteningAuthentication;
import com.thealtening.auth.service.AlteningServiceType;
import de.florianmichael.viamcp.gui.GuiProtocolSelector;
import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.awt.*;
import java.io.*;
import java.net.Proxy;
import java.util.ArrayList;

public class GuiAltManager extends GuiScreen {

    public final ArrayList<Alt> alts = new ArrayList<>();
    public Alt selectedAlt;
    public Alt loggedAccount;
    private LoginThread loginThread;

    private int selectedAltIndex;
    private GuiScreen parent;
    private final ClientBackground clientBackground;
    private final ClientColor clientColor;
    private final GuiScreen prevScreen;
    private final GuiAddAlt guiAddAlt = new GuiAddAlt(this);

    private GuiTextField usernameField;
    private PasswordField passwordField;

    private double current = 0, scrollOffset = 0;

    private final TheAlteningAuthentication serviceSwitcher = TheAlteningAuthentication.mojang();


    public GuiAltManager(final GuiScreen prevScreen) {
        this.parent = parent;
        this.clientBackground = new ClientBackground(new GuiMainMenu());
        this.clientColor = new ClientColor(new GuiMainMenu());
        this.prevScreen = prevScreen;
        selectedAlt = null;
        loggedAccount = null;
        BGShaderUtil.getInstance().setup();
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {

        handleScrolling(mouseX, mouseY, width / 2D - 150,height / 8D - 20,300,400);

        BGShaderUtil.getInstance().render(width, height, mouseX, mouseY);
        GL11.glPushMatrix();
        Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawCenteredString("AltManager", width / 2D + 3, height / 10D - 23, -1);
        ScaledResolution sr = new ScaledResolution(mc);
        int scaledHeight = sr.getScaledHeight();
        RenderUtil.drawRect(0, 0, 130, scaledHeight, new Color(0, 0, 0,130));

        // Rendering all the Alts
        int offset = (int) (height / 8D - 15);
        GuiBlurUtil.drawBlurredRect((float) (width / 2D - 150), (float) (height / 8D - 20),300,400,10, new Color(1, 1, 1, 140));
        int y = height / 3 - 20;
        GuiBlurUtil.drawBlurredRect((float) (width / 4D - 15), y - 35 - 5,90,142,10, new Color(1, 1, 1, 140));
        GL11.glPushMatrix();
        RenderUtil.prepareScissorBox(width / 2D - 150, height / 8D - 20  ,300,400);;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        for (Alt alt : alts)
        {
            RenderUtil.drawRoundedRect(width / 2D - 145, offset - scrollOffset,290,30, 5, selectedAlt == alt ? ColorUtil.getMainColor(150) : new Color(ColorUtil.getMainColor().getRed(), ColorUtil.getMainColor().getGreen(), ColorUtil.getMainColor().getBlue(), 255));
            RenderUtil.drawFullCircle(width / 2D + 135, offset + 15 - scrollOffset, 4, alt.getPassword().isEmpty() ? Color.RED.getRGB() : Color.GREEN.getRGB());
            Mega.INSTANCE.getFontManager().getFont("Roboto bold 20").drawString(alt.getName(),width / 2D - 135,offset + 5 - scrollOffset, new Color(255,255,255, 255).getRGB());
            switch (alt.getAltType()) {
                case THEALTENING:
                    Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").drawString("Altening",width / 2D - 135,offset + 17 - scrollOffset, new Color(255, 255, 255, 182).getRGB());

                    break;
                case MICROSOFT:
                    Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").drawString("Microsoft",width / 2D - 135,offset + 17 - scrollOffset, new Color(255, 255, 255, 182).getRGB());

                    break;

                case MOJANG:
                    Mega.INSTANCE.getFontManager().getFont("Roboto bold 18").drawString("Cracked",width / 2D - 135,offset + 17 - scrollOffset, new Color(255, 255, 255, 182).getRGB());

                    break;
            }
            offset += 32;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
        usernameField.drawTextBox("email");
        passwordField.drawTextBox("password");
        /*if (!usernameField.isFocused() && usernameField.getText().isEmpty())
            Mega.INSTANCE.getFontManager().getFont("Roboto medium 20").drawString("email", width / 4D - 5, height / 4D - 9, new Color(255,255,255,140));
        if (!passwordField.isFocused && passwordField.getText().isEmpty())
            Mega.INSTANCE.getFontManager().getFont("Roboto medium 20").drawString("password", width / 4D - 5, height / 4D + 16, new Color(255,255,255,140));*/
        super.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glPopMatrix();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);
        usernameField.textboxKeyTyped(typedChar, keyCode);
        passwordField.textboxKeyTyped(typedChar, keyCode);
        if (typedChar == '\t') {
            if (usernameField.isFocused())
                usernameField.setFocused(!usernameField.isFocused());
            if (passwordField.isFocused())
                passwordField.setFocused(!passwordField.isFocused());
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        passwordField.mouseClicked(mouseX, mouseY, mouseButton);
        usernameField.mouseClicked(mouseX, mouseY, mouseButton);
        int offset = (int) (height / 8D - 15);
        int count = 0;
        for (Alt alt : alts)
        {
            if (MouseUtil.isInside(mouseX, mouseY,width / 2D - 145, offset - scrollOffset, 290, 30) && mouseY >= (height / 8D - 20) && mouseY <= (height / 8D - 20 + 400) && (mouseButton == 0 || mouseButton == 1)) {
                selectedAlt = alt;
                selectedAltIndex = count;
                try {
                    switch (alt.getAltType())
                    {
                        case MOJANG:
                            serviceSwitcher.updateService(AlteningServiceType.MOJANG);
                        case MICROSOFT:
                            serviceSwitcher.updateService(AlteningServiceType.MOJANG);
                            LoginUtil.loginMicrosoft(selectedAlt.getEmail(), selectedAlt.getPassword());
                            break;
                        case THEALTENING:
                            serviceSwitcher.updateService(AlteningServiceType.THEALTENING);
                            break;
                    }
                    loginThread = new LoginThread(selectedAlt.getEmail(), selectedAlt.getPassword());
                    loginThread.start();
                    loggedAccount = selectedAlt;
                } catch (Exception ignored) {
                }
            }
            offset += 32;
            count++;
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
        switch (button.id)
        {
            case 1:
                if (usernameField.getText().isEmpty()) {
                    if (!getClipboardString().isEmpty() && (getClipboardString().contains(":") || getClipboardString().contains("@alt.com"))) {
                        if (getClipboardString().split(":")[0].toLowerCase().endsWith("@alt.com")) {
                            //Clipboard altening
                            serviceSwitcher.updateService(AlteningServiceType.THEALTENING);
                            Session auth = this.createSession(getClipboardString().split(":")[0], "Abc12345");
                            if (auth != null) {
                                alts.add(new Alt(auth.getUsername(), getClipboardString().split(":")[0],"Abc12345", AltType.THEALTENING));
                                loginThread = new LoginThread(getClipboardString().split(":")[0], "Abc12345");
                                loginThread.start();
                            }
                        } else {
                            //Clipboard Microsoft
                            MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                            MicrosoftAuthResult result = null;
                            try {
                                result = authenticator.loginWithCredentials(getClipboardString().split(":")[0], getClipboardString().split(":")[1]);
                                Minecraft.getMinecraft().session = new Session(result.getProfile().getName(),result.getProfile().getId(), result.getAccessToken(),"legacy");
                                alts.add(new Alt(result.getProfile().getName(), getClipboardString().split(":")[0], getClipboardString().split(":")[1], AltType.MICROSOFT));

                            } catch (MicrosoftAuthenticationException e) {
                                e.printStackTrace();
                            }

                        }
                    } else {
                        //Autogen
                        serviceSwitcher.updateService(AlteningServiceType.MOJANG);
                        String random = getUsername((int) RandomUtil.getRandomNumber(6, 9), (int) RandomUtil.getRandomNumber(3, 6));
                        alts.add(new Alt(random, random, passwordField.getText(), AltType.MOJANG));
                        loginThread = new LoginThread(random, passwordField.getText());
                        loginThread.start();
                    }
                } else {
                    ///////////From textboxes
                    if (usernameField.getText().toLowerCase().endsWith("@alt.com")) {
                        serviceSwitcher.updateService(AlteningServiceType.THEALTENING);
                        Session auth = this.createSession(usernameField.getText(), "Abc12345");
                        if (auth != null) {
                            alts.add(new Alt(auth.getUsername(),usernameField.getText(), passwordField.getText(), AltType.THEALTENING));
                            loginThread = new LoginThread(usernameField.getText(), passwordField.getText());
                            loginThread.start();
                        }
                    } else if (!passwordField.getText().isEmpty()){
                        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                        MicrosoftAuthResult result = null;
                        try {
                            result = authenticator.loginWithCredentials(usernameField.getText(), passwordField.getText());
                            System.out.printf("Logged in with '%s'%n", result.getProfile().getName());
                            Minecraft.getMinecraft().session = new Session(result.getProfile().getName(),result.getProfile().getId(), result.getAccessToken(),"legacy");
                            alts.add(new Alt(result.getProfile().getName(), usernameField.getText(), passwordField.getText(), AltType.MICROSOFT));

                        } catch (MicrosoftAuthenticationException e) {
                            e.printStackTrace();
                        }

                    } else {
                        serviceSwitcher.updateService(AlteningServiceType.MOJANG);
                        //     Session auth = this.createSession(usernameField.getText(), passwordField.getText());
                        //   if (auth != null) {
                        alts.add(new Alt(usernameField.getText(), usernameField.getText(), passwordField.getText(), AltType.MOJANG));
                        loginThread = new LoginThread(usernameField.getText(), passwordField.getText());
                        loginThread.start();
                    }
                }
                usernameField.setText("");
                passwordField.setText("");
                saveAlts();
                break;
            case 2:
                if (selectedAlt != null) {
                    selectedAlt = null;
                    alts.remove(selectedAltIndex);
                    saveAlts();
                }
                break;
            case 4:
                if (mc.session.getUsername() != null)
                    setClipboardString(mc.session.getUsername());
                else
                    setClipboardString("Failed copying your name");
                break;
            case 5:
                alts.clear();
                saveAlts();
                break;

        }
        if (button.id == 6) {
            this.mc.displayGuiScreen(this.clientBackground.start(this));
        }

        if (button.id == 7) {
            this.mc.displayGuiScreen(this.parent);
        }

        if (button.id == 8) {
            this.mc.displayGuiScreen(this.clientColor.start(this));
        }

        if (button.id == 9) {
            this.mc.displayGuiScreen(Mega.INSTANCE.getAltManager());
        }
        if (button.id == 69)
        {
            this.mc.displayGuiScreen(new GuiProtocolSelector(this));
        }
    }


    private String getUsername(int letters, int numbers) {
        String vowels = "AaEeIiOoUu";
        String consonants = "BbCcDdFfGgHhJjKkLlMmNnPpQqRrSsTtVvWwXxYyZz";
        StringBuilder username = new StringBuilder();
        for(int i = 0; i < letters; i++) {
            if(i % 2 == 0) {
                username.append(vowels.charAt((int) RandomUtil.getRandomNumber(0, vowels.length() - 1)));
            } else{
                username.append(consonants.charAt((int) RandomUtil.getRandomNumber(0, consonants.length() - 1)));
            }
        }
        for(int i = 0; i < numbers; i++) {
            if(i % 2 == 0) {
                username.append((int)RandomUtil.getRandomNumber(0,9));
            }
        }
        return username.toString();
    }

    @Override
    public void initGui() {
        super.initGui();



        if (alts.isEmpty())
            loadAlts();

        scrollOffset = 0;
        current = 0;
        Keyboard.enableRepeatEvents(true);
        int offset = 0, x = width / 4 - 10, y = height / 3 - 20;

        buttonList.clear();
        usernameField = new GuiTextField(3, fontRendererObj, x, y - 35, 80, 20);
        passwordField = new PasswordField(fontRendererObj, x, y - 10, 80, 20);
        buttonList.add(new RoundedButton(1, x, y + (offset += 25), 80, 20, new Color(10, 240, 20, 180), new Color(10, 240, 20, 130), 10, "Add"));
        buttonList.add(new RoundedButton(2, x, y + (offset += 25), 80, 20, new Color(201, 8, 8, 180), new Color(201, 8, 8, 130), 10, "Remove"));
        buttonList.add(new RoundedButton(5, x, y + (offset += 25), 80, 20, new Color(201, 8, 8, 180), new Color(201, 8, 8, 130), 10, "Clear"));
        buttonList.add(new RoundedButton(4,width - 55,0,55,15,0,"Copy Name"));

        ScaledResolution sr = new ScaledResolution(this.mc);
        int scaledHeight = sr.getScaledHeight();
        int startHeight = Math.min(40 + scaledHeight / 7, 135);
        this.buttonList.add(new GuiButton(6, 20, startHeight, 100, 20, "Backgrounds"));
        this.buttonList.add(new GuiButton(7, 20, scaledHeight - scaledHeight / 10, 100, 20, "Back"));
        this.buttonList.add(new GuiButton(8, 20, startHeight + 30, 100, 20, "ColorOptions"));
        this.buttonList.add(new GuiButton(9, 20, startHeight + 60, 100, 20, "AltManager"));
        this.buttonList.add(new GuiButton(69, 20, startHeight + 30 * 3, 100, 20, "ViaVersion"));

    }

    private void handleScrolling(final int mouseX, final int mouseY, final double x, final double y, final double width, final double height) {
        if (Mouse.hasWheel() && MouseUtil.isInside(mouseX, mouseY, x, y, width, height)) {
            int wheel = Mouse.getDWheel();
            if (wheel < 0) {
                current += 30;
                if (this.current < 0) {
                    this.current = 0;
                }
            } else if (wheel > 0) {
                this.current -= 30;
                if (this.current < 0) {
                    this.current = 0;
                }
            }
            this.scrollOffset = AnimationUtil.animate(scrollOffset, current, 2.5);
        }
    }

    public final void saveAlts() {
        final File file = new File(mc.mcDataDir, "Mega/Accounts.mega");
        if (new File(mc.mcDataDir, "Mega/").mkdir() || !file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (final Alt alt : Mega.INSTANCE.getAltManager().alts) {
                bufferedWriter.write(alt.getEmail() + ":" + (alt.getPassword().isEmpty() ? "!!PW_CRACKED_EMPTY!!" : alt.getPassword()) + ":" + alt.getAltType().name() +  "\n");
                bufferedWriter.flush();
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public final void loadAlts() {
        try {
            File file = new File(mc.mcDataDir, "Mega/Accounts.mega");
            if (new File(mc.mcDataDir, "Mega/").mkdir() || !file.exists()) {
                file.createNewFile();
                return;
            }
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
                bufferedReader.lines().forEach(s -> {
                    try {
                        String password = s.split(":")[1];
                        switch (AltType.valueOf(s.split(":")[2])) {
                            case MOJANG:
                                if (password.contains("!!PW_CRACKED_EMPTY!!")) {
                                    Mega.INSTANCE.getAltManager().alts.add(new Alt(s.split(":")[0], s.split(":")[0],"",AltType.valueOf(s.split(":")[2])));
                                } else {
                                    Session auth = this.createSession(s.split(":")[0], password);
                                    LoginThread login = new LoginThread(s.split(":")[0], password);
                                    login.start();
                                    Mega.INSTANCE.getAltManager().alts.add(new Alt(auth.getUsername(), s.split(":")[0],password,AltType.valueOf(s.split(":")[2])));
                                }
                                break;
                            case MICROSOFT:
                                serviceSwitcher.updateService(AlteningServiceType.MOJANG);
                                MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
                                MicrosoftAuthResult result = null;
                                try {
                                    result = authenticator.loginWithCredentials(s.split(":")[0], password);
                                    Mega.INSTANCE.getAltManager().alts.add(new Alt(result.getProfile().getName(), s.split(":")[0],password,AltType.valueOf(s.split(":")[2])));


                                } catch (MicrosoftAuthenticationException e) {
                                    e.printStackTrace();
                                }

                                break;
                            case THEALTENING:
                                serviceSwitcher.updateService(AlteningServiceType.THEALTENING);
                                if (password.contains("!!PW_CRACKED_EMPTY!!")) {
                                    Mega.INSTANCE.getAltManager().alts.add(new Alt(s.split(":")[0], s.split(":")[0],"",AltType.valueOf(s.split(":")[2])));
                                } else {
                                    Session auth = this.createSession(s.split(":")[0], password);
                                    LoginThread login = new LoginThread(s.split(":")[0], password);
                                    login.start();
                                    Mega.INSTANCE.getAltManager().alts.add(new Alt(auth.getUsername(), s.split(":")[0],password,AltType.valueOf(s.split(":")[2])));
                                }
                                break;
                        }


                    } catch (Exception ignored) {

                    }
                });
            }
        } catch (IOException ignored) {
        }
    }

    private Session createSession(final String username, final String password) {
        YggdrasilAuthenticationService service = new YggdrasilAuthenticationService(Proxy.NO_PROXY, "");
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication)service.createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);
        try {
            auth.logIn();
            return new Session(auth.getSelectedProfile().getName(), auth.getSelectedProfile().getId().toString(), auth.getAuthenticatedToken(), "mojang");
        }
        catch (AuthenticationException localAuthenticationException) {
            localAuthenticationException.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
    }
}
