package icsrv20192;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;

public class UnsafeClient 
{
private final static String HOLA = "HOLA";
	
	private final static String OK = "OK";
	
	private final static String ERROR = "ERROR";
	
	private final static String ALGORITMOS = "ALGORITMOS:";
	
	private final static String[] AlgorithmSet = {"AES", "Blowfish", "RSA", "HMACSHA1", "HMACSHA256", "HMACSHA384", "HMACSHA512"};
	
	private static Socket clientSocket; 
	
	private static InputStream auxiliary; 
	
	private static BufferedReader clientIn; 
	
	private static PrintWriter clientOut;
	
	private static X509Certificate certificadoDigital; 
	
	private static CertificateFactory CDF;
	
	private static int simPosition;
	
	private static int hmacPosition;
	
	private static SecretKey llaveSimetrica;
	
	private static PublicKey llaveServidor;
	
	public void run(String address, int port, int sim, int hmac) 
	{
		try 
		{
			clientSocket = new Socket(address, port);
			auxiliary = clientSocket.getInputStream();
			clientIn = new BufferedReader(new InputStreamReader(auxiliary));
			clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
			simPosition = sim; hmacPosition = hmac;
			etapa1();
			etapa2();
			etapa3();
			etapa4();
		} 
		catch (Exception e) 
		{ e.printStackTrace(); }
	}
	
	public static void etapa1() throws Exception
	{
		//Se eliminan todas las llamadas del cliente con la consola, ya que todfos los datos los recibirá por parámetros
		clientOut.println(HOLA);
		if(clientIn.readLine().equals(OK))
		{
			clientOut.println(ALGORITMOS + AlgorithmSet[simPosition] + ":" + AlgorithmSet[2] + ":" + AlgorithmSet[hmacPosition]);
			CDF = new CertificateFactory();
			if(clientIn.readLine().equals(OK)) certificadoDigital = (X509Certificate) CDF.engineGenerateCertificate(new ByteArrayInputStream(fromStringToByteArray(clientIn.readLine())));
			else throw new IOException("El servidor rechazó la propuesta de algoritmos"); 
		}
	}
	
	public static void etapa2() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		llaveServidor = certificadoDigital.getPublicKey();
		KeyGenerator generador = KeyGenerator.getInstance(AlgorithmSet[simPosition]);
		llaveSimetrica = generador.generateKey();
		clientOut.println(DatatypeConverter.printBase64Binary(llaveSimetrica.getEncoded()));
		String reto = "94130EAOSRNIDL57268ctumpbgvy@#$%&QHFZJÑXWK!?-,.";
		clientOut.println(DatatypeConverter.printBase64Binary(reto.getBytes()));
		String respuesta = clientIn.readLine();
		if (respuesta.equals(reto)) clientOut.println(OK);
		else clientOut.println(ERROR);
	}
	
	public static void etapa3() throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException
	{
		Cipher c = Cipher.getInstance(AlgorithmSet[simPosition]);
		c.init(Cipher.ENCRYPT_MODE, llaveSimetrica);
		byte[] cifrado = c.doFinal(fromStringToByteArray("1003592593"));
		clientOut.println(DatatypeConverter.printBase64Binary(cifrado));
		cifrado = c.doFinal(fromStringToByteArray("unamalaclave123"));
		clientOut.println(DatatypeConverter.printBase64Binary(cifrado));
	}
	
	public static void etapa4() throws NumberFormatException, IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException
	{
		Cipher c = Cipher.getInstance(AlgorithmSet[simPosition]);
		c.init(Cipher.DECRYPT_MODE, llaveSimetrica);
		String valor =  DatatypeConverter.printBase64Binary(c.doFinal(fromStringToByteArray(clientIn.readLine())));
		int valorHmac = Integer.parseInt(valor);
		c = Cipher.getInstance(AlgorithmSet[2]);
		c.init(Cipher.DECRYPT_MODE, llaveServidor);
		Mac mac = Mac.getInstance(AlgorithmSet[hmacPosition]);
		mac.init(llaveSimetrica);
		mac.update(valor.getBytes());
		byte[] digest = mac.doFinal();
		String hmac = DatatypeConverter.printBase64Binary((c.doFinal(fromStringToByteArray(clientIn.readLine()))));
		if(digest.equals(hmac.getBytes())) clientOut.println(OK);
		else clientOut.println(ERROR);
	}
	
	private static byte[] fromStringToByteArray(String cadena)
	{
		int a, l = 4 - cadena.length() % 4;
		for(a=0; l != 4 && a < l; a++)
			cadena = "0" + cadena;
		return DatatypeConverter.parseBase64Binary(cadena); 
	}
}
