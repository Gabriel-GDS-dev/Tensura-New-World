# 0.3.5 Alpha — seleção alinhada à câmera

- A seleção de blocos e mobs usa a mesma altura dos olhos da câmera da forma atual.
- Corrigido o deslocamento vertical que exigia mirar abaixo de baús e outros blocos interativos durante transformações.
- A posição interpolada acompanha o Morph; a posição usada pelas verificações de alcance no servidor segue o mesmo cálculo.
- Minecraft 1.21.1 / NeoForge 21.1.252. Pacote completo: `0.3.5-alpha-full.1`.

Validação: build e testes unitários; GameTests com Morph 0.1.4, incluindo comparação da origem da seleção com a altura da câmera em três frações de tick e na posição usada pelo servidor.

Conferência no jogo: em primeira pessoa, mire no centro de um baú e abra com o botão direito; mire em um mob e ataque/interaja. Repita como slime e em formas maiores, durante transformação e após retornar à forma original.
