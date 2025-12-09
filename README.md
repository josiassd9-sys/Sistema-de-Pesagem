# Pesagem Pagina Inicial

Projeto Android para pesagem com integração de balança via TCP/IP e gerenciamento de registros (Room).

Setup rápido

1. Instale o JDK 11 (ou superior) e Android Studio.
2. Certifique-se que o Gradle wrapper foi instalado. Você pode executar o script:

```powershell
powershell -ExecutionPolicy Bypass -File .\gradle\scripts\install-gradle.ps1 -DryRun
# Remova -DryRun para instalar de verdade:
# powershell -ExecutionPolicy Bypass -File .\gradle\scripts\install-gradle.ps1 -Version "8.14.3" -TimeoutSeconds 3600
```

3. Sincronize/Sync no Android Studio.
4. Build e run:

```powershell
.\gradlew.bat clean :app:assembleDebug --no-daemon --stacktrace
```

Conexão com a balança

- Parâmetros de conexão (host/port) são salvos nas SharedPreferences. Padrões:
  - host: 192.168.1.100
  - port: 1234

- O app lê linhas de texto via TCP e extrai o primeiro número decimal encontrado (ex.: "ST,GS, 0070.00kg").

Arquivos importantes

- `app/src/main/java/com/josias/pesagempaginainicial/PesoAvulsoActivity.kt` - lógica de UI e conexão com a balança.
- `app/src/main/java/com/josias/pesagempaginainicial/data` - entidades e DAO do Room.

Suporte

Se houver problemas no build, cole o log do Gradle aqui para que eu analise.

