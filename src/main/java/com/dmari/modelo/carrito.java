/*
    objetivo de este archivo:
    representar la tabla 'carrito' de la base de datos en java.
    funciona como la canasta principal o cabecera a la que se amarran los productos.
*/
package com.dmari.modelo;

public class carrito {
    
    private int idCarritoPk;
    private int idClienteFk;

    // constructor vacio por defecto
    public carrito() {
    }

    // metodos de acceso (getters y setters)
    public int getIdCarritoPk() {
        return idCarritoPk;
    }
    public void setIdCarritoPk(int idCarritoPk) {
        this.idCarritoPk = idCarritoPk;
    }

    public int getIdClienteFk() {
        return idClienteFk;
    }
    public void setIdClienteFk(int idClienteFk) {
        this.idClienteFk = idClienteFk;
    }
}