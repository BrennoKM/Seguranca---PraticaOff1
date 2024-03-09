package Cliente;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import Cifra.Cifrador;
import Conta.Conta;
import Mensagem.Mensagem;

public class ImplCliente implements Runnable {
	List<ConexaoCliente> clientes;
	private boolean conexao = false, login = false;
	public static int contConexoes = 0;
	private String nome;
	String chaveVigenere, chaveHmac, chaveAES;
	Scanner in = new Scanner(System.in);

	public ImplCliente(List<ConexaoCliente> clientes, String nome) {
		this.clientes = clientes;
		this.nome = nome;
		/*
		 * this.chaveVigenere = chaveVigenere; this.chaveHmac = chaveHmac; this.chaveAES
		 * = chaveAES; cifrador = new Cifrador(chaveVigenere, chaveHmac, chaveAES);
		 */
	}

	public void run() {
		try {

			// Prepara para leitura do teclado
			Thread.sleep(1000);
			Mensagem mensagem = null;
			while (conexao == false) {
				if(clientes.size() != 0) {
					conexao = true;
				}
				//System.out.println("conexao ainda false");
				Thread.sleep(2000);
			}
			System.out.println(this.clientes.get(0));
			for (ConexaoCliente cliente : this.clientes) {
				Thread.sleep(500);
				logar(in, cliente);
			}
			Thread ouvirServidor = new Thread(() -> ouvirServer());
			ouvirServidor.start();

			while (conexao) {
				if (login) {
					Thread.sleep(300);
					System.out.println("Escolha uma ação: " + "\n1 - Unicast" + "\n2 - Broadcast" + "\n3 - Saque"
							+ "\n4 - Depósito" + "\n5 - Transferência" + "\n6 - Consultar saldo"
							+ "\n7 - Simular investimentos" + "\n8 - Encerrar conexão");
					int opcaoInt = 0;
					do {
						try {
							String opcaoString = in.nextLine();
							opcaoInt = Integer.valueOf(opcaoString);
						} catch (NumberFormatException | NoSuchElementException e) {
							System.err.println("Formato ou valor invalido");
						}
					} while (opcaoInt < 1 || opcaoInt > 7);
					String emissor = this.nome;
					String destinatario = "", conteudo = "", sufixo = "";
					switch (opcaoInt) {
					case 1:
						System.out.println("Digite o destinatario da mensagem: ");
						destinatario = in.nextLine();
						System.out.println("Digite o conteudo da mensagem: ");
						conteudo = in.nextLine();
						mensagem = new Mensagem(emissor, destinatario, conteudo, "unicast");
						break;
					case 2:
						System.out.println("Digite o conteudo da mensagem: ");
						conteudo = in.nextLine();
						mensagem = new Mensagem(emissor, "todos (Broadcast)", conteudo, "broadcast");
						break;
					case 3:
						sufixo = "do seu saque: ";
						conteudo = stringDouble(sufixo, in);
						mensagem = new Mensagem(emissor, "", conteudo, "saque");
						break;
					case 4:
						sufixo = "do seu deposito: ";
						conteudo = stringDouble(sufixo, in);
						mensagem = new Mensagem(emissor, "", conteudo, "deposito");
						break;
					case 5:
						System.out.println("Digite o pix do destinatario (email): ");
						destinatario = in.nextLine();
						sufixo = "do seu pix: ";
						conteudo = stringDouble(sufixo, in);
						mensagem = new Mensagem(emissor, destinatario, conteudo, "transferencia");
						break;
					case 6:
						mensagem = new Mensagem(emissor, "", "protocolo", "saldo");
						break;
					case 7:
						mensagem = new Mensagem(emissor, "", "protocolo", "investimentos");
						break;
					case 8:
						mensagem = new Mensagem(emissor, "", "fim", "fim");
						break;
					default:

						break;
					}

					for (ConexaoCliente cliente : this.clientes) {
						Cifrador cifrador = cliente.getCifrador();

						mensagem.setHmac(cifrador.calcularHmac(mensagem.getConteudo()));
						mensagem.setConteudo(cifrador.criptografar(mensagem.getConteudo()));

						cliente.setMensagemAnterior(mensagem);

						if (mensagem.getConteudo().equalsIgnoreCase("fim")) {
							conexao = false;
						} else {
							if (mensagem.getTipo().equals("unicast") || mensagem.getTipo().equals("broadcast")) {
								System.out.println("Cliente " + this.nome + ": enviando mensagem para "
										+ cliente.getNomeServerConectado() + " com destino em "
										+ mensagem.getDestinatario());
							} else {
								System.out.println("Cliente " + this.nome + ": enviando resquisição para "
										+ cliente.getNomeServerConectado() + " tipo --> " + mensagem.getTipo());
							}
							cliente.outputObject.writeObject(mensagem);
							// Mensagem msg = ouvirServerUmaVez(cliente);
							// System.out.println("Cliente " + this.nome + ": recebeu a mensagem: " + msg);
						}
					}
					Thread.sleep(400);
					// System.out.println("Logado looooppp");
				}
			}
			for (ConexaoCliente cliente : this.clientes) {
				System.out.println(
						"Cliente " + this.nome + ": finaliza conexão com " + cliente.getNomeServerConectado() + ".");
				cliente.inputObject.close();
				cliente.outputObject.close();
				cliente.getSocket().close();

			}
			in.close();
			System.out.println("Cliente " + this.nome + ": finaliza conexão.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String stringDouble(String sufixo, Scanner in) {
		boolean entradaValida = false;
		String valor = "";

		while (!entradaValida) {
			System.out.println("Digite o valor " + sufixo);
			valor = in.nextLine();

			try {
				double valorConvertido = Double.parseDouble(valor);
				if (valorConvertido > 0) {
					entradaValida = true;
				}
			} catch (NumberFormatException e) {
				System.out.println("Entrada inválida. Por favor, digite um número válido.");
			}
		}
		return valor;
	}

	private void logar(Scanner in, ConexaoCliente cliente) throws Exception {
		Mensagem mensagem = null;
		Cifrador cifrador = cliente.getCifrador();
		login = false;
		// Thread.sleep(100);
		while (!login) {
			if (!login) {
				System.out
						.println("Cliente " + this.nome + ": escolha uma opção de mensagem \n1 - Logar\n2 - Cadastrar");
				int opcaoInt = 0;
				do {
					try {
						String opcaoString = in.nextLine();
						opcaoInt = Integer.valueOf(opcaoString);
					} catch (NumberFormatException | NoSuchElementException e) {
						System.err.println("Formato ou valor invalido");
					}
				} while (opcaoInt < 1 || opcaoInt > 2);
				switch (opcaoInt) {
				case 1:
					mensagem = construirMensagemLogin("login", in, cifrador);
					break;
				case 2:
					mensagem = construirMensagemLogin("cadastro", in, cifrador);
					break;
				}

				mensagem.setDestinatario(cliente.getNomeServerConectado());
				cliente.setMensagemAnterior(mensagem);
				if (mensagem.getConteudo().equalsIgnoreCase("fim")) {
					conexao = false;
				} else {
					System.out.println("Cliente " + this.nome + ": enviando resquisição para "
							+ cliente.getNomeServerConectado() + " tipo --> " + mensagem.getTipo());
					cliente.outputObject.writeObject(mensagem);

					/*
					 * Mensagem msg = ouvirServerUmaVez(cliente); System.out.println("Cliente " +
					 * this.nome + ": recebeu a mensagem: " + msg); msg =
					 * ouvirServerUmaVez(cliente); System.out.println("Cliente " + this.nome +
					 * ": recebeu a mensagem: " + msg);
					 * 
					 * if (msg.getTipo().equals("logado")) { this.login = true; }
					 */
				}
				Mensagem msg = (Mensagem) cliente.inputObject.readObject();
				if (msg.getTipo().equals("logado")) {
					msg.setConteudo(cifrador.descriptografar(msg.getConteudo()));
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: " + msg);
					cliente.receberChaveHmac();
					this.login = true;
					//Thread.sleep(300);
					System.out.println("Chaves: vigenere=" + cliente.chaveVigenere + " aes=" + cliente.chaveAES
							+ " hmac=" + cliente.chaveHmac);
				} else if (msg.getTipo().equals("falha")) {
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: falha na autentificação");
				} else if (msg.getTipo().equals("chaveVigenere") || msg.getTipo().equals("chaveAES")
						|| msg.getTipo().equals("chaveHmac")) {

				} else {
					msg.setConteudo(cifrador.descriptografar(msg.getConteudo()));
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: " + msg);
				}
				Thread.sleep(500);

			}
			Thread.sleep(200);
		}
	}

	@SuppressWarnings("unused")
	private Mensagem ouvirServerUmaVez(ConexaoCliente cliente) throws Exception {
		Cifrador cifrador = cliente.getCifrador();
		Mensagem msg = (Mensagem) cliente.inputObject.readObject();
		msg.setConteudo(cifrador.descriptografar(msg.getConteudo()));
		return msg;
	}

	private void ouvirServer() {
		int numThreads = 0;
		while (true) {
			if (numThreads < contConexoes) {
				numThreads++;
				final int index = numThreads - 1;
				Thread threadServidor = new Thread(() -> {
					try {
						ouvindoServer(index);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
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

	private void ouvindoServer(int index) throws Exception {
		Cifrador cifrador = clientes.get(index).getCifrador();
		
		while (conexao) {
			// System.out.println("Whilleee conexaoo");
			if (login) {
				Mensagem msg = (Mensagem) clientes.get(index).inputObject.readObject();
				//System.out.println("Mensagem ouvindo server: " + msg + " " + msg.getTipo());
				if (msg.getTipo().equals("logado")) {
					msg.setConteudo(cifrador.descriptografar(msg.getConteudo()));
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: " + msg);
					clientes.get(index).receberChaveHmac();
					System.out.println("Chave: vigenere=" + clientes.get(index).chaveVigenere + " aes="
							+ clientes.get(index).chaveAES + " hmac=" + clientes.get(index).chaveHmac);
					this.login = true;
				} else if (msg.getTipo().equals("falha")) {
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: falha na autentificação");
				} else {
					msg.setConteudo(cifrador.descriptografar(msg.getConteudo()));
					System.out.println("Cliente " + this.nome + ": recebeu a mensagem: " + msg);
				}
			}
			Thread.sleep(1000);
		}
	}

	@SuppressWarnings("unused")
	private Mensagem construirMensagem(String tipo, Scanner in) {
		String emissor = this.nome;
		String destinatario;
		if (tipo.equals("unicast")) {
			System.out.println("Digite o destinatario da mensagem: ");
			destinatario = in.nextLine();
		} else {
			destinatario = "todos(Broadcast)";
		}
		System.out.println("Digite o conteudo da mensagem: ");
		String conteudo = in.nextLine();
		return new Mensagem(emissor, destinatario, conteudo, tipo);
	}

	private Mensagem construirMensagemLogin(String tipo, Scanner in, Cifrador cifrador) throws Exception {
		String emissor = this.nome;
		String email, senha, nome, cpf, endereco, telefone;
		Conta conta = null;
		if (tipo.equals("login")) {
			System.out.println("Digite o seu email: ");
			email = in.nextLine();
			System.out.println("Digite a sua senha: ");
			senha = in.nextLine();
			conta = new Conta(email, senha);
		} else {
			System.out.println("Digite o seu nome: ");
			nome = in.nextLine();
			System.out.println("Digite o seu cpf: ");
			cpf = in.nextLine();
			System.out.println("Digite o seu endereco: ");
			endereco = in.nextLine();
			System.out.println("Digite o seu telefone: ");
			telefone = in.nextLine();
			System.out.println("Digite o seu email: ");
			email = in.nextLine();
			System.out.println("Digite a sua senha: ");
			senha = in.nextLine();

			conta = new Conta(nome, cpf, endereco, telefone, email, senha);
		}
		conta.setSenha(cifrador.criptografar(senha));
		Mensagem msg = new Mensagem(emissor, "", "protocolo", tipo);
		msg.setConta(conta);
		// msg.setHmac(cifrador.calcularHmac(msg.getConteudo()));
		msg.setConteudo(cifrador.criptografar(msg.getConteudo()));
		return msg;
	}
}
