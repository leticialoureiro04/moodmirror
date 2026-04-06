# MoodMirror - Espelho Inteligente de Emocoes e Ambiente

Aplicacao Android academica em Kotlin + Jetpack Compose que combina:
- CameraX (`Preview` + `ImageAnalysis`)
- ML Kit Face Detection
- Sensor de luz (com fallback quando indisponivel)
- Acelerometro para detetar estabilidade/movimento

Objetivo: analisar rosto + ambiente em tempo real e apresentar feedback simples de bem-estar e concentracao, sem qualquer diagnostico medico.

## Funcionalidades atuais

- Ecrã inicial com explicacao do projeto.
- Preview de camera em tempo real.
- Deteccao de rosto com ML Kit.
- Estimativa academica de emocao (`Feliz`, `Neutra/o`, `Triste ou baixa energia`, `Cansada/o`).
- Leitura de luminosidade e movimento.
- Feedback final contextual com qualidade da analise (`Boa`, `Media`, `Fraca`).
- Guardar registos de emocao/estado no dispositivo.
- Consultar historico de registos.
- Resumo simples de padrao de humor (emocao mais frequente).

## Arquitetura (resumo)

- `MainActivity`: ponto de entrada e gestao de navegacao (intro, analise, historico).
- `MoodViewModel`: estado da UI, logica de combinacao e operacoes de guardar registos.
- `SensorManagerHelper`: leitura de sensor de luz e acelerometro.
- `CameraPreview`: integracao CameraX com `Preview` e `ImageAnalysis`.
- `FaceAnalyzer`: pipeline de deteccao facial com ML Kit.
- `MoodDatabase` + `MoodRecordDao` + `MoodRepository`: persistencia local com Room.
- `IntroScreen`, `MainScreen`, `HistoryScreen`: UI Compose.

## Fluxo da app

1. Utilizador abre a app e ve o ecrã inicial.
2. Clica em "Comecar analise".
3. App pede permissao da camera.
4. CameraX inicia preview + analise.
5. ML Kit deteta rosto e atributos simples (sorriso/olhos).
6. Sensores atualizam dados de luz e movimento.
7. ViewModel gera feedback final e qualidade da analise.
8. Utilizador pode guardar registo e consultar historico.

## Como executar

1. Abrir a pasta `moodmirror` no Android Studio.
2. Sincronizar o Gradle.
3. Garantir Android SDK 34 instalado.
4. Executar em dispositivo Android (recomendado) ou em emulador.
5. Conceder permissao de camera.

## Requisitos tecnicos atendidos

- Kotlin
- Jetpack Compose
- CameraX com dois casos de uso:
  - `Preview`
  - `ImageAnalysis`
- ML Kit Face Detection
- Sensor de luz com fallback
- Acelerometro
- Ecrã inicial explicativo
- Aplicacao funcional organizada por camadas (UI, ViewModel, dados locais)
- Persistencia local e historico de registos para acompanhamento temporal

## Nota academica

Esta aplicacao e para fins academicos e demonstracao tecnica.  
As inferencias de emocao/fadiga sao aproximacoes simples e nao constituem avaliacao clinica.
