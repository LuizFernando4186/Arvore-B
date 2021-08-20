import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Stack;

public class ArvoreB {

	private static final int NULL = -1;
	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private static final int TAMANHO_CABECALHO = 4;
	private String nomeArq;
	private No raiz;
	private int t;
	private int TAMANHO_NO_INTERNO;
	private int TAMANHO_NO_FOLHA;
	private int NUM_MAX_CHAVES;
	private int NUM_MAX_FILHOS;
	private boolean impressaoAtiva = false;

	private void inicializaConstantes(int t) {
		this.t = t;
		TAMANHO_NO_INTERNO = 2 * 4 + 4 * (2 * t - 1) + 4 * (2 * t);
		TAMANHO_NO_FOLHA = 2 * 4 + 4 * (2 * t - 1);
		NUM_MAX_CHAVES = 2 * t - 1;
		NUM_MAX_FILHOS = NUM_MAX_CHAVES + 1;
	}

	// Construtor da árvore B.
	public ArvoreB(int ordem, String nomeArq) throws IOException {

		this.nomeArq = nomeArq;
		inicializaConstantes(ordem);

		// Verifica se o arquivo existe.
		if (!new File(nomeArq).exists()) {

			No no = new No(true, t);
			no.endereco = TAMANHO_CABECALHO;
			trocaRaiz(no);
			atualizaNo(no);

		} else {

			// Caso exista apenas lê o endereço que está localizado o nó raiz;
			carregaRaizNaRAM();

		}
	}

	// *******************************************
	// 					RAIZ
	// *******************************************

	private void trocaRaiz(No novaRaiz) throws FileNotFoundException, IOException {

		RandomAccessFile arq = new RandomAccessFile(nomeArq, "rw");
		this.raiz = novaRaiz;
		arq.writeInt(this.raiz.endereco);
		arq.close();

	}

	private void carregaRaizNaRAM() throws FileNotFoundException, IOException {
		RandomAccessFile arq = new RandomAccessFile(nomeArq, "r");
		this.raiz = leNo(arq.readInt());
		arq.close();
	}

	// *******************************************
	// 					LEITURA NO
	// *******************************************

	public No leNo(int endereco) throws IOException {

		RandomAccessFile arq = new RandomAccessFile(nomeArq, "r");

		// Se tiver nada devolve null;
		if (arq.length() == 0 || endereco == NULL) {
			arq.close();
			return null;
		}

		// Caso contrário coloque o ponteiro na folha e escreva as folhas;
		arq.seek(endereco);

		// Le o primeiro campo para verificar se é folha
		boolean ehFolha = arq.readInt() == TRUE ? true : false;

		// Le os bytes de uma vez, criando vetor de bytes e ler, o -4 é pq leu o
		// primeiro byte;
		byte[] bytes = ehFolha ? new byte[TAMANHO_NO_FOLHA - 4] : new byte[TAMANHO_NO_INTERNO - 4];

		// Le os bytes de uma vez.
		arq.read(bytes);

		// Le o vetor de bytes em vez do arquivo
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		No no = new No(ehFolha, t);
		no.nChaves = leInt(in);
		no.endereco = endereco;

		// Carrega as chaves
		for (int i = 0; i < no.chaves.length; i++) {
			no.chaves[i] = leInt(in);
		}

		// Se não for folha, então carrega os filhos
		if (!ehFolha) {
			for (int i = 0; i < no.filhos.length; i++) {
				no.filhos[i] = leInt(in);
			}
		}

		arq.close();
		return no;
	}

	private int leInt(ByteArrayInputStream in) {
		byte[] bInt = new byte[4];
		in.read(bInt, 0, 4);// Coloca os 4 bytes na posição 0;
		return ByteBuffer.wrap(bInt).asIntBuffer().get();// converte os bytes em int.
	}

	private void escreveInt(ByteArrayOutputStream out, int i) {
		byte[] num = ByteBuffer.allocate(4).putInt(i).array();
		out.write(num, 0, 4);
	}

