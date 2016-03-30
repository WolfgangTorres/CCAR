package com.example.andrestorresb.ccar;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by andrestorres on 2/11/16.
 */
public class MyAdapter extends BaseAdapter{

    //adapter needs a structure to translate to GUI
    private ArrayList<Vehiculo> vehiculo;
    private Activity activity;
    private Mycars carros;

    public MyAdapter(Activity activity, ArrayList<Vehiculo> vehiculo, Mycars carro){
        this.vehiculo = vehiculo;
        this.activity = activity;
        this.carros=carro;
    }

    public MyAdapter(Activity activity, ArrayList<Vehiculo> vehiculo){
        this.vehiculo = vehiculo;
        this.activity = activity;
    }

    @Override
    public int getCount() {

        return this.vehiculo.size();
    }

    @Override
    public Object getItem(int position) {

        return this.vehiculo.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(this.activity.getClass().getSimpleName()==Alerta.class.getSimpleName()) {
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.alerta_line, null);
            }

            ImageView imagen = (ImageView) convertView.findViewById(R.id.imageView2);
            TextView txt1 = (TextView) convertView.findViewById(R.id.textView);
            TextView txt2 = (TextView) convertView.findViewById(R.id.textView2);
            TextView txt3 = (TextView) convertView.findViewById(R.id.textView3);

            Vehiculo s = this.vehiculo.get(position);
            imagen.setImageResource(R.drawable.car);

            txt1.setText(s.getMarca());
            txt2.setText(s.getColor());
            txt3.setText(s.getPlaca());
            return convertView;

        }
        else {

            convertView = activity.getLayoutInflater().inflate(R.layout.mycars_line, null);


            TextView txt1 = (TextView) convertView.findViewById(R.id.brand);
            TextView txt2 = (TextView) convertView.findViewById(R.id.colorCarro);
            TextView txt3 = (TextView) convertView.findViewById(R.id.placas);




            Vehiculo s = this.vehiculo.get(position);

            txt1.setText(s.getMarca());
            txt2.setText(s.getColor());
            txt3.setText(s.getPlaca());


            return convertView;
        }

    }
}
