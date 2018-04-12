package com.tejasmehta.codeychat;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ChatCreateActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    ListView list;
    ArrayAdapter<String> listItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_create);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        list = findViewById(R.id.listChatPeople);

        if (mAuth.getCurrentUser() != null) {

            String email = mAuth.getCurrentUser().getEmail().replace(".", ",");
            mDatabase.child("users").child("c").child("emailToUsername").child(email).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    final String username = dataSnapshot.getValue(String.class);
                    if (username != null) {

                        mDatabase.child("users").child("names").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                final Map<String, Object> names = (Map<String, Object>) dataSnapshot.getValue();
                                if (names.keySet() != null) {
                                    names.remove(username);

                                    final String[] nameToUse = names.keySet().toArray(new String[names.keySet().size()]);
                                    MenuItem searchItem = menu.findItem(R.id.action_search);

                                    SearchManager searchManager = (SearchManager) ChatCreateActivity.this.getSystemService(Context.SEARCH_SERVICE);

                                    List<String> nameL = Arrays.asList(nameToUse);

                                    listItems = new ArrayAdapter<String>(ChatCreateActivity.this, R.layout.search_result, R.id.user, nameToUse);
                                    list.setAdapter(listItems);

                                    SearchView searchView = null;
                                    if (searchItem != null) {
                                        searchView = (SearchView) searchItem.getActionView();
                                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                                            @Override
                                            public boolean onQueryTextSubmit(String query) {
                                                return false;
                                            }

                                            @Override
                                            public boolean onQueryTextChange(String newText) {
                                                Log.i("text", "change");

                                                listItems.getFilter().filter(newText);
                                                listItems.notifyDataSetChanged();

                                                return false;
                                            }
                                        });
                                    }
                                    if (searchView != null) {
                                        searchView.setSearchableInfo(searchManager.getSearchableInfo(ChatCreateActivity.this.getComponentName()));
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }





        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                RelativeLayout lay = (RelativeLayout) view;
                CheckedTextView text = lay.findViewById(R.id.user);

                Intent newAct = new Intent(getApplicationContext(), ChatsViewActivity.class);
                newAct.putExtra("chatType", false);
                newAct.putExtra("name", text.getText().toString());
                newAct.putExtra("newCreate", true);
                startActivity(newAct);


            }
        });


        return true;

    }
}