	// *******************************************
	// 			ATUALIZACAO/GRAVACAO NO
	// *******************************************

	// Assume que o "nó" ja tem um endereco
	private void atualizaNo(No no) throws IOException {

		//System.out.println("\n------------ ATUALIZANDO NÓ -----------");
		//no.imprime();
		 
		int nBytes = no.ehFolha() ? TAMANHO_NO_FOLHA : TAMANHO_NO_INTERNO;
		ByteArrayOutputStream out = new ByteArrayOutputStream(nBytes);
		escreveInt(out, no.folha);
		escreveInt(out, no.nChaves);

		for (int i = 0; i < no.chaves.length; i++) {
			escreveInt(out, no.chaves[i]);
		}

		if (!no.ehFolha()) {
			for (int i = 0; i < no.filhos.length; i++) {
				escreveInt(out, no.filhos[i]);
			}
		}

		RandomAccessFile arq = new RandomAccessFile(nomeArq, "rw");
		arq.seek(no.endereco);
		arq.write(out.toByteArray());
		arq.close();
	}

	// apos chamar a funcao, o "no" terah um endereco
	private int gravaNovoNo(No no) throws IOException {

		/*
		 * obs 1: "new File" nao cria um arquivo novo, apenas carrega informacoes sobre
		 * o arquivo ja existente obs 2: o novo no sera gravado no final do arquivo (ou
		 * seja, vai aumentar o tamanho do arq qdo chamar "gravaNovoNo")
		 */

		// System.out.println("--------- gravaNovoNo -----------");
		no.endereco = (int) new File(nomeArq).length();
		atualizaNo(no);

		// System.out.println("Nó: " + no + " Posicao end: " + no.endereco);

		return no.endereco;
	}

	// ********************************
	// 				SPLIT
	// ********************************

	/*
	 * Separação os nó; Tamanho do Nó folha 2*t-1 e Nó filho 2*t;
	 */
	private void split(No x, int i, No y) throws IOException {

		// System.out.println("\n !!!! SPLIT !!!\n");

		boolean yEhFolha = y.ehFolha();// Passa um valor booleano do Nó y.
		No auxiliar = new No(yEhFolha, t);
		auxiliar.nChaves = t - 1;

		// Copia as chaves
		for (int j = 0; j < t - 1; j++) {
			// System.out.println(" Copiando as chaves \t" + j);
			auxiliar.chaves[j] = y.chaves[j + t];
		}

		// Copia os filhos
		if (!y.ehFolha()) {
			for (int j = 0; j < t; j++) {
				// System.out.println(" Copiando os filhos \t" + j);
				auxiliar.filhos[j] = y.filhos[j + t];
			}
		}

		y.nChaves = t - 1;

		for (int j = x.nChaves; j >= i + 1; j--) {
			// System.out.println(" Arrastando os filhos \t" + j);
			x.filhos[j + 1] = x.filhos[j];
		}

		x.filhos[i + 1] = gravaNovoNo(auxiliar);// Usar o gravaNovoNo

		// Arrasta as chaves para "abrir espaco"
		for (int j = x.nChaves - 1; j >= i; j--) {
			// System.out.println(" Arrastando as chaves \t" + j);
			x.chaves[j + 1] = x.chaves[j];
		}

		x.chaves[i] = y.chaves[t - 1];
		x.nChaves = x.nChaves + 1;

		// Apenas atualiza os Nó no arquivo.
		atualizaNo(y);
		atualizaNo(x);
	}

	// **********************************
	// 				INSERCAO
	// **********************************

	// Insere valor da chave;
	public void insereChave(int key) throws IOException {

		// System.out.println("Inserindo Chave: " + key);
		No r = raiz;

		// Verifica se a raiz está cheia, se estiver divide o Nó raiz.
		if (r.nChaves == NUM_MAX_CHAVES) {

			No pai = new No(false, t);// Cria um pai para a raiz;
			gravaNovoNo(pai);
			trocaRaiz(pai);

			pai.nChaves = 0;
			pai.filhos[0] = gravaNovoNo(r);// primeiro filho dele é o r;
			split(pai, 0, r);
			insereNo_NoFull(pai, key);

		} else {
			insereNo_NoFull(r, key);
		}

	}

