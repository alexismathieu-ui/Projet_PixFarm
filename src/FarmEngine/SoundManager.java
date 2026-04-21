package FarmEngine;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public final class SoundManager {
    private static final Map<String, AudioClip> CLIPS = new HashMap<>();
    private static MediaPlayer musicPlayer;
    private static boolean mediaUnavailableLogged;

    private SoundManager() {}

    private static URL resolveAudioUrl(String resourcePath) {
        URL direct = SoundManager.class.getResource(resourcePath);
        if (direct != null) return direct;

        // Compatibility fallback if resources are stored in subfolders (music/sfx).
        if (resourcePath.startsWith("/Audio/")) {
            String filename = resourcePath.substring("/Audio/".length());
            String altMusic = "/Audio/music/" + filename;
            URL music = SoundManager.class.getResource(altMusic);
            if (music != null) return music;

            String altSfx = "/Audio/sfx/" + filename;
            URL sfx = SoundManager.class.getResource(altSfx);
            if (sfx != null) return sfx;
        }
        return null;
    }

    public static void playMusic(String resourcePath) {
        try {
            URL url = resolveAudioUrl(resourcePath);
            if (url == null) return;
            if (musicPlayer != null) {
                musicPlayer.stop();
            }
            Media media = new Media(url.toExternalForm());
            musicPlayer = new MediaPlayer(media);
            musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            musicPlayer.setVolume(GameSettings.getVolume());
            musicPlayer.play();
        } catch (Throwable t) {
            if (!mediaUnavailableLogged) {
                System.out.println("Audio media indisponible sur ce runtime: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                mediaUnavailableLogged = true;
            }
        }
    }

    public static void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
        }
    }

    public static void updateVolume() {
        if (musicPlayer != null) {
            musicPlayer.setVolume(GameSettings.getVolume());
        }
    }

    public static void playSfx(String resourcePath) {
        try {
            AudioClip clip = CLIPS.computeIfAbsent(resourcePath, key -> {
                URL url = resolveAudioUrl(key);
                return url != null ? new AudioClip(url.toExternalForm()) : null;
            });
            if (clip == null) return;
            clip.play(GameSettings.getVolume());
        } catch (Throwable t) {
            if (!mediaUnavailableLogged) {
                System.out.println("Audio media indisponible sur ce runtime: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                mediaUnavailableLogged = true;
            }
        }
    }
}
