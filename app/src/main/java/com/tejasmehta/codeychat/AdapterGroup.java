package com.tejasmehta.codeychat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by tejasmehta on 3/1/18.
 */

public class AdapterGroup extends BaseAdapter {

    private LayoutInflater inflater;
    private ArrayList<GroupLayout> objects;
    int layout;

    private class ViewHolder {
        TextView name;
        TextView lastMsg;
    }

    public AdapterGroup(Context context, ArrayList<GroupLayout> objects) {
        inflater = LayoutInflater.from(context);
        this.objects = objects;
    }

    public int getCount() {
        return objects.size();
    }

    public GroupLayout getItem(int position) {
        return objects.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.group_chats, null);
            holder.name = (TextView) convertView.findViewById(R.id.groupName);
            holder.lastMsg = (TextView) convertView.findViewById(R.id.lastMsg);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(objects.get(position).getName());
        holder.lastMsg.setText(objects.get(position).LastMsg());
        return convertView;
    }

}
