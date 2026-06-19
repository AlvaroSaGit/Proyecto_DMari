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
    
    // llave secreta para que mysql mezcle la contrasena y sea ilegible
    private static final String LLAVE_SECRETA = "llave_dmari";

    /**
     * registra un usuario nuevo en tres tablas simultaneamente: 
     * usuario, correo y credenciales (encriptando la clave).
     * 
     * @param nuevoUsuario usuario: objeto con los datos digitados en el registro.
     * @return int: el ID del usuario generado si fue exitoso, o 0 si falló.
     */
    public int registrarUsuario(usuario nuevoUsuario) {
        // consulta para la tabla principal de usuario.
        // Ahora acepta nombre, apellido y el rol dinámicamente.
        String sqlUsuario = "INSERT INTO usuario (nombre, apellido, id_rol_fk, estado_cuenta) VALUES (?, ?, ?, 1)";
        
        // consulta para insertar el correo vinculado al usuario
        // vincula el id del usuario recien creado con su direccion de email principal
        String sqlCorreo = "INSERT INTO correo (id_usuario_fk, correo, correo_primario) VALUES (?, ?, 1)";
        
        // aes_encrypt es un comando nativo de mysql que convierte el texto en codigo ilegible (formato binario blob). 
        // sin la llave secreta, es matematicamente imposible revertirlo a texto plano.
        // guarda la contraseña de forma segura en la tabla credenciales
        String sqlCredenciales = "INSERT INTO credenciales (id_usuario, passwd_encript) VALUES (?, AES_ENCRYPT(?, ?))";
        
        Connection con = null;
        try {
            con = db.conectar();
            // setautocommit(false) pausa el guardado automatico de mysql. inicia una "transaccion".
            // esto es vital: si la tabla 'correo' falla, podemos cancelar la tabla 'usuario' para no dejar registros huerfanos.
            // apagamos el autoguardado para iniciar una transaccion manual
            // desactivamos el autoguardado para iniciar una transaccion manual.
            // esto es vital: si la tabla correo falla, podemos cancelar la tabla usuario para no dejar registros huerfanos.
            con.setAutoCommit(false);
            
            int idGenerado = 0;
            
            // return_generated_keys le ordena a mysql que, despues de insertar el usuario, nos devuelva el numero de id autoincrementable que acaba de crear.
            // insertamos el usuario y pedimos que nos devuelva el id creado por mysql
            // insertamos el usuario y pedimos que nos devuelva el id autoincrementable que acaba de crear.
            try (PreparedStatement psUsuario = con.prepareStatement(sqlUsuario, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psUsuario.setString(1, nuevoUsuario.getNombre());
                psUsuario.setString(2, nuevoUsuario.getApellido());
                psUsuario.setInt(3, nuevoUsuario.getIdRol());
                psUsuario.executeUpdate();
                try (ResultSet rs = psUsuario.getGeneratedKeys()) {
                    // condicional: verifica si mysql le otorgo un id unico al usuario.
                    // si mysql genero el id, lo guardamos para las siguientes tablas
                    // si mysql genero el id, lo guardamos para usarlo en las siguientes tablas.
                    if (rs.next()) idGenerado = rs.getInt(1);
                }
            }
            
            // condicional: solo continua si el usuario principal se guardo con exito.
            // si el usuario se creo bien, procedemos con los datos secundarios
            // solo continua si el usuario principal se guardo con exito.
            if (idGenerado > 0) {
                // guardamos el correo vinculado al id del usuario
                // insertamos el correo vinculado al id del usuario.
                try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo)) {
                    psCorreo.setInt(1, idGenerado);
                    psCorreo.setString(2, nuevoUsuario.getCorreo());
                    psCorreo.executeUpdate();
                }
                
                // guardamos la credencial encriptada
                // insertamos la credencial encriptada usando la llave secreta.
                try (PreparedStatement psCred = con.prepareStatement(sqlCredenciales)) {
                    psCred.setInt(1, idGenerado);
                    psCred.setString(2, nuevoUsuario.getPassword()); 
                    psCred.setString(3, LLAVE_SECRETA); 
                    psCred.executeUpdate();
                }
                
                // commit es la orden final que le dice a mysql: "todo salio perfecto, aplica los cambios definitivamente".
                // si llegamos aqui sin errores, confirmamos todos los cambios en la base de datos
                // confirmamos que todo salio perfecto y aplicamos los cambios definitivamente.
                con.commit(); 
                return idGenerado;
            }
            
            // rollback es el boton de panico. si no se genero id, deshace cualquier insert que se haya hecho en este intento.
            // si no hubo id, cancelamos cualquier cambio previo por seguridad
            // si no se genero id, cancelamos cualquier cambio previo por seguridad.
            con.rollback(); 
            return 0;
            
        } catch (SQLException e) {
            // en caso de fallo, intentamos deshacer lo que se haya alcanzado a insertar
            // en caso de fallo, deshacemos lo que se haya alcanzado a insertar.
            try { if (con != null) con.rollback(); } catch (SQLException ex) {
                System.err.println("Fallo critico al intentar hacer rollback en registro: " + ex.getMessage());
                System.err.println("fallo critico al intentar hacer rollback: " + ex.getMessage());
            }
            System.err.println("\n=== Error critico al registrar ===");
            System.err.println("Motivo: " + e.getMessage());
            System.err.println("==================================\n");
            System.err.println("error al registrar: " + e.getMessage());
            return 0;
        } finally {
            // cerramos los recursos para liberar memoria del servidor
            try { 
                if (con != null) {
                    // devolvemos el autocommit a su estado original
                    con.setAutoCommit(true); 
                    // usamos el helper para cerrar la conexion de forma segura
                    db.cerrar(con); 
                }
            } catch (SQLException e) {
                System.err.println("error al cerrar conexion: " + e.getMessage());
            }
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
        // el inner join con correo permite buscar por email (que es unico)
        // el inner join con credenciales permite acceder al blob encriptado para compararlo
        // la condicion aes_decrypt(..., llave) = ? es la que valida la contraseña en el motor de la base de datos
        // cast(aes_decrypt(...) as char) convierte el resultado binario del desencriptado
        // a texto plano (varchar) antes de compararlo con el parametro string que java envia.
        // sin este cast, la comparacion entre blob y varchar puede fallar silenciosamente.
        String sql = "SELECT u.id_usuario_pk, u.nombre, u.apellido, u.id_rol_fk, c.correo, u.estado_cuenta " +
                     "FROM usuario u " +
                     "INNER JOIN correo c ON u.id_usuario_pk = c.id_usuario_fk " +
                     "INNER JOIN credenciales cr ON u.id_usuario_pk = cr.id_usuario " +
                     "WHERE c.correo = ? AND CAST(AES_DECRYPT(cr.passwd_encript, ?) AS CHAR) = ?";
                     
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
                    usuarioLogueado.setApellido(rs.getString("apellido"));
                    usuarioLogueado.setCorreo(rs.getString("correo"));
                    usuarioLogueado.setIdRol(rs.getInt("id_rol_fk"));
                    usuarioLogueado.setEstadoCuenta(rs.getBoolean("estado_cuenta"));
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error al verificar login: " + e.getMessage());
        }
        
        return usuarioLogueado;
    }

    /**
     * comprueba si un correo electronico ya esta registrado en el sistema.
     * se usa para dar mensajes de error mas precisos en el login.
     * 
     * @param correo string: el email que el usuario intenta usar.
     * @return boolean: true si el correo ya existe en la tabla correo.
     */
    public boolean existeCorreo(String correo) {
        String sql = "SELECT 1 FROM correo WHERE correo = ?";
        
        try (Connection con = db.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // devuelve true si encontro el correo
            }
            
        } catch (SQLException e) {
            System.out.println("Error al validar existencia de correo: " + e.getMessage());
            return false;
        }
    }

    /**
     * extrae todos los usuarios registrados en el sistema (panel de administracion).
     * 
     * @return arraylist<usuario>: lista completa con roles, correos y estados.
     */
    public ArrayList<usuario> listarUsuarios() {
        ArrayList<usuario> lista = new ArrayList<>();
        // cruzamos la tabla usuario con el correo usando left join (por si algun usuario no tiene correo registrado)
        // selecciona los datos basicos de identidad y el estado de la cuenta (activo/bloqueado)
        // el left join asegura que el usuario aparezca en la lista incluso si hubo un error al guardar su correo
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
        } catch (SQLException e) { System.out.println("Error al listar usuarios: " + e.getMessage()); }
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
        // actualiza el nivel de acceso (rol) y la bandera de estado de cuenta
        // permite al admin habilitar o suspender el acceso de cualquier usuario
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
            // insert ignore evita errores si el usuario ya tenia un perfil de proveedor previo
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
            try { if (con != null) con.rollback(); } catch (SQLException ex) {
                System.err.println("Fallo critico al intentar hacer rollback en actualizacion de permisos: " + ex.getMessage());
            }
            System.out.println("Error al actualizar permisos: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {
                System.err.println("Error al cerrar la conexion en actualizacion de permisos: " + e.getMessage());
            }
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
        // usa aes_encrypt para sobreescribir la contraseña nueva con la llave de seguridad
        // la clausula where incluye la validacion de la contraseña anterior para mayor seguridad
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
        } catch (SQLException e) { System.out.println("Error al cambiar password: " + e.getMessage()); return false; }
    }

    /**
     * registra una solicitud para que un usuario existente se convierta en proveedor.
     * guarda los datos comerciales en la tabla de solicitudes sin cambiar el rol aun.
     * 
     * @param idUsuario int: el id del usuario que hace la peticion.
     * @param nit String: identificacion tributaria de la empresa.
     * @param marca String: nombre comercial.
     * @param cuenta String: numero de cuenta bancaria.
     * @param banco String: nombre de la entidad financiera.
     * @param tipo String: ahorros o corriente.
     * @return boolean: true si la solicitud se guardo correctamente.
     */
    public boolean registrarSolicitudProveedor(int idUsuario, String nit, String marca, String cuenta, String banco, String tipo) {
        // consulta para insertar los datos comerciales de la peticion
        String sqlSol = "insert into solicitud_proveedor (id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta, estado_solicitud) values (?, ?, ?, ?, ?, ?, 'pendiente')";
        // consulta para pausar la cuenta del usuario (estado_cuenta = 0)
        String sqlPausar = "update usuario set estado_cuenta = 0 where id_usuario_pk = ?";
        
        Connection con = null;
        try {
            con = db.conectar();
            // iniciamos transaccion para asegurar que si falla el bloqueo no se cree la solicitud
            con.setAutoCommit(false);
            
            // paso 1: registrar la peticion en la tabla de solicitudes
            try (PreparedStatement psSol = con.prepareStatement(sqlSol)) {
                psSol.setInt(1, idUsuario);
                psSol.setString(2, nit);
                psSol.setString(3, marca);
                psSol.setString(4, cuenta);
                psSol.setString(5, banco);
                psSol.setString(6, tipo);
                psSol.executeUpdate();
            }
            
            // paso 2: pausar la cuenta del usuario inmediatamente
            try (PreparedStatement psPause = con.prepareStatement(sqlPausar)) {
                psPause.setInt(1, idUsuario);
                psPause.executeUpdate();
            }
            
            // confirmamos ambos cambios
            con.commit();
            return true;
        } catch (SQLException e) {
            // si algo falla revertimos para que el usuario no quede bloqueado sin solicitud
            try { if (con != null) con.rollback(); } catch (SQLException ex) {
                System.err.println("fallo critico en rollback de solicitud: " + ex.getMessage());
            }
            System.out.println("error al procesar solicitud y pausa de cuenta: " + e.getMessage());
            return false;
        } finally {
            // restauramos el estado de la conexion y cerramos
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }

    /**
     * el administrador aprueba la peticion del usuario.
     * este proceso mueve los datos de la solicitud a la tabla de proveedor,
     * cambia el rol del usuario a 4 y reactiva su cuenta (estado 1).
     * 
     * @param idSolicitud int: identificador de la peticion a procesar.
     * @return boolean: true si toda la operacion atomica fue exitosa.
     */
    public boolean aprobarSolicitudProveedor(int idSolicitud) {
        // sql para obtener los datos de la solicitud antes de moverlos
        String sqlGet = "select id_usuario_fk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta from solicitud_proveedor where id_solicitud_pk = ?";
        // sql para actualizar el estado de la solicitud
        String sqlUpdateSol = "update solicitud_proveedor set estado_solicitud = 'aprobada' where id_solicitud_pk = ?";
        // sql para activar al usuario y subirlo a rol proveedor (4)
        String sqlUpdateUser = "update usuario set id_rol_fk = 4, estado_cuenta = 1 where id_usuario_pk = ?";
        // sql para insertar o actualizar el perfil comercial definitivo
        String sqlInsertProv = "insert into proveedor (id_proveedor_pk, nit_empresa, nombre_marca, cuenta_bancaria, banco_nombre, tipo_cuenta) values (?, ?, ?, ?, ?, ?) on duplicate key update nit_empresa = values(nit_empresa), nombre_marca = values(nombre_marca)";

        Connection con = null;
        try {
            con = db.conectar();
            con.setAutoCommit(false);

            int idUser = 0;
            String nit = "", marca = "", cuenta = "", banco = "", tipo = "";

            // paso 1: recuperamos los datos comerciales de la solicitud
            try (PreparedStatement psGet = con.prepareStatement(sqlGet)) {
                psGet.setInt(1, idSolicitud);
                try (ResultSet rs = psGet.executeQuery()) {
                    if (rs.next()) {
                        idUser = rs.getInt("id_usuario_fk");
                        nit = rs.getString("nit_empresa");
                        marca = rs.getString("nombre_marca");
                        cuenta = rs.getString("cuenta_bancaria");
                        banco = rs.getString("banco_nombre");
                        tipo = rs.getString("tipo_cuenta");
                    }
                }
            }

            if (idUser > 0) {
                // paso 2: marcamos la solicitud como aprobada
                try (PreparedStatement psSol = con.prepareStatement(sqlUpdateSol)) {
                    psSol.setInt(1, idSolicitud);
                    psSol.executeUpdate();
                }
                // paso 3: activamos el acceso al sistema con el nuevo rol
                try (PreparedStatement psUser = con.prepareStatement(sqlUpdateUser)) {
                    psUser.setInt(1, idUser);
                    psUser.executeUpdate();
                }
                // paso 4: creamos el perfil oficial de proveedor
                try (PreparedStatement psProv = con.prepareStatement(sqlInsertProv)) {
                    psProv.setInt(1, idUser);
                    psProv.setString(2, nit);
                    psProv.setString(3, marca);
                    psProv.setString(4, cuenta);
                    psProv.setString(5, banco);
                    psProv.setString(6, tipo);
                    psProv.executeUpdate();
                }
                con.commit();
                return true;
            }
            con.rollback();
            return false;
        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) {}
            System.out.println("error al aprobar proveedor: " + e.getMessage());
            return false;
        } finally {
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (SQLException e) {}
        }
    }
}