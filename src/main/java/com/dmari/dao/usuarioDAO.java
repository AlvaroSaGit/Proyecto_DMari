/*
    objetivo de este archivo:
    data access object (dao) que se encarga de las consultas sql relacionadas 
    con los usuarios. maneja comandos avanzados de mysql como transacciones 
    seguras, extraccion de ids autogenerados y funciones nativas de encriptacion 
    (aes_encrypt / aes_decrypt) para proteger las credenciales.
*/
package com.dmari.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.dmari.helper.databaseHelper;
import com.dmari.modelo.usuario;

public class usuarioDAO {
    
    databaseHelper db = new databaseHelper();
    
    // llave secreta que mysql utilizara como algoritmo para mezclar la contrasena
    private static final String LLAVE_SECRETA = "llave_dmari";

    /**
     * registra un usuario nuevo en tres tablas simultaneamente: 
     * usuario, correo y credenciales (encriptando la clave).
     * 
     * @param nuevoUsuario usuario: objeto con los datos digitados en el registro.
     * @return boolean: true si las 3 inserciones tuvieron exito, false si fallo.
     */
    public boolean registrarUsuario(usuario nuevoUsuario) {
        
        String sqlUsuario = "INSERT INTO usuario (nombre, id_rol_fk, estado_cuenta) VALUES (?, (SELECT id_rol_pk FROM rol WHERE tipo_rol = 'cliente' LIMIT 1), 1)";
        String sqlCorreo = "INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES (?, ?, 1)";
        
        // aes_encrypt es un comando nativo de mysql que convierte el texto en codigo ilegible (formato binario blob). 
        // sin la llave secreta, es matematicamente imposible revertirlo a texto plano.
        String sqlCredenciales = "INSERT INTO credenciales (id_usuario, passwd_encript) VALUES (?, AES_ENCRYPT(?, ?))";
        
        Connection con = null;
        try {
            con = db.conectar();
            // setautocommit(false) pausa el guardado automatico de mysql. inicia una "transaccion".
            // esto es vital: si la tabla 'correo' falla, podemos cancelar la tabla 'usuario' para no dejar registros huerfanos.
            con.setAutoCommit(false);
            
            int idGenerado = 0;
            
            // return_generated_keys le ordena a mysql que, despues de insertar el usuario, nos devuelva el numero de id autoincrementable que acaba de crear.
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, nuevoUsuario.getNombre());
                psUsuario.executeUpdate();
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    // condicional: verifica si mysql le otorgo un id unico al usuario.
                    if (rs.next()) idGenerado = rs.getInt(1);
                }
            }
            
            // condicional: solo continua si el usuario principal se guardo con exito.
            if (idGenerado > 0) {
                try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo)) {
                    psCorreo.setInt(1, idGenerado);
                    psCorreo.setString(2, nuevoUsuario.getCorreo());
                    psCorreo.executeUpdate();
                }
                
                try (PreparedStatement psCred = con.prepareStatement(sqlCredenciales)) {
                    psCred.setInt(1, idGenerado);
                    psCred.setString(2, nuevoUsuario.getPassword()); 
                    psCred.setString(3, LLAVE_SECRETA); 
                    psCred.executeUpdate();
                }
                
                // commit es la orden final que le dice a mysql: "todo salio perfecto, aplica los cambios definitivamente".
                con.commit(); 
                return true;
            }
            
            // rollback es el boton de panico. si no se genero id, deshace cualquier insert que se haya hecho en este intento.
            con.rollback(); 
            return false;
            
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.err.println("\n=== error critico al registrar ===");
            System.err.println("Motivo: " + e.getMessage());
            System.err.println("==================================\n");
            return false;
        } finally {
            try { 
                if (con != null) {
                    // se debe restaurar el comportamiento normal de la conexion antes de devolverla a la memoria
                    con.setAutoCommit(true); 
                    con.close(); 
                }
            } catch (SQLException e) {}
        }
    }

    /**
     * verifica si las credenciales coinciden con la base de datos usando aes_decrypt.
     * 
     * @param correo string: email digitado por el visitante.
     * @param password string: contrasena digitada en texto plano.
     * @return usuario: objeto con los datos del usuario si acerto, null si fallo.
     */
    public usuario verificarLogin(String correo, String password) {
        
        // aes_decrypt hace el proceso inverso: usa la llave secreta para destrabar el blob y lo compara con el texto digitado.
        // traemos tambien el estado_cuenta para validarlo desde java y poder darle un mensaje especifico al usuario
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.id_rol_fk, c.correo, u.estado_cuenta " +
                     "FROM usuario u " +
                     "INNER JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "INNER JOIN credenciales cr ON u.id_usuario_pk = cr.id_usuario " +
                     "WHERE c.correo = ? AND AES_DECRYPT(cr.passwd_encript, ?) = ?";
                     
        usuario usuarioLogueado = null;
        
        try (Connection con = db.conectar();
            PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            ps.setString(2, LLAVE_SECRETA);
            ps.setString(3, password);
            
            try (ResultSet rs = ps.executeQuery()) {
                // condicional: si el cursor avanza, encontro coincidencias exactas.
                // si no avanza, significa que el correo no existe o la clave esta mal.
                if (rs.next()) {
                    usuarioLogueado = new usuario();
                    usuarioLogueado.setIdUsuario(rs.getInt("id_usuario_pk"));
                    usuarioLogueado.setNombre(rs.getString("nombre"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    usuarioLogueado.setIdRol(rs.getInt("id_rol_fk"));
                    usuarioLogueado.setEstadoCuenta(rs.getBoolean("estado_cuenta"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("error al verificar login: " + e.getMessage());
        }
        
        return usuarioLogueado;
    }

    /**
     * extrae todos los usuarios registrados en el sistema (panel de administracion).
     * 
     * @return arraylist<usuario>: lista completa con roles, correos y estados.
     */
    public ArrayList<usuario> listarUsuarios() {
        ArrayList<usuario> lista = new ArrayList<>();
        // cruzamos la tabla usuario con el correo usando left join (por si algun usuario no tiene correo registrado)
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.apellido, u.id_rol_fk, u.estado_cuenta, c.correo " +
                     "FROM usuario u " +
                     "LEFT JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk";
                     
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            // iteracion: lee todos los usuarios y los empaqueta en objetos java
            while(rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario_pk"));
                u.setNombre(rs.getString("nombre"));
                u.setApellido(rs.getString("apellido")); 
                u.setIdRol(rs.getInt("id_rol_fk"));
                u.setCorreo(rs.getString("correo"));
                u.setEstadoCuenta(rs.getBoolean("estado_cuenta"));
                lista.add(u);
            }
        } catch (SQLException e) { System.out.println("error al listar usuarios: " + e.getMessage()); }
        return lista;
    }

    /**
     * actualiza los privilegios de un empleado o bloquea a un usuario malicioso.
     * incluye un disparador logico para crear perfiles comerciales automaticamente.
     * 
     * @param idUsuario int: identificador de la cuenta a afectar.
     * @param idRol int: nuevo numero de rol asignado (ej: 4 para proveedor).
     * @param estadoCuenta boolean: true para habilitar, false para suspender.
     * @return boolean: true si el cambio se guardo con exito.
     */
    public boolean actualizarPermisos(int idUsuario, int idRol, boolean estadoCuenta) {
        String sql = "UPDATE usuario SET id_rol_fk = ?, estado_cuenta = ? WHERE id_usuario_pk = ?";
        Connection con = null;
        try {
            con = db.conectar();
            // apagamos el autocommit para proteger toda la transaccion
            con.setAutoCommit(false); 
            
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, idRol);
                ps.setBoolean(2, estadoCuenta);
                ps.setInt(3, idUsuario);
                ps.executeUpdate();
            }
            
            // ARREGLO: Si el usuario es ascendido a Proveedor (rol 4), inicializamos su perfil comercial
            // Esto garantiza que proveedor_producto funcione perfectamente cuando intente crear un producto.
            // condicional: evalua si el nuevo rol otorgado es especificamente "proveedor".
            if (idRol == 4) {
                String sqlProv = "INSERT IGNORE INTO proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) VALUES (?, '000000000', 'Mi Tienda', '0000', 'Banco', 'Ahorros')";
                try (PreparedStatement psProv = con.prepareStatement(sqlProv)) {
                    psProv.setInt(1, idUsuario);
                    psProv.executeUpdate();
                }
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("Error al actualizar permisos: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /**
     * permite al usuario cambiar su clave, forzando una doble verificacion.
     * 
     * @param idUsuario int: dueno de la cuenta.
     * @param passwordActual string: clave vieja que debe coincidir con mysql.
     * @param nuevaPassword string: clave a encriptar y guardar.
     * @return boolean: true si se logro el cambio, false si la clave actual era incorrecta.
     */
    public boolean cambiarPassword(int idUsuario, String passwordActual, String nuevaPassword) {
        // la instruccion update solo hara el cambio si desencriptar (aes_decrypt) la clave actual coincide con la que digito el usuario
        String sql = "UPDATE credenciales SET passwd_encript = AES_ENCRYPT(?, ?) WHERE id_usuario = ? AND AES_DECRYPT(passwd_encript, ?) = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevaPassword);
            ps.setString(2, LLAVE_SECRETA);
            ps.setInt(3, idUsuario);
            ps.setString(4, LLAVE_SECRETA);
            ps.setString(5, passwordActual);
            
            // devuelve true si al menos 1 fila fue modificada (lo que confirma que la contrasena antigua era correcta)
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.out.println("error al cambiar password: " + e.getMessage()); return false; }
    }
}