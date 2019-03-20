/*

 * @Nombre    : Envio_Comprobantes_AMDC
 * @Author    : Erick Rodriguez
 * @Copyright : Erick Rodriguez
 * @Creado el : 14-ago-2017, 09:27:01 AM
 */
package envio_comprobantes_amdc;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Erick R. Rodriguez
 */
public class Envio_Comprobantes_AMDC {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
            mostrarSplash();
//              mostrarLogIn();
            
    }
    
     public static void mostrarSplash() {
        Splash f = new Splash();
        int w = 610;
        int h = 480;
        f.setMinimumSize(new Dimension(w, h));
        f.setMaximumSize(new Dimension(w, h));
        f.setResizable(false);
        f.setTitle("Iniciando...");
        
        //obtener el tamaño de la pantalla
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determinar la posición del inicio de pantalla
        int x = (dim.width - w) / 2;//Ancho de pantalla menos ancho de ventana dividido entre 2
        int y = (dim.height - h) / 2;//alto de pantalla menos alto de ventana dividido entre 2

        // Asignar la ventana
        f.setLocation(x, y);

//        f.setAlwaysOnTop(true);
        //this.setEnabled(false);
//        try {
//            f.getRootPane().setOpaque(false);
//            f.getContentPane().setBackground(new Color(0, 0, 0, 0));
//            //f.setBackground(new Color(0, 0, 0, 0));
//        } catch (Exception e) {
//        }
        f.setVisible(true);
        Splash.llenarBarra();
        esperar(300);
        
        f.dispose();
        
    }
     
     private static void esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
        }
        if (configuracionValida()) {
            //mostrarSplash();
            mostrarLogIn();
        } else {
            mostrarConfiguracion();
        }

    }

    public static void mostrarLogIn() {
        LogIn f = new LogIn();//623, 359
        int w = 623;
        int h = 359;
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

    public static void mostrarConfiguracion() {
        Configuracion f = new Configuracion();
        int w = 485;
        int h = 600;
        f.setMinimumSize(new Dimension(w, h));
        f.setMaximumSize(new Dimension(w, h));
        f.setResizable(false);
        f.setTitle("Configuración");

        //obtener el tamaño de la pantalla
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

        // Determinar la posición del inicio de pantalla
        int x = (dim.width - w) / 2;//Ancho de pantalla menos ancho de ventana dividido entre 2
        int y = (dim.height - h) / 2;//alto de pantalla menos alto de ventana dividido entre 2

        // Asignar la ventana
        f.setLocation(x, y);

        f.setVisible(true);
    }

    private static boolean configuracionValida() {

        String ruta = "";
        File midir = new File(".");
        try {
            ruta = midir.getCanonicalPath();

        } catch (IOException e) {
        }

        String rutaArchivo = ruta + "\\src\\configuraciones\\config_DB.cnf";
        File cnf = new File(rutaArchivo);
        
        String rutaCarpeta = ruta + "\\src\\configuraciones";
        
        File carpeta = new File(rutaCarpeta);
        
        //Validar que la carpeta existe sino crearla
        if(!carpeta.exists()){
            carpeta.mkdirs();
            return false;
        }
        
        //Validar que el archivo existe
        if (!cnf.exists()) {
            return false;
        }

        //validar que contiene parametros guardados
        FileReader fr;
        String texto = "";
        String textoCifrado = "";
        try {
            fr = new FileReader(rutaArchivo);

            BufferedReader br = new BufferedReader(fr);

            while ((texto = br.readLine()) != null) {
                //System.out.println(texto);
                textoCifrado = textoCifrado + texto;
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Envio_Comprobantes_AMDC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Envio_Comprobantes_AMDC.class.getName()).log(Level.SEVERE, null, ex);
        }

        if(textoCifrado.length() == 0){
            return false;
        }
        
        TripleDes tDES = new TripleDes();
        if (!tDES.validar_txt_3DES(textoCifrado)) {
            return false;
        }

        String txtDecifrado = tDES.desEncriptar_txt_3DES(textoCifrado);

        String datos[] = txtDecifrado.split("#&&#");

        if (datos.length < 5) {
            return false;
        }

        

        return true;
    }
}
