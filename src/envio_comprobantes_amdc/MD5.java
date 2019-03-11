/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package envio_comprobantes_amdc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class MD5 {

    private static final char[] CONSTS_HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    String fraseSecreta;

    public MD5() {
        fraseSecreta = "";
    }

    /**
     * Metodo para encriptar cadenas a MD5
     *
     * @param stringAEncriptar
     * @return Hash de la cadena recibida
     */
    public String obtenerHashMD5(String stringAEncriptar) {
        String texto = stringAEncriptar.concat(fraseSecreta);
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(texto.getBytes());
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {

                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(CONSTS_HEX[alto]);
                strbCadenaMD5.append(CONSTS_HEX[bajo]);

            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

    public String hash_XML_MD5(org.w3c.dom.Document XML_doc) {
        Document document = XML_doc;

        //Agrear un elemento nuevo solo para crear el Hash MD5 y que sea unico
        Element raiz = document.getDocumentElement();

        Element secreto = document.createElement("ElementoSecreto");
        secreto.setAttribute("Tipo", "Campo Secreto");

        raiz.appendChild(secreto);

        //Generar MD5 del documento con el nuevo elemento agregado
        try {
            MessageDigest msgd = MessageDigest.getInstance("MD5");
            byte[] bytes = msgd.digest(this.docXML2bytes(document));
            StringBuilder strbCadenaMD5 = new StringBuilder(2 * bytes.length);
            for (int i = 0; i < bytes.length; i++) {

                int bajo = (int) (bytes[i] & 0x0f);
                int alto = (int) ((bytes[i] & 0xf0) >> 4);
                strbCadenaMD5.append(CONSTS_HEX[alto]);
                strbCadenaMD5.append(CONSTS_HEX[bajo]);

            }
            return strbCadenaMD5.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

    }

    public byte[] docXML2bytes(org.w3c.dom.Document node) {
        try {
            Source source = new DOMSource(node);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(out);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.transform(source, result);
            return out.toByteArray();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSha256(String value, String complemento) {
        String valor = value + complemento;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(valor.getBytes());
            return bytesToHex(md.digest());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuffer result = new StringBuffer();
        for (byte b : bytes) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
