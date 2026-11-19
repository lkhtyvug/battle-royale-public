# Battle Royale — código-fonte

Este repositório é público. Qualquer pessoa pode ler e copiar o HTML, o código Android e os dados do jogo; não publique aqui senhas, tokens ou outros dados privados.

O APK de teste não contém `index.html`. Ele baixa o arquivo por HTTPS, confere o SHA-256 aprovado e só então o executa. O app não pede token nem envia credenciais ao GitHub. A cópia baixada fica no armazenamento privado do app. A primeira abertura precisa de Internet; o jogo também carrega bibliotecas externas, então o cache do HTML, por si só, não garante funcionamento offline. O hash valida o `index.html`, não os scripts servidos por CDNs externos.

O hash aprovado para `index.html` nesta versão é `cd71fbe4506fd7585d97ab9bef41ebbbf2032393eb9deaecd117c2d5e52a56eb`. Se o HTML mudar, o APK atual vai recusar a nova cópia; é preciso revisar o arquivo e compilar outro APK com o hash novo. Isso impede que uma alteração inesperada na branch pública seja executada por esta versão do app.

O APK é de teste e não está incluído neste repositório nem publicado na Play Store. Ele abre em orientação horizontal. O jogo usa bibliotecas externas, como Three.js e PeerJS; o login Google existente pode precisar de integração nativa ou autenticação pelo navegador, pois o Google pode bloquear OAuth em WebViews.
