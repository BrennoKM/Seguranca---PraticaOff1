package Cifra;

public class CifraVigenere {

    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+-={}[]:\\\";'<>?,./";

    public static String criptografar(String chave, String mensagem) {
        StringBuilder mensagemCifrada = new StringBuilder();
        int tamanhoChave = chave.length();
        int tamanhoMensagem = mensagem.length();
        for (int i = 0; i < tamanhoMensagem; i++) {
            char caractere = mensagem.charAt(i);
            int indiceCaractere = CARACTERES.indexOf(caractere);
            if (indiceCaractere != -1) {
                char letraChave = chave.charAt(i % tamanhoChave);
                int indiceChave = CARACTERES.indexOf(letraChave);
                int novoIndice = (indiceCaractere + indiceChave) % CARACTERES.length();
                mensagemCifrada.append(CARACTERES.charAt(novoIndice));
            } else {
                mensagemCifrada.append(caractere);
            }
        }
        return mensagemCifrada.toString();
    }


    public static String descriptografar(String chave, String mensagemCifrada) {
        StringBuilder mensagemOriginal = new StringBuilder();
        int tamanhoChave = chave.length();
        for (int i = 0; i < mensagemCifrada.length(); i++) {
            char caractere = mensagemCifrada.charAt(i);
            int indiceCaractere = CARACTERES.indexOf(caractere);
            if (indiceCaractere != -1) {
                char letraChave = chave.charAt(i % tamanhoChave);
                int indiceChave = CARACTERES.indexOf(letraChave);
                int novoIndice = (indiceCaractere - indiceChave + CARACTERES.length()) % CARACTERES.length();
                mensagemOriginal.append(CARACTERES.charAt(novoIndice));
            } else {
                mensagemOriginal.append(caractere);
            }
        }
        return mensagemOriginal.toString();
    }

    public static void main(String[] args) {
        String mensagemOriginal = "Mensagem com caracteres especiais: !@#$%^&*()_+-={}[]:\";'<>?,./";
        String chave = "chavechavechavechave";
        String mensagemCifrada = criptografar(chave, mensagemOriginal);
        System.out.println("Mensagem cifrada: " + mensagemCifrada);
        String mensagemDescriptografada = descriptografar(chave, mensagemCifrada);
        System.out.println("Mensagem descriptografada: " + mensagemDescriptografada);
    }
}

