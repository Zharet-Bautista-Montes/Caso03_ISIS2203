package icsrv20192;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.net.Socket;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.DatatypeConverter;

public class UnsafeServer extends Thread
{
	public static final String OK = "OK";
	public static final String ALGORITMOS = "ALGORITMOS";
	public static final String CERTSRV = "CERTSRV";
	public static final String CERCLNT = "CERCLNT";
	public static final String SEPARADOR = ":";
	public static final String HOLA = "HOLA";
	public static final String INICIO = "INICIO";
	public static final String ERROR = "ERROR";
	public static final String REC = "recibio-";
	public static final int numCadenas = 8;
	public static int transact = 0;

	// Atributos
	private Socket sc = null;
	private String dlg;
	private byte[] mybyte;
	private static File file;
	private static X509Certificate certSer;
	
	public static void contarTrans()
	{ transact++; }
	
	public static void init(X509Certificate pCertSer, File pFile) {
		certSer = pCertSer;
		file = pFile;
	}
	
	public UnsafeServer (Socket csP, int idP) {
		sc = csP;
		dlg = new String("delegado " + idP + ": ");
		try {
		mybyte = new byte[520]; 
		mybyte = certSer.getEncoded();
		} catch (Exception e) {
			System.out.println("Error creando encoded del certificado para el thread" + dlg);
			e.printStackTrace();
		}
	}
	
	private boolean validoAlgHMAC(String nombre) {
		return ((nombre.equals(S.HMACMD5) || 
			 nombre.equals(S.HMACSHA1) ||
			 nombre.equals(S.HMACSHA256) ||
			 nombre.equals(S.HMACSHA384) ||
			 nombre.equals(S.HMACSHA512)
			 ));
	}
	
