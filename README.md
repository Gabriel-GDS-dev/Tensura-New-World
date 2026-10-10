# Tensura - New World

Modpack RPG para Minecraft 1.21.1 e NeoForge, construido sobre **Tensura: Reincarnated**. O nucleo New World aplica as regras do GDD com o servidor como autoridade e usa o mod-base para racas, skills, entidades e conteudo do mundo.

## Sistemas implementados

- Progressao unificada por **Existence Points**: abates concedem Essencia, aumentam magicules e poder fisico; o EP determina o rank sem uma barra de nivel concorrente.
- Cinco atributos investiveis (Forca, Vitalidade, Agilidade, Inteligencia e Vontade), com pontos por marcos de Essencia e efeitos reais nos atributos do jogador.
- Perfis raciais para Slime, Humano, Goblin, Ogre/Oni, Orc, Daemon, Dwarf, Elf, Lizardman/Dragonewt, Merfolk, Beastfolk, Harpy, Giant, Vampire e Wight.
- Predator registra mobs derrotados como formas para Slimes; a ficha permite assumir e abandonar formas, aplicando modelo, estatisticas e resistencias registradas.
- Evolucao por linhagem, EP e condicoes reais do mundo: nomeacao, batalhas, titulos, fome critica, contratos, duelo no Nether, altares e Harvest Festival.
- Nenhuma evolucao normal e automatica: Raphael mostra os requisitos e o jogador confirma em **Evoluir** ou com `/tnw evolve`.
- Demon Lord Seed aos 1.000 pontos de alma e Harvest Festival aos 10.000.
- Sono de tres minutos, despertar, multiplicacao de magicules, gifts e fusao de skills.
- Naming com custo `(EP base do alvo * 2) + (tier * 10.000)`.
- Skills Common, Extra, Unique, Ultimate e Manas.
- Predator/Stomach, Raphael/Ciel, subordinados, nacao e diplomacia.
- Ficha Raphael redimensionada pela resolucao na tecla `R`, com status, evolucao, atributos, formas, skills, Stomach, seguidores e nacao.
- HUD de progressao do mod-base desativado no `.mrpack`; HUD superior New World com vida, magiculas e sobrevivencia, oculto durante menus. Hotbar e experiencia vanilla preservadas.
- Menu principal proprio com arte original do New World.
- Ponte reflexiva com as APIs de Tensura: Reincarnated e ManasCore.
- Compatibilidade local verificada com o addon Great Sage 0.0.6.1.

## Comeco rapido

```powershell
.\gradlew.bat test --no-configuration-cache
.\gradlew.bat build --no-configuration-cache
powershell -ExecutionPolicy Bypass -File .\tools\build-modrinth-pack.ps1 -SkipBuild
```

O arquivo final e `dist/Tensura-New-World-0.3.1-alpha.mrpack`. No Modrinth App, escolha **Create instance > Upload a modpack** e selecione esse arquivo.

Os procedimentos completos estao em [docs/TESTING.md](docs/TESTING.md), [docs/MODRINTH.md](docs/MODRINTH.md) e [docs/GREAT_SAGE.md](docs/GREAT_SAGE.md).

Os novos campos sao anexados aos dados existentes do jogador. Salvamentos antigos recebem valores padrao automaticamente; mundos existentes nao precisam ser apagados.

## Desenvolvimento

- Java 21
- Minecraft 1.21.1
- NeoForge 21.1.252
- Mod ID: `tensuranewworld`
- Mod-base: Tensura: Reincarnated 2.0.1.3

O `.mrpack` publico aponta para os arquivos das dependencias hospedados pelos proprios autores no Modrinth. O Great Sage e instalado separadamente a partir do arquivo oficial e nao e redistribuido dentro do pack.
