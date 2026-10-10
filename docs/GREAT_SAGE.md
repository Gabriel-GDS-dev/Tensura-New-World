# Grande Sabio

O addon oficial **Great Sage - Tensura Reincarnated Addon 0.0.6.1** e usado nos testes locais. Ele adiciona voz, legendas e apresentacoes para skills e marcos de progressao.

## Instalar na instancia do Modrinth

1. No Modrinth App, abra a instancia de `Tensura - New World`.
2. Abra `Options` e use `Open folder`. Feche o Minecraft antes de continuar.
3. Copie o caminho completo dessa pasta.
4. No PowerShell, dentro deste projeto, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\install-great-sage.ps1 -InstancePath "COLE_AQUI_A_PASTA_DA_INSTANCIA"
```

5. Abra a instancia novamente. Na lista de mods deve aparecer `Great Sage 0.0.6.1`.
6. Dentro do mundo, use `G` para o menu do Grande Sabio e `V` para analisar, conforme as teclas padrao do addon. A interface Raphael do New World continua em `R`.

O instalador baixa o JAR diretamente do arquivo oficial no CurseForge e valida o SHA-256. O addon nao esta embutido no `.mrpack` publico porque a licenca dele e `All Rights Reserved` e o arquivo nao possui uma URL de distribuicao aceita pelo formato Modrinth.

Para preparar apenas os testes de desenvolvimento, sem copiar nada para uma instancia, execute:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\install-great-sage.ps1 -DevelopmentOnly
```
