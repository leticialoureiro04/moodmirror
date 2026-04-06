# MoodMirror - Espelho Inteligente de Emocoes e Ambiente

Aplicacao Android em Kotlin + Jetpack Compose que combina:
- `CameraX` (`Preview` + `ImageAnalysis`)
- `ML Kit Face Detection`
- Sensor de luz (fallback quando indisponivel)
- Acelerometro para estado de movimento

Objetivo: mostrar feedback simples em tempo real sobre condicoes de utilizacao e sinais basicos de fadiga, sem diagnostico medico.

## Arquitetura (resumo)

- `MainActivity`: ponto de entrada, alterna entre ecrã inicial e ecrã principal, regista sensores.
- `MoodViewModel`: estado central da UI e regra simples de combinacao (`face + luz + movimento`).
- `SensorManagerHelper`: leitura do sensor de luz e acelerometro.
- `CameraPreview`: integra CameraX com `PreviewView`, `Preview` e `ImageAnalysis`.
- `FaceAnalyzer`: analisa frames com ML Kit Face Detection.
- `IntroScreen` e `MainScreen`: UI em Compose.

Fluxo:
1. Utilizador entra no ecrã inicial.
2. Clica em "Comecar analise".
3. App pede permissao da camera.
4. CameraX inicia preview + analise.
5. ML Kit deteta rosto e probabilidades de olhos abertos.
6. Sensores atualizam luminosidade e movimento.
7. ViewModel calcula feedback final.

## Como executar

1. Abrir a pasta `moodmirror` no Android Studio.
2. Esperar sincronizacao Gradle.
3. Confirmar SDK Android 34 instalado.
4. Correr em dispositivo fisico Android (recomendado para sensores e camera) ou em emulador com camera virtual.
5. Conceder permissao de camera quando pedido.

## Requisitos tecnicos atendidos

- Kotlin
- Jetpack Compose
- CameraX com:
  - `Preview`
  - `ImageAnalysis`
- ML Kit Face Detection
- Sensor de luz com fallback
- Acelerometro
- Ecrã inicial + ecrã principal
- Feedback em tempo real com logica academica simples

## Melhorias futuras

- Suporte a historico local de sessoes (Room).
- Melhorar heuristica de fadiga com janela temporal (media de varios frames).
- Ajustar limiares por calibracao no dispositivo.
- Adicionar overlay visual de bounding box no rosto.
- Adicionar internacionalizacao (PT/EN) em `strings.xml`.
