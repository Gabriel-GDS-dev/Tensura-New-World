# 0.3.2-alpha — Sincronização de raça

O Raphael passa a acompanhar a raça real selecionada no Tensura, incluindo escolhas após entrar no mundo, mudanças por comandos e evoluções nativas. Um estágio local antigo não sobrescreve mais a escolha com Slime.

Ao mudar de raça, o estágio de evolução acompanha a nova raça e a evolução pendente antiga é removida. Marcos locais como Named Slime são preservados quando a raça nativa continua a mesma. Dados salvos com raça divergente são reconciliados com a raça nativa ao entrar no mundo.

Se a versão anterior já alterou a raça nativa para Slime, a raça desejada precisa ser selecionada novamente; o valor anterior não pode ser deduzido com segurança.

O pacote completo mantém Great Sage, Morph, Iris, Sodium e os quatro shaders.
