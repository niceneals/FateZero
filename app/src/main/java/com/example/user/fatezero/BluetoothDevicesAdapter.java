package com.example.user.fatezero;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;


public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice> {


    static class ViewHolder {

        @Bind(R.id.device_name) TextView name;
        @Bind(R.id.device_address) TextView address;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    public BluetoothDevicesAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        BluetoothDevice device = getItem(position);

        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_device, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.name.setText(device.getName());
        viewHolder.address.setText(device.getAddress());

        return convertView;
    }
}