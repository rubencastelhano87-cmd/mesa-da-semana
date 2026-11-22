# Critérios de aceitação Android

Todos os itens abaixo estão pendentes de execução; não representam testes passados.

- Compilar release e executar lint sem erros bloqueantes.
- Verificar assinatura com apksigner; registar SHA-256 do certificado.
- Instalar APK por QR; abrir pelo launcher e confirmar nome/ícone.
- Testar offline: despensa, ementa, compras, favoritos e preferências.
- Adicionar, editar e remover ingredientes; confirmar persistência após reabrir.
- Gerar sete jantares distintos com distribuição 3/3/1 e limite temporal.
- Alterar quatro para seis pessoas: quantidades multiplicadas por 1,5.
- Testar distribuição vegetariana, sete peixes, valores inválidos e falta de alternativas.
- Trocar um jantar sem alterar os restantes ou introduzir duplicados.
- Cozinhar com stock suficiente/insuficiente: sem saldo negativo nem segunda dedução.
- Agregar ingredientes e unidades iguais; manter unidades incompatíveis separadas.
- Transferir compras uma vez e atualizar faltas.
- Guardar/eliminar favoritos; confeção não altera a receita base guardada.
- Importar Recipe JSON-LD permitido; confirmar quantidades e passos; cancelar sem persistir.
- Testar rede indisponível, HTTP inválido, ausência de Recipe e ficheiros/imagens grandes.
- Exportar/importar e comparar os dados; rejeitar ficheiro inválido sem apagar dados.
- Validar TalkBack, contraste, fontes grandes, teclado e ecrãs 360/412 dp.
- Testar rotação e retorno após seletor de documentos.
- Atualizar com mesma chave/versionCode maior: preservar dados e launcher.
- Verificar rejeição de atualização com assinatura diferente.
- Descarregar APK anónimo por HTTPS, conferir checksum e digitalizar QR em dois dispositivos.
