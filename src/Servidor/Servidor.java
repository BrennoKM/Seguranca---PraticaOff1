package Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Cliente.Cliente;
import Cliente.ConexaoCliente;
import Mensagem.Mensagem;


public class Servidor implements Runnable{

	ServerSocket socketServidor;
    List<ConexaoCliente> clientesInternos = new ArrayList<ConexaoCliente>();
    List<ConexaoServidor> conexoesServidor = new ArrayList<ConexaoServidor>();
    String nome;
    int porta;
    String chaveVinegere, chaveHmac, chaveAES;
    
	public Servidor(String nome, int porta, Cliente cliente) {
		this.nome = nome;
		this.porta = porta;
		if(cliente != null) {
			this.clientesInternos = cliente.getClientes();
		}
		//Thread run = new Thread(() -> run());
		//run.start();
	}

	public Servidor(String nome, int porta, Cliente cliente, String chaveVinegere, String chaveHmac, String chaveAES) {
		this.nome = nome;
		this.porta = porta;
		if(cliente != null) {
			this.clientesInternos = cliente.getClientes();
		}
		this.chaveVinegere = chaveVinegere;
		this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
	}

	public void run() {
		try {
            socketServidor = new ServerSocket(porta);
            System.out.println("Servidor " + this.nome + ": rodando na porta " + socketServidor.getLocalPort());
            System.out.println("Servidor " + this.nome + ": HostAddress = " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Servidor " + this.nome + ": HostName = " + InetAddress.getLocalHost().getHostName());

            System.out.println("Servidor " + this.nome + ": aguardando conexão do cliente...");
            
            ImplServidor servidor = new ImplServidor(this.clientesInternos, this.conexoesServidor, this.nome, this.chaveVinegere, this.chaveHmac, this.chaveAES);
            Thread t = new Thread(servidor);
            t.start();
            
            while (true) {
                Socket cliente = socketServidor.accept();
                
            	ObjectInputStream inputObject = new ObjectInputStream(cliente.getInputStream());
                
                Mensagem mensagemRecebida = (Mensagem) inputObject.readObject();

    			System.out.println("Servidor " + this.nome + ": conexão " + ImplServidor.contConexoes + " com o cliente "
    					+ mensagemRecebida.getConteudo() + " em " + cliente.getInetAddress().getHostAddress() + "/"
    					+ cliente.getInetAddress().getHostName());

            	ObjectOutputStream outputObject = new ObjectOutputStream(cliente.getOutputStream());
    			outputObject.writeObject(new Mensagem(this.nome, null, this.nome, "unicast"));
               
                
                ConexaoServidor cs = new ConexaoServidor(cliente, inputObject, outputObject, mensagemRecebida.getConteudo());
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