	/*
	 * Generacion del archivo log. 
	 * Nota: 
	 * - Debe conservar el metodo como está. 
	 * - Es el único metodo permitido para escribir en el log.
	 */
	//TODO cambios: a�ad� synchronized para que los threads ejecuten este m�todo uno a la vez.
	private synchronized void escribirMensaje(String pCadena) {
		
		try {
			FileWriter fw = new FileWriter(file,true);
			fw.write(pCadena + "\n");
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public synchronized void run() {
		String[] cadenas;
		cadenas = new String[numCadenas];
		long tiempoFinal, tiempoInicial; 
		String linea;
	    System.out.println(dlg + "Empezando atencion.");
	        try {

				PrintWriter ac = new PrintWriter(sc.getOutputStream() , true);
				BufferedReader dc = new BufferedReader(new InputStreamReader(sc.getInputStream()));

				/***** Fase 1:  *****/
				linea = dc.readLine();
				cadenas[0] = "Fase1: ";
				if (!linea.equals(HOLA)) {
					ac.println(ERROR);
				    sc.close();
					throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
				} else {
					ac.println(OK);
					cadenas[0] = dlg + REC + linea + "-continuando.";
					System.out.println(cadenas[0]);
				}
				
				/***** Fase 2:  *****/
				linea = dc.readLine();
				cadenas[1] = "Fase2: ";
				if (!(linea.contains(SEPARADOR) && linea.split(SEPARADOR)[0].equals(ALGORITMOS))) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + REC + linea +"-terminando.");
				}
				
				String[] algoritmos = linea.split(SEPARADOR);
				if (!algoritmos[1].equals(S.DES) && !algoritmos[1].equals(S.AES) &&
					!algoritmos[1].equals(S.BLOWFISH) && !algoritmos[1].equals(S.RC4)){
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "Alg.Simetrico" + REC + algoritmos + "-terminando.");
				}
				if (!algoritmos[2].equals(S.RSA) ) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "Alg.Asimetrico." + REC + algoritmos + "-terminando.");
				}
				if (!validoAlgHMAC(algoritmos[3])) {
					ac.println(ERROR);
					sc.close();
					throw new Exception(dlg + ERROR + "AlgHash." + REC + algoritmos + "-terminando.");
				}
				cadenas[1] = dlg + REC + linea + "-continuando.";
				System.out.println(cadenas[1]);
				ac.println(OK);
				
				/***** Fase 3:  *****/
				String testCert = toHexString(mybyte);
				ac.println(testCert);
				cadenas[2] = dlg + "envio certificado del servidor. continuando.";
				System.out.println(cadenas[2] + testCert);				

				/***** Fase 4: *****/
				cadenas[3] = "";
				tiempoInicial = System.currentTimeMillis();
				linea = dc.readLine();
				byte[] llaveSimetrica = toByteArray(linea);
				SecretKey simetrica = new SecretKeySpec(llaveSimetrica, 0, llaveSimetrica.length, algoritmos[1]);
				cadenas[3] = dlg + "recibio llave simetrica. continuando.";
				System.out.println(cadenas[3]);
				
				/***** Fase 5:  *****/
				cadenas[4]="";
				linea = dc.readLine();
				System.out.println(dlg + "Recibio reto del cliente:-" + linea + "-");
				ac.println(linea);
				System.out.println(dlg + "envio reto sin cifrado al cliente. continuado.");

				linea = dc.readLine();
				if ((linea.equals(OK))) {
					cadenas[4] = dlg + "recibio confirmacion del cliente:"+ linea +"-continuado.";
					System.out.println(cadenas[4]);
				} else {
					sc.close();
					throw new Exception(dlg + ERROR + "en confirmacion de llave simetrica." + REC + "-terminando.");
				}
				
				/***** Fase 6:  *****/
				linea = dc.readLine();
				System.out.println(dlg + "recibio cc:-" + linea + "-continuado.");				
				linea = dc.readLine();				
				System.out.println(dlg + "recibio clave:-" + linea + "-continuado.");
				cadenas[5] = dlg + "recibio cc y clave - continuando";
				
				Random rand = new Random(); 
				int valor = rand.nextInt(1000000);
				String strvalor = valor+"";
				ac.println(strvalor);
				cadenas[6] = dlg + "envio valor "+strvalor+" sin cifrado al cliente. continuado.";
				System.out.println(cadenas[6]);
				while (strvalor.length()%4!=0) strvalor += 0;
				byte[] valorByte = toByteArray(strvalor);
				byte [] hmac = S.hdg(valorByte, simetrica, algoritmos[3]);
				ac.println(toHexString(hmac));
				tiempoFinal = System.currentTimeMillis();
				System.out.println(dlg + "envio hmac sin cifrado. continuado.");
				
				cadenas[7] = "";
				linea = dc.readLine();	
				if (linea.equals(OK)) {
					cadenas[7] = dlg + "Terminando exitosamente." + linea;
					System.out.println(cadenas[7]);
				} else {
					cadenas[7] = dlg + "Terminando con error" + linea;
			        System.out.println(cadenas[7]);
				}
		        sc.close();

			    for (int i=0;i<numCadenas;i++) {
				    escribirMensaje(cadenas[i]);
			    }			    
			    escribirMensaje("Tiempo de respuesta: " + (tiempoFinal - tiempoInicial) + "\n" + "Uso de CPU: " + getSystemCpuLoad() + "\n" + "Transacciones exitosas: " + transact);
	        } catch (Exception e) {
	          e.printStackTrace();
	        }
	}
	
	public static String toHexString(byte[] array) {
	    return DatatypeConverter.printBase64Binary(array);
	}

	public static byte[] toByteArray(String s) {
	    return DatatypeConverter.parseBase64Binary(s);
	}
		
	public double getSystemCpuLoad() throws Exception 
	{
		 MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		 ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
		 AttributeList list = mbs.getAttributes(name, new String[]{ "SystemCpuLoad" });
		 if (list.isEmpty()) return Double.NaN;
		 Attribute att = (Attribute)list.get(0);
		 Double value = (Double)att.getValue();
		 if (value == -1.0) return Double.NaN; // usually takes a couple of seconds before we get real values
		 return ((int)(value * 1000) / 10.0); // returns a percentage value with 1 decimal point precision
	}
}
