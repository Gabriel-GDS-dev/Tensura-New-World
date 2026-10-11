# 0.3.4-alpha — Estabilidade gráfica

O relatório enviado registrou `GL_OUT_OF_MEMORY` no driver OpenGL da NVIDIA seguido por uma falha nativa fora da JVM. O perfil estava iniciando com Complementary Unbound ativado. Isso aponta para esgotamento de memória gráfica durante a renderização; o relatório não mostra uma exceção Java do New World como causa.

- Shaders agora iniciam desativados e sem um pacote selecionado.
- A distância máxima de sombras foi reduzida de 32 para 16 chunks como limite adicional quando um shader é ativado manualmente.
- BSL, Complementary Unbound, Photon e Solas continuam incluídos e podem ser ativados individualmente nas opções de vídeo.
- A documentação do pacote e o validador agora verificam essas configurações de estabilidade.

Em uma instância já existente, as configurações de vídeo podem ser preservadas pelo launcher. Para aplicar o padrão com shaders desligados, feche o jogo e defina `enableShaders=false`, `shaderPack=` e `maxShadowRenderDistance=16` em `config/iris.properties`, ou desative shaders nas opções de vídeo. Se a falha persistir com shaders desligados, atualize o driver NVIDIA e verifique o uso de memória da GPU por outros programas.
