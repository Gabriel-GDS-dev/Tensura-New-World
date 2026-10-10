# Rastreabilidade do GDD

| Requisito | Implementacao |
|---|---|
| Minecraft 1.21.1, NeoForge, Java 21 | `gradle.properties`, `build.gradle` |
| Dados persistentes do jogador | `data/ModAttachments`, `data/TensuraPlayerData` |
| Formula de EP | `gameplay/ExistencePointCalculator` |
| Essencia por abates, progresso de crescimento e atributos persistentes | `event/GameplayEvents`, `data/TensuraPlayerData`, `data/CharacterAttribute` |
| Perfis e estagios raciais do catalogo Tensura, rendimentos e passivas | `registry/RaceRegistry`, `registry/RaceDefinition`, `gameplay/PlayerProgression` |
| Mimetizacao Slime, formas e estatisticas/resistencias capturadas | `data/MimicForm`, `event/GameplayEvents`, `network/SyncMimicFormPayload`, `client/ClientGameEvents` |
| Ficha responsiva, atributos e formas | `client/RaphaelScreen`, `command/TensuraCommands` |
| Caminhos de marco, limites e condicoes de evolucao das linhagens | `registry/EvolutionRegistry`, `registry/EvolutionCondition`, `registry/EvolutionPath` |
| Evolucao confirmada, sem mudanca automatica por nivel | `gameplay/RaceEvolutionManager`, `client/RaphaelScreen` |
| Tiers de skills | `registry/SkillTier`, `registry/SkillRegistry` |
| Harvest Festival e evolucao Demon Slime | `gameplay/HarvestFestivalManager`, `RaceEvolutionManager.applyHarvestEvolution` |
| Naming e custo | `gameplay/NamingManager` |
| Integracao ao mod-base | `integration/ReincarnatedBridge` |
| Subordinados e gifts | `data/SubordinateData`, `HarvestFestivalManager` |
| Nacoes, titulos e diplomacia | `TensuraPlayerData`, `command/TensuraCommands` |
| Campanha e capitulos | `gameplay/StoryManager`, `data/tensuranewworld/story/chapters.json` |
| Raphael, Ciel, Predator/Stomach | `client/RaphaelScreen`, `registry/SkillRegistry` |
| HUD sem conflito com menus | `client/ClientGameEvents` |
| HUD externo do Tensura desativado no pacote | `overrides/config/tensura/client/hud_config.toml`, `tools/validate-release.ps1` |
| Menu principal e fundo original | `client/TensuraTitleScreen`, `assets/tensuranewworld/textures/gui/title_background.png` |
| Great Sage opcional | `tools/install-great-sage.ps1`, `docs/GREAT_SAGE.md` |
| Pacotes de sincronizacao | `network/SyncPlayerDataPayload`, `network/NetworkHandler` |
| Validacao da distribuicao | `tools/validate-release.ps1`, `registry/EvolutionRegistryTest` |

O servidor continua sendo a autoridade. O cliente recebe snapshots para HUD e telas. Tensura: Reincarnated fornece conteudo canonico, entidades, racas e skills externas; a ponte usa as APIs publicas em tempo de execucao sem incorporar codigo ou recursos dos mods de terceiros ao nucleo.
