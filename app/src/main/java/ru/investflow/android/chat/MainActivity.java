package ru.investflow.android.chat;

import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import ru.investflow.android.chat.util.ThreadUtils;

// https://developer.android.com/reference/android/app/Activity.html
public class MainActivity extends ListActivity {

    private EditText inputText;
    private ImageButton sendButton;
    private ImageButton menuButton;

    public enum State {
        Created,
        Started,
        Resumed,
        Paused,
        Stopped,
        Destroyed
    }

    private static MainActivity instance = null;

    private ValueEventListener firebaseConnectionListener;
    private ChatListAdapter chatListAdapter;
    private State state;

    @Nullable
    public static MainActivity getInstance() {
        return instance;
    }

    @NotNull
    public State getState() {
        return state;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = State.Created;
        instance = this;

        //Remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        // Setup our input methods. Enter key on the keyboard or pushing the send button
        inputText = (EditText) findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });

        sendButton = (ImageButton) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        menuButton = (ImageButton) findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsDialog.showDialog(MainActivity.this);
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        //Make both send & menu buttons have the same size
        menuButton.setMinimumWidth(sendButton.getMeasuredWidth());
        menuButton.setMinimumHeight(sendButton.getMeasuredHeight());

        // Align text editor height too
        inputText.setMinimumHeight(sendButton.getMeasuredHeight());
    }

    @Override
    public void onStart() {
        super.onStart();
        state = State.Started;

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        final ListView listView = getListView();

        // Tell our list adapter that we only want 100 messages at a time
        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference chatNodeRef = firebase.getReference(AppConstants.FIREBASE_CHAT_NODE);
        chatListAdapter = new ChatListAdapter(chatNodeRef.limitToLast(100), this, R.layout.chat_message);
        listView.setAdapter(chatListAdapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                hideVirtualKeyboard();
                return false;
            }
        });

        chatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatListAdapter.getCount() - 1);
            }
        });

        // Indication of connection status
        firebaseConnectionListener = firebase.getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                //noinspection deprecation
                Drawable drawable = getResources().getDrawable(connected ? android.R.drawable.presence_online : android.R.drawable.presence_invisible);
                menuButton.setImageDrawable(drawable);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // no op
            }
        });
        // Do not show virtual keyboard unless user activates it manually
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        state = State.Resumed;
        NotificationService.clearAllNotifications(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        state = State.Paused;
    }

    @Override
    public void onStop() {
        super.onStop();
        state = State.Stopped;

        FirebaseDatabase.getInstance().getReference(".info/connected").removeEventListener(firebaseConnectionListener);
        chatListAdapter.cleanup();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        state = State.Destroyed;

        if (instance == this) {
            instance = null;
        }
    }

    private void sendMessage() {
        String input = inputText.getText().toString();
        if (TextUtils.isEmpty(input)) {
            return;
        }
        Messenger.sendMessage(this, input, new Messenger.SendCallback() {
            @Override
            public void onSuccess() {
                ThreadUtils.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        inputText.setText("");
                    }
                });
            }

            @Override
            public void onError(final String message) {
                ThreadUtils.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Ошибка: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCanceled() {
                Toast.makeText(MainActivity.this, "Отменено", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hideVirtualKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
