package Servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Cliente.Cliente;
import Cliente.ConexaoCliente;

public class Servidor implements Runnable {

	ServerSocket socketServidor;
	List<ConexaoCliente> clientesInternos = new ArrayList<ConexaoCliente>();
	List<ConexaoServidor> conexoesServidor = new ArrayList<ConexaoServidor>();
	String nome;
	int porta;
	String chaveVigenere, chaveHmac, chaveAES;

	public Servidor(String nome, int porta, Cliente cliente) {
		this.nome = nome;
		this.porta = porta;
		if (cliente != null) {
			this.clientesInternos = cliente.getClientes();
		}
		// Thread run = new Thread(() -> run());
		// run.start();
	}

	public Servidor(String nome, int porta, Cliente cliente, String chaveVigenere, String chaveHmac, String chaveAES) {
		this.nome = nome;
		this.porta = porta;
		if (cliente != null) {
			this.clientesInternos = cliente.getClientes();
		}
		this.chaveVigenere = chaveVigenere;
		this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
	}

	public void run() {
		try {
			socketServidor = new ServerSocket(porta);
			System.out.println("Servidor " + this.nome + ": rodando na porta " + socketServidor.getLocalPort());
			System.out.println(
					"Servidor " + this.nome + ": HostAddress = " + InetAddress.getLocalHost().getHostAddress());
			System.out.println("Servidor " + this.nome + ": HostName = " + InetAddress.getLocalHost().getHostName());

			System.out.println("Servidor " + this.nome + ": aguardando conexão do cliente...");

			ImplServidor servidor = new ImplServidor(this.clientesInternos, this.conexoesServidor, this.nome,
					this.chaveVigenere, this.chaveHmac, this.chaveAES);
			Thread t = new Thread(servidor);
			t.start();

			while (true) {
				Socket cliente = socketServidor.accept();
				/*
				 * ObjectInputStream inputObject = new
				 * ObjectInputStream(cliente.getInputStream());
				 * 
				 * Mensagem mensagemRecebida = (Mensagem) inputObject.readObject();
				 * 
				 * System.out.println("Servidor " + this.nome + ": conexão " +
				 * ImplServidor.contConexoes + " com o cliente " +
				 * mensagemRecebida.getConteudo() + " em " +
				 * cliente.getInetAddress().getHostAddress() + "/" +
				 * cliente.getInetAddress().getHostName());
				 * 
				 * ObjectOutputStream outputObject = new
				 * ObjectOutputStream(cliente.getOutputStream()); outputObject.writeObject(new
				 * Mensagem(this.nome, null, this.nome, "unicast"));
				 */

				ConexaoServidor cs = new ConexaoServidor(cliente, this.nome);
				conexoesServidor.add(cs);

				ImplServidor.contConexoes++;

			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
