import java.io.IOException;
import java.io.File;
import java.util.Random;
import java.io.PrintWriter;
import java.io.Writer;
import java.io.RandomAccessFile;
import java.io.FileWriter;
import java.io.BufferedWriter;



public class Simulacao{

	private ArvoreB arvB;
	private No no;
	private MedidorTempo medidor;
	private int ordem;
	private Random rand = new Random(12345);
	private int[] vetorTeste;
	private DebugArvore debug = new DebugArvore();
	


	public Simulacao(int ordem) throws IOException {

		//Apagando arquivo antigo, se houver
		File file = new File("arv.txt");
		if (file.exists())
			file.delete();

		this.ordem = ordem;
		this.medidor = new MedidorTempo();
		this.vetorTeste = new int[1000000];

		// Cria vetor com 1 milhao de valores de teste		
		for(int i = 0; i < 1000000; i++)
			this.vetorTeste[i] = i;			
		
	}




	public void simulaTudo() throws IOException {

		// TESTES 
		System.out.print("Numero Testes: ");
		for (int i = 10; i <= 10000; i+=1000){
			
			this.arvB = new ArvoreB(this.ordem, "arv.txt");
			System.out.print(i + ", ");

			// TESTES DE INSERÇÃO
			simulaInsercao(i);			

			//debug.imprimeArvore(this.arvB);
			
			// TESTES DE BUSCA
			//simulaBusca(i);

			// 	TESTES DE REMOÇÃO
			simulaRemocao(i);

			// Corrigi numero para manter arredondamento
			i = i == 10 ? 0 : i;

			//Apaga arquivo
			File file = new File("arv.txt");
			file.delete();	

		}

	}


	private void simulaInsercao(int tamanho) throws IOException {
		
		medidor.comeca("");

		// Inserindo as chaves
		for(int i = 0; i < tamanho; i++){
			//System.out.println("\nInserindo ( " + i + " ): " + vetorTeste[i]);
			arvB.insereChave(this.vetorTeste[i]);
		}

		long tempo = medidor.termina();
		//System.out.println("Inserido " + tamanho + " chaves");

		gravarLogInsercao("TIPO:INSERCAO,QTD:"+ tamanho + ",TEMPO:" + tempo);

	}

	private void simulaBusca(int tamanho) throws IOException {

		int existente, naoExistente, contador;		
		long tempo;
		boolean temp;
		contador = 0;

		// 1.000 buscas repetidas
		medidor.comeca("");
		while(contador < 1000){

			for(int i = 0; i < tamanho && contador < 1000; i++){
				contador++;
				existente = this.vetorTeste[i];
				temp = this.arvB.buscaB(existente);				
			}

		}
	
		tempo = medidor.termina();		
		gravarLogBusca("TIPO:BUSCA,SUB-TIPO:EXISTENTE,TAMANHO:" + tamanho + ",TEMPO:" + tempo);
		

		medidor.comeca("");		

		for(int i = 0; i < 1000; i++){
			naoExistente = this.vetorTeste[i]+1;
			temp = arvB.buscaB(naoExistente);
			//System.out.println("Buscando inexistentes ( " + i + " ): " + naoExistente + " = " + temp);
		}
		
		tempo = medidor.termina();
		gravarLogBusca("TIPO:BUSCA,SUB-TIPO:NAO_EXISTENTE,TAMANHO:" + tamanho + ",TEMPO:" + tempo);

	}


	public void simulaRemocao(int tamanho) throws IOException{

		tamanho = (int) tamanho;
		long tempo;
		medidor.comeca("");
		
		for(int i = 0; i < tamanho; i++){
			arvB.remove(i);   
		}

		tempo = medidor.termina();

		gravarLogRemocao("TIPO:REMOCAO,SUB-TIPO:EXISTENTE,TAMANHO:" + tamanho + ",TEMPO:" + tempo);

	}




	public void gravarLogInsercao(String log) throws IOException {
		gravarArquivoLog("log_insercao.txt", log);
	}


	public void gravarLogBusca(String log) throws IOException{
		gravarArquivoLog("log_busca.txt", log);
	}

	public void gravarLogRemocao(String log) throws IOException{
		gravarArquivoLog("log_remocao.txt", log);
	}

	private void gravarArquivoLog(String arquivoLog, String log)throws IOException{

		if(!new File(arquivoLog).exists()){
			RandomAccessFile arq = new RandomAccessFile(arquivoLog, "rw");
		}

		Writer csv = new BufferedWriter(new FileWriter(arquivoLog, true));
		csv.append(log + ",ORDEM:" + this.ordem + "\n");
		csv.close();


	}

}