package com.example.al.sibirski;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AdapterDetails extends ArrayAdapter<ContainerPayment> {

    public AdapterDetails(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContainerPayment containerPayment = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView date = (TextView) convertView.findViewById(R.id.list_item_date);
        TextView text = (TextView) convertView.findViewById(R.id.list_item_text);
        TextView sum = (TextView) convertView.findViewById(R.id.list_item_sum);

        date.setText(containerPayment.getDate());
        text.setText(containerPayment.getText());
        sum.setText(containerPayment.getSum());

        if (containerPayment.isPlus()) {
            sum.setTextColor(getContext().getResources().getColor(R.color.colorPlus));
        }
        else {
            sum.setTextColor(getContext().getResources().getColor(R.color.colorMinus));
        }

        return convertView;
    }
}
