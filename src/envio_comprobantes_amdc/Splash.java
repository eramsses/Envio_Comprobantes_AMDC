/*

 * @Nombre    : Splash
 * @Author    : Erick Rodriguez
 * @Copyright : Erick Rodriguez
 * @Creado el : 09-mar-2019, 01:42:12 AM
 */
package envio_comprobantes_amdc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.Timer;

/**
 *
 * @author Erick R. Rodríguez
 */
public class Splash extends javax.swing.JFrame {

    /**
     * Creates new form Splash
     */
    public Splash() {
        //initComponents();
        
        //Visual_frame es el nombre del JFrame principal
        Splash.this.setUndecorated(true);
        initComponents();
        //Con esto hacemos que el JFrame no se vea 
        //Sino puedes quitarlo.
        Splash.this.setBackground(new Color(0, 0, 0, 0));
        //Con esto le damos tranparencia a los objetos que tiene el JFrame
        //Tambien al JFrame, desde 0.0f a 1.0f 
        Splash.this.setOpacity(0.92f);

        lblImgSplash.setBackground(new Color(0, 0, 0, 0));
        setVersion();
        llenarBarra();
    }
    
    @Override
    public Image getIconImage() {
        Image retValue = Toolkit.getDefaultToolkit().
                getImage(ClassLoader.getSystemResource("Imagenes/correo.png"));

        return retValue;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pBar_inicio = new javax.swing.JProgressBar();
        lblImgSplash = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Iniciando...");
        setIconImage(getIconImage());
        getContentPane().setLayout(null);

        pBar_inicio.setBackground(new java.awt.Color(49, 66, 82));
        pBar_inicio.setForeground(new java.awt.Color(255, 153, 0));
        pBar_inicio.setValue(10);
        pBar_inicio.setBorderPainted(false);
        pBar_inicio.setOpaque(true);
        getContentPane().add(pBar_inicio);
        pBar_inicio.setBounds(120, 360, 380, 6);

        lblVerSplash.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblVerSplash.setForeground(new java.awt.Color(153, 153, 153));
        lblVerSplash.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblVerSplash.setText("Versión 4.0.1");
        getContentPane().add(lblVerSplash);
        lblVerSplash.setBounds(200, 140, 250, 30);

        lblImgSplash.setBackground(new java.awt.Color(102, 102, 102));
        lblImgSplash.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblImgSplash.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Imagenes/Splash_Vouchers_hex_gen.png"))); // NOI18N
        lblImgSplash.setMaximumSize(new java.awt.Dimension(610, 480));
        lblImgSplash.setMinimumSize(new java.awt.Dimension(610, 480));
        lblImgSplash.setOpaque(true);
        lblImgSplash.setPreferredSize(new java.awt.Dimension(610, 480));
        lblImgSplash.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblImgSplashMouseClicked(evt);
            }
        });
        getContentPane().add(lblImgSplash);
        lblImgSplash.setBounds(0, 0, 610, 480);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void lblImgSplashMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblImgSplashMouseClicked
        // TODO add your handling code here:
//        dispose();
//        mostrarLogIn();
    }//GEN-LAST:event_lblImgSplashMouseClicked

    public static void mostrarLogIn() {
        LogIn f = new LogIn();
        int w = 400;
        int h = 182;
        f.setMinimumSize(new Dimension(w, h));
        f.setMaximumSize(new Dimension(w, h));
        f.setResizable(false);
        f.setTitle("Inicio de Sesión");

        //obtener el tamaño de la pantalla
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determinar la posición del inicio de pantalla
        int x = (dim.width - w) / 2;//Ancho de pantalla menos ancho de ventana dividido entre 2
        int y = (dim.height - h) / 2;//alto de pantalla menos alto de ventana dividido entre 2

        // Asignar la ventana
        f.setLocation(x, y);

        f.setVisible(true);
    }

    public static void llenarBarra() 
    { 
        int i = 0; 
        try { 
            while (i <= 100) { 
                // fill the menu bar 
                pBar_inicio.setValue(i); 
  
                // delay the thread 
                Thread.sleep(100); 
                i += 5; 
            } 
        } 
        catch (Exception e) { 
        } 
    } 
    
    private void setVersion() {
        String ruta = "";
        File midir = new File(".");
        try {
            ruta = midir.getCanonicalPath();

        } catch (IOException e) {
        }

        String rutaArchivo = ruta + "\\src\\configuraciones\\version.cnf";
        File ver = new File(rutaArchivo);
        //Validar que el archivo existe
        if (!ver.exists()) {
            try {
                ver.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Configuracion.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            FileWriter fw;

                try {

                    fw = new FileWriter(ver);
                    try (BufferedWriter bw = new BufferedWriter(fw)) {
                        PrintWriter salida = new PrintWriter(bw);
                        salida.write("4.0.1");

                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Error al guardar configuración:\n" + e.getMessage(), "ERROR",
                            JOptionPane.ERROR_MESSAGE);
                }
        } 
            
            FileReader fr;
            String texto;
            String txtVersion = "";
            try {
                fr = new FileReader(rutaArchivo);

                BufferedReader br = new BufferedReader(fr);

                while ((texto = br.readLine()) != null) {
                    //System.out.println(texto);
                    txtVersion = txtVersion + texto;
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Envio_Comprobantes_AMDC.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Envio_Comprobantes_AMDC.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        Splash.lblVerSplash.setText("Versión " + txtVersion);

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Splash.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Splash().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblImgSplash;
    public static final javax.swing.JLabel lblVerSplash = new javax.swing.JLabel();
    private static javax.swing.JProgressBar pBar_inicio;
    // End of variables declaration//GEN-END:variables
}
