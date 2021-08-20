/*

 * @Nombre    : ThreadProcesos
 * @Author    : Erick Rodriguez
 * @Copyright : Erick Rodriguez
 * @Creado el : 15-ago-2017, 02:58:58 PM
 */
package envio_comprobantes_amdc;

import static envio_comprobantes_amdc.Principal.cmbAnio;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/**
 *
 * @author Erick R. Rodriguez
 */
public class ThreadProcesos extends Thread {

    private final String funcion;

    private boolean cuentaAtrasActivo;
    private boolean tiempoTranscurridoActivo;
    private boolean cuentaAtrasActivoCH;
    private boolean tiempoTranscurridoActivoCH;

    private String urlLogoIzq;
    private String urlLogoDer;

    private String tituloLinea_1;
    private String tituloLinea_2;
    private String tituloLinea_3;

    public ThreadProcesos(String funcion) {
        this.funcion = funcion;
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

        if (this.funcion.equals("llenarAnios")) {
            llenarAnios();
        } else if (this.funcion.equals("getCorreosDiponibles")) {
            getCorreosDiponibles();
        } else if (this.funcion.equals("getCorreosDiponiblesCH")) {
            getCorreosDiponiblesCH();
        } else if (this.funcion.equals("llenarMeses")) {
            llenarMeses();
        } else if (this.funcion.equals("iniciarEnvio")) {
            recuperarUrlLogos();
            boolean ac = Principal.chkAcuerdo.isSelected();
            boolean ch = Principal.chkContratoPorHora.isSelected();
            if (ac) {
                iniciarEnvio(ch);
            } else if (ch) {
                try {
                    iniciarEnvioCH();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        //iniciarEnvio
    }

    private void recuperarUrlLogos() {

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

            this.urlLogoIzq = datos[5];
            this.urlLogoDer = datos[6];
            this.tituloLinea_1 = datos[7];
            this.tituloLinea_2 = datos[8];
            this.tituloLinea_3 = datos[9];

        }
    }

    private void llenarAnios() {
        Con_DB db = new Con_DB();
        db.conectar();
        String sql = "SELECT DISTINCT anio FROM planillas ORDER BY anio DESC";
        ResultSet RSanios = db.ejecutarSELECT(sql);
        cmbAnio.removeAllItems();
        boolean hayAnios = false;
        try {
            while (RSanios.next()) {
                cmbAnio.addItem(RSanios.getString("anio"));
                hayAnios = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);

        } finally {

        }

        if (!hayAnios) {
            String sql_ch = "SELECT DISTINCT anio FROM ch_planillas ORDER BY anio DESC";
            ResultSet RSanios_ch = db.ejecutarSELECT(sql_ch);
            cmbAnio.removeAllItems();

            try {
                while (RSanios_ch.next()) {
                    cmbAnio.addItem(RSanios_ch.getString("anio"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);

            } finally {

            }
        }

        db.cierraConexion();
        llenarMeses();
    }

    private void llenarMeses() {
        String anio = cmbAnio.getSelectedItem().toString();
        if (anio.length() > 0) {
            Con_DB db = new Con_DB();
            db.conectar();

            String sql = "SELECT DISTINCT mes FROM planillas WHERE anio = " + anio + " ORDER BY mes DESC";
            ResultSet RSmeses = db.ejecutarSELECT(sql);
            Principal.cmbMes.removeAllItems();

            boolean hayMeses = false;
            try {
                while (RSmeses.next()) {
                    Principal.cmbMes.addItem(this.mes2text(RSmeses.getString("mes")));
                    hayMeses = true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!hayMeses) {
                String sql_ch = "SELECT DISTINCT mes FROM ch_planillas WHERE anio = " + anio + " ORDER BY mes DESC";
                ResultSet RSmeses_ch = db.ejecutarSELECT(sql_ch);
                Principal.cmbMes.removeAllItems();

                try {
                    while (RSmeses_ch.next()) {
                        Principal.cmbMes.addItem(this.mes2text(RSmeses_ch.getString("mes")));

                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            db.cierraConexion();
            if (Principal.chkAcuerdo.isSelected()) {
                getCorreosDiponibles();
            }

            if (Principal.chkContratoPorHora.isSelected()) {
                getCorreosDiponiblesCH();
            }

            Principal.cargado = true;
        }
    }

    private void getCorreosDiponibles() {
        String mes = this.mesTxt2Numero(Principal.cmbMes.getSelectedItem().toString());
        String anio = cmbAnio.getSelectedItem().toString();
        //limpiar etiquetas
        Principal.pbrEnviados.setToolTipText("");
        Principal.lblPorcentaje.setText(" Cargando...");
        Principal.pbrEnviados.setMaximum(100);
        Principal.pbrEnviados.setValue(0);
        Principal.lblCorreosDisponibles.setText("");

        Con_DB db = new Con_DB();
        db.conectar();
        String sqlID = "SELECT id FROM planillas WHERE anio = " + anio + " AND mes = " + mes + "";
        ResultSet rsID = db.ejecutarSELECT(sqlID);

        String idPlanilla = "0";
        try {
            while (rsID.next()) {
                idPlanilla = rsID.getString("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        //Recuperar las identidades presentes en la planilla

        String sqlIdentidadesPlanilla = "SELECT DISTINCT lp.identidad, p.codigo_empleado AS codigo, p.correo "
                + "FROM  lineas_planilla AS lp, personas AS p "
                + "WHERE lp.planillas_id = " + idPlanilla + " "
                + "AND p.identidad = lp.identidad "
                + "AND p.correo <> '' "
                + "ORDER BY p.correo ASC";

        ResultSet rsIdentidadesPlanilla = db.ejecutarSELECT(sqlIdentidadesPlanilla);

        double correosDisponibles = 0;
        String correosEmpleados = "";
        int filas = 0;
        try {
            rsIdentidadesPlanilla.last();

            filas = rsIdentidadesPlanilla.getRow();
            rsIdentidadesPlanilla.first();

            Principal.pbrEnviados.setMaximum(filas);
//            while (rsIdentidadesPlanilla.next()) {
//                correosEmpleados = rsIdentidadesPlanilla.getString("correo");
//                if (correosEmpleados.trim().length() > 0) {
//                    correosDisponibles++;
//                    System.out.println(correosDisponibles);
//                    
//                }
//            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Recuperar la cantidad correos que ya fueron enviados
        String sqlCorreosEnviados = "SELECT COUNT(*) AS enviados "
                + "FROM est_correos_enviados AS ce "
                + "WHERE ce.id_planilla =" + idPlanilla + "";
        ResultSet rsCorreosEnviados = db.ejecutarSELECT(sqlCorreosEnviados);

        String correosEnviados = "0";
        try {
            while (rsCorreosEnviados.next()) {
                correosEnviados = rsCorreosEnviados.getString("enviados");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (Integer.parseInt(correosEnviados) > filas) {
            correosEnviados = "" + filas;
        }

        double cEnviados = Double.parseDouble(correosEnviados);

        Principal.pbrEnviados.setValue((int) cEnviados);

        double porcentaje = (cEnviados / (int) filas) * 100;

        DecimalFormat format = new DecimalFormat("###.#");
        String porcentajeTxt = format.format(porcentaje);
        Principal.pbrEnviados.setToolTipText(porcentajeTxt + " %");
        Principal.lblPorcentaje.setText(porcentajeTxt + " %");

        Principal.lblCorreosDisponibles.setText(correosEnviados + "/" + filas);

        db.cierraConexion();
    }

    private void getCorreosDiponiblesCH() {
        String mes = this.mesTxt2Numero(Principal.cmbMes.getSelectedItem().toString());
        String anio = cmbAnio.getSelectedItem().toString();
        //limpiar etiquetas
        Principal.pbrEnviadosCH.setToolTipText("");
        Principal.lblPorcentajeCH.setText(" Cargando...");
        Principal.pbrEnviadosCH.setMaximum(100);
        Principal.pbrEnviadosCH.setValue(0);
        Principal.lblCorreosDisponiblesCH.setText("");
        Principal.lblPlanillasEncontradasCH.setText("Cargando..");

        Con_DB db = new Con_DB();
        db.conectar();
        String sqlPlanillas = "SELECT id FROM ch_planillas WHERE anio = " + anio + " AND mes = " + mes + "";
        ResultSet rsPlanillas = db.ejecutarSELECT(sqlPlanillas);

        String idPlanilla;
        int planillas = 0;
        int contratosCorreoEnviados = 0;
        int contratosCorreosDisponibles = 0;
        try {
            while (rsPlanillas.next()) {
                idPlanilla = rsPlanillas.getString("id");

                //Por cada planilla encontrada recuperar los contratos de los empleados que tienen correo
                String sqlContratosPlanilla = "SELECT DISTINCT infp.id AS id_info, infp.num_contrato_ch, p.correo, p.nombres, p.apellidos "
                        + "FROM ch_info_ctrato_planilla AS infp, c_personas AS p "
                        + "WHERE infp.id_planilla_ch = " + idPlanilla + " "
                        + "AND p.id = infp.id_persona_ch "
                        + "AND p.correo <> '' "
                        + "ORDER BY p.correo ASC";

                ResultSet rsContratosPlanilla = db.ejecutarSELECT(sqlContratosPlanilla);

                rsContratosPlanilla.last();
                contratosCorreosDisponibles += rsContratosPlanilla.getRow();
                rsContratosPlanilla.first();

                //Recuperar la cantidad correos que ya fueron enviados en la planilla
                String sqlCorreosEnviados = "SELECT COUNT(*) AS enviados "
                        + "FROM est_correos_enviados_ch AS ce, ch_info_ctrato_planilla AS infp "
                        + "WHERE ce.id_info_contratos = infp.id "
                        + "AND infp.id_planilla_ch = " + idPlanilla + " ";
                ResultSet rsCorreosEnviados = db.ejecutarSELECT(sqlCorreosEnviados);

                try {
                    while (rsCorreosEnviados.next()) {
                        contratosCorreoEnviados += rsCorreosEnviados.getInt("enviados");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }

                planillas++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        Principal.lblPlanillasEncontradasCH.setText("Planillas encontradas " + planillas);

        if (planillas > 0) {
            if (contratosCorreoEnviados > contratosCorreosDisponibles) {
                contratosCorreoEnviados = contratosCorreosDisponibles;
            }
            Principal.pbrEnviadosCH.setMaximum(contratosCorreosDisponibles);

            Principal.pbrEnviadosCH.setValue((int) contratosCorreoEnviados);

            double porcentaje = (contratosCorreoEnviados / (int) contratosCorreosDisponibles) * 100;

            DecimalFormat format = new DecimalFormat("###.#");
            String porcentajeTxt = format.format(porcentaje);
            Principal.pbrEnviadosCH.setToolTipText(porcentajeTxt + " %");
            Principal.lblPorcentajeCH.setText(porcentajeTxt + " %");

            Principal.lblCorreosDisponiblesCH.setText(contratosCorreoEnviados + "/" + contratosCorreosDisponibles);
        } else {
            Principal.pbrEnviadosCH.setMaximum(contratosCorreosDisponibles);

            Principal.pbrEnviadosCH.setValue(0);

            Principal.pbrEnviadosCH.setToolTipText("0 %");
            Principal.lblPorcentajeCH.setText("0 %");

            Principal.lblCorreosDisponiblesCH.setText("0/0");
        }
        db.cierraConexion();
    }

    private void iniciarEnvio(boolean continuar) {
        //Leer configuración de envío
        String mes = this.mesTxt2Numero(Principal.cmbMes.getSelectedItem().toString());
        String mesFull = mes;
        if (mes.length() < 2) {
            mesFull = "0" + mes;
        }

        Principal.lblEstado.setForeground(Color.WHITE);
        String anio = cmbAnio.getSelectedItem().toString();
        String mesSel = Principal.cmbMes.getSelectedItem().toString();

        int maximoDeEnvios = Integer.parseInt(Principal.valMaxCorreos.getModel().getValue().toString());
        int minutosDeEspera = Integer.parseInt(Principal.tiempoEspera.getModel().getValue().toString());

        //limpiar etiquetas
        Principal.pbrEnviados.setToolTipText("");
        Principal.lblPorcentaje.setText(" Cargando...");
        Principal.pbrEnviados.setMaximum(100);
        Principal.pbrEnviados.setValue(0);
        Principal.lblCorreosDisponibles.setText("");
        Principal.lblTiempoTrancurrido.setText("00:00:00");

        //Inhabilitar controles de configuración de envío
        Principal.cmbAnio.setEnabled(false);
        Principal.cmbMes.setEnabled(false);
        Principal.valMaxCorreos.setEnabled(false);
        Principal.tiempoEspera.setEnabled(false);
        Principal.btnIniciarEnvio.setEnabled(false);
        Principal.chkAcuerdo.setEnabled(false);
        Principal.chkContratoPorHora.setEnabled(false);
        Principal.enviando = true;

        //iniciar reloj de tiempo transcurrido
        tiempoTranscurridoActivo = true;
        Thread hiloReloj = new TiempoTranscurrido();
        hiloReloj.start();

        Con_DB db = new Con_DB();
        db.conectar();
        String sqlID = "SELECT id FROM planillas WHERE anio = " + anio + " AND mes = " + mes + "";

        Principal.lblEstado.setText("Recuperando identificador de planilla");

        ResultSet rsID = db.ejecutarSELECT(sqlID);

        String idPlanilla = "0";
        try {
            while (rsID.next()) {
                idPlanilla = rsID.getString("id");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Recuperar la cantidad correos que ya fueron enviados
        double cEnviados = 0;
        String sqlCorreosEnviados = "SELECT COUNT(*) AS enviados "
                + "FROM est_correos_enviados AS ce "
                + "WHERE ce.id_planilla =" + idPlanilla + "";
        ResultSet rsCorreosEnviados = db.ejecutarSELECT(sqlCorreosEnviados);

        String correosEnviados = "0";
        try {
            while (rsCorreosEnviados.next()) {
                correosEnviados = rsCorreosEnviados.getString("enviados");
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
        cEnviados = Double.parseDouble(correosEnviados);

        //Recuperar las identidades presentes en la planilla
        String sqlIdentidadesPlanilla = "SELECT DISTINCT lp.identidad, p.codigo_empleado AS codigo, CONCAT_WS(' ', p.nombres, p.apellidos ) AS nombres , p.correo, "
                + "u.codigo_unidad, u.nombre AS depto, lp.id_cargo, c.nombre AS cargo "
                + "FROM  lineas_planilla AS lp, personas AS p, empleados AS e, cargos AS c, unidades AS u "
                + "WHERE lp.planillas_id = " + idPlanilla + " "
                + "AND p.identidad = lp.identidad "
                + "AND e.identidad_persona = lp.identidad "
                + "AND e.cargos_id = c.id "
                + "AND e.cargos_id = lp.id_cargo "
                + "AND c.unidades_id = u.id "
                + "AND p.correo <> '' "
                + "AND lp.id_cargo <> '' "
                + "ORDER BY u.codigo_unidad, p.correo ASC";

        Principal.lblEstado.setText("Recuperando correos disponibles en la planilla");

        //System.out.println(sqlIdentidadesPlanilla);
        ResultSet rsIdentidadesPlanilla = db.ejecutarSELECT(sqlIdentidadesPlanilla);

        String correoEmpleado;
        String depto;
        String codDepto;
        String codEmpleado;
        String nombreEmpleado;
        String cargoEmpleado;
        String identidad;
        DecimalFormat format = new DecimalFormat("###.#");
        DecimalFormat formatMoneda = new DecimalFormat("#,###,##0.00");

        int enviadosPorCiclo = 0;

        int filas = 0;
        try {
            rsIdentidadesPlanilla.last();

            filas = rsIdentidadesPlanilla.getRow();
            rsIdentidadesPlanilla.beforeFirst();

            Principal.lblCorreosDisponibles.setText((int) cEnviados + "/" + filas);

            Principal.pbrEnviados.setMaximum(filas);
            Principal.pbrEnviosCiclo.setMaximum(maximoDeEnvios);

            //ID de aguinaldos y RS para aguinaldos en caso de los meses 06 y 12
            String sqlAguinaldos = "SELECT * FROM aguinaldos WHERE mes = '" + mesFull + "' AND anio = '" + anio + "'";

            String idAguinaldo = "0";
            if (mes.equals("6") || mes.equals("12")) {

                ResultSet rsAguinaldos = db.ejecutarSELECT(sqlAguinaldos);

                while (rsAguinaldos.next()) {
                    idAguinaldo = rsAguinaldos.getString("id");
                }

            }

            //Recuperar la configuracion de conexion del correo y el mensaje
            //datos de conexion y mensaje
            String mensaje = "";

            String smtpServer = "";
            String userNameMailRS = "";
            String passwordMailRS = "";
            String asunto;
            String puerto = "";
            String tls = "";

            String sqlCnfGral = "SELECT mensaje_voucher FROM cnf_general";
            String sqlCnfMail = "SELECT * FROM cnf_mail";

            ResultSet rsCnfGral = db.ejecutarSELECT(sqlCnfGral);

            ResultSet rsCnfMail = db.ejecutarSELECT(sqlCnfMail);

            Principal.lblEstado.setText("Recuperando información de conexión");
            while (rsCnfGral.next()) {
                mensaje = rsCnfGral.getString("mensaje_voucher");
            }

            while (rsCnfMail.next()) {
                smtpServer = rsCnfMail.getString("smtp_server");
                userNameMailRS = rsCnfMail.getString("correo");
                passwordMailRS = rsCnfMail.getString("pass");
                puerto = rsCnfMail.getString("puerto");

                if (rsCnfMail.getInt("aplicar_tls") == 1) {
                    tls = "true";
                } else {
                    tls = "false";
                }
            }

            final String userNameMail = userNameMailRS;
            final String passwordMail = passwordMailRS;

            //Iniciar la conexión al servidor de correos
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", tls);
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.port", puerto);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userNameMail, passwordMail);
                }
            });

            while (rsIdentidadesPlanilla.next()) {
                correoEmpleado = rsIdentidadesPlanilla.getString("correo");
                nombreEmpleado = rsIdentidadesPlanilla.getString("nombres");
                codDepto = rsIdentidadesPlanilla.getString("codigo_unidad");
                depto = rsIdentidadesPlanilla.getString("depto");
                codEmpleado = rsIdentidadesPlanilla.getString("codigo");
                cargoEmpleado = rsIdentidadesPlanilla.getString("cargo");
                identidad = rsIdentidadesPlanilla.getString("identidad");
                boolean enviado = false;
                boolean errorEnvio = false;
                if (correoEmpleado.trim().length() > 0) {
                    //Verificar que no se ha enviado todavía
                    String sqlVefificarEnviado = "SELECT * FROM est_correos_enviados WHERE identidad = '" + identidad + "' AND id_planilla = " + idPlanilla;
                    ResultSet rsVefificarEnviado = db.ejecutarSELECT(sqlVefificarEnviado);
                    while (rsVefificarEnviado.next()) {
                        enviado = true;
                    }

                    if (!enviado) {
                        Principal.lblEstado.setText("Enviando a: " + nombreEmpleado + " > " + correoEmpleado + " En: " + codDepto + " >> " + depto);
                        //System.out.println(enviadosPorCiclo + " > Enviando a: " + nombreEmpleado + " > " + correoEmpleado + " En: " + codDepto + " >> " + depto);

                        //Preparar y enviar el correo
                        //datos de los aguinaldos
                        ResultSet rsCreditosAguinaldos = null;
                        ResultSet rsDebitosAguinaldos = null;
                        if (mes.equals("6") || mes.equals("12")) {
                            String sqlCreditosAguinaldos = "SELECT la.valor AS valor, la.descripcion AS descripcion "
                                    + "FROM  lineas_aguinaldo AS la "
                                    + "WHERE la.aguinaldos_id = " + idAguinaldo + " "
                                    + "AND la.identidad = '" + identidad + "' "
                                    + "AND la.tipo = 'Credito' "
                                    + "ORDER BY la.t_borrar";

                            String sqlDebitosAguinaldos = "SELECT la.valor AS valor, la.descripcion AS descripcion "
                                    + "FROM  lineas_aguinaldo AS la "
                                    + "WHERE la.aguinaldos_id = " + idAguinaldo + " "
                                    + "AND la.identidad = '" + identidad + "' "
                                    + "AND la.tipo = 'Debito' "
                                    + "ORDER BY la.t_borrar";

                            rsCreditosAguinaldos = db.ejecutarSELECT(sqlCreditosAguinaldos);
                            rsDebitosAguinaldos = db.ejecutarSELECT(sqlDebitosAguinaldos);

                        }

                        String sqlCreditosPlanilla = "SELECT lp.valor AS valor, lp.descripcion AS descripcion "
                                + "FROM  lineas_planilla AS lp "
                                + "WHERE lp.planillas_id = " + idPlanilla + " "
                                + "AND lp.identidad = '" + identidad + "' "
                                + "AND lp.tipo = 'Credito' "
                                + "ORDER BY lp.t_borrar";
                        String sqlDebitosPlanilla = "SELECT lp.valor AS valor, lp.descripcion AS descripcion "
                                + "FROM  lineas_planilla AS lp "
                                + "WHERE lp.planillas_id = " + idPlanilla + " "
                                + "AND lp.identidad = '" + identidad + "' "
                                + "AND lp.tipo = 'Debito' "
                                + "ORDER BY lp.t_borrar";

                        ResultSet rsCreditosPlanilla = db.ejecutarSELECT(sqlCreditosPlanilla);
                        ResultSet rsDebitosPlanilla = db.ejecutarSELECT(sqlDebitosPlanilla);

                        //Variables acumuladoras
                        double totaldebito = 0.00;
                        double totaldebitoMes = 0.00;
                        double totaling = 0.00;
                        double subTotAguin = 0.00;
                        double subTotDedAguin = 0.00;
                        double subTotMes = 0.00;
                        double subTotDedMes = 0.00;

                        //Crear el voucher
                        String voucher = "<html><head></head><body><div style=\"margin-top: 0px; font-family: arial; font-size: 11px;\">"
                                + "<div>"
                                + "<table border=\"0\" width=\"1\" cellspacing=\"1\" cellpadding=\"0\" style=\"width: 100%;\">"
                                + "<tbody>"
                                + "<tr>"
                                + "<td>";
                        if (this.urlLogoIzq.length() > 0) {
                            voucher += "<img width=\"40\" alt=\"escudo_hn2\" src=\"" + this.urlLogoIzq + "\"/>";
                        }
                        voucher += "</td>"
                                + "<td style=\"text-align: center; font-size: 12px;\">"
                                + "<p><span style='font-size: 16px;'><b>" + this.tituloLinea_1 + "</b></span><br><span style='font-size: 13px;'><b>" + this.tituloLinea_2 + "</b></span><br><span style='font-size: 12px;'><b>" + this.tituloLinea_3 + "</b></span><br>Comprobante de Pago de " + mesSel + " de " + anio + "</p>"
                                + "</td>"
                                + "<td style = \"text-align: right;\">";
                        if (this.urlLogoDer.length() > 0) {
                            voucher += "<img width = \"40\" alt = \"Logo\" src=\"" + this.urlLogoDer + "\"/>";
                        }
                        voucher += "</td>"
                                + "</tr>"
                                + "</tbody>"
                                + "</table>"
                                + "</div>"
                                + "<center>"
                                + "<div>"
                                + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">"
                                + "<tr>"
                                + "<th style=\"width: 55px; text-align: left;\">NOMBRE</th>"
                                + "<td>" + nombreEmpleado + "</td>"
                                + "<th style=\"width: 55px; text-align: left;\">DEPENDENCIA</th>"
                                + "<td>" + depto + "</td>"
                                + "</tr>"
                                + "<tr>"
                                + "<th style=\"width: 55px; text-align: left;\"><b>IDENTIDAD</b></th>"
                                + "<td>" + identidad + "</td>"
                                + "<th style=\"width: 55px; text-align: left;\">CARGO</th>"
                                + "<td>" + cargoEmpleado + "</td>"
                                + "</tr>"
                                + "<tr>"
                                + "<th style=\"width: 55px; text-align: left;\"><b>CÓDIGO</b></th>"
                                + "<td>" + codEmpleado + "</td>"
                                + "<th style=\"width: 55px; text-align: left;\"></th>"
                                + "<td></td>"
                                + "</tr>"
                                + "</table>"
                                + "<div style=\"border-left: 2px #000 solid; border-right: 2px #000 solid; border-top: 2px #000 solid; border-bottom: 2px #000 solid; margin-top: 10px; margin-right: 2px; margin-left: 2px;\">"
                                + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%; margin-top: 10px;\">"
                                + "<tr>"
                                + "<th  style=\"width:40%; text-align: center; font-family: arial; font-size: 15px;\">Deducciones</th>"
                                + "<th style=\"width:20%;\"></th>"
                                + "<th  style=\"width:40%; text-align: center; font-family: arial; font-size: 15px;\">Ingresos</th>"
                                + "</tr>"
                                + "<tr>"
                                + "<td  style=\"width:40%; vertical-align: bottom;\">"
                                + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">";

                        //Agregar los debitos de planilla
                        if (rsDebitosPlanilla.next()) {
                            rsDebitosPlanilla.beforeFirst();

                            while (rsDebitosPlanilla.next()) {
                                double val = rsDebitosPlanilla.getDouble("valor");
                                totaldebito = totaldebito + val;
                                totaldebitoMes = totaldebitoMes + val;

                                voucher += "<tr>"
                                        + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + rsDebitosPlanilla.getString("descripcion") + "</th>"
                                        + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                        + "</tr>";
                            }
                        }

                        //Agregar los debitos de aguinaldos en caso que aplique
                        if (rsDebitosAguinaldos != null) {
                            if (rsDebitosAguinaldos.next()) {
                                rsDebitosAguinaldos.beforeFirst();

                                while (rsDebitosAguinaldos.next()) {
                                    double val = rsDebitosAguinaldos.getDouble("valor");
                                    totaldebito = totaldebito + val;
                                    subTotDedAguin = subTotDedAguin + val;
                                    String des = rsDebitosAguinaldos.getString("descripcion");

                                    //Corregir textos
                                    if (des.equals("ANTICIPO") && mes.equals("6")) {
                                        des = "ANTICIPO DECIMO CUARTO";
                                    } else if (des.equals("ANTICIPO") && mes.equals("12")) {
                                        des = "ANTICIPO DECIMO TERCER";
                                    }
                                    if (val > 0.00) {
                                        voucher += "<tr>"
                                                + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + des + "</th>"
                                                + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                                + "</tr>";
                                    }
                                }
                            }
                        }

                        voucher += "<tr>"
                                + "<th>Total Deducciones</th>"
                                + "<td style=\"text-align: right; border-top: 1px #000 solid;\"><span style=\"text-align: left; float: left;\">L. </span>" + formatMoneda.format(totaldebito) + "</td>"
                                + "</tr>"
                                + "</table>"
                                + "</td>"
                                + "<th style=\"width:20%;\"></th>"
                                + "<td  style=\"width:40%; vertical-align: bottom;\">"
                                + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">";

                        //Agregar los creditos de planilla
                        if (rsCreditosPlanilla.next()) {
                            rsCreditosPlanilla.beforeFirst();

                            while (rsCreditosPlanilla.next()) {
                                double val = rsCreditosPlanilla.getDouble("valor");
                                totaling = totaling + val;
                                subTotMes = subTotMes + val;

                                voucher += "<tr>"
                                        + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + rsCreditosPlanilla.getString("descripcion") + "</th>"
                                        + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                        + "</tr>";

                            }
                        }

                        //Agregar los creditos de aguinaldos en caso que aplique
                        if (rsCreditosAguinaldos != null) {
                            if (rsCreditosAguinaldos.next()) {
                                rsCreditosAguinaldos.beforeFirst();

                                while (rsCreditosAguinaldos.next()) {
                                    double val = rsCreditosAguinaldos.getDouble("valor");
                                    totaling = totaling + val;
                                    subTotAguin = subTotAguin + val;
                                    String des = rsCreditosAguinaldos.getString("descripcion");

                                    //Corregir textos
                                    if (des.equals("AGUINALDO") && mes.equals("6")) {
                                        des = "DECIMO CUARTO";
                                    }

                                    if (des.equals("BONO AGUINALDO") && mes.equals("6")) {
                                        des = "RECARGO DECIMO CUARTO";
                                    }

                                    if (des.equals("AGUINALDO") && mes.equals("12")) {
                                        des = "DECIMO TERCER";
                                    }

                                    if (des.equals("BONO AGUINALDO") && mes.equals("12")) {
                                        des = "RECARGO DECIMO TERCER";
                                    }

                                    if (val > 0.00) {
                                        voucher += "<tr>"
                                                + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + des + "</th>"
                                                + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                                + "</tr>";
                                    }
                                }
                            }
                        }

                        voucher += "<tr>"
                                + "<th>Total de Ingresos</th>"
                                + "<td style=\"text-align: right; border-top: 1px #000 solid;\"><span style=\"text-align: left; float: left;\">L. </span>" + formatMoneda.format(totaling) + "</td>"
                                + "</tr>"
                                + "</table>"
                                + "</td>"
                                + "</tr>"
                                + "<tr >"
                                + "<th colspan=\"2\" style=\"border-top: 2px #000 solid;margin-top: 40px; text-align: left;\">Neto a Pagar en el Mes</th>"
                                + "<td style=\"text-align: right; font-size: 13px; border-top: 2px #000 solid;margin-top: 40px;\">"
                                + "<table border=\"0\" width=\"0\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">"
                                + "<tr>"
                                + "<th>"
                                + "</th>"
                                + "<td style=\"text-align: right;  width: 105px;\">"
                                + "<span style=\"text-align: left; float: left;\">L. </span>"
                                + formatMoneda.format((subTotMes - totaldebitoMes))
                                + "</td>"
                                + "</tr>"
                                + "</table>"
                                + "</td>"
                                + "</tr>";

                        if (rsCreditosAguinaldos != null) {
                            rsCreditosAguinaldos.beforeFirst();
                            if (rsCreditosAguinaldos.next()) {

                                voucher += "<tr>"
                                        + "<th colspan=\"2\" style=\"border-top: 2px #000 solid;margin-top: 40px; text-align: left;\">Neto a Pagar en el Aguinaldo</th>"
                                        + "<td style=\"text-align: right; font-size: 13px; border-top: 2px #000 solid;margin-top: 40px;\">"
                                        + "<table border=\"0\" width=\"0\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">"
                                        + "<tr>"
                                        + "<th>"
                                        + "</th>"
                                        + "<td style=\"text-align: right;  width: 105px;\">"
                                        + "<span style=\"text-align: left; float: left;\">L. </span>"
                                        + formatMoneda.format((subTotAguin - subTotDedAguin))
                                        + "</td>"
                                        + "</tr>"
                                        + "</table>"
                                        + "</td>"
                                        + "</tr>";

                            }

                        }

                        voucher += "</table>"
                                + "</div>"
                                + "</div>"
                                + "</center>"
                                + "<div style=\"text-align: left;margin-top: 10px; font-family: arial; font-size: 11px;\">"
                                + mensaje
                                + "</div>"
                                + "</div><br><br></body></html>";

                        Principal.txtVoucher.setContentType("text/html");
                        Principal.txtVoucher.setText(voucher);

                        //enviar correo SMTP
                        //Recuperar configuración de conexion
                        asunto = codEmpleado + " Comprobante de Pago de " + mesSel + " de " + anio;

                        boolean registrarEnvio = true;

                        try {
                            Message message = new MimeMessage(session);
                            message.setFrom(new InternetAddress(userNameMail));
                            message.setRecipients(Message.RecipientType.TO,
                                    InternetAddress.parse(correoEmpleado));
                            message.setSubject(asunto);
                            message.setContent(voucher, "text/html");//setText(Mensage);

                            Transport.send(message);
                            //JOptionPane.showMessageDialog(this, "Su mensaje ha sido enviado");
                            //Control y rotulación
                            enviadosPorCiclo++;

                            cEnviados++;

                            Principal.pbrEnviados.setValue((int) cEnviados);
                            Principal.pbrEnviosCiclo.setValue((int) enviadosPorCiclo);

                            double porcentaje = (cEnviados / (int) filas) * 100;

                            String porcentajeTxt = format.format(porcentaje);
                            Principal.pbrEnviados.setToolTipText(porcentajeTxt + " %");
                            Principal.lblPorcentaje.setText(porcentajeTxt + " %");

                            Principal.lblCorreosDisponibles.setText((int) cEnviados + "/" + filas);

                        } catch (MessagingException e) {
                            registrarEnvio = false;
                            if (e.getMessage().contains("Invalid Addresses")) {
                                errorEnvio = true;
                            }
                            Principal.lblEstado.setText("ERROR: " + e.getMessage());
                            TimeUnit.SECONDS.sleep(5);
//                            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR",
//                                    JOptionPane.ERROR_MESSAGE);
                            //throw new RuntimeException(e);
                        }
                        //Registrar que ya fue enviado
                        if (registrarEnvio) {
                            String tabla = "est_correos_enviados";
                            String campos = "identidad, id_planilla, enviado";
                            String valores = "'" + identidad + "','" + idPlanilla + "','1'";
                            db.insertarRegistro(tabla, campos, valores);
                        }
                        //Revisar si entra en pausa
                        if (enviadosPorCiclo >= maximoDeEnvios || errorEnvio) {
                            enviadosPorCiclo = 0;

                            if (errorEnvio) {
                                rsIdentidadesPlanilla.previous();
                            }
                            Principal.txtVoucher.setText("");
                            Principal.lblContinuara.setText("Continuará en:");
                            cuentaAtrasActivo = true;
                            Thread hiloCuentaAtras = new CuentaRegresiva(minutosDeEspera);
                            hiloCuentaAtras.start();

                            Principal.lblEstado.setText("Esperando para continuar ...");
                            TimeUnit.MINUTES.sleep(minutosDeEspera);

                            cuentaAtrasActivo = false;
                            Principal.pbrEnviosCiclo.setValue((int) enviadosPorCiclo);

                            //Iniciar la session nuevamente
                            session = Session.getInstance(props,
                                    new javax.mail.Authenticator() {
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(userNameMail, passwordMail);
                                }
                            });

                        }

                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (continuar) {
            db.cierraConexion();
            try {
                iniciarEnvioCH();
            } catch (InterruptedException ex) {
                Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            //habilitar controles de configuración de envío
            Principal.cmbAnio.setEnabled(true);
            Principal.cmbMes.setEnabled(true);
            Principal.valMaxCorreos.setEnabled(true);
            Principal.tiempoEspera.setEnabled(true);
            Principal.btnIniciarEnvio.setEnabled(true);
            Principal.chkAcuerdo.setEnabled(true);
            Principal.chkContratoPorHora.setEnabled(true);
            Principal.enviando = false;

            tiempoTranscurridoActivo = false;
            Principal.lblEstado.setForeground(Color.GREEN);
            Principal.txtVoucher.setText("");
            Principal.lblEstado.setText("Envío finalizado exitosamente!!!");
            db.cierraConexion();
        }

    }

    private void iniciarEnvioCH() throws InterruptedException {
        //Leer configuración de envío
        String mes = this.mesTxt2Numero(Principal.cmbMes.getSelectedItem().toString());
        String mesFull = mes;
        if (mes.length() < 2) {
            mesFull = "0" + mes;
        }

        Principal.lblEstado.setForeground(Color.WHITE);
        String anio = cmbAnio.getSelectedItem().toString();
        String mesSel = Principal.cmbMes.getSelectedItem().toString();

        int maximoDeEnvios = Integer.parseInt(Principal.valMaxCorreos.getModel().getValue().toString());
        int minutosDeEspera = Integer.parseInt(Principal.tiempoEspera.getModel().getValue().toString());

        //limpiar etiquetas
        Principal.pbrEnviadosCH.setToolTipText("");
        Principal.lblPorcentajeCH.setText(" Cargando...");
        Principal.pbrEnviadosCH.setMaximum(100);
        Principal.pbrEnviadosCH.setValue(0);
        Principal.lblCorreosDisponiblesCH.setText("");
        Principal.lblTiempoTrancurridoCH.setText("00:00:00");

        //Inhabilitar controles de configuración de envío
        Principal.cmbAnio.setEnabled(false);
        Principal.cmbMes.setEnabled(false);
        Principal.valMaxCorreos.setEnabled(false);
        Principal.tiempoEspera.setEnabled(false);
        Principal.btnIniciarEnvio.setEnabled(false);
        Principal.chkAcuerdo.setEnabled(false);
        Principal.chkContratoPorHora.setEnabled(false);
        Principal.enviando = true;

        //iniciar reloj de tiempo transcurrido
        tiempoTranscurridoActivoCH = true;
        Thread hiloReloj = new TiempoTranscurridoCH();
        hiloReloj.start();

        Con_DB db = new Con_DB();
        db.conectar();
        String sqlPlanillas = "SELECT id, mes, anio, tipo_planilla, num_complementaria FROM ch_planillas WHERE anio = " + anio + " AND mes = " + mes + "";
        ResultSet rsPlanillas = db.ejecutarSELECT(sqlPlanillas);

        String idPlanilla;
        int planillas = 0;
        int contratosCorreoEnviados = 0;
        int contratosCorreosDisponibles = 0;

        try {

            while (rsPlanillas.next()) {
                idPlanilla = rsPlanillas.getString("id");

                //Por cada planilla encontrada recuperar los contratos de los empleados que tienen correo
                String sqlContratosPlanilla = "SELECT DISTINCT infp.id AS id_info, infp.num_contrato_ch, p.correo, p.nombres, p.apellidos "
                        + "FROM ch_info_ctrato_planilla AS infp, c_personas AS p "
                        + "WHERE infp.id_planilla_ch = " + idPlanilla + " "
                        + "AND p.id = infp.id_persona_ch "
                        + "AND p.correo <> '' "
                        + "ORDER BY p.correo ASC";

                ResultSet rsContratosPlanilla = db.ejecutarSELECT(sqlContratosPlanilla);

                rsContratosPlanilla.last();
                contratosCorreosDisponibles += rsContratosPlanilla.getRow();
                rsContratosPlanilla.first();

                //Recuperar la cantidad correos que ya fueron enviados en la planilla
                String sqlCorreosEnviados = "SELECT COUNT(*) AS enviados "
                        + "FROM est_correos_enviados_ch AS ce, ch_info_ctrato_planilla AS infp "
                        + "WHERE ce.id_info_contratos = infp.id "
                        + "AND infp.id_planilla_ch = " + idPlanilla + " ";
                ResultSet rsCorreosEnviados = db.ejecutarSELECT(sqlCorreosEnviados);

                try {
                    while (rsCorreosEnviados.next()) {
                        contratosCorreoEnviados += rsCorreosEnviados.getInt("enviados");
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
                }

                planillas++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        Principal.lblPlanillasEncontradasCH.setText("Planillas encontradas " + planillas);

        if (planillas > 0) {
            Principal.pbrEnviadosCH.setMaximum(contratosCorreosDisponibles);

            Principal.pbrEnviadosCH.setValue((int) contratosCorreoEnviados);

            double porcentaje = (contratosCorreoEnviados / (int) contratosCorreosDisponibles) * 100;

            DecimalFormat format = new DecimalFormat("###.#");
            String porcentajeTxt = format.format(porcentaje);
            Principal.pbrEnviadosCH.setToolTipText(porcentajeTxt + " %");
            Principal.lblPorcentajeCH.setText(porcentajeTxt + " %");

            Principal.lblCorreosDisponiblesCH.setText(contratosCorreoEnviados + "/" + contratosCorreosDisponibles);
        } else {
            Principal.pbrEnviadosCH.setMaximum(contratosCorreosDisponibles);

            Principal.pbrEnviadosCH.setValue(0);

            Principal.pbrEnviadosCH.setToolTipText("0 %");
            Principal.lblPorcentajeCH.setText("0 %");

            Principal.lblCorreosDisponiblesCH.setText("0/0");
        }

        //Regresar el puntero de rsPlanillas para recorrerlo nuevamente
        try {

            //Recuperar la configuracion de conexion del correo y el mensaje
            //datos de conexion y mensaje
            String mensaje = "";

            String smtpServer = "";
            String userNameMailRS = "";
            String passwordMailRS = "";
            String asunto;
            String puerto = "";
            String tls = "";

            String sqlCnfGral = "SELECT mensaje_voucher FROM cnf_general";
            String sqlCnfMail = "SELECT * FROM cnf_mail";

            ResultSet rsCnfGral = db.ejecutarSELECT(sqlCnfGral);

            ResultSet rsCnfMail = db.ejecutarSELECT(sqlCnfMail);

            Principal.lblEstado.setText("Recuperando información de conexión");
            while (rsCnfGral.next()) {
                mensaje = rsCnfGral.getString("mensaje_voucher");
            }

            while (rsCnfMail.next()) {
                smtpServer = rsCnfMail.getString("smtp_server");
                userNameMailRS = rsCnfMail.getString("correo");
                passwordMailRS = rsCnfMail.getString("pass");
                puerto = rsCnfMail.getString("puerto");
                if (rsCnfMail.getInt("aplicar_tls") == 1) {
                    tls = "true";
                } else {
                    tls = "false";
                }
            }

            final String userNameMail = userNameMailRS;
            final String passwordMail = passwordMailRS;

            //Iniciar la conexión al servidor de correos
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", tls);
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.port", puerto);

            Session session = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userNameMail, passwordMail);
                }
            });

            rsPlanillas.beforeFirst();
            Principal.pbrEnviosCiclo.setMaximum(maximoDeEnvios);

            String id_persona_ch;
            String idInfo;
            String correoEmpleado;
            String depto;
            String codDepto;
            String codEmpleado;
            String nombreEmpleado;
            String cargoEmpleado;
            String identidad;
            String num_contrato;
            DecimalFormat format = new DecimalFormat("###.#");
            DecimalFormat formatMoneda = new DecimalFormat("#,###,##0.00");

            int enviadosPorCiclo = 0;

            while (rsPlanillas.next()) {
                Principal.lblEstado.setText("Recuperando identificador de planilla");
                idPlanilla = rsPlanillas.getString("id");

                String tipoPlan = rsPlanillas.getString("tipo_planilla").toUpperCase();

                if (tipoPlan.equals("COMPLEMENTARIA")) {
                    tipoPlan += " " + rsPlanillas.getString("num_complementaria");
                }

                //Por cada planilla encontrada recuperar los contratos de los empleados que tienen correo
                String sqlContratosPlanilla = "SELECT DISTINCT infp.id AS id_info, infp.num_contrato_ch, p.id AS id_persona_ch, "
                        + "p.correo, p.nombres, p.apellidos, "
                        + "p.identidad, p.codigo_empleado, c.nombre AS cargo, u.nombre AS unidad, u.codigo_unidad "
                        + "FROM ch_info_ctrato_planilla AS infp, c_personas AS p, unidades AS u, cargos AS c "
                        + "WHERE infp.id_planilla_ch = " + idPlanilla + " "
                        + "AND infp.id_cargo_ch = c.id "
                        + "AND c.unidades_id = u.id "
                        + "AND p.id = infp.id_persona_ch "
                        + "AND p.correo <> '' "
                        + "ORDER BY p.correo ASC";

                ResultSet rsContratosPlanilla = db.ejecutarSELECT(sqlContratosPlanilla);

                while (rsContratosPlanilla.next()) {

                    id_persona_ch = rsContratosPlanilla.getString("id_persona_ch");
                    idInfo = rsContratosPlanilla.getString("id_info");
                    correoEmpleado = rsContratosPlanilla.getString("correo");
                    nombreEmpleado = rsContratosPlanilla.getString("nombres") + " " + rsContratosPlanilla.getString("apellidos");
                    identidad = rsContratosPlanilla.getString("identidad");
                    num_contrato = rsContratosPlanilla.getString("num_contrato_ch");
                    codEmpleado = rsContratosPlanilla.getString("codigo_empleado");
                    depto = rsContratosPlanilla.getString("unidad");
                    codDepto = rsContratosPlanilla.getString("codigo_unidad");
                    cargoEmpleado = rsContratosPlanilla.getString("cargo");

                    boolean enviado = false;
                    boolean errorEnvio = false;

                    String voucher = "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">"
                            + "<html><head></head><body>";

                    if (correoEmpleado.trim().length() > 0) {
                        String sqlVefificarEnviado = "SELECT * FROM est_correos_enviados_ch WHERE id_persona_ch = '" + id_persona_ch + "' AND id_info_contratos = " + idInfo;
                        ResultSet rsVefificarEnviado = db.ejecutarSELECT(sqlVefificarEnviado);
                        while (rsVefificarEnviado.next()) {
                            enviado = true;
                        }

                        if (!enviado) {
                            Principal.lblEstado.setText("Enviando a: " + nombreEmpleado + " > " + correoEmpleado + " En: " + codDepto + " >> " + depto);

                            //Preparar y enviar el correo
                            //datos de los aguinaldos
                            String sqlCreditosPlanilla = "SELECT lp.valor, cc.aplica_imp_vecinal, cc.codigo, lp.descripcion  "
                                    + "FROM  ch_lineas_planilla AS lp, catalogo_cuentas AS cc "
                                    + "WHERE lp.id_info_planillas_ch = " + idInfo + " "
                                    + "AND lp.cuenta_id = cc.id "
                                    + "AND lp.tipo = 'Credito' "
                                    + "ORDER BY lp.t_borrar";
                            String sqlDebitosPlanilla = "SELECT lp.valor, cc.aplica_imp_vecinal, cc.codigo, lp.descripcion  "
                                    + "FROM  ch_lineas_planilla AS lp, catalogo_cuentas AS cc "
                                    + "WHERE lp.id_info_planillas_ch = " + idInfo + " "
                                    + "AND lp.cuenta_id = cc.id "
                                    + "AND lp.tipo = 'Debito' "
                                    + "ORDER BY lp.t_borrar";

                            ResultSet rsCreditosPlanilla = db.ejecutarSELECT(sqlCreditosPlanilla);
                            ResultSet rsDebitosPlanilla = db.ejecutarSELECT(sqlDebitosPlanilla);

                            //Variables acumuladoras
                            double totaldebito = 0.00;
                            double totaldebitoMes = 0.00;
                            double totaling = 0.00;
                            double subTotAguin = 0.00;
                            double subTotDedAguin = 0.00;
                            double subTotMes = 0.00;
                            double subTotDedMes = 0.00;

                            //CREAR EL VOUCHER
                            //Crear el voucher
                            voucher += "<div style=\"margin-top: 0px; font-family: arial; font-size: 11px;\">"
                                    + "<div>"
                                    + "<table border=\"0\" width=\"1\" cellspacing=\"1\" cellpadding=\"0\" style=\"width: 100%;\">"
                                    + "<tbody>"
                                    + "<tr>"
                                    + "<td>";
                            if (this.urlLogoIzq.length() > 0) {
                                voucher += "<img width=\"40\" alt=\"escudo_hn2\" src=\"" + this.urlLogoIzq + "\"/>";
                            }
                            voucher += "</td>"
                                    + "<td style=\"text-align: center; font-size: 12px;\">"
                                    + "<p><span style='font-size: 16px;'><b>" + this.tituloLinea_1 + "</b></span><br><span style='font-size: 13px;'><b>" + this.tituloLinea_2 + "</b></span><br><span style='font-size: 12px;'><b>" + this.tituloLinea_3 + "</b></span><br>Comprobante de Pago de " + mesSel + " de " + anio + "</p>"
                                    + "</td>"
                                    + "<td style = \"text-align: right;\">";
                            if (this.urlLogoDer.length() > 0) {
                                voucher += "<img width = \"40\" alt = \"Logo\" src=\"" + this.urlLogoDer + "\"/>";
                            }
                            voucher += "</td>"
                                    + "</tr>"
                                    + "</tbody>"
                                    + "</table>"
                                    + "</div>"
                                    + "<center>"
                                    + "<div>"
                                    + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">"
                                    + "<tr>"
                                    + "<th style=\"width: 90px; text-align: left;\">NOMBRE</th>"
                                    + "<td>" + nombreEmpleado + "</td>"
                                    + "<th style=\"width: 90px; text-align: left;\">DEPENDENCIA</th>"
                                    + "<td>" + depto + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<th style=\"width: 90px; text-align: left;\"><b>IDENTIDAD</b></th>"
                                    + "<td>" + identidad + "</td>"
                                    + "<th style=\"width: 90px; text-align: left;\">CARGO</th>"
                                    + "<td>" + cargoEmpleado + "</td>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<th style=\"width: 90px; text-align: left;\"><b>CÓDIGO</b></th>"
                                    + "<td>" + codEmpleado + "</td>"
                                    + "<th style=\"width: 90px; text-align: left;\">MODALIDAD</th>"
                                    + "<td>CONTRATO POR HORA</td>"
                                    + "</tr>"
                                    + "<th style=\"width: 90px; text-align: left;\"><b>Nº CONTRATO</b></th>"
                                    + "<td>" + num_contrato + "</td>"
                                    + "<th style=\"width: 90px; text-align: left;\"></th>"
                                    + "<td></td>"
                                    + "</tr>"
                                    + "</table>"
                                    + "<div style=\"border-left: 2px #000 solid; border-right: 2px #000 solid; border-top: 2px #000 solid; border-bottom: 2px #000 solid; margin-top: 10px; margin-right: 2px; margin-left: 2px;\">"
                                    + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%; margin-top: 10px;\">"
                                    + "<tr>"
                                    + "<th  style=\"width:40%; text-align: center; font-family: arial; font-size: 15px;\">Deducciones</th>"
                                    + "<th style=\"width:20%;\"></th>"
                                    + "<th  style=\"width:40%; text-align: center; font-family: arial; font-size: 15px;\">Ingresos</th>"
                                    + "</tr>"
                                    + "<tr>"
                                    + "<td  style=\"width:40%; vertical-align: bottom;\">"
                                    + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">";

                            //Agregar los debitos de planilla
                            if (rsDebitosPlanilla.next()) {
                                rsDebitosPlanilla.beforeFirst();

                                while (rsDebitosPlanilla.next()) {
                                    double val = rsDebitosPlanilla.getDouble("valor");
                                    totaldebito = totaldebito + val;
                                    totaldebitoMes = totaldebitoMes + val;

                                    voucher += "<tr>"
                                            + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + rsDebitosPlanilla.getString("descripcion") + "</th>"
                                            + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                            + "</tr>";
                                }
                            }

                            voucher += "<tr>"
                                    + "<th>Total Deducciones</th>"
                                    + "<td style=\"text-align: right; border-top: 1px #000 solid;\"><b><span style=\"text-align: left; float: left;\">L. </span>" + formatMoneda.format(totaldebito) + "</b></td>"
                                    + "</tr>"
                                    + "</table>"
                                    + "</td>"
                                    + "<th style=\"width:20%;\"></th>"
                                    + "<td  style=\"width:40%; vertical-align: bottom;\">"
                                    + "<table border=\"0\" width=\"2\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">";

                            //Agregar los creditos de planilla
                            if (rsCreditosPlanilla.next()) {
                                rsCreditosPlanilla.beforeFirst();

                                while (rsCreditosPlanilla.next()) {
                                    double val = rsCreditosPlanilla.getDouble("valor");
                                    totaling = totaling + val;
                                    subTotMes = subTotMes + val;

                                    voucher += "<tr>"
                                            + "<th style=\"font-family: arial; font-size: 12px; text-align: left;\">" + rsCreditosPlanilla.getString("descripcion") + "</th>"
                                            + "<td style=\"text-align: right; width: 105px; font-family: arial; font-size: 12px;\">" + formatMoneda.format(val) + "</td>"
                                            + "</tr>";

                                }
                            }

                            voucher += "<tr>"
                                    + "<th>Total de Ingresos</th>"
                                    + "<td style=\"text-align: right; border-top: 1px #000 solid;\"><b><span style=\"text-align: left; float: left;\">L. </span>" + formatMoneda.format(totaling) + "</b></td>"
                                    + "</tr>"
                                    + "</table>"
                                    + "</td>"
                                    + "</tr>"
                                    + "<tr >"
                                    + "<th colspan=\"2\" style=\"border-top: 2px #000 solid;margin-top: 40px; text-align: left;\">Neto a Pagar en el Mes</th>"
                                    + "<td style=\"text-align: right; font-size: 13px; border-top: 2px #000 solid;margin-top: 40px;\">"
                                    + "<table border=\"0\" width=\"0\" cellspacing=\"0\" cellpadding=\"3\" style=\"width: 100%;\">"
                                    + "<tr>"
                                    + "<th>"
                                    + "</th>"
                                    + "<td style=\"text-align: right;  width: 105px;\">"
                                    + "<b><span style=\"text-align: left; float: left;\">L. </span>"
                                    + formatMoneda.format((subTotMes - totaldebitoMes))
                                    + "</b></td>"
                                    + "</tr>"
                                    + "</table>"
                                    + "</td>"
                                    + "</tr>";

                            voucher += "</table>"
                                    + "</div>"
                                    + "</div>"
                                    + "</center>"
                                    + "<div style=\"text-align: left;margin-top: 10px; font-family: arial; font-size: 11px;\">"
                                    + mensaje
                                    + "</div>"
                                    + "</div><br><br></body></html>";

                            Principal.txtVoucher.setContentType("text/html");
                            Principal.txtVoucher.setText(voucher);

                            asunto = codEmpleado + " Comprobante de Pago Contrato por Hora " + num_contrato + " " + mesSel + " de " + anio + " " + nombreEmpleado;

                            boolean registrarEnvio = true;

                            try {
                                Message message = new MimeMessage(session);
                                message.setFrom(new InternetAddress(userNameMail));
                                message.setRecipients(Message.RecipientType.TO,
                                        InternetAddress.parse(correoEmpleado));
                                message.setSubject(asunto);
                                message.setContent(voucher, "text/html");//setText(Mensage);

                                Transport.send(message);
                                //JOptionPane.showMessageDialog(this, "Su mensaje ha sido enviado");
                                //Control y rotulación
                                enviadosPorCiclo++;

                                contratosCorreoEnviados++;

                                Principal.pbrEnviadosCH.setValue((int) contratosCorreoEnviados);
                                Principal.pbrEnviosCiclo.setValue((int) enviadosPorCiclo);

                                double porcentaje = ((int) contratosCorreoEnviados / (int) contratosCorreosDisponibles) * 100;

                                String porcentajeTxt = format.format(porcentaje);
                                Principal.pbrEnviadosCH.setToolTipText(porcentajeTxt + " %");
                                Principal.lblPorcentajeCH.setText(porcentajeTxt + " %");

                                Principal.lblCorreosDisponiblesCH.setText((int) contratosCorreoEnviados + "/" + contratosCorreosDisponibles);

                            } catch (MessagingException e) {
                                registrarEnvio = false;
                                if (e.getMessage().contains("Invalid Addresses")) {
                                    errorEnvio = true;
                                }
                                Principal.lblEstado.setText("ERROR: " + e.getMessage());
                                TimeUnit.SECONDS.sleep(15);
//                            JOptionPane.showMessageDialog(null, e.getMessage(), "ERROR",
//                                    JOptionPane.ERROR_MESSAGE);
                                //throw new RuntimeException(e);
                            }
                            //Registrar que ya fue enviado
                            if (registrarEnvio) {
                                String tabla = "est_correos_enviados_ch";
                                String campos = "id_persona_ch, id_info_contratos, enviado";
                                String valores = "'" + id_persona_ch + "','" + idInfo + "','1'";
                                db.insertarRegistro(tabla, campos, valores);
                            }
                            //Revisar si entra en pausa
                            if (enviadosPorCiclo >= maximoDeEnvios || errorEnvio) {
                                enviadosPorCiclo = 0;

                                if (errorEnvio) {
                                    rsContratosPlanilla.previous();
                                }
                                Principal.txtVoucher.setText("");
                                Principal.lblContinuara.setText("Continuará en:");
                                cuentaAtrasActivoCH = true;
                                Thread hiloCuentaAtras = new CuentaRegresivaCH(minutosDeEspera);
                                hiloCuentaAtras.start();

                                Principal.lblEstado.setText("Esperando para continuar ...");
                                TimeUnit.MINUTES.sleep(minutosDeEspera);

                                cuentaAtrasActivoCH = false;
                                Principal.pbrEnviosCiclo.setValue((int) enviadosPorCiclo);

                                //Iniciar la session nuevamente
                                session = Session.getInstance(props,
                                        new javax.mail.Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(userNameMail, passwordMail);
                                    }
                                });

                            }

                        }
                    }

                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        //habilitar controles de configuración de envío
        Principal.cmbAnio.setEnabled(true);
        Principal.cmbMes.setEnabled(true);
        Principal.valMaxCorreos.setEnabled(true);
        Principal.tiempoEspera.setEnabled(true);
        Principal.btnIniciarEnvio.setEnabled(true);
        Principal.chkAcuerdo.setEnabled(true);
        Principal.chkContratoPorHora.setEnabled(true);
        Principal.enviando = false;

        tiempoTranscurridoActivoCH = false;
        Principal.lblEstado.setForeground(Color.GREEN);
        Principal.lblEstado.setText("Envío finalizado exitosamente!!!");
        db.cierraConexion();
    }

    private String mesTxt2Numero(String mes) {
        String mesTxt = "";
        switch (mes) {
            case "Enero":
                mesTxt = "1";
                break;
            case "Febrero":
                mesTxt = "2";
                break;
            case "Marzo":
                mesTxt = "3";
                break;
            case "Abril":
                mesTxt = "4";
                break;
            case "Mayo":
                mesTxt = "5";
                break;
            case "Junio":
                mesTxt = "6";
                break;
            case "Julio":
                mesTxt = "7";
                break;
            case "Agosto":
                mesTxt = "8";
                break;
            case "Septiembre":
                mesTxt = "9";
                break;
            case "Octubre":
                mesTxt = "10";
                break;
            case "Noviembre":
                mesTxt = "11";
                break;
            case "Diciembre":
                mesTxt = "12";
                break;
            default:
                break;
        }

        return mesTxt;
    }

    private String mes2text(String mes) {
        String mesTxt = "";
        if (mes.equals("1")) {
            mesTxt = "Enero";
        } else if (mes.equals("2")) {
            mesTxt = "Febrero";
        } else if (mes.equals("3")) {
            mesTxt = "Marzo";
        } else if (mes.equals("4")) {
            mesTxt = "Abril";
        } else if (mes.equals("5")) {
            mesTxt = "Mayo";
        } else if (mes.equals("6")) {
            mesTxt = "Junio";
        } else if (mes.equals("7")) {
            mesTxt = "Julio";
        } else if (mes.equals("8")) {
            mesTxt = "Agosto";
        } else if (mes.equals("9")) {
            mesTxt = "Septiembre";
        } else if (mes.equals("10")) {
            mesTxt = "Octubre";
        } else if (mes.equals("11")) {
            mesTxt = "Noviembre";
        } else if (mes.equals("12")) {
            mesTxt = "Diciembre";
        }

        return mesTxt;
    }

    public class CuentaRegresiva extends Thread {

        private int segundos;

        public CuentaRegresiva(int minutos) {
            this.segundos = (minutos * 60) - 1;
        }

        @Override
        public void run() {
            do {

                Principal.lblCuentaAtras.setText(segundos2reloj(segundos));

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.segundos--;
            } while (cuentaAtrasActivo);

            Principal.lblCuentaAtras.setText("");
            Principal.lblContinuara.setText("");
        }

    }

    public class CuentaRegresivaCH extends Thread {

        private int segundos;

        public CuentaRegresivaCH(int minutos) {
            this.segundos = (minutos * 60) - 1;
        }

        @Override
        public void run() {
            do {

                Principal.lblCuentaAtras.setText(segundos2reloj(segundos));

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.segundos--;
            } while (cuentaAtrasActivoCH);

            Principal.lblCuentaAtras.setText("");
            Principal.lblContinuara.setText("");
        }

    }

    public class TiempoTranscurrido extends Thread {

        private int segundos;

        public TiempoTranscurrido() {
            this.segundos = 1;
        }

        @Override
        public void run() {
            do {

                Principal.lblTiempoTrancurrido.setText(segundos2reloj(segundos));

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.segundos++;
            } while (tiempoTranscurridoActivo);

            Principal.lblCuentaAtras.setText("");
            Principal.lblContinuara.setText("");
        }

    }

    public class TiempoTranscurridoCH extends Thread {

        private int segundos;

        public TiempoTranscurridoCH() {
            this.segundos = 1;
        }

        @Override
        public void run() {
            do {

                Principal.lblTiempoTrancurridoCH.setText(segundos2reloj(segundos));

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ThreadProcesos.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.segundos++;
            } while (tiempoTranscurridoActivoCH);

            Principal.lblCuentaAtras.setText("");
            Principal.lblContinuara.setText("");
        }

    }

    private String segundos2reloj(int segundos) {
        String reloj = "";

        int horas = (int) segundos / 3600;

        int mins = (int) segundos % 3600 / 60;

        int segs = segundos - ((horas * 3600) + (mins * 60));

        if (horas < 10) {
            reloj = "0" + horas;
        } else {
            reloj = "" + horas;
        }

        if (mins < 10) {
            reloj += ":0" + mins;
        } else {
            reloj += ":" + mins;
        }

        if (segs < 10) {
            reloj += ":0" + segs;
        } else {
            reloj += ":" + segs;
        }

        return reloj;
    }

}
