package Servidor;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ConexaoServidor {
	Socket cliente;
	ObjectInputStream inputObject;
	ObjectOutputStream outputObject;
	String nomeCliente;
	
	public ConexaoServidor(Socket cliente, ObjectInputStream inputObject, ObjectOutputStream outputObject, String nomeCliente) {
		this.cliente = cliente;
		this.inputObject = inputObject;
		this.outputObject = outputObject;
		this.nomeCliente = nomeCliente;
	}


}
