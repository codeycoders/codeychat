package com.tejasmehta.codeychat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.app.SearchManager;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.widget.AbsListView;
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

public class UserGroupSeachActivity extends AbstractActivity {

    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    ListView list;
    ArrayList<String> namesFinal;
    ArrayAdapter<String> listItems;
    ArrayList<String> checked;
    TextView people;
    HorizontalScrollView scroll;
    Button next;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group_seach);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.search_menu, menu);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        list = findViewById(R.id.listView);
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        namesFinal = new ArrayList<>();
        checked = new ArrayList<>();
        people = findViewById(R.id.textView);
        people.setVisibility(View.GONE);
        scroll = findViewById(R.id.horizontalScrollView);
        scroll.setVisibility(View.GONE);
        next = findViewById(R.id.button7);


        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);


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

                                    SearchManager searchManager = (SearchManager) UserGroupSeachActivity.this.getSystemService(Context.SEARCH_SERVICE);

                                    List<String> nameL = Arrays.asList(nameToUse);

                                    listItems = new ArrayAdapter<String>(UserGroupSeachActivity.this, R.layout.search_result, R.id.user, nameToUse);
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

/*                                namesFinal.clear();

                                for (int i = 0; i < nameToUse.length; i++) {

                                    if (nameToUse[i].contains(newText)) {

                                        namesFinal.add(nameToUse[i]);
                                        if (i == (nameToUse.length - 1)) {

                                            Log.i("names", namesFinal.toString());
                                            list.setAdapter(listItems);

                                        }

                                    }

                                } */

                                                return false;
                                            }
                                        });
                                    }
                                    if (searchView != null) {
                                        searchView.setSearchableInfo(searchManager.getSearchableInfo(UserGroupSeachActivity.this.getComponentName()));
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
                scroll.setVisibility(View.VISIBLE);

                if (!people.getText().toString().contains(text.getText().toString())) {
                    people.setText(people.getText().toString() + text.getText().toString() + ", ");
                    people.invalidate();
                    people.requestLayout();
                    text.setChecked(true);
                    people.setVisibility(View.VISIBLE);
                } else if (people.getText().toString().contains(text.getText().toString())) {
                    people.setText(people.getText().toString().replace(text.getText().toString() + ", ", ""));
                    people.invalidate();
                    people.requestLayout();
                    text.setChecked(false);
                    //text.setBackground(null);
                }



            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (people.getText().toString().isEmpty()) {

                    Toast.makeText(UserGroupSeachActivity.this, "Choose People to Add Into the Chat", Toast.LENGTH_SHORT).show();

                } else {

                    Intent intentToMove = new Intent(getApplicationContext(), FinalGroupChooseActivity.class);

                    intentToMove.putExtra("people", people.getText().toString());

                    startActivity(intentToMove);
                }

            }
        });



        return true;

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {

            getSupportActionBar().setTitle("");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
