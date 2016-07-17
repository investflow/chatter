package ru.investflow.android.chat;

import android.content.SharedPreferences;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AppSettings {

    @NotNull
    private static SharedPreferences getPreferences() {
        return App.get().getSettingsPreferences();
    }

    @Nullable
    public static String getLogin() {
        return getPreferences().getString("login", null);
    }

    @Nullable
    public static String getPasswordHash() {
        return getPreferences().getString("password", null);
    }

    public static void setLoginAndPasswordHash(@Nullable String login, @Nullable String passwordHash) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString("login", login);
        editor.putString("password", passwordHash);
        editor.apply();
    }

    public static void resetLoginAndPassword() {
        setLoginAndPasswordHash(null, null);
    }

}
