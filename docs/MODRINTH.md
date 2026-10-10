# Publicar no Modrinth

## 1. Gerar e validar o arquivo

No terminal do VS Code, dentro do projeto:

```powershell
.\gradlew.bat test --no-configuration-cache
.\gradlew.bat build --no-configuration-cache
powershell -ExecutionPolicy Bypass -File .\tools\build-modrinth-pack.ps1 -SkipBuild
```

O resultado e `dist/Tensura-New-World-0.3.1-alpha.mrpack`. O gerador chama o validador automaticamente e imprime tamanho e SHA-256. Nao envie um ZIP comum, o JAR do nucleo ou a pasta `libs` como versao do modpack.

## 2. Instalar o arquivo para testar

1. Abra o Modrinth App e clique no botao `+`.
2. Na tela **Create instance**, escolha **Upload a modpack**.
3. Selecione `dist/Tensura-New-World-0.3.1-alpha.mrpack`.
4. Aguarde as dependencias e reserve 6 GB a 8 GB de RAM.
5. Para incluir o Great Sage, siga `docs/GREAT_SAGE.md` depois que a instancia existir.
6. Inicie a instancia por conta propria e conclua a conferencia visual de `docs/TESTING.md`.

## 3. Criar o projeto no site

1. Entre em [modrinth.com](https://modrinth.com) e faca login.
2. Abra o menu do perfil e clique **Create a project**.
3. Escolha o tipo **Modpack**.
4. Nome: `Tensura - New World`.
5. Resumo: `Progressao unificada por EP, Essencia de abates, atributos, racas, mimetizacao Slime, Raphael e Harvest Festival.`
6. Marque suporte para **Client and server**.
7. Use `src/main/resources/tensuranewworld.png` como icone.
8. Informe na descricao que este e um projeto de fa independente e nao oficial.
9. Nao use imagens, logos, frames ou personagens oficiais sem autorizacao.

## 4. Descricao recomendada

Inclua estes dados na pagina:

- Minecraft 1.21.1, NeoForge 21.1.252 e Java 21.
- 6 GB de RAM no minimo e 8 GB recomendados.
- Evolucao por EP mais condicoes do mundo, confirmada no Raphael.
- Essencia de abates, cinco atributos investiveis e rank derivado do EP.
- Mimetizacao Slime: catalogo persistente de mobs derrotados com modelo, atributos e resistencias capturados.
- Predacao automatica Slime: absorva habilidades basicas aprendidas dos mobs derrotados, incluindo o sopro paralisante da Evil Centipede, e equipe pelo Raphael.
- Morph V2 opcional para transformacoes animadas (instalacao manual; nao incluido nem redistribuido no `.mrpack`).
- Ficha responsiva em `R` com tema de RPG; HUD superior de vida, magiculas e sobrevivencia. Hotbar e experiencia vanilla preservadas.
- Catalogo de habilidades integrado ao Reincarnated: categorias, busca, modos, custos de magicules/aura, aprendizado, recarga e nove presets nativos com tres slots cada; veja `docs/SKILLS.md`.
- Predator/Stomach nativo continua sob controle do Reincarnated. A aba Estomago do New World mostra somente registros proprios e nao deposita, retira ou sincroniza itens.
- Naming, subordinados, nacoes, Predator/Stomach, Great Sage opcional e Harvest Festival.
- Tecla `R` para Raphael, `G` para Great Sage e `V` para analise quando o addon estiver instalado.
- Creditos e links de `CREDITS.md`.
- Aviso de projeto de fa nao oficial.

## 5. Enviar a versao

1. Na pagina do projeto, clique **Upload version**.
2. Nome da versao: `0.3.1 Alpha`.
3. Numero da versao: `0.3.1-alpha`.
4. Canal: **Alpha**.
5. Game version: **1.21.1**.
6. Loader: **NeoForge**.
7. Arquivo: `dist/Tensura-New-World-0.3.1-alpha.mrpack`.
8. Marque compatibilidade de cliente e servidor quando o formulario pedir.
9. Cole o changelog abaixo.
10. Salve, resolva qualquer alerta do validador do Modrinth e envie para revisao.

### Banners e descricao pronta

A descricao revisada, com a evolucao, controles, presets e limites da aba Estomago, esta em [MODRINTH_DESCRIPTION.md](MODRINTH_DESCRIPTION.md). Quatro banners originais em PNG estao em [modrinth-assets](modrinth-assets). Antes de colar a descricao, envie os PNGs pela ferramenta de imagens do editor do Modrinth e substitua os marcadores `URL_DO_BANNER_...` pelas URLs que o site fornecer. Os caminhos locais da pasta `docs` servem para abrir e revisar as artes no repositorio; eles nao funcionam como URL de imagem na pagina do Modrinth.

## Changelog sugerido

```text
0.3.1 Alpha

- Novo sistema de evolucao por linhagem, EP e acontecimentos do mundo.
- Evolucoes normais agora exigem confirmacao no painel Raphael.
- Nova aba Evolucao com checklist completo de requisitos.
- Ficha Raphael responsiva com status, atributos, evolucao, formas, habilidades, Stomach, seguidores e nacao.
- Habilidades organizadas por categoria, com busca, detalhes de modos/custos, estados bloqueados em cinza e orientacoes de desbloqueio.
- Equipamento nos slots e alternancia de passivas pelas APIs nativas do Reincarnated, com layout renovado e cores por categoria.
- Corrigida desconexao ao equipar skills ou alternar passivas: os pacotes nativos agora passam pela codificacao do Architectury.
- Abates deixam de restaurar magicules gastos imediatamente antes por uma habilidade.
- Essencia por abates, cinco atributos investiveis e perfis com passivas para 16 linhagens.
- Mimetizacao de mobs para Slime com aparencia, atributos e resistencias sincronizados.
- Morph V2 opcional para transformacoes animadas; instalacao manual no cliente e servidor, sem redistribuicao no pacote.
- HUD do Tensura: Reincarnated desativado pelo override do modpack; novo HUD de RPG para vida, magiculas e sobrevivencia.
- Camera do Morph corrigida para evitar aplicar o tamanho da raca slime aos olhos de mobs maiores.
- Menus inicial, de mundos e painel R com tema de RPG, ornamentos dourados e icones de habilidades.
- Telas concorrentes de Status/Evolucao redirecionadas para Raphael; Great Sage continua em `G`.
- Menu principal e telas de selecao/criacao de mundo com a identidade visual de New World.
- Integracao e guia de instalacao opcional do Great Sage 0.0.6.1.
- Sincronizacao corrigida com as APIs de magicules, EP e racas do Tensura: Reincarnated.
- Testes automaticos, servidor headless e validacao estrutural do .mrpack.
```

## 6. Great Sage e permissoes

O Great Sage 0.0.6.1 tem licenca `All Rights Reserved` e nao esta hospedado no Modrinth. Por isso, seu JAR nao e colocado em `overrides/mods` e nao e redistribuido no `.mrpack` publico sem permissao expressa do autor.

O pacote inclui apenas um guia de instalacao. As outras seis dependencias sao baixadas pelas URLs do CDN do Modrinth declaradas em `modrinth.index.json`. Nao envie os JARs de terceiros como arquivos adicionais.

## 7. Atualizacoes futuras

1. Altere `mod_version` em `gradle.properties`.
2. Atualize a versao nos scripts de build e validacao.
3. Execute testes, build, geracao e validacao do `.mrpack`.
4. Publique uma nova versao com changelog; nunca substitua silenciosamente um arquivo ja publicado.

## Guia de habilidades e Great Sage

Em `R > Habilidades`, a lista inclui habilidades adquiridas e bloqueadas, organizadas por categoria. Selecione uma e role a ficha ate **COMO DESBLOQUEAR**. O guia cobre os registros do Reincarnated e do addon Great Sage destas versoes, alem de Raphael/Ciel do New World, informa requisitos da configuracao do servidor e distingue rotas naturais de comandos administrativos. Veja [SKILLS.md](SKILLS.md#great-sage-por-padrao) para a concessao automatica gratuita de Great Sage e [SAGE_EVOLUTION.md](SAGE_EVOLUTION.md) para a linha automatica Great Sage > Raphael > Ciel.

Na ficha da habilidade, use o botao de conjunto para alternar os presets nativos do Reincarnated antes de escolher um dos tres slots; cada conjunto mantem seu proprio equipamento. Para Predator, Gluttony ou Beelzebuth, equipe e ative o modo indicado pela ficha e consulte **Opcoes > Controles > Tensura** para os controles nativos. `R > Estomago` nao e o inventario nativo e nao sincroniza deposito/retirada; veja [MENUS.md](MENUS.md#estomago) e [SKILLS.md](SKILLS.md).

## Ajuda de menus (0.3.1)

O HUD superior fica a direita, deixando as opcoes do Morph livres. No painel `R`, selecione qualquer aba e clique **AJUDA**. Role para ler; **VOLTAR** ou **Esc** retorna a aba. O guia inclui custos, controles, requisitos raciais e limites das integracoes. Veja [MENUS.md](MENUS.md) para as oito abas, menus externos e as 68 racas registradas; [RELEASE_0.3.1.md](RELEASE_0.3.1.md) descreve a atualizacao.

## Dados separados por mundo

O progresso e a selecao do battle mode ficam separados por mundo. Mundos novos comecam no nivel inicial do Great Sage, com zero XP ganho e lista de batalha vazia; o progresso dos saves existentes e preservado. Veja [WORLD_ISOLATION.md](WORLD_ISOLATION.md) para detalhes e verificacao.

## Morph e painel de sobrevivencia

Na versao 0.3.1 Alpha, os menus e o painel `R` usam um novo tema de RPG com azul, dourado e marfim. O HUD superior traz vida, magiculas e informacoes do corpo atual. A camera deixa de aplicar o tamanho da raca slime aos olhos do mob transformado. Veja [RELEASE_0.3.0.md](RELEASE_0.3.0.md) para as alteracoes e instrucoes de atualizacao.

A camera acompanha a altura dos olhos durante a transformacao e volta a altura da forma atual ao retornar para slime. Em terceira pessoa, tanto de frente quanto de costas, a distancia aumenta conforme a altura/largura do corpo e diminui para formas pequenas. A colisao vanilla com paredes continua ativa.

Integracao opcional com Morph 0.1.4: atributos corporais sem duplicacao, correcao da velocidade dos cavalos, voo de corpos aereos e alimentacao conforme a forma atual. O painel inclui vida, absorcao, defesa, ataque, velocidade e ar quando necessario. Veja [MORPH.md](MORPH.md) para controles, regras e testes. Morph continua sendo uma dependencia opcional instalada separadamente.
