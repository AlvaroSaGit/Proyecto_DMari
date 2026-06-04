/*
    objetivo de este archivo:
    representar la tabla 'carrito' de la base de datos en java.
    funciona como la canasta principal o cabecera a la que se amarran los productos.
*/
package com.dmari.modelo;

public class carrito {
    // llave primaria de la cabecera del carrito
    private int idCarritoPk;
    // llave foranea al cliente propietario
    private int idClienteFk;

    // constructor vacio por defecto
    public carrito() {
    }

    // metodos de acceso (getters y setters)
    // recupera el id del carrito
    public int getIdCarritoPk() {
        return idCarritoPk;
    }
    // asigna el id del carrito
    public void setIdCarritoPk(int idCarritoPk) {
        this.idCarritoPk = idCarritoPk;
    }

    // recupera el id del cliente
    public int getIdClienteFk() {
        return idClienteFk;
    }
    // asigna el id del cliente
    public void setIdClienteFk(int idClienteFk) {
        this.idClienteFk = idClienteFk;
    }
}