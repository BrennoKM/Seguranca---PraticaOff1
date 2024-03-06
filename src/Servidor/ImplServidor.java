package Servidor;

import java.io.IOException;
import java.util.List;

import Cifra.Cifrador;
import Cliente.ConexaoCliente;
import Conta.Conta;
import Conta.ContaManager;
import Mensagem.Mensagem;

public class ImplServidor implements Runnable {

	List<ConexaoCliente> clientesInternos;
	List<ConexaoServidor> conexoesServidor;
	ContaManager contas;

	public String nome;
	public static int contConexoes = 0;
	private boolean conexao = true;
	private static Mensagem ultimoBroadcast = new Mensagem(null, null, "", null),
			ultimoUnicast = new Mensagem(null, null, "", null);

	String chaveVigenere, chaveHmac, chaveAES;

	public ImplServidor(List<ConexaoCliente> clientesInternos, List<ConexaoServidor> conexoesServidor, String nome,
			String chaveVigenere, String chaveHmac, String chaveAES) throws Exception {
		this.clientesInternos = clientesInternos;
		this.conexoesServidor = conexoesServidor;
		this.nome = nome;

		this.chaveVigenere = chaveVigenere;
		this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
		// cifrador = new Cifrador(chaveVigenere, chaveHmac, chaveAES);
		contas = new ContaManager(chaveVigenere, chaveHmac, chaveAES);
		contas.carregarLista();
		// contas.printContas();
	}

