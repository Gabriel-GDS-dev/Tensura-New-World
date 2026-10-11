# Pacote completo — 0.3.4-alpha-full.1

Inclui automaticamente Great Sage 0.0.6.1, Morph 0.1.4, Iris 1.8.14-beta.1 e Sodium 0.8.13, além de BSL 10.1.8, Complementary Unbound r5.9.3, Photon v1.3b e Solas 3.7b.

Por estabilidade, o pacote inicia com shaders desativados, nenhum shader selecionado e distância máxima de sombras limitada a 16 chunks. Os quatro shaders continuam disponíveis para ativação manual nas opções de vídeo; apenas um shader fica ativo por vez. Ativar shaders pode aumentar o uso de memória gráfica e causar instabilidade em alguns computadores. Iris, Sodium e shaders são exclusivos do cliente e não são instalados no servidor.

Os oito arquivos foram identificados pela API do Modrinth usando SHA-1 dos arquivos instalados. Os downloads usam o CDN do Modrinth, com SHA-1, SHA-512 e tamanho fixados em `tools/modrinth-extra-files.json`. Great Sage e Morph estão hospedados no Modrinth nessas versões: as instruções antigas de instalação manual não se aplicam a esta versão do pacote. Morph 0.1.4 não declara iChunUtil como dependência na API; este pacote reproduz a instância instalada sem acrescentar essa biblioteca.

Gerar: `powershell -ExecutionPolicy Bypass -File .\tools\build-modrinth-pack.ps1`

Publicar o novo `.mrpack` como nova versão no projeto existente. Changelog sugerido:

> Correção de estabilidade gráfica: shaders agora começam desativados para evitar o esgotamento de memória de vídeo observado com Complementary Unbound. BSL, Complementary Unbound, Photon e Solas continuam incluídos e podem ser ativados manualmente. Distância de sombras reduzida para 16 chunks.
