package com.pcortex.pcgmclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by fdam on 16-03-2015.
 */
public class AppAdapter extends ArrayAdapter<String> {
    private final String LOG_TAG = AppAdapter.class.getSimpleName();

    Context mContext;
    List<Integer> icons = new ArrayList<Integer>();
    List<String> titles = new ArrayList<String>();
    List<String> description = new ArrayList<String>();

    AppAdapter(Context c , Integer[] icons , String[] titles , String[] description ) {
        super( c , R.layout.row_layout , R.id.rowTitle , titles  );
        mContext = c;

        this.icons.addAll(Arrays.asList(icons));
        this.titles.addAll(Arrays.asList(titles));
        this.description.addAll(Arrays.asList(description));

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        AppViewHolder appViewHolder = null;

        if ( row == null ) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.row_layout, parent, false);
            appViewHolder = new AppViewHolder(row);
            row.setTag(appViewHolder);
            //Log.i (LOG_TAG , "New Row!");
        } else {
            appViewHolder = (AppViewHolder) row.getTag();
            //Log.i (LOG_TAG , "Recycling row!");
        }

        appViewHolder.icon.setImageResource((Integer) icons.get(position));
        appViewHolder.title.setText((String)titles.get(position));
        appViewHolder.desc.setText((String)description.get(position));

        return row; //super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return icons.size();
    }

    public void add(Integer i , String title , String desc) {
        icons.add(i);
        titles.add(title);
        description.add(desc);

        this.notifyDataSetChanged();
//        Log.i(LOG_TAG, " on click send....");
//        Integer cnt = icons.size();
//        Log.i(LOG_TAG, " Numero de elementos: " + cnt.toString());
    }

    @Override
    public void clear() {
        icons.clear();
        titles.clear();
        description.clear();

        this.notifyDataSetChanged();
    }

    // ViewHolder support class
    class AppViewHolder {
        ImageView icon;
        TextView title ;
        TextView desc;

        AppViewHolder ( View v ) {
            icon = (ImageView) v.findViewById(R.id.rowIcon);
            title = (TextView)  v.findViewById(R.id.rowTitle);
            desc  = (TextView)  v.findViewById(R.id.rowDesc);
        }
    }

}
