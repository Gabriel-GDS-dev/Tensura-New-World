# Tensura New World 0.3.0 Alpha — Crônicas de Tempest

## Interface

Interface original de RPG com azul profundo, dourado, marfim e ornamentos geométricos, mantendo a arte e a identidade de New World. Inspiração visual em menus de aventura como Zelda e Genshin; sem reutilizar assets desses jogos.

- Menu inicial com composição de cenário e painel de ações, adaptado à escala da interface.
- Seleção/criação de mundos e menu de pausa com cenário e botões no mesmo estilo. Opções e controles nativos de criação de mundo são preservados.
- HUD superior com vida, absorção, magículas, EP, almas, defesa e ataque. Alimento e ar aparecem conforme o corpo. O HUD substitui as barras vanilla correspondentes; hotbar e experiência continuam disponíveis.
- Painel `R` com navegação lateral, cartões de personagem e ícones no catálogo de habilidades. Busca, categorias, guias, equipar habilidades e evolução continuam disponíveis.

## Câmera

A câmera usa as medidas do corpo renderizado pelo Morph, sem multiplicar os olhos do mob pelo tamanho da raça slime do jogador. Alturas zeradas, inválidas ou fora do corpo recebem uma altura proporcional ao corpo. As transições do Morph e a colisão em terceira pessoa são preservadas.

## Atualizar

Para atualizar um perfil existente, feche o Minecraft e substitua o JAR `tensuranewworld-0.2.0-alpha.jar` pelo `tensuranewworld-0.3.0-alpha.jar`. Mantenha apenas um JAR do núcleo na pasta `mods`. O `.mrpack` permite criar um novo perfil no Modrinth App. Great Sage e Morph continuam sendo instalações opcionais separadas, conforme os guias incluídos.

A atualização não apaga mundos ou progresso. Não é necessário recriar um mundo.

## Validação

Testes unitários de geometria da câmera, teste com Morph para esqueleto/lagartos e multiplicador de slime, testes de integração de habilidades/evolução/isolamento e capturas do cliente em perfil separado. Os recursos das interfaces são desenhados na escala atual do Minecraft.
