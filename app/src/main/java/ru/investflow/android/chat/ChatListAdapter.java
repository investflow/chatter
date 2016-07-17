package ru.investflow.android.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.Query;

import ru.investflow.android.chat.util.AppUtils;
import ru.investflow.android.chat.util.FirebaseListAdapter;

public class ChatListAdapter extends FirebaseListAdapter<ChatMessage> {

    public ChatListAdapter(Query ref, Activity activity, int layout) {
        super(ref, ChatMessage.class, layout, activity);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void populateView(View view, ChatMessage cm) {
        String myUsername = AppSettings.getLogin();
        String color = myUsername != null && myUsername.equals(cm.getUser()) ? "#792F2D" : "#003C8C";
        String userHtml = "<font color='" + color + "'><b>" + cm.getUser() + ":</b></font> ";

        TextView messageView = (TextView) view.findViewById(R.id.message);
        String messageHtml = AppUtils.textToHtmlConvertingURLsToLinks(cm.getText());
        //noinspection deprecation
        messageView.setText(Html.fromHtml(userHtml + messageHtml));
        messageView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
