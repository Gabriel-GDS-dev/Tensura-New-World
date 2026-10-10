# Transformações e sobrevivência

Com o Morph 0.1.4 instalado, derrote a criatura para adquirir sua forma, abra a seleção com `[` ou `]` e confirme com Enter ou clique. As teclas podem ser alteradas em Opções → Controles. Selecione sua forma original para desfazer a transformação. A aba Formas do Raphael mostra o corpo atual, os atributos e os controles.

O corpo transformado fornece vida máxima, ataque, armadura e velocidade. Resistência a empurrões, força de salto, dureza da armadura e empurrão dos ataques são herdados quando esses atributos estão disponíveis na entidade. Os investimentos do personagem, equipamentos e outros modificadores continuam somando seus bônus. O corpo visual, dimensões e animações continuam sob controle do Morph.

A integração corrige o limite de velocidade 0.1 do Morph: cavalos passam a usar a velocidade corporal. Abates registrados pelo New World conservam a velocidade observada, inclusive em formas adquiridas novamente; para formas antigas sem esse registro, usa-se a velocidade base da entidade reconstruída pelo Morph. O catálogo mantém os melhores valores registrados por tipo de criatura, seguindo o comportamento de aquisição do Morph.

Aves voadoras, morcegos e criaturas aéreas recebem voo. Dê dois toques em Espaço para ativar ou parar; segure Espaço para subir e Shift para descer. Formas terrestres não recebem voo. Galinhas planam, sem voo livre. Ctrl ativa a corrida. O voo introduzido pela transformação termina ao trocar para um corpo terrestre; o voo de Criativo é preservado.

Transformações mantêm a **proporção da vida restante**: 10/20 torna-se 15/30, voltando a 10/20. A animação é respeitada e os ajustes adicionais aguardam a troca real dos modificadores do Morph. Não há cura gratuita ao mudar de corpo.

A alimentação acompanha o corpo atual. Slime, mortos-vivos e golens inorgânicos não precisam comer. O corpo original também respeita as linhagens sem alimentação cadastradas, como Slime e demônios. Um slime transformado em cavalo, por exemplo, precisa se alimentar enquanto permanecer nessa forma. A fome anterior é guardada ao entrar em um corpo sem alimentação e restaurada ao voltar a um corpo que come: alternar formas não recarrega a barra. Raças e habilidades do Reincarnated continuam com suas regras próprias de regeneração.

Corpos que respiram debaixo d'água mantêm o ar; corpos com imunidade natural ao fogo bloqueiam dano de fogo. Essas condições são retiradas ao sair da forma. Transformações não concedem automaticamente os ataques especiais ou skills da criatura.

O painel de sobrevivência mostra vida e absorção, magicules, EP, almas, defesa, ataque, velocidade e forma atual. Alimentação aparece apenas quando necessária; ar aparece quando relevante e o corpo não respira na água. Vida, alimento e ar vêm dos valores atuais do Minecraft; as informações corporais vêm do servidor. A barra vanilla de fome também fica oculta em corpos sem alimentação.

Sem Morph, a mimetização New World continua disponível para linhagens Slime com formas absorvidas. Os atributos e as regras de sobrevivência também são aplicados nesse modo.

## Verificação automatizada

```powershell
.\gradlew.bat test build --no-configuration-cache
.\gradlew.bat runGameTestServer -PtnwGameTests --no-configuration-cache
.\gradlew.bat runGameTestServer -PtnwGameTests "-PmorphTestJar=C:\caminho\morph-0.1.4.jar" --no-configuration-cache
```

Os GameTests usam mundos isolados em `build/body-gametest-nw` e `build/body-gametest-morph`, entidades reais e jogadores simulados. As classes e a estrutura de teste ficam em `src/gametest` e não entram no JAR distribuído. A validação visual do HUD e dos controles físicos de voo exige abrir o cliente.
