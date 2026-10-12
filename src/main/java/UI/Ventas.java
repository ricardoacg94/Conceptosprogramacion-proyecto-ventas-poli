package UI;

import javax.swing.SwingUtilities;

/**
 * Clase de apoyo para lanzar la interfaz grafica de usuario.
 * No es un punto de entrada principal del proyecto; los programas
 * ejecutables son GenerateInfoFiles y main.
 */
public class Ventas {

    public static void lanzarInterfaz() {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
