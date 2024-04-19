package envio_comprobantes_amdc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Con_DB {
    //Inicio Variables

    private static String computadoraResidente = ""; //computadora residente
    private static String usuario = "";
    private static String contra = "";
    private String baseActiva = "";
    /**
     * La conexion con la base de datos
     */
    Connection conexion;
    public final String CONTROLADOR_JDBC = "com.mysql.cj.jdbc.Driver"; //Para la conexion 

    public Con_DB() {

        String ruta = "";
        File midir = new File(".");
        try {
            ruta = midir.getCanonicalPath();

        } catch (IOException e) {
        }

        String rutaArchivo = ruta + "\\src\\configuraciones\\config_DB.cnf";
        File cnf = new File(rutaArchivo);
        //Validar que el archivo existe
        if (!cnf.exists()) {

        } else {

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

            TripleDes tDES = new TripleDes();

            String txtDecifrado = tDES.desEncriptar_txt_3DES(textoCifrado);

            String datos[] = txtDecifrado.split("#&&#");

            computadoraResidente = datos[0] + ":" + datos[1];
            baseActiva = datos[2];
            usuario = datos[3];
            contra = datos[4];
        }
    }

    /**
     * Valída si el usuario y la contaseña son correctas.
     *
     * @param usuario
     * @param password
     * @param tabla
     * @param nombreCampoUsuario
     * @param nombreCampoPassword
     * @return Retorna true si y solo si el usuario y la contraseña conciden,
     * false si no conciden.
     *
     */
    public boolean autenticado(String usuario, String password, String tabla, String nombreCampoUsuario, String nombreCampoPassword) {
        boolean respuesta = false;
        String select = "SELECT " + nombreCampoUsuario + ", " + nombreCampoPassword + " "
                + "FROM " + tabla + " "
                + "WHERE " + nombreCampoUsuario + " = '" + usuario + "' "
                + "AND " + nombreCampoPassword + " = '" + password + "' ";
        ResultSet lista = this.ejecutarSELECT(select);

        try {

            while (lista.next()) {
                respuesta = true;
            }
        } catch (Exception e) {

        } finally {
//            try {
//                //conexion.close();
//            } catch (SQLException ex) {
//                
//            }
        }

        return respuesta;
    }

    /**
     * Establece la conexión con la base de datos despues de ser leida la
     * configuración de conexión.
     *
     * @return verdadero si esta conectado o falso si no esta conectado.
     */
    public boolean conectar() {

        if (conexion != null) {
            return true;
        } else {
            try {
                Class.forName(CONTROLADOR_JDBC);
                conexion = DriverManager.getConnection("jdbc:mysql://" + computadoraResidente + "/" + baseActiva, usuario, contra);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ocurrió Un error al conectar\n" + e.getMessage(), "Atencion",
                        JOptionPane.WARNING_MESSAGE);
                conexion = null;
                return false;
            }
        }
        return true;

    }

    public boolean probarConexion(String urlDB, String puertoDB, String baseDB, String usuarioDB, String passDB) {

        if (conexion != null) {
            return true;
        } else {
            try {
                Class.forName(CONTROLADOR_JDBC);
                conexion = DriverManager.getConnection("jdbc:mysql://" + urlDB + ":" + puertoDB + "/" + baseDB, usuarioDB, passDB);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Ocurrió Un error al probar la conexión\n" + e.getMessage(), "Atencion",
                        JOptionPane.WARNING_MESSAGE);
                conexion = null;
                return false;
            }
        }

        this.cierraConexion();
        return true;

    }

    /**
     * Recibe una sentencia de SELECT en SQL y la ejecuta
     *
     * @param sql
     * @return Un objeto ResultSet
     * @throws SQLException
     */
    public ResultSet ejecutarSELECT(String sql) {
        ResultSet tabla = null;

        try {
            // Se crea un Statement, para realizar la consulta
            Statement instruccion = conexion.createStatement();

            tabla = instruccion.executeQuery(sql);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Ocurrió Un error\n" + e.getMessage(), "Atencion",
                    JOptionPane.WARNING_MESSAGE);

        }

        return tabla;

    }

    /**
     * Actualiza los campos en la tabla y la condición establecidos Sin
     * Confirmacion.
     *
     * @param tabla
     * @param campos
     * @param criterio
     * @return
     */
    public boolean actualizarRegistro(String tabla, String campos, String criterio) {
        int resultado = 0;
        boolean resp = false;
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement();
            // Se realiza la consulta.
            resultado = s.executeUpdate("update " + tabla + " set " + campos + " where " + criterio);

        } catch (SQLException e) {
            //e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Ocurrió Un error\n" + e.getMessage(), "Atención!!",
                    JOptionPane.WARNING_MESSAGE);
            resp = false;
        }
        if (resultado > 0) {
            resp = true;
        }
        return resp;
    }

    /**
     * Inserta un registro a la base da datos si mensaje de confirmación.
     *
     * @param tabla
     * @param campos
     * @param valores
     * @return
     */
    public boolean insertarRegistro(String tabla, String campos, String valores) {
        int resultado;

        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement();
            // Se realiza la consulta.
            resultado = s.executeUpdate("insert into " + tabla + " (" + campos + ") values (" + valores + ")");

        } catch (Exception e) {

            JOptionPane.showMessageDialog(null, "Ocurrio Un error\n" + e.getMessage(), "Atencion",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Cierra la conexión con la base de datos
     */
    public void cierraConexion() {
        try {
            conexion.close();
        } catch (Exception e) {
        }
    }

}
