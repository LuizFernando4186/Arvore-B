import java.io.*;

public class Convert {
 
  public void converte(String nomeArq) {
    
	try {
	  
	  System.out.println("\n\n!! Convertendo arquivo BIN: " + nomeArq);
    
	  BufferedReader input = new BufferedReader(new FileReader(nomeArq));
	  File file = new File(nomeArq);
		FileInputStream fin = new FileInputStream(file);
		 StringBuffer strContent = new StringBuffer("");

		byte[] buffer = new byte[1024];

		int n = fin.read(buffer);


		strContent.append(new String(buffer, 0, n, "UTF-8"));

		System.out.println(strContent);

      String linha = input.readLine();

      while (linha != null) {
		
		System.out.println("String: " + linha);
        int valor = Integer.parseInt(linha, 2);           
		System.out.println("Valor " + valor);
        linha = input.readLine();
      
	  }

      input.close();
    }

    catch (Exception e) {
      System.out.println(e.getMessage());
    }

  }
 
}