	private void insereNo_NoFull(No x, int key) throws IOException {

		// System.out.println("...Inserindo em No_NoFull");

		if (x.ehFolha()) {

			int i = 0;

			for (i = x.nChaves - 1; i >= 0 && key < x.chaves[i]; i--) {
				x.chaves[i + 1] = x.chaves[i];
			}

			x.chaves[i + 1] = key;
			x.nChaves++;

			atualizaNo(x);

		} else {

			int i = 0;

			for (i = x.nChaves - 1; i >= 0 && key < x.chaves[i]; i--) {}

			i++;

			No temp = leNo(x.filhos[i]);

			if (temp.nChaves == NUM_MAX_CHAVES) {

				split(x, i, temp);

				if (key > x.chaves[i]) {
					i++;
				}
			}

			insereNo_NoFull(leNo(x.filhos[i]), key);

		}

	}

	// **************************************
	// 				BUSCA
	// **************************************

	// Verificar se está presente usando o busca.
	public boolean buscaB(int k) throws IOException {

		if (this.buscaB(raiz, k) != null) {
			return true;
		} else {
			return false;
		}

	}

	private No buscaB(No no, int chave) throws IOException {

		int i = 0;

		if (no == null) {// Verifica se o no é null;
			return no;
		}

		for (i = 0; i < no.nChaves; i++) {

			if (chave < no.chaves[i])
				break;

			if (chave == no.chaves[i])
				return no;

		}

		// Verifica se é folha.
		if (no.ehFolha()) {
			return null;

			// Caso contrário lê o nó da árvore.
		} else {
			// Chamar recursivamente, antes disso precisa ler o índice i;
			return buscaB(leNo(no.filhos[i]), chave);
		}
	}

	// **************************************
	// 				REMOCAO
	// **************************************

	// Método para ser chamado no Main

	public void remove(int chave) throws IOException {

		if(impressaoAtiva) System.out.print(" = " + chave);

		No no = buscaB(raiz, chave);
		//System.out.println(" -- Endereco: " + no);

		if (no != null)
			removeChave(raiz, chave);
			
		//System.out.println("\n\n\t!!!!!!!! FIM REMOÇÃO - CHAVE: " + chave + "!!!!!!\n");

	}

	// Método para decidir qual tipo de remocao deve ocorrer

	private void removeChave(No no, int chave) throws IOException {

		// Primeiro busca a posicao da chave no array de chaves do No Atual
		int posicaoChave = no.buscaChave(chave);

		if(impressaoAtiva) System.out.println("\n\n\t!!!!!!!! INICIANDO REMOÇÃO - NO: " + no.endereco +
			 " - BUSCANDO CHAVE: " + chave + " ---> Posicao: " + posicaoChave + " !!!!!!!!\n");
		

		// Se a chave estiver no NÓ
		if (posicaoChave != NULL) {

			// CASO 1 = Remove de um NÓ FOLHA
			if (no.ehFolha()) {
				removeChave_noFolha(no, posicaoChave);
				return;
			}

			// CASO 2 = Remove de um NÓ INTERNO
			removeChave_noInterno(no, posicaoChave);
			return;

		}

		// CASO 3 = Se a chave NÃO estiver no NÓ
		removeChave_naoEstaNo(no, chave);

	}

	// **************************************
	// 				CASO 1
	// **************************************

	private void removeChave_noFolha(No no, int posicaoChave) throws IOException {

		if(impressaoAtiva) System.out.println("\tCASO 1 = ...Removendo de um NO FOLHA");

		for (; posicaoChave < no.nChaves - 1; posicaoChave++)
			no.chaves[posicaoChave] = no.chaves[posicaoChave + 1];

		no.nChaves--; // Decrementa a quantidade
		atualizaNo(no); // Atualiza no DISCO
	}

