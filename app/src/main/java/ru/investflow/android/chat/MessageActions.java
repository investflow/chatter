package ru.investflow.android.chat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.ClipboardManager;

import org.jetbrains.annotations.NotNull;

public class MessageActions {
    public static void showActions(@NotNull final MainActivity ctx, @NotNull final ChatMessage cm) {
        CharSequence[] modes = new CharSequence[]{"Ответить", "Скопировать"};
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(cm.getUser())
                .setItems(modes, new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            ctx.inputText.setText("");
                            ctx.inputText.append(cm.getUser() + ", ");
                            ctx.showVirtualKeyboard();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText(cm.getUser() + ", сообщение", cm.getText());
                                clipboard.setPrimaryClip(clip);
                            } else {
                                //noinspection deprecation
                                ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
                                clipboard.setText(cm.getText());
                            }
                        }
                    }
                })
                .setNegativeButton("Отмена", null)
                .create();
        dialog.show();
    }

}
