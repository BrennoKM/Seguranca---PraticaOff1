package Conta;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

import Cifra.Cifrador;

public class ContaManager implements Serializable{
	private static final long serialVersionUID = 1L;
	private Map<String, Conta> mapaContas;
    private final String arquivo = "contas.ser";
    String chaveVigenere = "chaveVigenere";
	String chaveHmac = "chaveHmac";
	String chaveAES = "chaveAES12341234";
    Cifrador cifrador;
    
    public ContaManager(String chaveVigenere, String chaveHmac, String chaveAES) throws Exception {
        mapaContas = new HashMap<>();
        this.chaveVigenere = chaveVigenere;
		this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
		cifrador = new Cifrador(this.chaveVigenere, this.chaveHmac, this.chaveAES);
		
		
		//System.out.println(cifrador.descriptografar("g0XgXoe+stE7tsfrA8N1qQ=="));
		//String email = cifrador.descriptografar("/EojTzc2PV56n5igCGnEzA==");
		//System.out.println(email + " Variavel");
    }

    public void adicionarConta(String email, Conta conta) throws Exception {
        mapaContas.put(email, conta);
    }

    public Conta buscarConta(String email) throws Exception {
    	if (mapaContas.containsKey(email)) {
	    	Conta conta = mapaContas.get(email);
	        return conta;
    	}
    	return null;
    }

    public void removerConta(String email) throws Exception {
        mapaContas.remove(email);
        salvarLista();
    }

    public void atualizarConta(String email, Conta novaConta) throws Exception {
        if (mapaContas.containsKey(email)) {
        	adicionarConta(email, novaConta);
        } else {
            System.out.println("Conta não encontrada.");
        }
    }
    
    @SuppressWarnings("unchecked")
	public void carregarLista() throws Exception {
        try (FileInputStream fileIn = new FileInputStream(arquivo);
             ObjectInputStream in = new ObjectInputStream(fileIn)) {
            mapaContas = (HashMap<String, Conta>) in.readObject();
            for (Map.Entry<String, Conta> entry : mapaContas.entrySet()) {
        		Conta conta = entry.getValue();
        		//conta.setEmail(cifrador.descriptografar(conta.getEmail()));
            	conta.setSenha(cifrador.descriptografar(conta.getSenha()));
            }
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void salvarLista() throws Exception {
        try (FileOutputStream fileOut = new FileOutputStream(arquivo);
             ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
        	for (Map.Entry<String, Conta> entry : mapaContas.entrySet()) {
        		Conta conta = entry.getValue();
        		//conta.setEmail(cifrador.criptografar(conta.getEmail()));
            	conta.setSenha(cifrador.criptografar(conta.getSenha()));
            }
            out.writeObject(mapaContas);
            //System.out.println("Lista de contas salva em: " + new File(arquivo).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void printContas() {
    	System.out.println("\n\n");
		for (Map.Entry<String, Conta> entry : mapaContas.entrySet()) {
    		System.out.println(entry.getValue());
        }
		
	}

    public Map<String, Conta> getMapaContas(){
    	return this.mapaContas;
    }
    
    public static void main(String[] args) throws Exception {
    	final String chaveVinegere = "chaveVigenere";
		final String chaveHmac = "chaveHmac";
		final String chaveAES = "chaveAES12341234";
        ContaManager manager = new ContaManager(chaveVinegere, chaveHmac, chaveAES);
        
        // Carregar lista do arquivo
        manager.carregarLista();

        // Adicionar ou manipular contas
        //manager.adicionarConta("davi@gmail.com", new Conta("Davi", "12345678900", "bem ali do meu lado", "12345", "davi@gmail.com", "qwe1234"));
        //manager.adicionarConta("brennokm@gmail.com", new Conta("Brenno", "98765432100", "bem ali", "1234", "brennokm@gmail.com", "qwe123"));
        //manager.adicionarConta("icaro@gmail.com", new Conta("Icaro", "98765432109", "bem ali pertinho", "123466", "icaro@gmail.com", "qwe12"));
        //Conta contaBusca = manager.buscarConta("98765432109");
        //contaBusca.setSaldo(95);
        //manager.atualizarConta("98765432109", contaBusca);
        // Salvar lista no arquivo
        //manager.salvarLista();

        // Buscar conta pelo CPF
        Conta conta = manager.buscarConta("icaro@gmail.com");
        if (conta != null) {
            System.out.println("Conta encontrada: " + conta);
        } else {
            System.out.println("Conta não encontrada.");
        }
        manager.printContas();
    }
}