	public void run() {
		int numThreads = 0;
		while (true) {
			if (numThreads < contConexoes) {
				numThreads++;
				final int index = numThreads - 1;
				Thread threadServidor = new Thread(() -> {
					try {
						servidorThread(index);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				threadServidor.start();
			}
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}

	private void servidorThread(int index) throws Exception {
		Mensagem mensagemRecebida = new Mensagem(null, null, "", null),
				mensagemAnterior = new Mensagem(null, null, "", null);
		Conta contaLogada = null;
		boolean autentificacao = false;
		Cifrador cifrador = new Cifrador(conexoesServidor.get(index).chaveVigenere,
				conexoesServidor.get(index).chaveAES);
		try {
			contaLogada = autentificarLoginCadastro(mensagemRecebida, index, cifrador);
			while (conexao) {
				contas.salvarLista();
				contas.carregarLista();
				mensagemRecebida = (Mensagem) conexoesServidor.get(index).inputObject.readObject();
				String descripMsg = cifrador.descriptografar(mensagemRecebida.getConteudo());
				autentificacao = cifrador.autentificarMensagem(conexoesServidor.get(index).chaveHmac, descripMsg,
						mensagemRecebida.getHmac());
				if (autentificacao == true) {
					String tipo = mensagemRecebida.getTipo();
					switch (tipo) {
					case "saque":
						saque(contaLogada, mensagemRecebida, index, cifrador);
						break;
					case "deposito":
						deposito(contaLogada, mensagemRecebida, index, cifrador);
						break;
					case "transferencia":
						transferencia(contaLogada, mensagemRecebida, index, cifrador);
						break;
					case "saldo":
						saldo(contaLogada, mensagemRecebida, index, cifrador);
						break;
					case "investimentos":
						investimentos(contaLogada, mensagemRecebida, index, cifrador);
						break;
					case "unicast":
						receptarUnicast(mensagemRecebida, index, mensagemAnterior, cifrador);
						break;
					case "broadcast":
						receptarBroadcast(mensagemRecebida, index, mensagemAnterior, cifrador);
						break;
					case "fim":
						conexao = false;
						break;
					default:
						System.out.println("Mensagem desconhecida: " + mensagemRecebida + " " + tipo);
						break;
					}
				} else {
					System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor()
							+ " não autentificado, hmacRecebido=" + mensagemRecebida.getHmac() + " chaveHmac="
							+ conexoesServidor.get(index).chaveHmac);
					conexoesServidor.get(index).outputObject.writeObject(
							new Mensagem(this.nome, "", cifrador.criptografar("Não autentificado"), "falha"));
				}
				Thread.sleep(300);
				mensagemAnterior = mensagemRecebida;
			}

			conexoesServidor.get(index).inputObject.close();
			conexoesServidor.get(index).outputObject.close();
			System.out
					.println("Fim do cliente " + conexoesServidor.get(index).cliente.getInetAddress().getHostAddress());
			conexoesServidor.get(index).cliente.close();

		} catch (IOException e) {
			e.getMessage();
		}

	}

	private void saque(Conta conta, Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		String descripMsg = cifrador.descriptografar(mensagemRecebida.getConteudo());
		double valorSaque = Double.parseDouble(descripMsg);
		Conta contaLogada = contas.buscarConta(conta.getEmail());
		double saldo = contaLogada.getSaldo();
		if (saldo >= valorSaque) {
			contaLogada.setSaldo(saldo - valorSaque);
			System.out.println("Servidor " + this.nome + ": Saque de " + valorSaque + " do cliente "
					+ mensagemRecebida.getEmissor() + " efetuado");
			conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "",
					cifrador.criptografar("Saque realizado, seu novo saldo é: " + contaLogada.getSaldo()), "saldo"));
		} else {
			System.out.println("Servidor " + this.nome + ": Saque de " + mensagemRecebida.getEmissor()
					+ " não foi efetuado, saldo insuficiente");
			conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "",
					cifrador.criptografar("Saldo insuficiente, seu saldo é: " + contaLogada.getSaldo()), "saldo"));
		}

	}

	private void deposito(Conta conta, Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		String descripMsg = cifrador.descriptografar(mensagemRecebida.getConteudo());
		double valorDeposito = Double.parseDouble(descripMsg);
		Conta contaLogada = contas.buscarConta(conta.getEmail());
		contaLogada.setSaldo(contaLogada.getSaldo() + valorDeposito);
		// contas.atualizarConta(contaLogada.getEmail(), contaLogada);
		System.out.println("Servidor " + this.nome + ": Deposito de " + valorDeposito + " do cliente "
				+ mensagemRecebida.getEmissor() + " efetuado");
		conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "",
				cifrador.criptografar("Deposito realizado, seu novo saldo é: " + contaLogada.getSaldo()), "deposito"));
		// contas.salvarLista();
		// contas.carregarLista();
	}

	private void transferencia(Conta conta, Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		String descripMsg = cifrador.descriptografar(mensagemRecebida.getConteudo());
		double valorTransferencia = Double.parseDouble(descripMsg);
		Conta favorecido = contas.buscarConta(mensagemRecebida.getDestinatario());
		Conta contaLogada = contas.buscarConta(conta.getEmail());
		if (favorecido != null) {
			if (contaLogada.getSaldo() >= valorTransferencia) {
				contaLogada.setSaldo(contaLogada.getSaldo() - valorTransferencia);
				favorecido.setSaldo(favorecido.getSaldo() + valorTransferencia);
				System.out.println("Servidor " + this.nome + ": Transferencia de " + valorTransferencia + " do cliente "
						+ mensagemRecebida.getEmissor() + " para a conta " + favorecido.getEmail() + " foi efetuada");
				conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "",
						cifrador.criptografar("Transferencia realizada, seu novo saldo é: " + contaLogada.getSaldo()),
						"transferencia"));
			} else {
				System.out.println("Servidor " + this.nome + ": Transferencia de " + valorTransferencia + " do cliente "
						+ mensagemRecebida.getEmissor() + " para a conta " + favorecido.getEmail()
						+ " não foi efetuada, saldo insuficiente");
				conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "", cifrador.criptografar(
						"Transferencia não foi realizada, seu saldo de " + contaLogada.getSaldo() + " é insuficiente"),
						"transferencia"));
			}
		} else {
			System.out.println("Servidor " + this.nome + ": Transferencia de " + valorTransferencia + " do cliente "
					+ mensagemRecebida.getEmissor() + " não foi efetuada, conta favorecida não existe");
			conexoesServidor.get(index).outputObject.writeObject(new Mensagem(this.nome, "",
					cifrador.criptografar("Transferencia não foi realizada, o favorecido da não existe"),
					"transferencia"));
		}
	}

	private void saldo(Conta contaLogada, Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		System.out.println("recebeu requisição saldo");
		System.out.println("Servidor " + this.nome + ": Usuario " + mensagemRecebida.getEmissor() + " saldo -> "
				+ contaLogada.getSaldo());
		conexoesServidor.get(index).outputObject.writeObject(
				new Mensagem(this.nome, "", cifrador.criptografar("Seu saldo é: " + contaLogada.getSaldo()), "saldo"));

	}

	public double calcularRendimento(double dinheiro, double taxa, int meses) {
		if (meses == 0) {
			return dinheiro;
		}
		return calcularRendimento(dinheiro * taxa, taxa, --meses);
	}

	private void investimentos(Conta conta, Mensagem mensagemRecebida, int index, Cifrador cifrador)
			throws Exception {
		System.out.println("Servidor " + this.nome + ": Simulando investimentos do cliente "
				+ mensagemRecebida.getEmissor() + "...");
		Conta contaLogada = contas.buscarConta(conta.getEmail());
		double saldoOriginal = contaLogada.getSaldo();
		double poupanca3 = calcularRendimento(saldoOriginal, 1.005, 3);
		double poupanca6 = calcularRendimento(poupanca3, 1.005, 3);
		double poupanca12 = calcularRendimento(poupanca6, 1.005, 3);

		double rendaFixa3 = calcularRendimento(saldoOriginal, 1.015, 3);
		double rendaFixa6 = calcularRendimento(rendaFixa3, 1.015, 3);
		double rendaFixa12 = calcularRendimento(rendaFixa6, 1.015, 3);

		String conteudo = String.format(
				"\n\tSaldo original: %.2f\n" + "\tPoupança após 3 meses: %.2f\n" + "\tPoupança após 6 meses: %.2f\n"
						+ "\tPoupança após 12 meses: %.2f\n\n" + "\tRenda fixa após 3 meses: %.2f\n"
						+ "\tRenda fixa após 6 meses: %.2f\n" + "\tRenda fixa após 12 meses: %.2f",
				saldoOriginal, poupanca3, poupanca6, poupanca12, rendaFixa3, rendaFixa6, rendaFixa12);

		conexoesServidor.get(index).outputObject
				.writeObject(new Mensagem(this.nome, "", cifrador.criptografar(conteudo), "investimentos"));

	}

	private Conta autentificarLoginCadastro(Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		boolean logado = false;
		Conta conta = null;
		while (!logado) {
			mensagemRecebida = (Mensagem) conexoesServidor.get(index).inputObject.readObject();
			// String descripMsg = cifrador.descriptografar(mensagemRecebida.getConteudo());
			// autentificacao = cifrador.autentificarMensagem(chaveHmac, descripMsg,
			// mensagemRecebida.getHmac());

			if (mensagemRecebida.getTipo().equals("cadastro")) {
				conta = fazerCadastro(mensagemRecebida, index, cifrador);
			} else if (mensagemRecebida.getTipo().equals("login")) {
				conta = fazerLogin(mensagemRecebida, index, cifrador);
			}

			if (conta == null) {
				logado = false;
			} else {
				logado = true;
				conexao = true;
			}
			Thread.sleep(300);
		}
		return conta;
	}

	private Conta fazerLogin(Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor() + " enviou um login:");
		String email = mensagemRecebida.getConta().getEmail();
		String senha = mensagemRecebida.getConta().getSenha();
		senha = cifrador.descriptografar(senha);
		System.out.println("\tEmail: " + email);
		System.out.println("\tSenha: " + senha);
		Conta conta = contas.buscarConta(email);

		if (conta == null) {
			System.out.println("Servidor " + this.nome + ": Conta inexistente");
			conexoesServidor.get(index).outputObject
					.writeObject(new Mensagem(this.nome, "", cifrador.criptografar("Conta inexistente"), "falha"));

		} else {
			if (conta.getSenha().equals(senha)) {
				System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor()
						+ " se logou com sucesso " + conta);
				System.out.println("Servidor " + this.nome + ": Chaves: vigenere="
						+ conexoesServidor.get(index).chaveVigenere + " aes=" + conexoesServidor.get(index).chaveAES
						+ " hmac=" + conexoesServidor.get(index).chaveHmac);
				conexoesServidor.get(index).outputObject.writeObject(
						new Mensagem(this.nome, "", cifrador.criptografar("Logado com sucesso"), "logado"));
				conexoesServidor.get(index).enviarChaveHmac();

			} else {
				System.out.println(
						"Servidor " + this.nome + ": " + mensagemRecebida.getEmissor() + " digitou a senha incorreta");
				conexoesServidor.get(index).outputObject
						.writeObject(new Mensagem(this.nome, "", cifrador.criptografar("Senha incorreta"), "falha"));

			}
		}
		return conta;
	}

	private Conta fazerCadastro(Mensagem mensagemRecebida, int index, Cifrador cifrador) throws Exception {
		System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor() + " enviou um cadastro:");
		String email = mensagemRecebida.getConta().getEmail();
		String senha = mensagemRecebida.getConta().getSenha();
		senha = cifrador.descriptografar(senha);
		System.out.println("\tEmail: " + email);
		System.out.println("\tSenha: " + senha);
		Conta conta = contas.buscarConta(email);

		if (conta == null) {
			conta = mensagemRecebida.getConta();
			conta.setSenha(senha);
			contas.adicionarConta(email, conta);

			System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor()
					+ " se cadastrou com sucesso na " + conta);
			System.out.println("Servidor " + this.nome + ": Chaves: vigenere="
					+ conexoesServidor.get(index).chaveVigenere + " aes=" + conexoesServidor.get(index).chaveAES
					+ " hmac=" + conexoesServidor.get(index).chaveHmac);
			conexoesServidor.get(index).outputObject.writeObject(
					new Mensagem(this.nome, "", cifrador.criptografar("Conta cadastrada com sucesso"), "logado"));
			conexoesServidor.get(index).enviarChaveHmac();
		} else {
			System.out.println("Servidor " + this.nome + ": Email já está cadastrado ");
			conexoesServidor.get(index).outputObject.writeObject(
					new Mensagem(this.nome, "", cifrador.criptografar("Email já está cadastrado"), "falha"));

		}
		return conta;
	}

	private void receptarUnicast(Mensagem mensagemRecebida, int index, Mensagem mensagemAnterior, Cifrador cifrador)
			throws Exception {
		if (mensagemAnterior != null && !(mensagemAnterior.getConteudo().equals(mensagemRecebida.getConteudo()))) {
			if (mensagemRecebida.getDestinatario() != null && mensagemRecebida.getDestinatario().equals(this.nome)) {
				if (mensagemRecebida.getConteudo().equalsIgnoreCase("fim")) {
					conexao = false;
				} else {
					if (!ultimoUnicast.getConteudo().equals(mensagemRecebida.getConteudo())) {
						System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor() + " --> "
								+ cifrador.descriptografar(mensagemRecebida.getConteudo()));
						mensagemRecebida.setConteudo(cifrador.descriptografar(mensagemRecebida.getConteudo()));
						encaminharMensagem(mensagemRecebida, index);
					}
					ultimoUnicast = mensagemRecebida;
				}
			} else if (!this.nome.equals(mensagemRecebida.getEmissor())) {
				if (!this.nome.equals(mensagemRecebida.getDestinatario())) {
					mensagemRecebida.setConteudo(cifrador.descriptografar(mensagemRecebida.getConteudo()));
					encaminharMensagem(mensagemRecebida, index);
				}
			} else {
				System.out.println("Servidor " + this.nome + ": destinatario não encontrado");
			}
		}
	}

	private void receptarBroadcast(Mensagem mensagemRecebida, int index, Mensagem mensagemAnterior, Cifrador cifrador)
			throws Exception {
		if (mensagemAnterior != null && !(mensagemAnterior.getConteudo().equals(mensagemRecebida.getConteudo()))) {
			if (mensagemRecebida.getConteudo().equalsIgnoreCase("fim"))
				conexao = false;
			else if (!this.nome.equals(mensagemRecebida.getEmissor())) { // evita que o mesmo emissor repasse sua propia
																			// mensagem
				if (!ultimoBroadcast.getConteudo().equals(mensagemRecebida.getConteudo())) {
					System.out.println("Servidor " + this.nome + ": " + mensagemRecebida.getEmissor() + " --> "
							+ cifrador.descriptografar(mensagemRecebida.getConteudo()));
					mensagemRecebida.setConteudo(cifrador.descriptografar(mensagemRecebida.getConteudo()));
					encaminharBroadcast(mensagemRecebida, index);
				}
				ultimoBroadcast = mensagemRecebida;
			}
		}
	}

	private void encaminharMensagem(Mensagem mensagemRecebida, int index) throws Exception {
		String receptor = mensagemRecebida.getUltimoReceptor();
		for (ConexaoServidor conexaoServidor : conexoesServidor) {
			try {
				if (!conexaoServidor.nomeCliente.equals(mensagemRecebida.getEmissor())) {
					if (!conexaoServidor.nomeCliente.equals(receptor)) {
						if (mensagemRecebida.getDestinatario() != null
								&& mensagemRecebida.getDestinatario().equals(conexaoServidor.nomeCliente)) {
							System.out.println("Servidor " + this.nome + ": encaminhando mensagem de "
									+ mensagemRecebida.getEmissor() + " para " + conexaoServidor.nomeCliente
									+ " com destino em " + mensagemRecebida.getDestinatario() + " e ultimo receptor "
									+ receptor);
							Cifrador cifrador = new Cifrador(conexaoServidor.chaveVigenere,
									conexaoServidor.chaveAES);
							
							mensagemRecebida.setConteudo(cifrador.criptografar(mensagemRecebida.getConteudo()));
							mensagemRecebida.setUltimoReceptor(this.nome);
							conexaoServidor.outputObject.writeObject(mensagemRecebida);

						}

					}

				}

			} catch (IOException e) {
				System.out.println(e);
			}
		}

	}

	private void encaminharBroadcast(Mensagem mensagemRecebida, int index) throws Exception {
		String receptor = mensagemRecebida.getUltimoReceptor();
		for (ConexaoServidor conexaoServidor : conexoesServidor) {
			try {
				if (!conexaoServidor.nomeCliente.equals(mensagemRecebida.getEmissor())) {
					if (!conexaoServidor.nomeCliente.equals(receptor)) {
						if (mensagemRecebida.getDestinatario() != null) {
							System.out.println("Servidor " + this.nome + ": encaminhando mensagem de "
									+ mensagemRecebida.getEmissor() + " para " + conexaoServidor.nomeCliente
									+ " com destino em " + mensagemRecebida.getDestinatario() + " e ultimo receptor "
									+ receptor);
							Cifrador cifrador = new Cifrador(conexaoServidor.chaveVigenere,
									conexaoServidor.chaveAES);
							
							mensagemRecebida.setConteudo(cifrador.criptografar(mensagemRecebida.getConteudo()));
							mensagemRecebida.setUltimoReceptor(this.nome);
							conexaoServidor.outputObject.writeObject(mensagemRecebida);

						}

					}

				}

			} catch (IOException e) {
				System.out.println(e);
			}
		}
	}
}