	// **************************************
	// 				CASO 2
	// **************************************

	private void removeChave_noInterno(No no, int posicaoChave) throws IOException {

		if(impressaoAtiva) System.out.print("\tCASO 2 = ...Removendo de um NO INTERNO");

		int chaveRemovida = no.chaves[posicaoChave];

		// CASO 2a
		// O filho à ESQUERDA da CHAVE tem pelo menos T chaves
		No filhoEsquerda = getFilhoEsquerda(no, posicaoChave);

		if (filhoEsquerda.temMaisqueMinimo()) {
			
			if(impressaoAtiva){
				System.out.println("\t--> CASO 2a");
				System.out.println("** Numero Chaves FILHO ESQUERDA: " + filhoEsquerda.nChaves);
			}
			
			trocaChave_noInterno_predecessor(no, posicaoChave);
			
			if(impressaoAtiva) {
				System.out.println("\nNO apos trocar chave pelo PREDECESSOR")			;
				no.imprime();
			}
			return;
		}

		// CASO 2b
		// O filho à DIREITA da CHAVE tem pelo menos T chaves
		No filhoDireita = getFilhoDireita(no, posicaoChave);

		if (filhoDireita.temMaisqueMinimo()) {
			if(impressaoAtiva) System.out.println("\t--> CASO 2b");
			trocaChave_noInterno_sucessor(no, posicaoChave);
			return;
		}

		// CASO 2c
		// Ambos os filhos tem apenas T-1
		// Fazer MERGE no filho da ESQUERDA

		if(impressaoAtiva) System.out.println("\t--> CASO 2c");
		merge(filhoEsquerda, no, posicaoChave, filhoDireita);
		removeChave(filhoEsquerda, chaveRemovida);

	}

	// Dado um NO e a posicao de uma CHAVE
	// Troca a CHAVE deste NO por sua CHAVE PREDECESSORA

	private void trocaChave_noInterno_predecessor(No no, int posicaoChave) throws IOException {

		No noPredecessor = getNoPredecessor(no, posicaoChave);
		int chavePredecessora = noPredecessor.getUltimaChave();

		if(impressaoAtiva) System.out.println("\nTrocando NO " + no.endereco + " - Posicao Chave: " + posicaoChave + " >> NO PREDECESSOR: " + noPredecessor.endereco + "\tChave: " + chavePredecessora);

		removeChave(no, chavePredecessora);

		//noPredecessor.atualizaUltimaChave(no.chaves[posicaoChave]);
		no.chaves[posicaoChave] = chavePredecessora;

		atualizaNo(no);

	}

	// Dado um NO e a posicao de uma CHAVE
	// Troca a CHAVE deste NO por sua CHAVE SUCESSORA

	private void trocaChave_noInterno_sucessor(No no, int posicaoChave) throws IOException {

		No noSucessor = getNoSucessor(no, posicaoChave);
		int chaveSucessora = noSucessor.getPrimeiraChave();


		if(impressaoAtiva) System.out.println("\nTrocando NO " + no.endereco + " - Posicao Chave: " + posicaoChave + " >> NO SUCESSOR: " + noSucessor.endereco + "\tChave: " + chaveSucessora);

		removeChave(noSucessor, chaveSucessora);
		noSucessor.atualizaPrimeiraChave(no.chaves[posicaoChave]);
		no.chaves[posicaoChave] = chaveSucessora;

		atualizaNo(no);

	}

	// **************************************
	// 					CASO 3
	// **************************************

