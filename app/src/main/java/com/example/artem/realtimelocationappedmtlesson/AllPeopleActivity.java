package com.example.artem.realtimelocationappedmtlesson;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.artem.realtimelocationappedmtlesson.Interface.IFirebaseLoadDone;
import com.example.artem.realtimelocationappedmtlesson.Interface.IRecyclerItemClickListener;
import com.example.artem.realtimelocationappedmtlesson.Models.User;
import com.example.artem.realtimelocationappedmtlesson.Utils.Common;
import com.example.artem.realtimelocationappedmtlesson.ViewHolder.UserViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;
import java.util.List;

public class AllPeopleActivity extends AppCompatActivity implements IFirebaseLoadDone {

    FirebaseRecyclerAdapter<User, UserViewHolder> adapter, searchAdapter;
    RecyclerView recycler_all_user;
    IFirebaseLoadDone firebaseLoadDone;
    MaterialSearchBar material_search_bar;
    List<String> suggestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_people);

        recycler_all_user = (RecyclerView)findViewById(R.id.recycler_all_people);
        recycler_all_user.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recycler_all_user.setLayoutManager(layoutManager);
        recycler_all_user.addItemDecoration(new DividerItemDecoration(this, ((LinearLayoutManager) layoutManager).getOrientation()));

        material_search_bar = (MaterialSearchBar)findViewById(R.id.material_search_bar);
        material_search_bar.setCardViewElevation(10);
        material_search_bar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<>();
                for (String search:suggestList){
                    if (search.toLowerCase().contains(material_search_bar.getText().toLowerCase())){
                        suggest.add(search);
                    }
                }
                material_search_bar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        material_search_bar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled){
                    if (adapter != null){
//                      If close search, restore default
                        recycler_all_user.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text.toString());
            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

        firebaseLoadDone = this;
        loadUserList();
        loadSearchData();
    }

    private void loadSearchData() {
        final List<String> listUserEmail = new ArrayList<>();

        DatabaseReference userList = FirebaseDatabase.getInstance().getReference(Common.USER_INFO);
        userList.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapShot : dataSnapshot.getChildren()){
                    User user = userSnapShot.getValue(User.class);
                    listUserEmail.add(user.getEmail());

                }
                firebaseLoadDone.onFirebaseLoadUserNameDone(listUserEmail);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                firebaseLoadDone.onFirebaseLoadFailed(databaseError.getMessage());
            }
        });
    }

    private void loadUserList() {
        Query query = FirebaseDatabase.getInstance().getReference().child(Common.USER_INFO);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                if (model.getEmail().equals(Common.loggedUser.getEmail())){
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append(" (me) "));
                    holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                }else {
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                }

//              Event
                holder.iRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {

                    }
                });{

                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        adapter.startListening();
        recycler_all_user.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        if (adapter != null) {
            adapter.stopListening();
        }
        if (searchAdapter != null){
            searchAdapter.stopListening();
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null){
            adapter.startListening();
        }
        if (searchAdapter != null){
            searchAdapter.startListening();
        }
    }

    private void startSearch(String txt_search) {
        Query query = FirebaseDatabase.getInstance().getReference(Common.USER_INFO).orderByChild("name").startAt(txt_search);

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User.class)
                .build();

        searchAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull User model) {
                if (model.getEmail().equals(Common.loggedUser.getEmail())){
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()).append(" (me) "));
                    holder.txt_user_email.setTypeface(holder.txt_user_email.getTypeface(), Typeface.ITALIC);
                }else {
                    holder.txt_user_email.setText(new StringBuilder(model.getEmail()));
                }

//              Event
                holder.iRecyclerItemClickListener(new IRecyclerItemClickListener() {
                    @Override
                    public void onItemClickListener(View view, int position) {

                    }
                });{

                }
            }

            @NonNull
            @Override
            public UserViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_user, viewGroup, false);
                return new UserViewHolder(itemView);
            }
        };
        searchAdapter.startListening();
        recycler_all_user.setAdapter(searchAdapter);


    }

    @Override
    public void onFirebaseLoadUserNameDone(List<String> listEmail) {
        material_search_bar.setLastSuggestions(listEmail);
    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
