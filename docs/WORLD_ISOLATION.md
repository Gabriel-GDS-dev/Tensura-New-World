# Progresso separado por mundo

Cada mundo novo possui uma identidade propria e comeca com o personagem inicial: Great Sage gratuito, sem XP ganho do sabio, sem selecao de battle mode, sem observacoes e sem evolucoes herdadas. O nivel inicial do addon e **1**, com **0 XP**. Habilidades raciais iniciais continuam seguindo a reencarnacao escolhida.

## Battle mode

O addon originalmente guardava `battleSkills` na configuracao global `greatsage-client.toml`. Essa lista reaparecia em outros mundos e inflava a contagem com habilidades que o personagem nao tinha.

O New World agora salva essa selecao no **personagem daquele mundo**, no servidor. A configuracao global antiga e ignorada. A contagem considera somente IDs distintos de habilidades aprendidas pelo personagem atual e aceitas na lista de battle mode; habilidades nao adquiridas, em aprendizado, resistencias e o registro interno de compatibilidade Raphael nao entram na selecao. O servidor confirma as mudancas e rejeita pedidos com a identidade de outro mundo.

Reabrir o mesmo mundo, mudar de dimensao ou morrer preserva sua selecao. Criar outro mundo com a mesma conta comeca com selecao vazia. E necessario selecionar novamente as habilidades desejadas no primeiro uso da nova lista por mundo; a lista global anterior nao e importada automaticamente.

## Nivel do Great Sage

O addon concedia XP ao registrar habilidades iniciais, inclusive algumas com dominio negativo que ainda nao estavam aprendidas. Isso podia fazer um personagem novo parecer trazer nivel de outro mundo.

Agora as habilidades de entrada/reencarnacao sao registradas como parte do kit inicial, sem XP ganho. Registros ainda em aprendizado nao sao anunciados nem contabilizados como novas habilidades aprendidas. Depois da inicializacao, aprender habilidades e observar criaturas continua concedendo XP pelas regras do addon. O progresso salvo em mundos existentes e preservado; esta correcao nao zera personagens toda vez que entram no jogo.

## Outros dados

- Ficha, nivel exibido, slots de analise, avisos e selecao de battle mode sao limpos na troca de conexao, antes de receber o estado do novo mundo.
- Observacoes locais continuam no arquivo do respectivo save. Em servidores, o arquivo e separado pela identidade do mundo, mesmo quando o endereco do servidor e igual.
- Arquivos globais antigos de observacoes nao sao importados automaticamente apenas porque possuem o mesmo nome de um mundo novo.
- Estados temporarios do addon, inclusive uma evolucao de Raphael pendente, sao descartados ao sair e ao encerrar o servidor; eles nao continuam no proximo mundo da mesma conta.

## Verificacao

Os testes incluem personagens diferentes da mesma conta, preservacao do progresso anterior, starter sem XP, ganhos reais posteriores, selecoes invalidas e duplicadas, pedidos de outro mundo, morte/reabertura, identidade persistente e cancelamento de evolucao pendente. Um teste no cliente real confirma que a configuracao global de batalha e ignorada e que nivel, XP, selecao e observacoes nao sobrevivem a troca de sessao.

Reproducao automatica do cliente, em uma pasta de desenvolvimento separada e sem tocar nos saves do Modrinth:

```powershell
.\gradlew.bat runClient -PtnwGameTests -PtnwClientIsolationSmoke -PtnwResourceRun=world-isolation-client --no-configuration-cache
```