	private void removeChave_naoEstaNo(No no, int chave) throws IOException{

		if(impressaoAtiva) System.out.println("\n\tCASO 3 = ...Chave nao encontrada nesse NO... procurando outro...\n");

		try {

			int posicaoChaveNoAtual_contemRaizSubarvore = getPosicaoNoAtual_raizSubarvore(no, chave);
			No proximoNo_raizSubarvore = getProximoNo_possivelmenteEstaChave(no, chave);
			No noFilhoEsquerda = getFilhoEsquerda(no, posicaoChaveNoAtual_contemRaizSubarvore);
			No nofilhoDireita = getFilhoDireita(no, posicaoChaveNoAtual_contemRaizSubarvore);

			if(impressaoAtiva) System.out.println("Proximo No, RAIZ sub-arvore: " + proximoNo_raizSubarvore.endereco);
			if(impressaoAtiva) System.out.println("Filho ESQUERDA (endereco): " + noFilhoEsquerda.endereco);
			if(impressaoAtiva) System.out.println("Filho DIREITA (endereco): " + nofilhoDireita.endereco);

			if (no.nChaves == 0 || nofilhoDireita.nChaves == 0 || noFilhoEsquerda.nChaves == 0)
				System.out.println("\n\n\n!!!!! Problemaooooo !!!!!\n\n\n");

			// CASO 3a
			// Proximo NO tem apenas T-1 chaves

			if (!proximoNo_raizSubarvore.temMaisqueMinimo()) {

				if(impressaoAtiva) System.out.println("\nCASO 3a - A RAIZ da sub-arvore que contem a chave NAO tem o minimo... procurando filhos para emprestimo...");

				// Se o proximo Filho for a DIREITA,
				// verifica o irmao da ESQUERDA para emprestar chaves

				if (proximoNo_raizSubarvore.endereco == nofilhoDireita.endereco)
					if (noFilhoEsquerda.temMaisqueMinimo()) {
						if(impressaoAtiva) System.out.println("Emprestando do Filho ESQUERDA para Filho DIREITA");
						no.transferirChave_esquerda_direita(posicaoChaveNoAtual_contemRaizSubarvore, noFilhoEsquerda,
								nofilhoDireita);

						atualizaNo(no);
						atualizaNo(noFilhoEsquerda);
						atualizaNo(nofilhoDireita);

						removeChave(nofilhoDireita, chave);
						return;
					}

				// Se o proximo Filho for a ESQUERDA,
				// verifica o irmao da DIREITA para emprestar chaves

				if (proximoNo_raizSubarvore.endereco == noFilhoEsquerda.endereco)
					if (nofilhoDireita.temMaisqueMinimo()) {
						if(impressaoAtiva) System.out.println("Emprestando do Filho DIREITA para Filho ESQUERDA");
						no.transferirChave_direita_esquerda(posicaoChaveNoAtual_contemRaizSubarvore, noFilhoEsquerda,
								nofilhoDireita);

						atualizaNo(no);
						atualizaNo(noFilhoEsquerda);
						atualizaNo(nofilhoDireita);

						removeChave(noFilhoEsquerda, chave);
						return;
					}

				// CASO 3b
				// Todos os irmaos tem apenas T-1 CHAVES

				if(impressaoAtiva) System.out.println("CASO 3b - Nenhum filho pode emprestar... fazendo Merge com No Centro (posicao Chave): " + posicaoChaveNoAtual_contemRaizSubarvore);
				merge(noFilhoEsquerda, no, posicaoChaveNoAtual_contemRaizSubarvore, nofilhoDireita);
				removeChave(noFilhoEsquerda, chave);
				return;

			}

			removeChave(proximoNo_raizSubarvore, chave);

		} catch (Exception e) {

			System.out.println("\n\n !!! ERRO CRITICO  !!!");
			e.printStackTrace();
			throw e;
		}

	}

	// ******************************************
	// 					MERGE
	// ******************************************

