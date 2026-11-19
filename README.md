# Battle Royale — código-fonte

Este repositório é público. Qualquer pessoa pode ler e copiar o HTML, o código Android e os dados do jogo; não publique senhas, tokens ou outros dados privados aqui.

A tela de login Google foi removida. O cadastro exige nome e senha (mínimo 6 caracteres); senha vazia não cria a conta. Senhas são armazenadas como hash PBKDF2-SHA-256 com salt aleatório, nunca em texto aberto. Cadastro e login não ficam bloqueados pela marcação da versão do jogo. A versão ainda precisa ser atualizada antes de iniciar uma partida.

Contas e progresso ficam somente no aparelho. Não há servidor de contas nem sincronização entre dispositivos. Contas antigas locais com senha podem entrar e são atualizadas após login válido; contas Google antigas armazenadas só neste aparelho podem definir uma senha local mantendo o mesmo progresso.

O HTML corrigido nesta branch tem SHA-256 `00ef53fba455dcd3088112294b90e2c6db432dc0203ce159a610d006912e24bc`. O carregador Android no código-fonte foi atualizado para esse hash. **Nenhum APK novo foi compilado nesta atualização.** O APK já instalado tem o hash anterior fixado dentro dele; ele continuará usando o HTML antigo em cache (ou recusará o HTML novo). Para aplicar esta correção dentro do APK instalado, será necessária uma nova compilação e instalação. A verificação por hash impede que um APK existente execute silenciosamente qualquer HTML alterado no repositório público.

O APK de teste não contém `index.html`, abre em orientação horizontal e não foi publicado na Play Store. O jogo também depende de bibliotecas externas, como Three.js e PeerJS, que precisam de Internet.
