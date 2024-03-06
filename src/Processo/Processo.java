package Processo;

import Cliente.Cliente;
import Servidor.Servidor;

public class Processo {
	Cliente cliente;
	Servidor servidor;
	
	public Processo(String nome, int host, int[] porta) {
		// chaves que o servidor usa pra abrir o "banco de dados"
		String chaveVigenere = "chaveVigenere";
		String chaveHmac = "chaveHmac";
		String chaveAES = "chaveAES12341234";
		
		if(porta != null) {
			this.cliente = new Cliente(nome, "localhost", porta, host);
			Thread client = new Thread(this.cliente);
			client.start();
		}
		if(host != 0) {
			if(porta != null) {
				this.servidor = new Servidor(nome, host, null, chaveVigenere, chaveHmac, chaveAES);
				Thread server = new Thread(this.servidor);
				server.start();
			} else {
				this.servidor = new Servidor(nome, host, this.cliente, chaveVigenere, chaveHmac, chaveAES);
				Thread server = new Thread(this.servidor);
				server.start();
			}
		}
		
		
	}
	
	public static void main(String[]args) {
		
		// exemplo extrela
				new Processo("Banco", 5007, null);
		//		new Processo("p4", 0, new int[]{5007});
		//		new Processo("p3", 0, new int[]{5007});
		//		new Processo("p4", 0, new int[]{5007});
		
	}
}

