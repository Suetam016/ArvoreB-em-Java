import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
											/* Autores: 
											Matheus Batista de Andrade		8944449
											Renan Seguin 				9882216 */

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

	private class No {
		private int folha;
		int nChaves;
		int endereco;
		int[] chaves = new int[NUM_MAX_CHAVES];
		int[] filhos;

		No(boolean ehFolha) {
			folha = ehFolha ? TRUE : FALSE;

			if (!ehFolha()) {
				filhos = new int[NUM_MAX_FILHOS];
				Arrays.fill(filhos, NULL);
			}
		}

		boolean ehFolha() {
			return folha == TRUE;
		}

		boolean estaCheio() {
			return nChaves == NUM_MAX_CHAVES;
		}
		
		void imprime() {
			System.out.println("Eh folha: " + (folha == TRUE ? true : false));
			System.out.println("nChaves: " + nChaves);
			System.out.print("Chaves:");

			for (int i = 0; i < nChaves; i++) {
				System.out.print(" " + chaves[i]);
			}

			System.out.println();

			if (!ehFolha()) {
				System.out.print("Endereco dos filhos:");

				for (int i = 0; i < nChaves + 1; i++) {
					System.out.print(" " + filhos[i]);
				}

				System.out.println();
			}
		}
	}

	private void inicializaConstantes(int t) {
		this.t = t;
		TAMANHO_NO_INTERNO = 2 * 4 + 4 * (2 * t - 1) + 4 * (2 * t);
		TAMANHO_NO_FOLHA = 2 * 4 + 4 * (2 * t - 1);
		NUM_MAX_CHAVES = 2 * t - 1;
		NUM_MAX_FILHOS = NUM_MAX_CHAVES + 1;
	}

	public ArvoreB(String nomeArq, int t) throws IOException {
		this.nomeArq = nomeArq;
		inicializaConstantes(t);
		
		if (!new File(nomeArq).exists()) {
			No no = new No(true);
			no.endereco = TAMANHO_CABECALHO;
			trocaRaiz(no);
			atualizaNo(no);
		} else {
			carregaRaizNaRAM();
		}
	}

	private void trocaRaiz(No novaRaiz) throws FileNotFoundException,
			IOException {
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

	private No leNo(int endereco) throws IOException {
		RandomAccessFile arq = new RandomAccessFile(nomeArq, "r");

		if (arq.length() == 0 || endereco == NULL) {
			arq.close();
			return null;
		}

		arq.seek(endereco);
		boolean ehFolha = arq.readInt() == TRUE ? true : false;
		byte[] bytes = ehFolha ? new byte[TAMANHO_NO_FOLHA - 4]
				: new byte[TAMANHO_NO_INTERNO - 4];
		arq.read(bytes);
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		No no = new No(ehFolha);
		no.nChaves = leInt(in);
		no.endereco = endereco;

		for (int i = 0; i < no.chaves.length; i++) {
			no.chaves[i] = leInt(in);
		}

		if (!ehFolha) {
			for (int i = 0; i < no.filhos.length; i++) {
				no.filhos[i] = leInt(in);
			}
		}

		arq.close();
		return no;
	}

	// assume que o "no" ja tem um endereco
	private void atualizaNo(No no) throws IOException {
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
	private void gravaNovoNo(No no) throws IOException {
		// obs 1: "new File" nao cria um arquivo novo, apenas carrega
		// informacoes sobre o arquivo ja existente
		// obs 2: o novo no sera gravado no final do arquivo 
		// (ou seja, vai aumentar o tamanho do arq qdo chamar "gravaNovoNo")
		no.endereco = (int) new File(nomeArq).length();
		atualizaNo(no);
	}

	private int leInt(ByteArrayInputStream in) {
		byte[] bInt = new byte[4];
		in.read(bInt, 0, 4);
		return ByteBuffer.wrap(bInt).asIntBuffer().get();
	}

	private void escreveInt(ByteArrayOutputStream out, int i) {
		byte[] num = ByteBuffer.allocate(4).putInt(i).array();
		out.write(num, 0, 4);
	}
	
	void split(int i, No no, No pai) throws IOException{
	//conta quantas chaves foram passadas pro novo irmao
    //int contaChavestiradas = 0;
	//cria novo irmao
	/* System.out.println("***************** Split *************"); */
    No novoIrmao= new No(no.ehFolha());
    novoIrmao.nChaves = this.t - 1; 
	gravaNovoNo(novoIrmao); // grava o novo irmao em arquivo 
    //copia as chaves para o novo irmao 
    for (int j = 0; j < t-1; j++){
        novoIrmao.chaves[j] = no.chaves[j+t];
		//contaChavestiradas++;
	}
	//Se não for folha, então tambem copia os irmãos
    if (!no.ehFolha()){
		//System.out.println("Nao Folha");
        for (int j = 0; j < t; j++){
            novoIrmao.filhos[j] = no.filhos[j+t];
			no.filhos[j+t]=NULL;
		}
    }
  
    // Diminui o n Chaves do no 
    no.nChaves = t-1;
  
   //REALOCANDO FILHOS PARA ENCAIXAR A CHAVE Q VAI SUBIR
    for (int j = t; j >= i+1; j--){
    
        pai.filhos[j+1] = pai.filhos[j];
	} 
    // Um espaço adiante vai estar o novoIrmao 
    pai.filhos[i+1] = novoIrmao.endereco;
  
    //REALOCANDO CHAVES PARA A CHAVE QUE VAI SUBIR 
    for (int j = t-1; j >= i; j--){
        pai.chaves[j+1] = pai.chaves[j];
	}
	//COPIANDO A CHAVE 
    pai.chaves[i] = no.chaves[t-1];
  
    //atualizando
    pai.nChaves = pai.nChaves + 1;
	atualizaNo(pai);
	atualizaNo(no);
	atualizaNo(novoIrmao);
}


	private boolean buscaChave(No no,int chave)throws IOException {
		
		for (int i =0; i<no.nChaves; i++){
			//CASO BASE:
			if(chave == no.chaves[i]){
				//System.out.println("Chave encontrada");
				return true;
			}
			//CASO MENOR QUE O NÓ
			if(chave<no.chaves[0]){
				No no2 = leNo(no.filhos[0]);
				return buscaChave(no2,chave);
			}
			//CASO MAIOR QUE O NÓ
			if(chave>no.chaves[no.nChaves-1]){
				No no2 = leNo(no.filhos[no.nChaves]);
				return buscaChave(no2,chave);
				
			}
			//CASO ENTRE CHAVES
			if(chave>no.chaves[i] && chave<no.chaves[i+1]){
				No no2 = leNo(no.filhos[i+1]);
				return buscaChave(no2,chave);
			}
			
			
		}
		
		return false;
		
	}
	
	
	
	void inserirNaoCheio(int chave, No no) throws IOException
{//System.out.println("----------------------INSERIR NAO CHEIO -------------------------------");
    // VAMOS COMEÇAR DO MAIOR PRO MENOR 
    int i = no.nChaves-1;
  
    //SE FOLHA
    if (no.ehFolha()){
        //System.out.println("Folha");
		//simplesmente acha espaço e coloca
        while (i >= 0 && no.chaves[i] > chave){
            no.chaves[i+1] = no.chaves[i];
            i--;
        }
  
        // insere a chave 
        no.chaves[i+1] = chave;
         no.nChaves++;
		 atualizaNo(no);
    }
    else //SE NÃO FOLHA
    {//ai tem que percorrer até uma folha...
	//System.out.println("Não Folha");
        // PROCURA O FILHO (apontado por i+1)
        while (i >= 0 && no.chaves[i] > chave){
			i--;
        }
        
		No filhoApontado = leNo(no.filhos[i+1]);
		
        // SE O FILHO ESTIVER CHEIO 
        if ( filhoApontado.nChaves== NUM_MAX_CHAVES){
			//System.out.println("Filho apontado esta cheio");
            split(i+1,filhoApontado,no);
  
            // checa qual filho vai continuar a funcao, se nao for este é o proximo
            if (no.chaves[i+1] < chave){
				i++;
				filhoApontado = leNo(no.filhos[i+1]); // trazer pra memoria o filho escolhido
            }
                
        } //System.out.println("Filho apontado não cheio");
		
		inserirNaoCheio(chave, filhoApontado); // chama funcao
        
    }
}

public void inserirChave(int chave)throws IOException{
	//CASO INICIALIZAÇÃO
	if(this.raiz.nChaves == 0){
		//System.out.println("Caso raiz sem chaves");
		this.raiz.chaves[0] = chave;
		this.raiz.nChaves = 1;
		
		atualizaNo(raiz);
	}else{//ok a raiz tem algo 
		//System.out.println("Caso raiz com chaves");
		// se a raiz estiver cheia
		if(this.raiz.nChaves == NUM_MAX_CHAVES){
			//System.out.println("Caso raiz cheia");
			No novaRaiz = new No(false);
			novaRaiz.filhos[0] = this.raiz.endereco;
			gravaNovoNo(novaRaiz);
			
			split(0,this.raiz,novaRaiz); //i(filho[i]),no,pai
			
			int j = 0;
			//DESCOBRE PRA QUAL FILHO VAI A NOVA CHAVE 
			if(novaRaiz.chaves[0]<chave){
				//System.out.println("Segundo filho");
				j = 1;
			} //System.out.println("Primeiro filho");
			
			No filho = leNo(novaRaiz.filhos[j]);
			inserirNaoCheio(chave, filho);
			
			trocaRaiz(novaRaiz); // talvez tenha que vir antes do split, conferir.
		}else{ //System.out.println("Raiz tem espaço - chamando naoCheio");
			//se a raiz tiver espaço, então chama a ometodo que faz naoCheio
			inserirNaoCheio(chave,this.raiz);
			
		}
		
	}
}


	public boolean buscaArvoreB(No no,int chave){
		try{
			boolean x = buscaChave(no,chave);
			return x;
		}catch(Exception e){
			return false;
		}
	}

/* public static void main (String[] args){
	
	System.out.println("Chamando construtor ArvoreB ");
	try{
		
		ArvoreB arvoreB = new ArvoreB("barvore.txt",2);
		arvoreB.inserirChave(20);
		No no1 = arvoreB.leNo(arvoreB.raiz.endereco);
		System.out.println("Inseriu");
		
		arvoreB.inserirChave(4);
		System.out.println("Inseriu");
		
		arvoreB.inserirChave(3);
		System.out.println("Inseriu ");	
		
		arvoreB.inserirChave(5);
		System.out.println("Inseriu");
		
		arvoreB.inserirChave(67);
		System.out.println("Inseriu");	
		
		arvoreB.inserirChave(97);
		System.out.println("Inseriu");	
		
		arvoreB.inserirChave(83);
		System.out.println("Inseriu");	
		
		arvoreB.inserirChave(77);
		System.out.println("Inseriu");	
		
		arvoreB.inserirChave(53);
		System.out.println("Inseriu");	
		
		arvoreB.inserirChave(76);
		System.out.println("Inseriu");	
		
		System.out.println("");	
		System.out.println("");	
		System.out.println("");	
		System.out.println("");	
		System.out.println("");	
		System.out.println("");	
		System.out.println("");	
		System.out.println("***********************************************************************************");	
		System.out.println("***********************************************BUSCA************************************");	
		
		if(arvoreB.buscaArvoreB(arvoreB.raiz,3)){
			System.out.println("ACHOU O 3");
		}
		if(arvoreB.buscaArvoreB(arvoreB.raiz,2)){
			System.out.println("ACHOU O 2");
		}else System.out.println("NAO ACHOU O 2");
		
		
		
		} catch(Exception e){ 
			System.out.println("Error!!");
		}
	
	
	
	
} */


}