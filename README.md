# Mesa da Semana — Android nativo

**Estado: implementação inicial, sem compilação Android nem testes em dispositivo. Não existe APK publicado nesta entrega.**

Java e interfaces Android nativas; sem WebView, servidor, conta, Supabase, telemetria ou APIs pagas. Android 8 ou superior. Persistem apenas despensa, ementa em curso, preferências e receitas explicitamente guardadas. Exportação/importação JSON pelo seletor Android. Backups automáticos desativados.

## Implementado no código

- Cinco áreas: Despensa, Ementa, Trends, Compras e Guardadas.
- Adição, edição e eliminação de ingredientes com categorias e quantidades g/ml/un.
- Sete jantares distintos; quatro pessoas; distribuição inicial 3 peixe / 3 carne / 1 vegetariana.
- Preferências de porções, duração e distribuição. Se faltarem alternativas, a ementa anterior mantém-se.
- Priorização de ingredientes disponíveis, sem acumular histórico.
- Troca individual do mesmo tipo; confeção confirmada com dedução única do stock existente.
- Compras por categorias, agregação de quantidades, desconto da despensa e exclusão de pratos preparados.
- Favoritos explícitos e eliminação.
- Catálogo inicial de 24 receitas originais em português, sem alegações de popularidade.
- Pesquisa externa YouTube, TikTok e Reels; importador HTTPS de Recipe JSON-LD com revisão explícita de quantidades, porções, tempo e português.
- Fotografias das receitas importadas, quando a fonte fornece URL HTTPS acessível.
- Guia de peixe: frigideira, forno, papelote, grelhado e air fryer; segurança com termómetro e referência de 63 °C.
- Ícone vetorial original: prato, talheres e calendário.

## Limites conhecidos

A descoberta automática contínua de tendências não está implementada. As pesquisas abrem as plataformas originais, sem recolher rankings. A importação exige uma página acessível com Recipe JSON-LD e permissão da fonte. Não contorna restrições nem traduz receitas; as quantidades devem ser normalizadas manualmente antes de guardar. Redirecionamentos não são seguidos.

As receitas iniciais ainda não têm fotografias. Falta completar o tratamento visual premium e validar o layout num Android real. O motor procura variedade por receita, mas ainda não garante variedade ótima de técnicas e espécies, nem exclui as receitas da ementa anterior.

As marcações de Compras perdem-se ao sair desse ecrã antes da transferência. Não há conversão entre unidades, sinónimos ou equivalências culinárias; ingredientes devem ter o mesmo nome/unidade para serem conciliados.

## Compilação oficial

Pré-requisitos: JDK 17, Android SDK platform 35, build-tools 35.0.0 e Gradle 8.9. Plugin Android 8.7.3. Importar esta pasta no Android Studio, ou executar:

    python scripts/validate.py
    gradle lintRelease assembleRelease

Sem assinatura configurada, o resultado local será unsigned. Não distribuir como APK final.

Gerar uma chave persistente com o keytool do JDK, fora do repositório:

    keytool -genkeypair -keystore mesa-release.jks -alias mesa -keyalg RSA -keysize 3072 -validity 10000

Definir MESA_KEYSTORE (caminho absoluto), MESA_STORE_PASSWORD, MESA_KEY_ALIAS e MESA_KEY_PASSWORD. Nunca colocar chave ou passwords em commits ou releases. Guardar cópia segura da chave: atualizações precisam da mesma assinatura e de versionCode superior.

## GitHub e distribuição

O repositório remoto não foi criado. O conector disponível não expõe criação de repositórios; a sessão GitHub no navegador pôde ser lida, mas não operada. Nenhum outro projeto foi alterado.

Está incluído um workflow manual, sem execução automática. Não foi executado. Só executar num contexto gratuito confirmado, sem ativar faturação. Configurar os secrets MESA_KEYSTORE_BASE64, MESA_STORE_PASSWORD, MESA_KEY_ALIAS e MESA_KEY_PASSWORD.

O workflow usa ferramentas oficiais Android, verifica a assinatura, publica um artefacto e, opcionalmente, uma release. Download anónimo requer repositório público; este código não contém dados familiares. Incrementar versionCode/versionName antes de cada atualização.

O QR 246 × 246 px só é gerado depois de publicar o APK e voltar a descarregá-lo, comparando os bytes. Inclui checksum SHA-256. Não há QR nesta entrega porque ainda não existe URL real de APK.

## Validação

Executado: validação estática das 24 receitas, IDs, unidades, quantidades, categorias, referências de segurança e XML Android.

Não executado: compilação Java/Gradle, Android lint, arranque Android, testes instrumentados, revisão visual, importação online real, instalação e atualização. A validação estática não comprova compilação ou funcionamento.

Consultar VALIDACAO.md antes de distribuir.
