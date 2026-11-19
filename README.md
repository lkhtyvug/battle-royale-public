# Battle Royale — código-fonte

Este repositório é público. Qualquer pessoa pode ler e copiar o HTML, o código Android e os dados do jogo; não publique senhas, tokens ou outros dados privados aqui.

A tela de login Google foi removida. O jogo permite criar uma conta local com nome e senha e exige os dois campos; a senha deve ter pelo menos 6 caracteres. As senhas novas são armazenadas como hash PBKDF2-SHA-256 com salt aleatório, nunca em texto aberto. Contas antigas locais com senha ainda podem entrar e são atualizadas para o novo formato após um login válido. Contas Google antigas salvas somente neste aparelho podem definir uma senha local no fluxo de criação se mantiverem o nome já registrado.

As contas e o progresso ficam no armazenamento local deste aparelho. Não existe servidor de contas nem sincronização entre aparelhos; desinstalar ou limpar os dados do app pode apagar esse progresso. O cadastro local é para teste e não equivale à autenticação de uma conta em servidor.

O APK de teste não contém `index.html`. Ele baixa o arquivo por HTTPS e valida o SHA-256 aprovado antes de executar. O APK atual aceita `86d3f3dd20bb6a755b924356ec2495d29454d4985b24d1d1839264cffef98a7b`. Mudanças posteriores no HTML exigem atualizar o hash e recompilar o APK. O app permanece em orientação horizontal.

O APK é debug, apenas para instalação e teste; não foi publicado na Play Store. O jogo ainda depende de bibliotecas externas, como Three.js e PeerJS, que precisam de Internet.
