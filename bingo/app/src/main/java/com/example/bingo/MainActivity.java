package com.example.bingo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.Group;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, ValueEventListener {

    private static final int RC_SIGN_IN = 100;
    private static final String TAG = MainActivity.class.getSimpleName();
    private FirebaseAuth auth;
    private FirebaseUser user;
    private TextView nickText;
    private Member member;
    private ImageView avatar;
    private RecyclerView recyclerView;
    int[] avatars = {R.drawable.bingo2,
            R.drawable.bingo3,
            R.drawable.bingo4,
            R.drawable.bingo5 ,
            R.drawable.bingo6,
            R.drawable.bingo7,
            R.drawable.binog};
    private FirebaseRecyclerAdapter<Room, RoomAdapter.RoomHolder> adapter;
    private Group avatargroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText titleEdit = new EditText(MainActivity.this);
                new AlertDialog.Builder(MainActivity.this)  //設定對話框
                        .setTitle("Room title")
                        .setView(titleEdit)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            //將nickname依uid加入firebase
                            public void onClick(DialogInterface dialog, int which) {
                                String roomTitle = titleEdit.getText().toString();
                                DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").push();
                                Room room = new Room(roomTitle, member);
                                roomRef.setValue(room);
                                String key = roomRef.getKey();
                                Log.d(TAG, "onClick: Room key" + key);
                                roomRef.child("key").setValue(key);
                                //TODO:enter game room
                                Intent bingo = new Intent(MainActivity.this, bingoActivity.class);
                                bingo.putExtra("ROOM_KEY", key);
                                bingo.putExtra("IS_CREATOR", true);
                                startActivity(bingo);
                            }
                        }).setNeutralButton("Cancel", null)
                        .show();
            }
        });
        auth = FirebaseAuth.getInstance();    //取得身分驗證
    }

    private void findView() {
        nickText = findViewById(R.id.nickname);
        avatar = findViewById(R.id.avatar);
        avatargroup = findViewById(R.id.group);
        recyclerView = findViewById(R.id.recycler);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatargroup.setVisibility(avatargroup.getVisibility() == View.VISIBLE? View.GONE : View.VISIBLE);
            }
        });
        findViewById(R.id.avatar_1).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_2).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_3).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_4).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_5).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_6).setOnClickListener((View.OnClickListener) this);
        findViewById(R.id.avatar_7).setOnClickListener((View.OnClickListener) this);
        //recyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Query query = FirebaseDatabase.getInstance().getReference("rooms").orderByKey();
        FirebaseRecyclerOptions<Room> options = new FirebaseRecyclerOptions.Builder<Room>()
                .setQuery(query, Room.class).build();
        adapter = new FirebaseRecyclerAdapter<Room, RoomAdapter.RoomHolder>(options) {
            @NonNull
            @Override
            public RoomAdapter.RoomHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().inflate(R.layout.item_room, parent, false);
                return null;
            }

            @Override
            protected void onBindViewHolder(@NonNull RoomAdapter.RoomHolder holder, int position, @NonNull Room model) {
                holder.title.setText(model.getTitle());
                holder.image.setImageResource(avatars[model.getCreator().getAvatar()]);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent bingo = new Intent(MainActivity.this, bingoActivity.class);
                        bingo.putExtra("ROOM_KEY", model.getId());
                        bingo.putExtra("IS_CREATOR", false);
                        startActivity(bingo);
                    }
                });

            }
        };
        recyclerView.setAdapter(adapter);
    }

    class RoomAdapter{
        class RoomHolder extends RecyclerView.ViewHolder{
            ImageView image;
            TextView title;
            public RoomHolder(@NonNull View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.room_image);
                title = itemView.findViewById(R.id.room_title);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(this);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        auth.removeAuthStateListener(this);
        adapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                break;
            case R.id.action_signout:
                auth.signOut();         //登出身分認證
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        //取得當前登陸的用戶
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
            FirebaseDatabase.getInstance() //取得資料庫，讀取或寫入資料
                    .getReference("users")
                    .child(user.getUid())
                    .child("uid")
                    .setValue(user.getUid());
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .addValueEventListener(this);
        } else {  //// Create and launch sign-in intent & Choose authentication providers
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    /*new AuthUI.IdpConfig.PhoneBuilder().build(),    //額外加入
                    new AuthUI.IdpConfig.FacebookBuilder().build(), //額外加入
                    new AuthUI.IdpConfig.TwitterBuilder().build(),  //額外加入*/
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            )).setIsSmartLockEnabled(false).build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        //this snapshot into a class of your choosing.
        member = snapshot.getValue(Member.class);
        Log.d(TAG, "onDataChange: " + member.getUid());
        if (member.getNickName() == null){
            final EditText nickEdit = new EditText(this);
            new AlertDialog.Builder(this)  //設定對話框
                    .setTitle("Nickname")
                    .setView(nickEdit)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        //將nickname依uid加入firebase
                        public void onClick(DialogInterface dialog, int which) {
                            String nickname = nickEdit.getText().toString();
                            member.setNickName(nickname);
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(user.getUid())
                                    .setValue(member);
                        }
                    }).setNeutralButton("Cancel", null)
                    .show();
        }else {
            FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //member data changed
                            nickText.setText(member.getNickName());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}