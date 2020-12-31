package com.example.bingo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class bingoActivity extends AppCompatActivity implements ValueEventListener {

    private static final int NUMBER_COUNT = 25;
    private static final String TAG = bingoActivity.class.getSimpleName();
    private String roomkey;
    private boolean creator;
    private TextView info;
    private RecyclerView recyclerView;
    private List<NumberBall> numbers;
    private List<Button> buttons;
    Map<Integer, Integer> numberPositions = new HashMap<>();
    private NumberAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bingo);
        roomkey = getIntent().getStringExtra("ROOM_KEY");
        creator = getIntent().getBooleanExtra("IS_CREATOR", false);
        generateRandom();
        if (isCreator()) {
            for (int i = 0; i < NUMBER_COUNT; i++) {
                FirebaseDatabase.getInstance().getReference("rooms")
                        .child("rooms")
                        .child(roomkey)
                        .child("numbers")
                        .child((i+1) + "")
                        .setValue(false);
            }
        }else { //joiner

        }
        findViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference("rooms")
                .child(roomkey)
                .child("numbers")
                .addValueEventListener(this);

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void generateRandom() {
        //generate random numbers
        numbers = new ArrayList<>();
        buttons = new ArrayList<>();
        for (int i = 0; i < NUMBER_COUNT; i++) {
            numbers.add(new NumberBall(i));
        }
        Collections.shuffle(numbers);
        for (int i = 0; i < NUMBER_COUNT; i++) {
            Button button = new Button(this);
            button.setText(numbers + "");
            buttons.add(button);
            numberPositions.put(numbers.get(i).getNumber(), i);
        }
    }

    private void findViews() {
        info = findViewById(R.id.info);
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        adapter = new NumberAdapter();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            boolean picked =(boolean) snapshot.getValue();
            int num = Integer.parseInt(snapshot.getKey());
            if(picked){
                numbers.get(numberPositions.get(num)).setPicked(true);
            }
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    class NumberAdapter extends RecyclerView.Adapter<NumberHolder>{

        @NonNull
        @Override
        public NumberHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new NumberHolder(getLayoutInflater().inflate(R.layout.number_item, parent, false));
        }

        @Override //顯示球的畫面
        public void onBindViewHolder(@NonNull NumberHolder holder, int position) {
            holder.button.setText(numbers.get(position).getNumber() +"");
            holder.button.setTag(position);
            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override //Firebase 資料收集
                public void onClick(View v) {
                    Log.d(TAG, "onClick:number "+ numbers.get(position));
                    FirebaseDatabase.getInstance().getReference("rooms")
                            .child(roomkey)
                            .child("numbers")
                            .child(numbers.get(position).getNumber() + "")
                            .setValue(true);
                    holder.button.setEnabled(false);
                }
            });

        }

        @Override
        public int getItemCount() {
            return NUMBER_COUNT;
        }
    }
        class NumberHolder extends RecyclerView.ViewHolder{
            Button button;
            public NumberHolder(@NonNull View itemView) {
                super(itemView);
                button = itemView.findViewById(R.id.button2);
            }
        }

    public boolean isCreator() {
        return creator;
    }

    public void setCreator(boolean creator) {
        this.creator = creator;
    }
}