package com.example.andrestorresb.ccar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Mycars extends ActionBarActivity implements JSONRequest.JSONListener, AdapterView.OnItemLongClickListener, ActionMode.Callback {

    TextView brandLabel,colorLabel,placasLabel;
    Intent i;
    ListView lv;
    View v;

    private Button addCarButton;

    private String brand,
                color,
                model,
                plates;

    private String deviceID,
                userID;

    private static String OK = "OK",
                          OMITTED = "OMITTED";

    private final int GET_CAR_INFO = 0,
                      DELETE_CAR = 1,
                      ADD_DEVICE = 2,
                      LINK_DEVICE = 3,
                      UPDATE_CAR = 4;

    private int status;

    private String newDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mycars);

        this.getSupportActionBar().setTitle("Vehiculos");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        this.lv=(ListView)findViewById(R.id.listView2);
        this.addCarButton = (Button)findViewById(R.id.addCarButton);

        this.i = getIntent();

        this.deviceID = this.i.getStringExtra("deviceID");
        this.userID = this.i.getStringExtra("userID");

        //Get data from firebase (brand, color, plates)
        this.getCarInfo();

        this.lv.setOnItemLongClickListener(this);
    }

    @Override
    public void onResume() {
        //Returned from any activity
        super.onResume();

        //Reaload car info only when Editar previous
        if(this.status == UPDATE_CAR || this.status == LINK_DEVICE) this.getCarInfo();
    }

    public void getCarInfo(){
        this.status = GET_CAR_INFO;

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/getDeviceCarInfo.php?devID=" + this.deviceID + "&userID=" + this.userID;

        Log.d("endpointGetCra", endpoint);

        new JSONRequest(this, this).execute(endpoint);
    }

    public void eliminar(){
        this.status = DELETE_CAR;

        String endpoint = "http://renatogutierrez.com/apps/CCAR/Plataforma/unlinkDevice.php?devID="
                + this.deviceID + "&userID=" + this.userID;

        //Finish activity when request done
        new JSONRequest(this,this).execute(endpoint);
    }

    public void configurar(){
        this.status = UPDATE_CAR;

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

    /*Read QR of device*/
    public void readQR(View v){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivityPortrait.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Add a device");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case IntentIntegrator.REQUEST_CODE:
                if (resultCode == RESULT_CANCELED){

                }else {
                    //QR Scanned successfully
                    this.status = ADD_DEVICE;

                    IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

                    this.newDevice = scanResult.getContents();
                    this.deviceID = this.newDevice;

                    String endpointRegisterDevice = "http://renatogutierrez.com/apps/CCAR/Plataforma/registerDevice.php?devID=" + this.newDevice;

                    new JSONRequest(this, this).execute(endpointRegisterDevice);

                    //Toast.makeText(this, "Device ID: " + this.newDevice, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void returnAndFinish(){
        Intent returnIntent = new Intent();

        returnIntent.putExtra("deviceID", this.deviceID);

        setResult(RESULT_OK, returnIntent);
        finish();
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

                        Vehiculo newCar = new Vehiculo(this.brand, this.color, this.plates);

                        if(newCar.isEmpty()){
                            //Show "Add" button
                            this.addCarButton.setVisibility(View.VISIBLE);

                            //Go to configuration
                            this.configurar();
                        }else{
                            //Hide "Add" button
                            this.addCarButton.setVisibility(View.INVISIBLE);

                            //Generate array of cars
                            ArrayList<Vehiculo> slist = new ArrayList<Vehiculo>();
                            slist.add(newCar);
                            MyAdapter a = new MyAdapter(this, slist, this);

                            //Set adapter to list of cars
                            this.lv.setAdapter(a);
                        }

                        break;
                    case DELETE_CAR:

                        String status = array.getString("response");

                        if(status.equals(OK)){
                            this.deviceID = "";

                            Toast.makeText(this,"Se Borro Satisfactoriamente",Toast.LENGTH_SHORT).show();

                            //Return new device ID (if exists)
                            this.returnAndFinish();
                        }else{
                            Toast.makeText(this, "No se logro eliminar el auto", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case ADD_DEVICE:

                        String status2 = array.getString("response");

                        Log.d("testing - response", status2);

                        if(status2.equals(OK) || status2.equals(OMITTED)){
                            //Link device with user
                            this.status = LINK_DEVICE;

                            String endpointLinkDevice = "http://renatogutierrez.com/apps/CCAR/Plataforma/linkDevice.php?devID=*DEVID*&userID=*USERID*";

                            endpointLinkDevice = endpointLinkDevice.replace("*DEVID*", this.newDevice);
                            endpointLinkDevice = endpointLinkDevice.replace("*USERID*", this.userID);

                            Log.d("endpointLinkDevice", endpointLinkDevice);

                            new JSONRequest(this, this).execute(endpointLinkDevice);
                        }else{
                            Toast.makeText(this, status2, Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case LINK_DEVICE:

                        String status3 = array.getString("response");

                        if(status3.equals(OK) || status3.equals(OMITTED)){
                            //Reaload cars
                            this.onResume();

                            Toast.makeText(this, "Se agrego exitosamente un nuevo dispositivo", Toast.LENGTH_SHORT).show();

                            //Redirigir a editar datos de auto del nuevo dispositivo (talves no aparecera en un principio porque son vacios los campos) por eso se redigire a editarlos
                            //Se redirige en el this.onResume() ->

                            //Creo que esta clase no esta apta para cargar mas de dos vehiculos, verificar
                        }else{
                            Toast.makeText(this, "No se logro agregar nuevo dispositivo", Toast.LENGTH_SHORT).show();
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

            this.returnAndFinish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
