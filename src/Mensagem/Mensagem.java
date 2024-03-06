package Mensagem;

import java.io.Serializable;

import Conta.Conta;

public class Mensagem implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String emissor, destinatario, conteudo, tipo, ultimoReceptor, hmac;
	private Conta conta;
	
	public Mensagem(String emissor, String destinatario, String conteudo, String tipo) {
		this.emissor = emissor;
		this.destinatario = destinatario;
		this.conteudo = conteudo;
		this.tipo = tipo;
	}
	
	public String getEmissor() {
		return this.emissor;
	}
	
	public String getDestinatario() {
		return this.destinatario;
	}
	
	public void setDestinatario(String destinatario) {
		this.destinatario = destinatario;
	}
	
	public String getConteudo() {
		return this.conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	public String getTipo() {
		return this.tipo;
	}
	
	public String toString() {
		return this.emissor + " --> " + this.conteudo;
	}

	public String getUltimoReceptor() {
		return ultimoReceptor;
	}

	public void setUltimoReceptor(String ultimoReceptor) {
		this.ultimoReceptor = ultimoReceptor;
	}

	public String getHmac() {
		return this.hmac;
	}
	
	public void setHmac(String hmac) {
		this.hmac = hmac;
	}

	public Conta getConta() {
		return conta;
	}

	public void setConta(Conta conta) {
		this.conta = conta;
	}
}
