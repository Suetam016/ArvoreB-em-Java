#########################################################
README
Autores: 
Matheus Batista de Andrade		8944449
Renan Seguin 				9882216
#########################################################

COMO USAR:

1. buscaArvoreB(No no, int chave)
   Sempre passar o nó raiz como nó.
   Chama uma auxiliar.
   Tem que ser chamada a partir de uma ArvoreB 
   Ex. de chamada:
   [arvoreB.buscaArvoreB(...)]

2. inserirChave(int chave)
   Insere a chave na árvore, não funciona com chave 0 já que 0 é a inicialização das chaves
   Aceita duplicatas
   Splita ao passar por um nó cheio e chama uma auxiliar recursiva que insere em nao cheios.
   Tem que ser chamada a partir de uma ArvoreB 
   Ex. de chamada:
   [arvoreB.inserirChave(...)]

3. split(int i, No no, No pai)
   é chamada antes da inserção
   sobe uma chave para o pai


OBS: 
O main foi comentado no codigo.
Testes e remoção só ma semana que vem