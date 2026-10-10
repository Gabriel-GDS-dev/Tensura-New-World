# Como instalar, jogar e testar

## Testes automaticos sem abrir o jogo

Abra o terminal do VS Code na pasta `tensura-new-world-core` e execute:

```powershell
.\gradlew.bat test --no-configuration-cache
.\gradlew.bat build --no-configuration-cache
powershell -ExecutionPolicy Bypass -File .\tools\build-modrinth-pack.ps1 -SkipBuild
```

O primeiro comando valida as regras e os destinos registrados de evolucao, marcos de Essencia, atributos, estagios do catalogo racial e persistencia das formas. O segundo compila e testa o JAR. O terceiro cria o `.mrpack` e executa `tools/validate-release.ps1`, que confere versoes, hashes, dependencias, classes, arte do menu, configuracao de HUD e a ausencia do JAR Great Sage no pacote publico.

Os testes de habilidades tambem verificam categorias, custos/estados de uso, sincronizacao de modos e os contratos das APIs dos JARs do Reincarnated e ManasCore instalados em `libs`.

As mesmas acoes estao em **Terminal > Run Task** como `TNW: Build and test`, `TNW: Build Modrinth pack` e `TNW: Validate Modrinth pack`.

## Teste do servidor sem interface grafica

Este teste carrega Minecraft, NeoForge, todos os mods e os comandos sem abrir janela:

```powershell
New-Item -ItemType Directory -Force .\run | Out-Null
Set-Content -LiteralPath .\run\eula.txt -Value 'eula=true' -Encoding ascii
.\gradlew.bat runServer --no-configuration-cache
```

Espere aparecer `Done` no terminal. Digite `stop` e pressione Enter para encerrar corretamente. Tambem e possivel usar a tarefa `TNW: Run Headless Server` no VS Code.

Verifique `run/logs/latest.log`. O teste passa quando o servidor chega a `Done`, registra o comando `/tnw` e encerra sem `ERROR`, `Exception` ou crash report.

## Instalar para jogar no Modrinth App

