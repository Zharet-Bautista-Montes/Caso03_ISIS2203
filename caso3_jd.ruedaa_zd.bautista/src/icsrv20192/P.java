package icsrv20192;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
//TODO cambios: comentado import java.net.Socket;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class P {
	private static ServerSocket ss;	
	private static final String MAESTRO = "MAESTRO: ";
	private static X509Certificate certSer; /* acceso default */
	private static KeyPair keyPairServidor; /* acceso default */
	private static boolean switcher; 
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		System.out.println(MAESTRO + "Establezca puerto de conexion:");
		InputStreamReader isr = new InputStreamReader(System.in);
		BufferedReader br = new BufferedReader(isr);
		int ip = Integer.parseInt(br.readLine());
		System.out.println(MAESTRO + "Empezando servidor maestro en puerto " + ip);
		// Adiciona la libreria como un proveedor de seguridad.
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());	
		System.out.println(MAESTRO + "¿Iniciar el servidor en modo seguro (true) o inseguro (false)?:");
		switcher = new Boolean(br.readLine());

		// Crea el archivo de log
		File file = null;
		keyPairServidor = S.grsa();
		certSer = S.gc(keyPairServidor);
		String ruta = "./resultados.txt";
   
        file = new File(ruta);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileWriter fw = new FileWriter(file);
        fw.close();
        
        if(switcher) D.init(certSer, keyPairServidor, file);
        else UnsafeServer.init(certSer, file);
        
		// Crea el socket que escucha en el puerto seleccionado y configura los threads del generador.
		ss = new ServerSocket(ip);
		System.out.println(MAESTRO + "Socket creado.");
		//TODO cambios: añadido 55 a 57, 61 y 66
		System.out.println(MAESTRO + "Establezca número de threads en el pool:");
		int poolSize = Integer.parseInt(br.readLine());
		ExecutorService pool = Executors.newFixedThreadPool(poolSize);
		for (int i=0;true;i++) {
			try {
				//modificado para contar las transacciones exitosas
				if(switcher)
				{ D succeeded = new D(ss.accept(),i);
				if(succeeded != null)
				{ pool.execute(succeeded); D.contarTrans(); } }
				else
				{ UnsafeServer succeeded = new UnsafeServer(ss.accept(),i);
				if(succeeded != null)
				{ pool.execute(succeeded); UnsafeServer.contarTrans(); } }
				//TODO cambios: comentado Socket sc = ss.accept(); 
				System.out.println(MAESTRO + "Cliente " + i + " aceptado.");
			} catch (IOException e) {
				System.out.println(MAESTRO + "Error creando el socket cliente.");
				pool.shutdown();
				e.printStackTrace();
			}
		}
	}
}
