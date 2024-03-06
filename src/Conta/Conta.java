package Conta;

import java.io.Serializable;


public class Conta implements Serializable{
	private static final long serialVersionUID = 1L;
	String nome, cpf, endereco, telefone, email, senha;
	double saldo;

	public Conta(String email, String senha) {
		this.email = email;
		this.senha = senha;

	}
	
	public Conta(String nome, String cpf, String endereco, String telefone, String email, String senha) {
		this.nome = nome;
		this.cpf = cpf;
		this.endereco = endereco;
		this.telefone = telefone;
		this.email = email;
		this.senha = senha;

	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public double getSaldo() {
		return saldo;
	}

	public void setSaldo(double saldo) {
		this.saldo = saldo;
	}
	
	public String toString() {
        return "Conta{" +
                "nome='" + this.nome + '\'' +
                ", cpf=" + this.cpf +
                ", email=" + this.email +
                ", senha='" + this.senha +
                ", endereco='" + this.endereco +
                ", telefone='" + this.telefone +
                ", saldo='" + this.saldo + '\'' +
                '}';
    }
}
