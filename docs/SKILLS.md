# Habilidades no Raphael

Abra `R` e escolha **Habilidades**. O primeiro botao abre uma lista de categorias com contagens: Todas as habilidades, Unicas, Intrinsecas, Comuns, Extras, Resistencias, Supremas, Manas, Magias, Artes de combate e Outras. Clique diretamente na categoria desejada; em telas pequenas, role a lista. Esc fecha a lista sem fechar o Raphael. A busca aceita o nome traduzido ou o ID. O filtro **Utilizaveis** mostra registros com pelo menos um modo disponivel ou passivo.

Clique em uma habilidade para abrir sua ficha. Use `<` e `>` para examinar cada modo; a roda do mouse permite ler todos os detalhes. **LISTA** volta ao catalogo. Cores identificam as categorias; aprendizado, falta de energia, recarga e impedimentos aparecem em cinza, com orientacao na ficha e no tooltip do registro.

## Como usar

### Absorcao de monstros pelo slime

Ao derrotar um mob como slime com Predator, Gluttony ou Beelzebuth, o New World absorve automaticamente suas habilidades basicas nativas: intrinsecas, comuns, extras e resistencias. O abate representa a predacao automatica, como no registro de formas; nao exige comer um item de drop. Abates com a predacao do Reincarnated tambem entram nesse fluxo.

Somente skills ja aprendidas pela criatura sao absorvidas: registros com dominio negativo e habilidades temporarias sao ignorados. A aquisicao nao cobra magicules nem reduz sua capacidade maxima.

A cada nova habilidade absorvida, o Grande Sabio confirma a aquisicao no chat e acima da hotbar. Com o addon Great Sage instalado, o anuncio tambem entra na fila nativa de voz e legendas, mesmo durante a janela de silencio inicial do login. Os controles de voz, volume e categorias anunciadas do addon continuam valendo. Derrotar outra criatura com uma skill que voce ja tem nao repete o anuncio.

O sopro paralisante fica na categoria **Intrinsecas**, nao em **Unicas**. Se a instancia do Modrinth ainda estiver usando um JAR antigo, as novas skills nao serao adquiridas: atualize o nucleo, reinicie o jogo e derrote outra criatura. Abates anteriores feitos sem o sistema de absorcao nao concedem skills retroativamente.

Por exemplo, derrotar uma Evil Centipede concede `tensura:paralysing_breath`. A habilidade e permanente, aparece em `R > Habilidades` e pode ser equipada nos slots do Reincarnated. Ela comeca aprendida, com dominio zero; custos, treino de modos, recarga e uso continuam seguindo o mod-base. Skills ja aprendidas nao perdem dominio nem recarga quando voce derrota outra criatura igual. Skills unicas, supremas, Manas, magias e artes de combate nao entram nessa absorcao automatica. Apenas habilidades realmente presentes no armazenamento nativo da criatura sao copiadas; mobs sem skills registradas nao geram habilidades novas.

1. Selecione uma habilidade e o modo desejado.
2. Use o botao de conjunto (**C n/9** em portugues; **P n/9** em ingles) para alternar o preset nativo do Reincarnated. Cada preset tem tres slots independentes. Depois clique em **SLOT 1**, **SLOT 2** ou **SLOT 3** para substituir o slot do preset selecionado. A mudanca e solicitada ao servidor; a ficha mostra o equipamento sincronizado.
3. Feche o Raphael com `R` ou Esc e mire no alvo, quando necessario.
4. Use a tecla do slot: por padrao, **Z**, **X** e **C**. A ficha mostra as teclas configuradas; mudancas em **Opcoes > Controles** sao respeitadas.
5. Algumas habilidades exigem segurar a tecla. Por padrao, **Alt esquerdo + tecla do slot** troca o modo no mundo. O botao de preset troca o conjunto atualmente ativo do Reincarnated, nao apenas o conjunto exibido no Raphael.

Resistencias e outras passivas que aceitam alternancia exibem **LIGAR/DESLIGAR EFEITO PASSIVO**. Elas normalmente nao ocupam slot; acompanhe o estado LIGADO/DESLIGADO na ficha. Habilidades contextuais seguem a descricao do mod.

### Predator e Estomago

