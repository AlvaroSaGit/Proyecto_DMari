package com.dmari.helper;

import java.util.ArrayList;

import com.dmari.modelo.categoria;
import com.dmari.modelo.detallePedido;
import com.dmari.modelo.etiqueta;
import com.dmari.modelo.producto;

/*
    objetivo de este archivo:
    esta clase es un ayudante disenado para centralizar la conversion
    de listas de objetos java a cadenas de texto en formato json.
    al modularizar esta logica aqui, evitamos repetir codigo en los controladores
    y mantenemos los servlets limpios y dedicados solo a responder peticiones.
*/
public class jsonHelper {
    
    /**
     * Limpia un texto escapando caracteres especiales que pueden romper el formato JSON.
     * Se aplica en descripciones, nombres con comillas y textos con saltos de linea.
     *
     * @param texto la cadena que se quiere limpiar. Puede ser nulo.
     * @return el texto con los caracteres escapados, o una cadena vacia si el texto era nulo.
     */
    public static String escaparTexto(String texto) {
        // validamos si el texto es nulo para evitar errores de ejecucion
        if (texto == null) return "";
        // reemplazamos caracteres de escape de forma encadenada
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    /**
     * Convierte una lista de productos a una cadena en formato JSON.
     * Incluye todos los campos del producto: id, nombre, descripcion, precio, stock,
     * estado, categoria, etiquetas, imagen y datos del proveedor.
     *
     * @param lista lista de objetos producto obtenidos del DAO.
     * @return cadena de texto con el arreglo JSON de productos.
     */
    public String productosAJson(ArrayList<producto> lista) {
        // usamos stringbuilder para construir la cadena de texto de forma eficiente
        StringBuilder json = new StringBuilder();
        json.append("["); 

        // recorremos la lista de objetos producto para mapear sus campos
        for (int i = 0; i < lista.size(); i++) {
            producto p = lista.get(i);

            json.append("{");
            json.append("\"id\":").append(p.getIdProductoPk()).append(",");
            json.append("\"id_categoria_pk\":").append(p.getIdCategoriaFk()).append(",");
            json.append("\"nombre\":\"").append(escaparTexto(p.getNombreProducto())).append("\",");
            json.append("\"descripcion\":\"").append(escaparTexto(p.getDescripcion())).append("\",");
            json.append("\"precio\":").append(p.getPrecio()).append(",");
            json.append("\"stock\":").append(p.getStock()).append(",");
            json.append("\"estado\":").append(p.isEstado()).append(",");
            json.append("\"categoria\":\"").append(p.getCategoria() != null ? p.getCategoria() : "Sin categoria").append("\",");
            json.append("\"proveedorMarca\":\"").append(p.getProveedorMarca() != null ? escaparTexto(p.getProveedorMarca()) : "DMari Oficial").append("\",");
            json.append("\"id_proveedor_fk\":").append(p.getIdProveedorFk() > 0 ? p.getIdProveedorFk() : "null").append(",");
            
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

    /**
     * Convierte una lista de categorias a formato JSON.
     * Solo incluye el id y el nombre de cada categoria,
     * que es lo que necesita el frontend para los menus desplegables.
     *
     * @param lista lista de objetos categoria.
     * @return cadena JSON con las categorias.
     */
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

    /**
     * Convierte una lista de etiquetas a formato JSON.
     * Incluye el id y el nombre de cada etiqueta para los filtros de busqueda
     * y los formularios de creacion de productos.
     *
     * @param lista lista de objetos etiqueta.
     * @return cadena JSON con las etiquetas.
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

    /**
     * Convierte una lista de detalles de pedidos a formato JSON.
     * Incluye la fecha, estado, motivo de cancelacion, nombre del producto,
     * proveedor, cantidad, precio unitario y subtotal de cada linea.
     * Si el registro tiene nombre de cliente (vista admin o proveedor), tambien lo agrega.
     *
     * @param lista lista de objetos detallePedido obtenidos del DAO.
     * @return cadena JSON con el historial de pedidos.
     */
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
            json.append("\"producto\":\"").append(dp.getNombreProducto() != null ? escaparTexto(dp.getNombreProducto()) : "").append("\",");
            json.append("\"proveedor\":\"").append(dp.getNombreProveedor() != null ? escaparTexto(dp.getNombreProveedor()) : "DMari Oficial").append("\",");
            json.append("\"contacto\":\"").append(dp.getContactoProveedor() != null ? escaparTexto(dp.getContactoProveedor()) : "").append("\",");
            json.append("\"cantidad\":").append(dp.getCantidad()).append(",");
            json.append("\"precio\":").append(dp.getPrecioUnitario()).append(",");
            json.append("\"subtotal\":").append(dp.getSubtotal());
            
            if (dp.getNombreCliente() != null) {
                json.append(",\"cliente\":\"").append(escaparTexto(dp.getNombreCliente())).append("\"");
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