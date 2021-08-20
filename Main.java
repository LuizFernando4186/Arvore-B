class Main {

	public static void main(String[] args) {

		try{

			MedidorTempo medidorTotal = new MedidorTempo();

			Simulacao simulacao; 
			int tMaximo = 10000;
			int i = 2;

			medidorTotal.comeca("");

			//	Testa diversos valores para T
			while(i <= tMaximo){
				
				System.out.println("\n*** Simulando com T = " + i + "/" + tMaximo);
				
				simulacao = new Simulacao(i);
				simulacao.simulaTudo();

				//Padronizacao dos valores
				if (i < 10)
					i++;
				else
					if (i < 50)
						i+=10;
					else
						if (i < 100)
							i+=50;
						else
							if (i < 1000)
								i+=100;
							else
								i+=1000;

			}
			
		long tempoTotal = medidorTotal.termina();
		System.out.println("\n\nTempo TOTAL\t" + tempoTotal + "\n");
		System.out.println("FIIIM");
	}
	catch (Exception e){
		System.out.println("ERRO NA SIMULACAO");
		e.printStackTrace();
	}

  }



}
