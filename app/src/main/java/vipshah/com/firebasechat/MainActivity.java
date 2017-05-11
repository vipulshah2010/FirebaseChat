package vipshah.com.firebasechat;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int SIGN_IN_REQUEST_CODE = 123;

    private RecyclerView messagesRecyclerView;
    private EditText messageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messagesRecyclerView = (RecyclerView) findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        messageEditText = (EditText) findViewById(R.id.messageEditText);
        View sendMessageView = findViewById(R.id.sendMessageView);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(), SIGN_IN_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Welcome " + FirebaseAuth.getInstance().getCurrentUser().getEmail(),
                    Toast.LENGTH_LONG)
                    .show();

            displayChatMessages();
        }

        sendMessageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance()
                        .getReference()
                        .push()
                        .setValue(new ChatMessage(messageEditText.getText().toString(),
                                FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getEmail())
                        );
                messageEditText.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayChatMessages() {
        MessageAdapter adapter = new MessageAdapter(ChatMessage.class, R.layout.item_row_message, ViewHolder.class, FirebaseDatabase.getInstance().getReference());
        messagesRecyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Successfully signed in. Welcome!", Toast.LENGTH_LONG).show();
                displayChatMessages();
            } else {
                Toast.makeText(this, "We couldn't sign you in. Please try again later.", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView usernameTextView;
        public final TextView timeTextView;
        public final TextView messageTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            usernameTextView = (TextView) itemView.findViewById(R.id.usernameTextView);
            timeTextView = (TextView) itemView.findViewById(R.id.timeTextView);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
        }
    }

    class MessageAdapter extends FirebaseRecyclerAdapter<ChatMessage, ViewHolder> {

        public MessageAdapter(Class<ChatMessage> modelClass, int modelLayout, Class<ViewHolder> viewHolderClass, Query ref) {
            super(modelClass, modelLayout, viewHolderClass, ref);
        }

        @Override
        protected void populateViewHolder(ViewHolder viewHolder, ChatMessage chatMessage, int i) {
            viewHolder.usernameTextView.setText(chatMessage.getMessageUser());
            viewHolder.messageTextView.setText(chatMessage.getMessageText());
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault());
            viewHolder.timeTextView.setText(format.format(new Date(chatMessage.getMessageTime())));
        }
    }
}
