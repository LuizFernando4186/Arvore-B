public class MedidorTempo {

	private long inicio;
	
	public void comeca(String metodo) {
		System.out.print(metodo);
		//Marca o inicio do tempo.
		inicio = System.currentTimeMillis();
	}
	
	public long termina() {
		//Pega o instante que terminou menos oque comecou e divide por 1000(milisegundo)
		return (System.currentTimeMillis() - inicio) / 1000;
	}
}