package ru.investflow.android.chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.SparseBooleanArray;

public class SettingsDialog {

    public static void showDialog(Context ctx) {
        CharSequence[] modes = new CharSequence[]{"Показывать оповещения", "Звук в оповещениях"};
        boolean[] modeFlags = {AppSettings.useNotifications(), AppSettings.useSoundInNotifications()};

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Настройки")
                .setMultiChoiceItems(modes, modeFlags, new NoOpListener())
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Применить", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int i) {
                        SparseBooleanArray checkedItems = ((AlertDialog) d).getListView().getCheckedItemPositions();
                        AppSettings.setUseNotifications(checkedItems.get(0));
                        AppSettings.setUseSoundInNotifications(checkedItems.get(1));
                    }
                }).create();
        dialog.show();
    }

    // Android bug in old version: must have a listener to make checkboxes to work
    private static class NoOpListener implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
        }
    }
}


