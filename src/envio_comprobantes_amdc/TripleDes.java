package envio_comprobantes_amdc;


import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.*;
import org.xml.sax.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.Document;
import org.apache.commons.codec.binary.Base64;

public class TripleDes {

    private static final String UNICODE_FORMAT = "UTF8";
    public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
    private KeySpec myKeySpec;
    private SecretKeyFactory mySecretKeyFactory;
    private Cipher cipher;
    byte[] keyAsBytes;
    private String myEncryptionKey;
    private String myEncryptionScheme;
    SecretKey key;

    public TripleDes() {
        try {
            //myEncryptionKey = "ClaveDeSeguridadAmDc@1978";
            myEncryptionKey = "ClaveDeSeguridadAmDc@1978";
            myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
            keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
            myKeySpec = new DESedeKeySpec(keyAsBytes);
            mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
            cipher = Cipher.getInstance(myEncryptionScheme);
            key = mySecretKeyFactory.generateSecret(myKeySpec);
        } catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            System.err.println("ERROR EN Enc_3DES\n"+e.getMessage());
        }

    }

    /**
     * Método para Encriptar la cadena de texto
     *
     * @param cadenaParaEncriptar
     * @return cadenaEncriptada
     */
    public String encriptar_txt_3DES(String cadenaParaEncriptar) {
        String StringEncriptado = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = cadenaParaEncriptar.getBytes(UNICODE_FORMAT);
            byte[] encryptedText = cipher.doFinal(plainText);
//            BASE64Encoder base64encoder = new BASE64Encoder();
//            StringEncriptado = base64encoder.encode(encryptedText);
            
            StringEncriptado = Base64.encodeBase64String(encryptedText);
        } catch (InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("ERROR desEncriptar_txt_3DES\n" + e.getMessage());
        }
        return StringEncriptado;
    }
    
    public boolean validar_txt_3DES(String cadenaParaDesencriptar) {
        String TextoDesencriptado = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] TextoEncriptado = Base64.decodeBase64(cadenaParaDesencriptar);
            
            //StringEncriptado = Base64.encodeBase64String(encryptedText);
//            BASE64Decoder base64decoder = new BASE64Decoder();
//            byte[] TextoEncriptado = base64decoder.decodeBuffer(cadenaParaDesencriptar);
            byte[] plainText = cipher.doFinal(TextoEncriptado);
            TextoDesencriptado = new String(plainText, "UTF8");//bytes2String(plainText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            return false;
        }
        //return new String(plainText, "UTF8");
        return true;
    }
    
    /**
     * Toma el contenido de un fichero en byte Array y lo cifra en 3DES
     * @param contenido
     * @return Cadena 3DES cifrada
     */
    public String cifrarByteArray2TDES(byte[] contenido){
        String cadena3des = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = contenido;
            byte[] encryptedText = cipher.doFinal(plainText);

            cadena3des = Base64.encodeBase64String(encryptedText);

        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("ERROR AL ENCRIPTAR:\n" + e.getMessage());
        }
        
        
        return cadena3des;
    }

    /**
     * Método para Desencriptar la cadena de texto
     *
     * @param cadenaParaDesencriptar
     * @return cadenaDesencriptada
     */
    public String desEncriptar_txt_3DES(String cadenaParaDesencriptar) {
        String TextoDesencriptado = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] TextoEncriptado = Base64.decodeBase64(cadenaParaDesencriptar);
            
            //StringEncriptado = Base64.encodeBase64String(encryptedText);
//            BASE64Decoder base64decoder = new BASE64Decoder();
//            byte[] TextoEncriptado = base64decoder.decodeBuffer(cadenaParaDesencriptar);
            byte[] plainText = cipher.doFinal(TextoEncriptado);
            TextoDesencriptado = new String(plainText, "UTF8");//bytes2String(plainText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException e) {
            System.err.println("ERROR desEncriptar_txt_3DES\n" + e.getMessage());
        }
        //return new String(plainText, "UTF8");
        return TextoDesencriptado;
    }
    
    /**
     * Recibe un fichero en una cadena 3DES y la convierte en Byte Array
     * 
     * @param cadena3DES
     * @return fichero En byteArray
     */
    public byte[] TDES2byteArray(String cadena3DES){
        byte[] fichero_byteArray = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] TextoEncriptado = Base64.decodeBase64(cadena3DES);
            
            fichero_byteArray = cipher.doFinal(TextoEncriptado);
            
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("ERROR desEncriptar_txt_3DES\n" + e.getMessage());
        }
        return fichero_byteArray;
    }

    /**
     *
     * @param XMLParaEncriptar
     * @return
     */
    public String encriptarXML_3DES(org.w3c.dom.Document XMLParaEncriptar) {
        String StringEncriptado = null;
        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] plainText = doc2bytes(XMLParaEncriptar);
            byte[] encryptedText = cipher.doFinal(plainText);

            StringEncriptado = Base64.encodeBase64String(encryptedText);

//            BASE64Encoder base64encoder = new BASE64Encoder();
//            StringEncriptado = base64encoder.encode(encryptedText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("ERROR encriptar XML \n" + e.getMessage());
        }
        return StringEncriptado;
    }

    /**
     * Método para Desencriptar en Documento XML
     *
     * @param XMLParaDesencriptar
     * @return Documento XML desencriptado en un objeto Document.
     */
    public Document desEncriptarXML_3DES(String XMLParaDesencriptar) {

        Document docXML = null;
        try {
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] TextoEncriptado = Base64.decodeBase64(XMLParaDesencriptar);
            byte[] plainText = cipher.doFinal(TextoEncriptado);
            docXML = bytesToXml(plainText);
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | SAXException | ParserConfigurationException | IOException e) {
            System.err.println("ERROR desencriptar XML \n" + e.getMessage());
        }
        return docXML;
    }

    public static byte[] doc2bytes(org.w3c.dom.Document node) {
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
            System.err.println("ERROR EN doc2bytes:\n" + e.getMessage());
        } catch (TransformerException e) {
            System.err.println("ERROR EN doc2bytes:\n" + e.getMessage());
        }
        return null;
    }

    public static Document bytesToXml(byte[] xml) throws SAXException, ParserConfigurationException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return (Document) builder.parse(new ByteArrayInputStream(xml));
    }

}
