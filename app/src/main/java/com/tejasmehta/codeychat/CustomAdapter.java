package com.tejasmehta.codeychat;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tejasmehta on 2/21/18.
 */

public class CustomAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<ChatBubble> objects;
    int layout;

    private class ViewHolder {
        TextView msg;
        TextView date;
    }

    public CustomAdapter(Context context, ArrayList<ChatBubble> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public ChatBubble getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {

        if (getItem(position).MsgType().equals("myMessage")) {

            return 1;

        } else if (getItem(position).MsgType().equals("notMyMessage")) {

            return 0;

        } else {

            return 2;

        }

        //return layout;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        int layoutResource; // determined by view type
        ChatBubble ChatBubble = getItem(position);

        if (ChatBubble.MsgType().equals("myMessage")) {
            layoutResource = R.layout.right_bubble;
        } else if (ChatBubble.MsgType().equals("notMyMessage")){
            layoutResource = R.layout.left_bubble;
        } else {
            layoutResource = R.layout.server_msg;
        }
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(layoutResource, null);
            holder.msg = convertView.findViewById(R.id.txt_msg);
            holder.date = convertView.findViewById(R.id.date);
            holder.msg.setGravity(Gravity.CENTER);
            holder.date.setGravity(Gravity.CENTER);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.msg.setText(objects.get(position).Msg());
        holder.date.setText(objects.get(position).Date());
        return convertView;
    }
}
