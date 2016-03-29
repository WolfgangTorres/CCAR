package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;

public class Mycars extends ActionBarActivity implements JSONRequest.JSONListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    TextView modelo,color,placas;
    Intent i;
    ListView lv;
    View v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycars);

        this.getSupportActionBar().setTitle("Vehiculos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.lv=(ListView)findViewById(R.id.listView2);


        this.i = getIntent();

        //Generate array of cars
        ArrayList<Vehiculo> slist = new ArrayList<Vehiculo>();
        //Get data from firebase (brand, color, plates)
        slist.add(new Vehiculo("Ferrari","Rojo", "FAW-ASF-RE"));
        slist.add(new Vehiculo("Audi","Blanco","AAA-TTT_56"));
        slist.add(new Vehiculo("Ford", "Azul","QWT-HJK-A1"));
        MyAdapter a = new MyAdapter(this, slist, this);

        //Set adapter to list of cars
        this.lv.setAdapter(a);
        this.lv.setOnItemLongClickListener(this);
        //Toast.makeText(this,this.modelo.getText().toString(),Toast.LENGTH_SHORT).show();



    }

    public void eliminar(){
        new JSONRequest(this,this).execute("http://renatogutierrez.com/apps/CCAR/Plataforma/unlinkDevice.php?devID="
                + this.i.getStringExtra("deviceID") + "&userID=" + this.i.getStringExtra("userID"));
        finish();
    }

    public void configurar(){
        Intent i=new Intent(this, Editar.class);
        ArrayList al=new ArrayList();
        al.add(this.modelo.getText().toString());
        al.add(this.color.getText().toString());
        al.add(this.placas.getText().toString());
        i.putStringArrayListExtra("carro", al);
        startActivity(i);
    }

    @Override
    public void doSomething(JSONObject array) {
        if(array!=null){
            Toast.makeText(this,"Se Borro Satisfactoriamente",Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        this.modelo = (TextView)view.findViewById(R.id.modelo);
        this.color= (TextView)view.findViewById(R.id.colorCarro);
        this.placas = (TextView)view.findViewById(R.id.placas);
        view.setSelected(true);
        view.setBackgroundColor(Color.LTGRAY);
        this.v=view;
        startActionMode(this);
        return true;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.editar_menu:
                configurar();
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.borrar_menu:
                eliminar();
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode=null;
        this.v.setSelected(false);
        this.v.setBackgroundColor(Color.TRANSPARENT);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
