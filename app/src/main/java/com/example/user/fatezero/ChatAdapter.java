package com.example.user.fatezero;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ChatAdapter extends ArrayAdapter<ChatMessage> {


    static class ViewHolder {

        @Bind(R.id.time_text_view) TextView time;
        @Bind(R.id.device_text_view) TextView device;
        @Bind(R.id.message_text_view) TextView message;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public ChatAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMessage chatMessage = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_message, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }



        if (BluetoothActivity.showTimeIsChecked) {
            viewHolder.time.setText(chatMessage.getTime());
        } else {
            viewHolder.time.setText("");
        }
        viewHolder.device.setText(chatMessage.getDevice().concat(":"));
        viewHolder.message.setText(chatMessage.getMessage());

        return convertView;
    }
}