1. Instale o [Modrinth App](https://modrinth.com/app).
2. Abra **Create instance** pelo botao `+`.
3. Entre as quatro opcoes mostradas, escolha **Upload a modpack**. Essa e a opcao que instala um arquivo `.mrpack`.
4. Nao escolha **Custom setup**, **Start from a mod or modpack** nem **Import instance** para este arquivo.
5. Selecione `dist/Tensura-New-World-0.2.0-alpha.mrpack`.
6. Aguarde o download das seis dependencias.
7. Abra **Options** da instancia e reserve entre 6 GB e 8 GB de memoria.
8. Instale o Great Sage seguindo `docs/GREAT_SAGE.md`.
9. Clique em **Play**.

O Modrinth App instala o Java adequado. Para desenvolvimento pelo VS Code, use JDK 21. Nao adicione Fabric, Forge ou OptiFine; o pack usa NeoForge.

## Conferencia visual feita pelo jogador

1. No primeiro menu, confirme que aparece a arte de New World (paisagem da floresta/cidade), nao o panorama azul do Tensura: Reincarnated; confira o painel de navegacao, o cartao de ambientacao e os botoes **Novo Mundo**, **Multijogador**, **Configuracoes** e **Sair**.
2. Crie um mundo com comandos ativados, em dificuldade Normal ou Hard.
3. Confirme que as telas **Selecionar Mundo** e **Criar Novo Mundo** usam a mesma arte escurecida e exibem a identificacao **Tensura New World**, sem cobrir os controles.
4. Confirme que o HUD aparece no canto superior direito e desaparece enquanto outra tela esta aberta.
5. Pressione `R`. O painel Raphael deve ficar nitido, sem desfoque do mundo e sem sobrepor o menu do Tensura: Reincarnated.
6. Confira as abas Status, Atributos, Evolucao, Formas, Habilidades, Estomago, Seguidores e Nacao; reduza a resolucao para confirmar que a ficha permanece dentro da tela e que os textos continuam legiveis.
   - Em Atributos, use a roda do mouse sobre o conteudo para acessar todas as cinco linhas quando a janela estiver pequena. Os botoes devem permanecer alinhados e dentro do painel.
   - Invista o ultimo ponto disponivel: todos os botoes `+ 1` devem desativar sem fechar e reabrir a tela. Receber novos pontos deve reativa-los.
   - Em Evolucao, role para ler todos os requisitos; o botao de confirmacao deve permanecer fixo e atualizar quando o servidor liberar a evolucao.
   - Sem Morph, role uma lista longa de Formas e confira se cada botao assume a forma da mesma linha, inclusive depois de redimensionar a janela.
   - Teste Tab, Enter, Esc e `R`; o foco dos botoes deve continuar visivel e as duas teclas de fechamento devem funcionar.
7. Pressione `G` para o menu do Great Sage e `V` para a analise do addon. O Raphael do New World permanece em `R`.
8. Confirme que o HUD de progressao do Tensura: Reincarnated nao aparece junto do painel New World e que os indicadores vanilla de vida e fome continuam visiveis.

## Teste de Essencia, atributos e mimetizacao

1. Execute `/tnw debug set_race slime` em um mundo de teste e derrote mobs. O Raphael deve mostrar Essencia total, progresso para o proximo ponto e EP recalculado.
2. Abra **Atributos**, invista pontos em Forca, Vitalidade, Agilidade, Inteligencia e Vontade e confira os atributos/EP apos cada investimento.
3. Derrote mobs como Slime. A aba **Formas** deve registrar nome, quantidade, vida, ataque e armadura dos tipos derrotados.
4. Clique em **Assumir** e confira o modelo do mob, atributos e resistencias aplicados. IDs com namespace, como `minecraft:sheep`, devem ser aceitos; **Abandonar** remove a forma e seus modificadores.
5. Reconecte ao mundo e confirme que Essencia, pontos, atributos e catalogo de formas foram persistidos.

## Morph opcional para transformacoes animadas

O Morph V2 pode ser instalado separadamente para substituir a representacao estatica das formas do New World por transformacoes animadas. Use uma versao marcada para Minecraft 1.21.1 e NeoForge (por exemplo, Morph V2 0.1.4) e instale o mod no cliente e no servidor. O Morph e opcional e nao e incluido no `.mrpack`; sem ele, a mimetizacao nativa continua disponivel.

1. Confirme que Morph e sua dependencia iChunUtil estao carregados nos dois lados.
2. Entre no mundo como Slime, derrote criaturas para registrar formas no catalogo New World e abra a aba **Formas** do Raphael.
3. Com Morph instalado, use `[` e `]` para navegar pelas formas do proprio Morph e Enter ou clique para transformar; use Esc para fechar a selecao. A selecao do Morph e independente do catalogo New World.
4. Confirme que o modelo da forma e suas animacoes aparecem em terceira pessoa. Remover o Morph restaura os controles nativos do New World.

## Teste da nova evolucao

Use um mundo de teste com comandos ativados:

1. Execute `/tnw debug set_race goblin`.
2. Execute `/tnw debug set_magicules 5000` e depois `/tnw evolution`.
3. Confirme que apenas ter EP nao evolui o personagem; receber um nome ainda esta pendente.
4. Pressione `R`, abra **Evolucao** e confira a lista marcada por `[OK]` e `[  ]`.
5. Para uma linhagem que estiver com todas as condicoes completas, clique **Evoluir** ou use `/tnw evolve`.
6. Confirme que a raca so muda depois dessa confirmacao.
7. Teste o altar com um beacon a ate dois blocos e `/tnw ritual altar`.
8. Teste o contrato com um villager a ate seis blocos, 10.000 magicules disponiveis e `/tnw ritual soul_contract`.
9. Use `/tnw status` e `/tnw evolution` para conferir estagio, EP e proxima forma.

## Teste do Harvest Festival

1. Prepare a forma Named Slime com pelo menos 500.000 EP.
2. Use `/tnw debug add_souls 1000` e confirme a Demon Lord Seed.
3. Use `/tnw debug add_souls 9000`; o personagem deve entrar no sono ritual de tres minutos.
4. Para um teste curto em outro mundo, use `/tnw debug complete_harvest`.
5. Confirme True Demon Lord, cinco vezes os magicules, Raphael, Beelzebuth, gifts dos subordinados e a forma Demon Slime quando os requisitos da linhagem forem atendidos.

## Teste de sistemas e persistencia

### Catalogo e uso das habilidades

Como slime com Predator, derrote uma Evil Centipede. Confirme a fala e a legenda do Great Sage, o aviso de aquisicao e a entrada em `R > Habilidades > Intrinsecas`. A lista de categorias deve abrir com as contagens, permitir escolher Unicas ou Todas diretamente e aceitar rolagem em telas pequenas. Equipe o sopro e use a tecla do slot. Derrotar outra centopeia nao deve repetir a aquisicao nem apagar dominio ou recarga. Para testar na instancia do Modrinth, confira que ela usa o JAR atualizado; compilar no projeto nao substitui automaticamente o arquivo instalado.

Use as habilidades realmente adquiridas no Reincarnated; `/tnw debug grant_skill` cria um registro de progressao New World e nao concede uma habilidade executavel do mod-base.

1. Abra `R > Habilidades` e confira as categorias, a busca por nome/ID e o filtro Utilizaveis. Consulte `docs/SKILLS.md` para os controles completos.
2. Abra uma habilidade com varios modos e confira o custo de magicules/aura de cada modo contra a configuracao do Reincarnated. Custos fracionados devem ser preservados.
3. Selecione um modo, equipe em um slot e aguarde a ficha indicar o slot confirmado. Feche o painel e use Z/X/C, ou a tecla remapeada; teste tambem habilidades que exigem segurar a tecla e Alt + tecla do slot.
4. Use uma resistencia/passiva com alternancia e confira LIGADO/DESLIGADO. Abrir e fechar o painel repetidamente nao deve alterar efeitos ou gastar energia.
5. Confira os estados cinza e suas mensagens: habilidade com dominio negativo, modo nao aprendido, energia insuficiente, recarga, modo espectador e restricao de area/batalha.
6. Equipe uma habilidade em aprendizado e tente usar no mundo: os pontos devem progredir com os custos e penalidades nativos, sem liberar instantaneamente a habilidade.
7. Adquira a equivalente nativa de um registro New World: o catalogo deve mostrar a versao nativa sem o registro duplicado. Registros sem acao implementada devem continuar em cinza, sem botao de equipar.
8. Mate um mob imediatamente depois de gastar magicules; o abate nao deve restaurar a energia que acabou de ser consumida.
9. Redimensione a janela, role catalogos/descricoes longos e navegue com Tab/Enter. Digitar a letra R na busca nao deve fechar o menu; a sincronizacao nao deve apagar a busca ou perder o foco.
10. Equipe nos tres slots e ligue/desligue uma passiva repetidamente. A conexao deve permanecer ativa, sem `Failed encoding custom payload tensura:request_ability_mode_change` ou `ClassCastException` envolvendo `NetworkAggregator$BufCustomPacketPayload`. O envio de pacotes nativos passa pelo Architectury; os testes de regressao verificam esse caminho.

### Dados de progressao e persistencia

1. Use `/tnw debug grant_skill degenerate` e confira **Habilidades**.
2. Convoque uma criatura, fique perto e use `/tnw naming_nearest Rigurd`.
3. Nomeie cinco subordinados e use `/tnw nation_found Tempest`.
4. Anote EP, raca, almas, skills, subordinados e nacao.
5. Saia do mundo, feche o jogo pelo menu e abra novamente por conta propria.
6. Confira os dados com `/tnw status`, `/tnw story` e `R`.

Para adiantar um marco de atributo em teste, use `/tnw debug add_essence <quantidade>`; o comando de investimento e `/tnw attribute <strength|vitality|agility|intelligence|will>`.

## Diagnostico

- Servidor e cliente: `run/logs/latest.log`.
- Crash reports: `run/crash-reports/`.
- Testes JUnit: pasta mais recente em `build/reports/tests/test-*`.
- Pacote: `powershell -ExecutionPolicy Bypass -File .\tools\validate-release.ps1`.

Ao relatar um erro, informe a acao executada, o comando usado e anexe `latest.log` e o crash report, quando existir.

## Transformacoes e sobrevivencia

Execute os GameTests descritos em [MORPH.md](MORPH.md), com e sem o Morph. No cliente, confirme o HUD em escalas de GUI diferentes, coracoes de absorcao, fome oculta no slime e visivel no cavalo, ar sob a agua, voo por dois toques em Espaco, corrida com Ctrl, troca de dimensao, reconexao e morte/respawn. Verifique que os indicadores seguem os valores reais e que investimentos e equipamentos continuam somando uma unica vez.

