# Mesa da Semana — Android

APK inicial assinado disponível na release v0.1.0-5. Código Java nativo, armazenamento local e exportação/importação JSON.

Download: https://github.com/rubencastelhano87-cmd/mesa-da-semana/releases/tag/v0.1.0-5

Validação executada: compilação oficial Android, lintRelease, verificação apksigner e dois testes instrumentados no emulador API 29 (distribuição semanal, escalamento de porções, dedução de stock, preservação de ementa impossível e persistência após recriação). Download autenticado do APK publicado validado por SHA-256. QR 246 × 246 px publicado. A tentativa anónima pelo runner devolveu HTTP 403; não foi confirmada instalação num telemóvel físico.

Funcionalidades: despensa, sete jantares configuráveis, trocas individuais, lista agregada de faltas, transferência de compras, favoritos, guia de peixe e importação HTTPS de receitas estruturadas com confirmação manual.

Limites: descoberta automática de tendências, fotografias próprias do catálogo e acabamento visual premium ainda incompletos. Marcação de compras não persiste ao sair do ecrã. Não converter automaticamente unidades ou sinónimos. Instalação física e atualização entre versões ainda precisam de validação.

Assinatura: a release contém apenas uma cópia cifrada da chave de assinatura. A chave privada de recuperação está fora deste repositório. Antes de atualizar, recuperar a assinatura original com scripts/seal-signing.cjs e configurar MESA_KEYSTORE_BASE64, MESA_STORE_PASSWORD e MESA_KEY_PASSWORD como secrets. Nunca publicar o material privado. Incrementar versionCode. O workflow recusa gerar outra assinatura se já houver uma release.
