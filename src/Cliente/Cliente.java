package Cliente;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Cliente implements Runnable{
	
	private ImplCliente implCliente;
	private List<ConexaoCliente> clientes = new ArrayList<ConexaoCliente>();
	Socket socket = new Socket();
	InetAddress inet;
	String nome, ip;
	int[] porta;
	int meuHost;
	
	String chaveVinegere, chaveHmac, chaveAES;
	
	public Cliente(String nome, String ip, int[] porta, int host, String chaveVinegere, String chaveHmac, String chaveAES) {
		this.nome = nome;
		this.ip = ip;
		this.porta = porta;
		this.meuHost = host;
		//Thread run = new Thread(() -> run());
		//run.start();
		//run();
		this.chaveVinegere = chaveVinegere;
		this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
	}

	public void run() {
		this.implCliente = new ImplCliente(this.getClientes(), this.nome, chaveVinegere, chaveHmac, chaveAES);
		Thread t = new Thread(this.implCliente);
        t.start();
		for (int i = 0; i < porta.length;i++) {
			final int index = i;
			Thread threadConectar = new Thread(()->conectar(index));
			threadConectar.start();
        }
	}
	
	public void conectar(int i) {
		Socket socket = new Socket();
		while (!socket.isConnected()) {
			try {
	            socket = new Socket(ip, porta[i]);
	            inet = socket.getInetAddress();
	            System.out.println("Cliente " + this.nome + ": HostAddress = " + inet.getHostAddress());
	            System.out.println("Cliente " + this.nome + ": HostName = " + inet.getHostName());
	            ConexaoCliente c = new ConexaoCliente(socket, this.nome);
	            getClientes().add(c);
	            ImplCliente.contConexoes++;
	        } catch (IOException e) {
	        	System.err.println("Cliente " + this.nome + ": tentando se conectar na porta " +this.porta[i]+ " novamente em 5 segundos.");
	        	try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
	        	//System.err.println("Cliente " + this.nome + ": Erro: " + e.getMessage());
	        } 
			
		}
	}

	public List<ConexaoCliente> getClientes() {
		return clientes;
	}

	public void setClientes(List<ConexaoCliente> clientes) {
		this.clientes = clientes;
	}
}
