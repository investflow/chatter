package ru.investflow.android.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ru.investflow.android.chat.util.AppUtils;
import ru.investflow.android.chat.util.ThreadUtils;

public class Messenger {

    public interface SendCallback {
        void onSuccess();

        void onError(String message);

        void onCanceled();
    }

    public static void sendMessage(@NotNull Context ctx, @NotNull String text, @NotNull SendCallback callback) {
        String login = AppSettings.getLogin();
        String passwordHash = AppSettings.getPasswordHash();
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(passwordHash)) {
            sendWithLoginDialog(ctx, text, callback);
        } else {
            sendMessage(text, login, passwordHash, callback);
        }

    }

    private static void sendMessage(final String text, final String login, final String passwordHash, final SendCallback callback) {
        ThreadUtils.runOnBackgroundThread(new Runnable() {
            @Override
            public void run() {
                _sendMessage(text, login, passwordHash, callback);
            }
        });
    }

    public static void sendWithLoginDialog(@NotNull Context ctx, final String text, final SendCallback callback) {
        @SuppressLint("InflateParams")
        final View prompt = LayoutInflater.from(ctx).inflate(R.layout.login_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);
        alertDialogBuilder.setView(prompt);
        alertDialogBuilder.setTitle("Вход");
        alertDialogBuilder.setCancelable(false).setPositiveButton("ОК", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int id) {
                EditText loginField = (EditText) prompt.findViewById(R.id.login_name);
                EditText passwordField = (EditText) prompt.findViewById(R.id.login_password);
                String login = loginField.getText().toString();
                String password = passwordField.getText().toString();
                String passwordHash = AppUtils.md5(password);
                if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) {
                    return;
                }
                AppSettings.setLoginAndPasswordHash(login, passwordHash);
                sendMessage(text, login, passwordHash, callback);
            }
        });
        alertDialogBuilder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
                callback.onCanceled();
            }
        });
        alertDialogBuilder.show();
    }


    public static void _sendMessage(String text, String login, String passwordHash, SendCallback callback) {
        try {
            String urlParameters = "login=" + encode(login) + "&password=" + passwordHash + "&message=" + encode(text);
            URL url = new URL(AppConstants.INVESTFLOW_RU + "/api/chat/send?" + urlParameters);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            for (int c; (c = in.read()) >= 0; ) {
                response.append((char) c);
            }
            JSONObject json = new JSONObject(response.toString());
            if (json.optBoolean("success", false)) {
                callback.onSuccess();
                return;
            }
            String error = json.optString("error", "");
            if ("1".equals(error) || "2".equals(error)) {
                callback.onError("Ошибка: неверное имя пользователя или пароль!");
                AppSettings.resetLoginAndPassword();
            } else if ("3".equals(error)) {
                callback.onError("Ошибка: попытка отправить пустое сообщение!");
            } else {
                callback.onError("Ошибка: " + json.toString());
            }
        } catch (Exception e) {
            callback.onError("Ошибка: " + e.getMessage());
        }
    }

    private static String encode(String text) throws UnsupportedEncodingException {
        return URLEncoder.encode(text, "UTF-8");
    }
}
