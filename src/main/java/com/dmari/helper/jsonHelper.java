/*
    objetivo de este archivo:
    esta clase es un ayudante (helper) disenado para centralizar la conversion
    de listas de objetos java a cadenas de texto en formato json.
    al modularizar esta logica aqui, evitamos repetir codigo en los controladores
    y mantenemos los servlets limpios y dedicados solo a responder peticiones.
*/
package com.dmari.helper;

import java.util.ArrayList;
import com.dmari.modelo.producto;
import com.dmari.modelo.categoria;
import com.dmari.modelo.etiqueta;

public class jsonHelper {
    
    /*
        metodo para convertir una lista de productos a formato json.
        se centraliza aqui para que cualquier controlador pueda usarlo
        sin repetir codigo.
    */
    public String productosAJson(ArrayList<producto> lista) {
        StringBuilder json = new StringBuilder();
        json.append("["); 

        for (int i = 0; i < lista.size(); i++) {
            producto p = lista.get(i);

            json.append("{");
            json.append("\"id\":").append(p.getIdProductoPk()).append(",");
            json.append("\"nombre\":\"").append(p.getNombreProducto()).append("\",");
            json.append("\"precio\":").append(p.getPrecio()).append(",");
            json.append("\"stock\":").append(p.getStock()).append(",");
            json.append("\"estado\":").append(p.isEstado()).append(",");
            json.append("\"categoria\":\"").append(p.getCategoria() != null ? p.getCategoria() : "Sin categoria").append("\",");
            
            json.append("\"etiquetas\":[");
            ArrayList<String> tags = p.getEtiquetas();
            if (tags != null) {
                for(int j = 0; j < tags.size(); j++){
                    json.append("\"").append(tags.get(j)).append("\"");
                    if(j < tags.size() - 1) json.append(",");
                }
            }
            json.append("],");
            
            json.append("\"imagen\":\"").append(p.getUrlRuta() != null ? p.getUrlRuta() : "").append("\"");
            json.append("}");

            if (i < lista.size() - 1) {
                json.append(",");
            }
        }

        json.append("]"); 
        return json.toString();
    }

    /*
        metodo para convertir una lista de categorias a formato json.
    */
    public String categoriasAJson(ArrayList<categoria> lista) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < lista.size(); i++) {
            categoria c = lista.get(i);
            
            json.append("{");
            json.append("\"id\":").append(c.getIdCategoriaPk()).append(",");
            json.append("\"nombre\":\"").append(c.getNombre()).append("\"");
            json.append("}");
            
            if (i < lista.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }

    /*
        metodo para convertir una lista de etiquetas a formato json.
    */
    public String etiquetasAJson(ArrayList<etiqueta> lista) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < lista.size(); i++) {
            etiqueta e = lista.get(i);
            
            json.append("{");
            json.append("\"id\":").append(e.getIdEtiquetaPk()).append(",");
            json.append("\"nombre\":\"").append(e.getNombreEtiqueta()).append("\"");
            json.append("}");
            
            if (i < lista.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }

    /*
        metodo para convertir el historial de pedidos a formato json.
    */
    public String pedidosAJson(ArrayList<detallePedido> lista) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < lista.size(); i++) {
            detallePedido dp = lista.get(i);
            
            json.append("{");
            json.append("\"idPedido\":").append(dp.getIdPedidoFk()).append(",");
            json.append("\"fecha\":\"").append(dp.getFechaPedido() != null ? dp.getFechaPedido() : "").append("\",");
            json.append("\"estado\":\"").append(dp.getEstadoPedido() != null ? dp.getEstadoPedido() : "").append("\",");
            json.append("\"producto\":\"").append(dp.getNombreProducto() != null ? dp.getNombreProducto() : "").append("\",");
            json.append("\"cantidad\":").append(dp.getCantidad()).append(",");
            json.append("\"precio\":").append(dp.getPrecioUnitario()).append(",");
            json.append("\"subtotal\":").append(dp.getSubtotal());
            
            if (dp.getNombreCliente() != null) {
                json.append(",\"cliente\":\"").append(dp.getNombreCliente()).append("\"");
            }
            json.append("}");
            
            if (i < lista.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]");
        return json.toString();
    }
}