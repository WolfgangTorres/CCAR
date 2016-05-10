package com.example.andrestorresb.ccar;

import android.widget.ImageView;

/**
 * Created by andrestorres on 2/11/16.
 */
public class Vehiculo {

    private String marca;
    private String color;
    private String placa;
    private ImageView imagen;



    public Vehiculo(String marca,String color, String placa){
        this.marca=marca;
        this.color=color;
        this.placa=placa;
    }

    public ImageView getImagen() {
        return imagen;
    }

    public void setImagen(ImageView imagen) {
        this.imagen = imagen;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public boolean isEmpty(){
        return this.marca.equals("") && this.color.equals("") && this.placa.equals("");
    }
}
