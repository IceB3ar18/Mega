package club.mega.util;

import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*;
import javax.sound.sampled.FloatControl.Type;

public class SoundUtil {

    public static void playSound(String relativePath) {
        // Basispfad zum Soundordner
        String basePath = "ressources/sounds/";

        // Vervollständige den relativen Pfad mit dem Basispfad
        String fullPath = basePath + relativePath;

        // Erstelle eine Datei aus dem vollständigen Pfad
        File soundFile = new File(fullPath);

        try {
            // Öffne die Sounddatei
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);

            // Holen Sie sich einen Clip
            Clip clip = AudioSystem.getClip();

            // Öffnen Sie den Clip und laden Sie den Sound
            clip.open(audioIn);

            // Spiele den Sound ab
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
