package com.example.kerzak.cook4me.Activities;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.kerzak.cook4me.R;
import com.example.kerzak.cook4me.Sockets.ClientThread;

import java.util.ArrayList;
import java.util.List;

public class SearchUserActivity extends ListActivity  {

    EditText searchUsersEditText;

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    List<String> listItems=new ArrayList<String>();

    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    String myLogin;

    String json;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_search_user);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        setListAdapter(adapter);


        searchUsersEditText = (EditText) findViewById(R.id.searchUsersEditText);
        searchUsersEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    listItems.clear();
                    adapter.notifyDataSetChanged();;
                } else {
                    ClientThread.getInstance(null).search(s.toString(), searchHandler);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ListView listView = getListView();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent myIntent = new Intent(SearchUserActivity.this,RankingActivity.class);
                String name = (String)parent.getItemAtPosition(position);
                myIntent.putExtra("name", name);
                if (json != null) {
                    myIntent.putExtra("json", json);
                }
                SearchUserActivity.this.startActivity(myIntent);
            }
        });
        json = getIntent().getStringExtra("json");

    }

    private Handler searchHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            List<String> results = (List<String>) msg.obj;
            listItems.clear();
            listItems.addAll(results);
            adapter.notifyDataSetChanged();;
        }
    };

    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(SearchUserActivity.this,MapsActivity.class);
        if (json != null) {
            myIntent.putExtra("json", json);
        }
        SearchUserActivity.this.startActivity(myIntent);
    }




}
