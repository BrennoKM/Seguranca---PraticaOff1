package Cifra;

public class Cifrador {
	String chaveVinegere, chaveAES;
	String chaveHmac = "chaveHmacfalsificada";;
	// String chaveVinegere = "chaveVigenere";
	// String chaveHmac = "chaveHmac";
	// String chaveAES = "chaveAES12341234";
	public Cifrador(String chaveVinegere, String chaveHmac, String chaveAES) {
		this.chaveVinegere = chaveVinegere;
		//this.chaveHmac = chaveHmac;
		this.chaveAES = chaveAES;
	}

	public Cifrador(String chaveVinegere, String chaveAES) {
		this.chaveVinegere = chaveVinegere;
		this.chaveAES = chaveAES;
	}

	public Cifrador() {

	}

	public String criptografar(String conteudo) throws Exception {
		String cripVinegere = CifraVigenere.criptografar(this.chaveVinegere, conteudo);
		String cripAES = CifraAES.criptografar(this.chaveAES, cripVinegere);
		return cripAES;
	}

	public String descriptografar(String conteudo) throws Exception {
		String descripAES = CifraAES.descriptografar(chaveAES, conteudo);
		String descriptVigenere = CifraVigenere.descriptografar(chaveVinegere, descripAES);
		return descriptVigenere;
	}

	public String calcularHmac(String mensagem) throws Exception {
		return Hmac.calcular(chaveHmac, mensagem);
	}

	public boolean autentificarMensagem(String chaveHmac, String descriptVigenere, String hmacRecebido)
			throws Exception {
		return Hmac.verificar(chaveHmac, descriptVigenere, hmacRecebido);
	}
	
	public void setChaveHmac(String chaveHmac) {
		this.chaveHmac = chaveHmac;
	}

	public String getChaveAES() {
		return this.chaveAES;
	}

	public static void main(String[] args) throws Exception {
		Cifrador c = new Cifrador();
		String mensagem = "dJLfiXxYxIydSWs7";
		System.out.println(c.descriptografar(mensagem));
	}

	
}
