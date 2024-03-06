package Servidor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import Mensagem.Mensagem;

public class ConexaoServidor {
	Socket cliente;
	ObjectInputStream inputObject;
	ObjectOutputStream outputObject;
	String nomeCliente, nomeServer;
	String chaveVigenere, chaveHmac, chaveAES;
	
	public ConexaoServidor(Socket cliente, String nomeServer) throws IOException, ClassNotFoundException {
		this.inputObject = new ObjectInputStream(cliente.getInputStream());
        Mensagem mensagemRecebida = (Mensagem) inputObject.readObject();
        this.nomeCliente = mensagemRecebida.getConteudo();
        this.nomeServer = nomeServer;
		System.out.println("Servidor " + nomeServer + ": conexão " + ImplServidor.contConexoes + " com o cliente "
				+ nomeCliente + " em " + cliente.getInetAddress().getHostAddress() + "/"
				+ cliente.getInetAddress().getHostName());
    	outputObject = new ObjectOutputStream(cliente.getOutputStream());
		outputObject.writeObject(new Mensagem(nomeServer, null, nomeServer, "unicast"));
		this.cliente = cliente;
		
		
		enviarChaveVigenere();
		enviarChaveAES();
	}
	
	public void enviarChaveHmac() throws IOException {
		String chaveHmac = gerarChaveHmac();
		this.chaveHmac = chaveHmac;
		outputObject.writeObject(new Mensagem(nomeServer, null, chaveHmac, "chaveHmac"));
	}

	private String gerarChaveHmac() {
		return gerarStringAleatoria(16);
	}

	private void enviarChaveAES() throws IOException {
		String chaveAES = gerarChaveAES();
		this.chaveAES = chaveAES;
		outputObject.writeObject(new Mensagem(nomeServer, null, chaveAES, "chaveAES"));
	}

	private String gerarChaveAES() {
		return gerarStringAleatoria(16);
	}

	private void enviarChaveVigenere() throws IOException {
		String chaveVigenere = gerarChaveVigenere();	
		this.chaveVigenere = chaveVigenere;
		outputObject.writeObject(new Mensagem(nomeServer, null, chaveVigenere, "chaveVigenere"));
	}

	private String gerarChaveVigenere() {
		return gerarStringAleatoria(16);
	}

	public String gerarStringAleatoria(int tamanho) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }

        return sb.toString();
    }
}
