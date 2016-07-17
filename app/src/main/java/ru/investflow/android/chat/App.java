package ru.investflow.android.chat;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import ru.investflow.android.chat.util.AppUtils;

public class App extends android.app.Application {

    private static final Logger log = AppUtils.getLogger(App.class);

    private static App instance;

    @Override
    public void onCreate() {
        log.info("App created");

        instance = this;
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        log.info("Created Firebase instance");

        FirebaseMessaging.getInstance().subscribeToTopic(AppConstants.FCM_CHAT_TOPIC);
        log.info("Subscribed to messaging topic: " + AppConstants.FCM_CHAT_TOPIC);
    }

    public static App get() {
        return instance;
    }

    @NotNull
    public SharedPreferences getSettingsPreferences() {
        return getSharedPreferences(AppConstants.SETTINGS_PREFERENCES, Context.MODE_PRIVATE);
    }
}
