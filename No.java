import java.util.Arrays;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;

public class No {

	private static final int TRUE = 1;
	private static final int FALSE = 0;
	private static final int NULL = -1;
	private int t;

	int folha;
	int nChaves;
	int endereco;// endereço dos Nó;
	int[] filhos, chaves;

	No(boolean ehFolha, int ordem) {

		this.t = ordem;
		this.nChaves = 0;
		this.chaves = new int[2 * t - 1];
		this.folha = ehFolha ? TRUE : FALSE;

		if (!ehFolha()) {
			this.filhos = new int[2 * t];
			Arrays.fill(filhos, NULL);
		}

	}

	public int buscaChave(int k) {

		for (int i = 0; i < this.nChaves; i++)
			if (this.chaves[i] == k)
				return i;

		return NULL;

	}

	public void imprime() {

		//System.out.println("\nEh folha: " + (folha == TRUE ? true : false));
		System.out.println("nChaves: " + nChaves);

		System.out.print("Chaves:\t   ");

		for (int i = 0; i < nChaves; i++) {
			System.out.print(" |" + chaves[i] + "| ");
		}

		System.out.println();

		if (!ehFolha()) {
			System.out.print("Filhos:");

			for (int i = 0; i < nChaves + 1; i++) {
				System.out.print(" |" + filhos[i] + "| ");
			}

			System.out.println("\n");
		}
	}

	// *****************************
	// 			GETs e SETs
	// *****************************
	public boolean ehFolha() {
		return folha == TRUE;
	}

	public int[] getFilhos() {
		return this.filhos;
	}

	public int getEnderecoPrimeiroFilho() {
		if (!this.ehFolha())
			return this.filhos[0];
		return NULL;
	}

	public int getEnderecoUltimoFilho() {
		if (!this.ehFolha())
			return this.filhos[this.nChaves];
		;
		return NULL;
	}

	public int getUltimaChave() {
		return this.chaves[this.nChaves - 1];
	}

	public void atualizaUltimaChave(int valor) {
		this.chaves[this.nChaves - 1] = valor;
	}

	public int getPrimeiraChave() {
		return this.chaves[0];
	}

	public void atualizaPrimeiraChave(int valor) {
		this.chaves[0] = valor;
	}

	public boolean estaMaximo() {
		return this.nChaves == this.chaves.length - 1;
	}

	public boolean temMaisqueMinimo() {
		return this.nChaves >= t;
	}

	public void importarChaves(No no) {

		boolean noTemCapacidade = (this.nChaves + no.nChaves) <= ((2 * t) - 1);
		int posicaoChave = 0;

		if (noTemCapacidade) {

			// Copia todas as CHAVES
			for (int i = this.nChaves; no.nChaves > 0; i++) {

				this.chaves[i] = no.chaves[posicaoChave];

				posicaoChave++;
				this.nChaves++;
				no.nChaves--;

			}

		}

	}

	public void importarChave(No no, int posicaoChave) {

		boolean noTemCapacidade = (this.nChaves + 1) <= ((2 * t) - 1);

		if (noTemCapacidade) {

			this.chaves[nChaves] = no.chaves[posicaoChave];

			// Move demais chaves
			for (int i = posicaoChave; i < no.nChaves - 1; i++)
				no.chaves[i] = no.chaves[i + 1];

			// Move filhos
			if (!no.ehFolha())
				for (int i = posicaoChave + 1; i < no.nChaves; i++)
					no.filhos[i] = no.filhos[i + 1];

			this.nChaves++;
			no.nChaves--;
		}

	}

	public void importarFilhos(No no) {

		int posicaoUltimoFilho = this.nChaves;

		for (int i = 0; i <= no.nChaves; i++) {
			// System.out.println("\tImportando Filho: " + no.filhos[i] + " do NO: " +
			// no.endereco);
			this.filhos[posicaoUltimoFilho + 1] = no.filhos[i];
			posicaoUltimoFilho++;
		}

	}

	public void transferirChave_direita_esquerda(int posicaoChave, No noEsquerda, No noDireita) {

		// Acrescenta a CHAVE no NO ESQUERDA
		noEsquerda.chaves[noEsquerda.nChaves] = this.chaves[posicaoChave];

		noEsquerda.nChaves++;

		// Acrescenta o FILHO do NO DIREITA
		if (!noEsquerda.ehFolha())
			noEsquerda.filhos[noEsquerda.nChaves] = noDireita.filhos[0];

		// Sobe do NO DIREITA para NO CENTRO
		this.chaves[posicaoChave] = noDireita.getPrimeiraChave();

		// Move as CHAVES e FILHOS no NO DIREITA
		for (int i = 0; i < noDireita.nChaves - 1; i++)
			noDireita.chaves[i] = noDireita.chaves[i + 1];

		if (!noDireita.ehFolha())
			for (int i = 0; i < noDireita.nChaves; i++)
				noDireita.filhos[i] = noDireita.filhos[i + 1];

		noDireita.nChaves--;

	}

	public void transferirChave_esquerda_direita(int posicaoChave, No noEsquerda, No noDireita) {

		// Move CHAVES NO DIREITA para abrir espaco
		for (int i = noDireita.nChaves; i > 0; i--)
			noDireita.chaves[i] = noDireita.chaves[i - 1];

		// Move FILHOS NO DIRETA para abrir espaco
		if (!noDireita.ehFolha()) {

			for (int i = noDireita.nChaves + 1; i > 0; i--)
				noDireita.filhos[i] = noDireita.filhos[i - 1];

			noDireita.filhos[0] = noEsquerda.filhos[noEsquerda.nChaves];
		}

		noDireita.nChaves++;

		// Desce a mediana para DIREITA
		noDireita.chaves[0] = this.chaves[posicaoChave];

		// Sobe do NO ESQUERDA para NO CENTRO
		this.chaves[posicaoChave] = noEsquerda.getUltimaChave();

		// Decrementa o NO ESQUERDA
		noEsquerda.nChaves--;

	}

}
