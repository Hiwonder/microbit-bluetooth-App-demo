package com.lobot.HiwonderDemo.widget;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.lobot.HiwonderDemo.R;

import java.util.ArrayList;

/**
 * Created by WangLei on 2018/9/14.
 */
public class BluetoothDataAdapter extends BaseAdapter {

    private ArrayList<BluetoothDevice> devices;

    private Context context;

    public BluetoothDataAdapter(Context context, ArrayList<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
    }

    public void add(BluetoothDevice device) {
        if (!devices.contains(device)) {
            this.devices.add(device);
            notifyDataSetChanged();
        }
    }


    public void clear() {
        devices.clear();
        devices = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder holder = new ViewHolder();

        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.item_search_device, parent,
                    false);
            holder.titleView = (TextView) view.findViewById(R.id.item_device_title);
            holder.contentView = (TextView) view.findViewById(R.id.item_device_content);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        BluetoothDevice device = devices.get(position);
        holder.titleView.setText(device.getName());
        holder.contentView.setText(device.getAddress());
        return view;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    class ViewHolder {
        TextView titleView;
        TextView contentView;
    }
}
