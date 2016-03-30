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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Mycars extends ActionBarActivity implements JSONRequest.JSONListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    TextView brandLabel,colorLabel,placasLabel;
    Intent i;
    ListView lv;
    View v;

    private String brand,
                color,
                model,
                plates;

    private String deviceID,
                userID;

    private static String OK = "OK";
    private final int GET_CAR_INFO = 0,
                    DELETE_CAR = 1;
    private int status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycars);

        this.getSupportActionBar().setTitle("Vehiculos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.lv=(ListView)findViewById(R.id.listView2);


        this.i = getIntent();

        this.deviceID = this.i.getStringExtra("deviceID");
        this.userID = this.i.getStringExtra("userID");

        //Get data from firebase (brand, color, plates)
        this.getCarInfo();

        this.lv.setOnItemLongClickListener(this);
    }

    @Override
    public void onResume() {
        //Returned from Editar
        super.onResume();

        //Reaload car info
        this.getCarInfo();
    }

    public void getCarInfo(){
        this.status = GET_CAR_INFO;

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceCarInfo.php?devID=" + this.deviceID;

        new JSONRequest(this, this).execute(endpoint);
    }

    public void eliminar(){
        this.status = DELETE_CAR;

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/unlinkDevice.php?devID="
                + this.deviceID + "&userID=" + this.userID;

        new JSONRequest(this,this).execute(endpoint);

        finish();
    }

    public void configurar(){
        Intent i=new Intent(this, Editar.class);
        ArrayList al=new ArrayList();
        al.add(this.brand);
        al.add(this.color);
        al.add(this.plates);
        al.add(this.model);
        i.putStringArrayListExtra("carro", al);
        i.putExtra("deviceID", this.deviceID);
        startActivity(i);
    }

    @Override
    public void doSomething(JSONObject array) {
        if(array != null){

            try{
                switch (this.status) {
                    case GET_CAR_INFO:

                        JSONObject response = array.getJSONObject("response");

                        this.brand = response.getString("brand");
                        this.color = response.getString("color");
                        this.model = response.getString("model");
                        this.plates = response.getString("plates");

                        //Generate array of cars
                        ArrayList<Vehiculo> slist = new ArrayList<Vehiculo>();
                        slist.add(new Vehiculo(this.brand, this.color, this.plates));
                        MyAdapter a = new MyAdapter(this, slist, this);

                        //Set adapter to list of cars
                        this.lv.setAdapter(a);
                        break;
                    case DELETE_CAR:

                        String status = array.getString("response");

                        if(status.equals(OK)){
                            Toast.makeText(this,"Se Borro Satisfactoriamente",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }



    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        this.brandLabel = (TextView)view.findViewById(R.id.brand);
        this.colorLabel = (TextView)view.findViewById(R.id.colorCarro);
        this.placasLabel = (TextView)view.findViewById(R.id.placas);
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
        mode = null;
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