Para usar Predator, Gluttony ou Beelzebuth, equipe o modo desejado em um dos tres slots do preset selecionado e ative-o pela tecla que a ficha apresenta. Segure a tecla quando o modo exigir canalizacao. Os modos e o armazenamento nativo pertencem ao Reincarnated; consulte **Opcoes > Controles > Tensura** para as teclas da versao instalada. A aba `R > Estomago` e apenas um registro New World: ela nao abre, altera ou sincroniza o inventario nativo do Reincarnated. Habilidades obtidas ao derrotar mobs aparecem em `R > Habilidades`, nao como itens nesse registro.

## Custos e desbloqueio

A lista agora inclui habilidades nativas ainda nao adquiridas. Em `R > Habilidades`, desative o filtro de utilizaveis, escolha a categoria e selecione a habilidade. Role a ficha ate **COMO DESBLOQUEAR**: cada registro informa as rotas identificadas, os requisitos de dominio e os numeros da configuracao ativa do servidor. Consultar o catalogo nao concede habilidades. O guia cobre 355 registros do Reincarnated 2.0.1.3, os dois registros do addon Great Sage 0.0.6.1 e as duas evolucoes executaveis do New World; onde nao existe uma rota natural confirmada, isso fica explicito.

### Great Sage por padrao

Todos os jogadores recebem `tensura:great_sage` automaticamente ao entrar no mundo, incluindo personagens existentes e qualquer raca. Nao precisa de comandos, sorteio, EP ou magicules para adquirir. A habilidade e permanente e ja aprendida. Um personagem que ja a possui conserva dominio, modos e configuracoes; a evolucao nativa para Raphael nao e revertida.

Em `R > Habilidades > Unicas`, selecione Great Sage. Ligue a passiva para seus bonus e equipe um modo quando desejar usar suas acoes. Com o addon instalado, `G` abre a interface e `V` analisa, conforme suas teclas em Controles. A Extra `Sage` e outra habilidade. Os custos nativos de uso continuam valendo; somente a aquisicao inicial e gratuita.

Great Sage evolui automaticamente para Raphael no despertar de Verdadeiro Lorde Demonio, e Raphael evolui para Ciel quando o personagem despertado alcanca Ultimate Slime. Veja [SAGE_EVOLUTION.md](SAGE_EVOLUTION.md) para requisitos, custos e adaptacoes da historia. O capstone do addon continua desativado, mas a linha New World utiliza habilidades executaveis proprias.

- A ficha le os custos de magicules e aura de cada modo da API da versao instalada do Reincarnated. Nao usa os valores fixos do catalogo de progressao New World como custo real.
- A lista mostra a faixa de custos entre os modos. A ficha mostra o valor de um modo, recarga restante, dominio e duracao de habilidades temporarias.
- Os valores representam o custo informado pela API do modo. Canalizacao, cargas, alvos, modificadores de magia e armas de conjuracao podem alterar o gasto final; o Reincarnated calcula e cobra esse gasto no servidor.
- **Em aprendizado:** faltam pontos para tornar a habilidade utilizavel. Quando ela permite equipar, tente usa-la no mundo para treinar. As tentativas seguem os custos multiplicados, a recarga e as penalidades nativas. Resistencias progridem com o dano/efeito correspondente.
- **Modo em aprendizado:** uma habilidade ja aprendida pode ter modos ainda bloqueados. A ficha mostra os pontos atuais e necessarios para esse modo.
- **Energia insuficiente:** recupere magicules/aura ate os valores indicados.
- **Em recarga:** aguarde o tempo indicado; excecoes permitidas pela API nativa sao respeitadas.
- **Uso impedido:** saia do modo espectador, remova os efeitos impeditivos ou saia da area/batalha que proibe a habilidade, conforme a mensagem.
- **Registro sem ativacao:** os registros de progressao do New World, incluindo Supremas/Manas ainda sem acao implementada, nao concedem automaticamente uma habilidade executavel do Reincarnated. A ficha explica essa limitacao. Quando a equivalente nativa foi adquirida, o registro duplicado New World e ocultado e a ficha nativa prevalece.

Os dados de disponibilidade sao enviados pelo servidor aproximadamente a cada segundo. Custos, aprendizado e autorizacao continuam sob controle do Reincarnated. Abrir a ficha nao ativa habilidades, nao gasta energia e nao remove efeitos passivos.

## Validacao

Os testes automaticos conferem categorias, disponibilidade, energia, recargas, aprendizado, custos fracionados na sincronizacao e os contratos das APIs dos JARs instalados. A verificacao de layout e uso com jogador esta em `docs/TESTING.md`.
