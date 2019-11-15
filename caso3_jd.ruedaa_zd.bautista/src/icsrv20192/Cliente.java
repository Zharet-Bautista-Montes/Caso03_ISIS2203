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
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;

public class Cliente extends Thread
{
	private final static String HOLA = "HOLA";
	
	private final static String OK = "OK";
	
	private final static String ERROR = "ERROR";
	
	private final static String ALGORITMOS = "ALGORITMOS:";
	
	private final static String[] AlgorithmSet = {"AES", "BLOWFISH", "RSA", "HMACSHA1", "HMACSHA256", "HMACSHA384", "HMACSHA512"};
	
	private static Socket clientSocket; 
	
	private static InputStream auxiliary; 
	
	private static BufferedReader clientIn; 
	
	private static PrintWriter clientOut;
	
	private static X509Certificate certificadoDigital; 
	
	private static CertificateFactory CDF;
	
	private static Scanner Iconsole = new Scanner(System.in); 
	
	private static int simPosition;
	
	private static int hmacPosition;
	
	private static SecretKey llaveSimetrica;
	
	private static PublicKey llaveServidor;
	
	public void run(String address, int port) 
	{
		try 
		{
			clientSocket = new Socket(address, port);
			auxiliary = clientSocket.getInputStream();
			clientIn = new BufferedReader(new InputStreamReader(auxiliary));
			clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
			etapa1();
			etapa2();
			etapa3();
			etapa4();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void etapa1() throws Exception
	{
		System.out.println("Ingrese el número del algoritmo de cifrado simétrico a proponer: 1. AES o 2. Blowfish");
		int simalgo = Iconsole.nextInt();
		while(simalgo < 1 || simalgo > 2)
		{
			System.out.println("El valor ingresado no corresponde a las opciones.");
			System.out.println("Ingrese el número del algoritmo de cifrado simétrico a proponer: 1. AES o 2. Blowfish");
			simalgo = Iconsole.nextInt();
		}
		simPosition = simalgo -1;
		System.out.println("Ingrese el número del algoritmo HMAC a proponer: 1. SHA1, 2. SHA256, 3. SHA384 o 4. SHA512");
		int hmacalgo = Iconsole.nextInt();
		while(hmacalgo < 1 || hmacalgo > 3)
		{
			System.out.println("El valor ingresado no corresponde a las opciones.");
			System.out.println("Ingrese el número del algoritmo HMAC a proponer: 1. SHA1, 2. SHA256, 3. SHA384 o 4. SHA512");
			hmacalgo = Iconsole.nextInt();
		}
		hmacPosition = hmacalgo + 2;
		System.out.println("Por default se elegirá RSA como algoritmo de cifrado asimétrico");
		clientOut.println(HOLA);
		if(clientIn.readLine().equals(OK))
		{
			clientOut.println(ALGORITMOS + AlgorithmSet[simalgo-1] + ":" + AlgorithmSet[2] + ":" + AlgorithmSet[hmacalgo+2]);
			CDF = new CertificateFactory();
			if(clientIn.readLine().equals(OK))
			{	
				certificadoDigital = (X509Certificate) CDF.engineGenerateCertificate(new ByteArrayInputStream(fromStringToByteArray(clientIn.readLine())));		
			}
			else
			{
				throw new IOException("El servidor rechazó la propuesta de algoritmos"); 
			}
		}
	}
	
	public static void etapa2() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException
	{
		llaveServidor = certificadoDigital.getPublicKey();
		KeyGenerator generador = KeyGenerator.getInstance(AlgorithmSet[simPosition]);
		llaveSimetrica = generador.generateKey();
		Cipher c = Cipher.getInstance(AlgorithmSet[2]);
		c.init(Cipher.ENCRYPT_MODE, llaveServidor);
		byte[] cifrado = c.doFinal(llaveSimetrica.getEncoded());
		clientOut.println(DatatypeConverter.printBase64Binary(cifrado));
		String reto = "94130EA0EAOSRNIDL57268ctumpbgvy@#$%&QHFZJÑXWK!?-,.";
		clientOut.println(DatatypeConverter.printBase64Binary(reto.getBytes()));
		String respuesta = clientIn.readLine();
		c = Cipher.getInstance(AlgorithmSet[simPosition]);
		c.init(Cipher.DECRYPT_MODE, llaveSimetrica);
		respuesta =  new String(c.doFinal(fromStringToByteArray(respuesta)));
		if (respuesta.equals(reto))
		{
			clientOut.println(OK);
		}
		else
		{
			clientOut.println(ERROR);
		}
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
		if(digest.equals(hmac.getBytes()))
		{
			clientOut.println(OK);
		}
		else
		{
			clientOut.println(ERROR);
		}
	}
	private static byte[] fromStringToByteArray(String cadena)
	{
		int a, l = 4 - cadena.length() % 4;
		for(a=0; l != 4 && a < l; a++)
		{
			cadena = "0" + cadena;
		}
		return DatatypeConverter.parseBase64Binary(cadena); 
	}
}