	private void merge(No noEsquerda, No noCentro, int posicaoChave, No noDireita) throws IOException {

		if(impressaoAtiva){
			System.out.println("\n\n...Iniciando Merge... ");
			System.out.println("\n\t\t  NO CENTRO: " + noCentro.endereco);
			System.out.println("\t\t  Posicao: " + posicaoChave);
			System.out.print("\nNO ESQUERDA: " + noEsquerda.endereco);
			System.out.println("\t\tNO DIREITA: " + noDireita.endereco + "\n");
		}

		// Se não for FOLHA
		if (!noDireita.ehFolha())
			noEsquerda.importarFilhos(noDireita);

		noEsquerda.importarChave(noCentro, posicaoChave);
		noEsquerda.importarChaves(noDireita);

		atualizaNo(noEsquerda);
		atualizaNo(noCentro);
		atualizaNo(noDireita);

		if (noCentro == raiz && noCentro.nChaves == 0)
			trocaRaiz(noEsquerda);

		if(impressaoAtiva){
			System.out.println("\n...Finalizando Merge...\n");
			System.out.println("...NOs depois do Merge...\n");

			System.out.println("ESQUERDA");
			noEsquerda.imprime();
			System.out.println("\nCENTRO");
			noCentro.imprime();
			System.out.println("DIREITA");
			noDireita.imprime();
		}

	}

	// Método para saber a quantidade de memória usada no programa, será usado na
	// bateria de testes.
	public void obterMemoriaUsada() {

		final int MB = 1024 * 1024;// converter bytes para megas;
		Runtime runtime = Runtime.getRuntime();

		System.out.println("MEMORIA USADA \t" + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");

	}

	// *********************************
	// 			  GETs e SETs
	// *********************************

	public int getOrdemArvore() {
		return this.t;
	}

	No get(boolean ehFolha) {
		return new No(ehFolha, t);
	}

	public No getRaiz() {
		return this.raiz;
	}

	// Dado um NO e a posicao de uma CHAVE
	// Retorna o NO que contem o PREDECESSOR

	private No getNoPredecessor(No no, int posicaoChave) throws IOException {

		int enderecoProximoFilho = no.filhos[posicaoChave];
		No noProximoFilho = leNo(enderecoProximoFilho);

		if(impressaoAtiva) System.out.println("Buscando NO PREDECESSOR... Proximo Filho (endereco): " + noProximoFilho.endereco);

		if (noProximoFilho.ehFolha()) {
			return noProximoFilho;
		} else {
			return getNoPredecessor(noProximoFilho, noProximoFilho.nChaves);
		}

	}

	// Dado um NO e a posicao de uma CHAVE
	// Retorna o NO que contem o SUCESSOR

	private No getNoSucessor(No no, int posicaoChave) throws IOException {

		int enderecoProximoFilho = no.filhos[posicaoChave + 1];
		No noProximoFilho = leNo(enderecoProximoFilho);

		if (noProximoFilho.ehFolha()) {
			return noProximoFilho;
		} else {
			return getNoSucessor(noProximoFilho, -1);
		}

	}

	private No getProximoNo_possivelmenteEstaChave(No no, int chave) throws IOException {

		if (no.ehFolha())
			return no;

		int posicaoProximoNo = getPosicaoNoFilho_possivelmenteEstaChave(no, chave);
		return leNo(no.filhos[posicaoProximoNo]);

	}

	private int getPosicaoNoFilho_possivelmenteEstaChave(No no, int chave) throws IOException {

		if (no == null)
			System.out.println("\n\n !!! ERRO AQUIII >>>>>" + chave);

		int posicaoChave;

		for (posicaoChave = 0; posicaoChave < no.nChaves; posicaoChave++)
			if (no.chaves[posicaoChave] > chave)
				break;

		return posicaoChave;

	}

	private int getPosicaoNoAtual_raizSubarvore(No no, int chave) throws IOException {

		int posicaoChave;

		for (posicaoChave = 0; posicaoChave < no.nChaves; posicaoChave++) {

			if (no.chaves[posicaoChave] > chave)
				return posicaoChave;

		}

		return posicaoChave - 1;

	}

	// Dado um NÓ e uma POSICAO,
	// obtem os filhos da ESQUERDA e DIREITA

	public No getFilhoEsquerda(No no, int posicao) throws IOException {
		return getFilhoX(no, posicao);
	}

	public No getFilhoDireita(No no, int posicao) throws IOException {
		return getFilhoX(no, posicao + 1);
	}

	public No getFilhoX(No no, int posicao) throws IOException {
		No filho = leNo(no.filhos[posicao]);
		return filho;
	}

}