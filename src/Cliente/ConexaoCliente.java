package Cliente;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import Cifra.Cifrador;
import Mensagem.Mensagem;

public class ConexaoCliente {
	private Socket socket;
	ObjectInputStream inputObject;
	ObjectOutputStream outputObject;
	private String nome, nomeServerConectado;
	private Mensagem mensagemAnterior;
	String chaveVigenere, chaveHmac, chaveAES;
	private Cifrador cifrador;
	

	public ConexaoCliente(Socket socket, String nome) {
		this.setSocket(socket);
		this.nome = nome;
		try {
			// Troca de mensagens padroes para saber o nome do servidor e o servidor saber o nome do cliente
			this.outputObject = new ObjectOutputStream(socket.getOutputStream());
			this.outputObject.writeObject(new Mensagem(this.nome, null, this.nome, "unicast"));
			this.inputObject = new ObjectInputStream(socket.getInputStream());
			Mensagem serverNome = (Mensagem) this.inputObject.readObject();
			System.out.println("Cliente " + this.nome + ": conectou-se ao servidor " + serverNome.getEmissor());
			this.setNomeServerConectado(serverNome.getEmissor());
			
			receberChaveVigenere();
			receberChaveAES();
			this.cifrador = new Cifrador(this.chaveVigenere, this.chaveAES);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void receberChaveHmac() throws ClassNotFoundException, IOException {
		Mensagem msg = (Mensagem) this.inputObject.readObject();
		this.chaveHmac = msg.getConteudo();
		this.cifrador.setChaveHmac(this.chaveHmac);
	}
	
	private void receberChaveAES() throws ClassNotFoundException, IOException {
		Mensagem msg = (Mensagem) this.inputObject.readObject();
		this.chaveAES = msg.getConteudo();
	}

	private void receberChaveVigenere() throws ClassNotFoundException, IOException {
		Mensagem msg = (Mensagem) this.inputObject.readObject();
		this.chaveVigenere = msg.getConteudo();
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public Mensagem getMensagemAnterior() {
		return mensagemAnterior;
	}

	public void setMensagemAnterior(Mensagem mensagemAnterior) {
		this.mensagemAnterior = mensagemAnterior;
	}

	public String getNomeServerConectado() {
		return nomeServerConectado;
	}

	public void setNomeServerConectado(String nomeServerConectado) {
		this.nomeServerConectado = nomeServerConectado;
	}

	public String getNome() {
		return nome;
	}

	public Cifrador getCifrador() {
		return cifrador;
	}
}
