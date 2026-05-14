// Asegúrate de que tenga el "export" al inicio
export async function cargarVistaLogin() {
    // inyectamos el html de login en el main
    await cargarComponente('component-main', './src/views/login/login.html');
    
    // iniciamos la logica del formulario
    prepararFormularioLogin();
}
