/*
    objetivo de este archivo:
    esta clase es un ayudante (helper) disenado para centralizar la conversion
    de listas de objetos java a cadenas de texto en formato json.
    al modularizar esta logica aqui, evitamos repetir codigo en los controladores
    y mantenemos los servlets limpios y dedicados solo a responder peticiones.
*/
package com.dmari.helper;

import java.util.ArrayList;

import com.dmari.modelo.categoria;
import com.dmari.modelo.detallePedido;
import com.dmari.modelo.etiqueta;
import com.dmari.modelo.producto;

public class jsonHelper {
    
    // metodo interno para limpiar textos y evitar que caracteres especiales 
    // rompan la estructura del json en descripciones largas.
    private String escaparTexto(String texto) {
        // validamos si el texto es nulo para evitar errores de ejecucion
        if (texto == null) return "";
        // reemplazamos caracteres de escape de forma encadenada
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    // metodo para convertir una lista de productos a formato json.
    // se centraliza aqui para que cualquier controlador pueda usarlo sin repetir codigo.
    public String productosAJson(ArrayList<producto> lista) {
        // usamos stringbuilder para construir la cadena de texto de forma eficiente
        StringBuilder json = new StringBuilder();
        json.append("["); 

        // recorremos la lista de objetos producto para mapear sus campos
        for (int i = 0; i < lista.size(); i++) {
            producto p = lista.get(i);

            // iniciamos la estructura del objeto json para cada producto
            json.append("{");
            json.append("\"id\":").append(p.getIdProductoPk()).append(",");
            json.append("\"nombre\":\"").append(escaparTexto(p.getNombreProducto())).append("\",");
            json.append("\"descripcion\":\"").append(escaparTexto(p.getDescripcion())).append("\",");
            // agregamos los valores numericos y booleanos sin comillas
            json.append("\"precio\":").append(p.getPrecio()).append(",");
            json.append("\"stock\":").append(p.getStock()).append(",");
            json.append("\"estado\":").append(p.isEstado()).append(",");
            // verificamos si la categoria es nula para poner un texto por defecto
            json.append("\"categoria\":\"").append(p.getCategoria() != null ? p.getCategoria() : "Sin categoria").append("\",");
            
            // abrimos la lista de etiquetas como un sub arreglo json
            json.append("\"etiquetas\":[");
            ArrayList<String> tags = p.getEtiquetas();
            // procesamos la coleccion de tags si no es nula
            if (tags != null) {
                for(int j = 0; j < tags.size(); j++){
                    json.append("\"").append(tags.get(j)).append("\"");
                    // colocamos la coma de separacion entre tags internos
                    if(j < tags.size() - 1) json.append(",");
                }
            }
            json.append("],");
            
            // incluimos la ruta de la imagen y cerramos el objeto del producto
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
    // convierte la lista de categorias en un formato json basico para filtros
    public String categoriasAJson(ArrayList<categoria> lista) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        // bucle de construccion para objetos categoria
        for (int i = 0; i < lista.size(); i++) {
            categoria c = lista.get(i);
            
            // solo mapeamos id y nombre para los select del frontend
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
    // serializa el historial de pedidos con sus detalles financieros
    public String pedidosAJson(ArrayList<detallePedido> lista) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        // iteramos sobre cada registro de pedido recuperado del dao
        for (int i = 0; i < lista.size(); i++) {
            detallePedido dp = lista.get(i);
            
            // asignamos las propiedades de fecha, estado y datos del producto
            json.append("{");
            json.append("\"idPedido\":").append(dp.getIdPedidoFk()).append(",");
            json.append("\"fecha\":\"").append(dp.getFechaPedido() != null ? dp.getFechaPedido() : "").append("\",");
            json.append("\"estado\":\"").append(dp.getEstadoPedido() != null ? dp.getEstadoPedido() : "").append("\",");
            json.append("\"motivo_cancelacion\":\"").append(dp.getMotivoCancelacion() != null ? escaparTexto(dp.getMotivoCancelacion()) : "").append("\",");
            json.append("\"producto\":\"").append(dp.getNombreProducto() != null ? dp.getNombreProducto() : "").append("\",");
            json.append("\"cantidad\":").append(dp.getCantidad()).append(",");
            json.append("\"precio\":").append(dp.getPrecioUnitario()).append(",");
            json.append("\"subtotal\":").append(dp.getSubtotal());
            
            // incluimos el nombre del cliente opcionalmente si el reporte lo requiere
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