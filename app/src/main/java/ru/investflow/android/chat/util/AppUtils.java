package ru.investflow.android.chat.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.investflow.android.chat.App;
import ru.investflow.android.chat.AppSettings;
import ru.investflow.android.chat.MainActivity;


public class AppUtils {

    private static final Logger log = AppUtils.getLogger(AppUtils.class);

    public static String md5(final @NotNull String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) {
                    h = "0" + h;
                }
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            log.error("", e);
        }
        return "";
    }

    @NotNull
    public static Logger getLogger(Class cls) {
        return LoggerFactory.getLogger("chat_" + cls.getSimpleName());
    }


    public static String textToHtmlConvertingURLsToLinks(String text) {
        if (TextUtils.isEmpty(text)) {
            return text;
        }
        String escapedText = StringEscapeUtils.escapeHtml4(text);
        return escapedText.replaceAll("(\\A|\\s)((http|https|ftp|mailto):\\S+)(\\s|\\z)", "$1<a href=\"$2\">$2</a>$4");
    }

    public static boolean isForeground() {
        MainActivity a = MainActivity.getInstance();
        return a != null && a.getState() == MainActivity.State.Resumed;
    }

    public static void addNotificationSound(NotificationCompat.Builder builder) {
        if (!AppSettings.useSoundInNotifications()) {
            return;
        }
        AudioManager am = (AudioManager) App.get().getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            switch (am.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    log.debug("Silent mode, ignoring notification");
                    return;
                case AudioManager.RINGER_MODE_VIBRATE:
                    log.debug("Vibrate mode, bzz...");
                    builder.setVibrate(new long[]{500, 500, 500, 500, 500});
                    return;
                case AudioManager.RINGER_MODE_NORMAL:
                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    break;
            }
        }
    }
}
