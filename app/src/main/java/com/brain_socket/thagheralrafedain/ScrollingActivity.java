package com.brain_socket.thagheralrafedain;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brain_socket.thagheralrafedain.data.DataStore;
import com.brain_socket.thagheralrafedain.data.PhotoProvider;
import com.brain_socket.thagheralrafedain.data.ServerResult;

public class ScrollingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rvBrands);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AppsAdapter(this));
    }


    class AppsAdapter extends RecyclerView.Adapter<AppViewHolder> {

        private LayoutInflater inflater;

        public AppsAdapter(Context context) {
            inflater = LayoutInflater.from(context);
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View root = inflater.inflate(R.layout.row_diag_filter_child, viewGroup, false);
            AppViewHolder holder = new AppViewHolder(root);
            return holder;
        }

        @Override
        public void onBindViewHolder(AppViewHolder viewHolder, int i) {
            try {

            } catch (Exception ignored) {

            }
        }

        @Override
        public int getItemCount() {
            return 8;
        }
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {

        TextView tvName1, tvDesc,tvUpVotes;
        ImageView ivPic, ivUpVote;
        View vUpvoteContainer;

        public AppViewHolder(View view) {
            super(view);
        }
    }

}
