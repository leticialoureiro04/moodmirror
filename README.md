# MoodMirror - Espelho Inteligente de Emoções e Ambiente

Aplicação Android académica em Kotlin + Jetpack Compose que combina:
- CameraX (`Preview` + `ImageAnalysis`)
- ML Kit Face Detection
- Sensor de luz (com fallback quando indisponível)
- Acelerómetro para detetar estabilidade/movimento

Objetivo: analisar rosto + ambiente em tempo real e apresentar feedback simples de bem-estar e concentração, sem qualquer diagnóstico médico.

## Funcionalidades atuais

- Ecrã inicial com explicação do projeto.
- Preview de câmara em tempo real.
- Deteção de rosto com ML Kit.
- Estimativa académica de emoção (`Feliz`, `Neutra/o`, `Triste ou baixa energia`, `Cansada/o`).
- Leitura de luminosidade e movimento.
- Feedback final contextual com qualidade da análise (`Boa`, `Média`, `Fraca`).
- Guardar registos de emoção/estado no dispositivo.
- Consultar histórico de registos.
- Resumo simples de padrão de humor (emoção mais frequente).

## Arquitetura (resumo)

- `MainActivity`: ponto de entrada e gestão de navegação (intro, análise, histórico).
- `MoodViewModel`: estado da UI, lógica de combinação e operações de guardar registos.
- `SensorManagerHelper`: leitura de sensor de luz e acelerómetro.
- `CameraPreview`: integração CameraX com `Preview` e `ImageAnalysis`.
- `FaceAnalyzer`: pipeline de deteção facial com ML Kit.
- `MoodDatabase` + `MoodRecordDao` + `MoodRepository`: persistência local com Room.
- `IntroScreen`, `MainScreen`, `HistoryScreen`: UI Compose.

## Fluxo da app

1. Utilizador abre a app e vê o ecrã inicial.
2. Clica em "Começar análise".
3. App pede permissão da câmara.
4. CameraX inicia preview + análise.
5. ML Kit deteta rosto e atributos simples (sorriso/olhos).
6. Sensores atualizam dados de luz e movimento.
7. ViewModel gera feedback final e qualidade da análise.
8. Utilizador pode guardar registo e consultar histórico.

## Como executar

1. Abrir a pasta `moodmirror` no Android Studio.
2. Sincronizar o Gradle.
3. Garantir Android SDK 34 instalado.
4. Executar em dispositivo Android (recomendado) ou em emulador.
5. Conceder permissão de câmara.

## Requisitos técnicos atendidos

- Kotlin
- Jetpack Compose
- CameraX com dois casos de uso:
  - `Preview`
  - `ImageAnalysis`
- ML Kit Face Detection
- Sensor de luz com fallback
- Acelerómetro
- Ecrã inicial explicativo
- Aplicação funcional organizada por camadas (UI, ViewModel, dados locais)
- Persistência local e histórico de registos para acompanhamento temporal

## Nota académica

Esta aplicação é para fins académicos e demonstração técnica.  
As inferências de emoção/fadiga são aproximações simples e não constituem avaliação clínica.
