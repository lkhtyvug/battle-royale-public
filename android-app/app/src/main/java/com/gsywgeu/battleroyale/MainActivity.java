package com.gsywgeu.battleroyale;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends Activity {
    private static final String HTML_URL = "https://raw.githubusercontent.com/lkhtyvug/battle-royale-public/main/index.html";
    private static final String EXPECTED_SHA256 = "86d3f3dd20bb6a755b924356ec2495d29454d4985b24d1d1839264cffef98a7b";
    private static final long MAX_HTML_BYTES = 25L * 1024L * 1024L;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(11, 18, 32));
        getWindow().setNavigationBarColor(Color.rgb(11, 18, 32));
        loadGame();
    }

    private void loadGame() {
        showLoading("Carregando Battle Royale…");
        worker.execute(() -> {
            File verified = null;
            String error = null;
            try {
                verified = downloadAndVerify();
            } catch (Exception e) {
                error = e.getMessage() == null ? "Não foi possível acessar a versão pública do jogo." : e.getMessage();
                File cached = cachedHtml();
                if (cached != null && cached.isFile()) {
                    try {
                        if (EXPECTED_SHA256.equals(sha256(cached))) {
                            verified = cached;
                            runOnUiThread(() -> Toast.makeText(this, "Sem conexão: usando a última versão verificada.", Toast.LENGTH_LONG).show());
                        }
                    } catch (Exception ignored) { }
                }
            }
            final File finalFile = verified;
            final String finalError = error;
            runOnUiThread(() -> {
                if (finalFile != null) openGame(finalFile);
                else showError(finalError == null ? "Falha ao carregar o jogo." : finalError);
            });
        });
    }

    private File downloadAndVerify() throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(HTML_URL).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("Accept", "text/html,application/xhtml+xml");
        connection.setRequestProperty("User-Agent", "BattleRoyaleAndroidTest/0.2");
        try {
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new Exception("Não consegui baixar o jogo (HTTP " + status + "). Tente novamente.");
            }
            long length = connection.getContentLengthLong();
            if (length > MAX_HTML_BYTES) throw new Exception("O arquivo passou do limite de segurança de 25 MB.");
            File gameDir = new File(getFilesDir(), "game");
            if (!gameDir.exists() && !gameDir.mkdirs()) throw new Exception("Não consegui preparar o armazenamento privado do jogo.");
            File temp = new File(gameDir, "index.tmp");
            File destination = cachedHtml();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long total = 0;
            byte[] buffer = new byte[32768];
            try (InputStream input = connection.getInputStream(); FileOutputStream output = new FileOutputStream(temp)) {
                int read;
                while ((read = input.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_HTML_BYTES) throw new Exception("O arquivo passou do limite de segurança de 25 MB.");
                    digest.update(buffer, 0, read);
                    output.write(buffer, 0, read);
                }
            } catch (Exception e) {
                temp.delete();
                throw e;
            }
            if (total < 64) {
                temp.delete();
                throw new Exception("O arquivo baixado está vazio ou incompleto.");
            }
            String actualHash = toHex(digest.digest());
            if (!EXPECTED_SHA256.equals(actualHash)) {
                temp.delete();
                throw new Exception("A verificação de integridade falhou. A versão remota não corresponde à versão aprovada pelo APK.");
            }
            if (destination.exists() && !destination.delete()) {
                temp.delete();
                throw new Exception("Não consegui atualizar a cópia local do jogo.");
            }
            if (!temp.renameTo(destination)) {
                temp.delete();
                throw new Exception("Não consegui salvar a cópia verificada do jogo.");
            }
            return destination;
        } finally {
            connection.disconnect();
        }
    }

    private File cachedHtml() {
        return new File(new File(getFilesDir(), "game"), "index.html");
    }

    private String sha256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[32768];
        try (InputStream input = new java.io.FileInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        return toHex(digest.digest());
    }

    private String toHex(byte[] bytes) {
        StringBuilder value = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) value.append(String.format(java.util.Locale.ROOT, "%02x", b & 0xff));
        return value.toString();
    }

    private void openGame(File html) {
        File gameDir = html.getParentFile();
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/game/", new WebViewAssetLoader.InternalStoragePathHandler(this, gameDir))
                .build();
        WebView webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(11, 18, 32));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        WebView.setWebContentsDebuggingEnabled(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAllowFileAccess(false);
        webView.getSettings().setAllowContentAccess(false);
        webView.getSettings().setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.getSettings().setSupportMultipleWindows(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public android.webkit.WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("appassets.androidplatform.net".equalsIgnoreCase(uri.getHost())) return false;
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                if (scheme.equals("https") || scheme.equals("http") || scheme.equals("mailto") || scheme.equals("tel") || scheme.equals("line")) {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, uri));
                    } catch (Exception ignored) {
                        Toast.makeText(MainActivity.this, "Não há aplicativo disponível para abrir esse link.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/game/index.html");
    }

    private void showLoading(String status) {
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(28), dp(28), dp(28), dp(28));
        layout.setBackgroundColor(Color.rgb(11, 18, 32));
        ProgressBar progress = new ProgressBar(this);
        layout.addView(progress, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView label = text(status, 18, Color.WHITE, true);
        label.setGravity(Gravity.CENTER);
        label.setPadding(0, dp(18), 0, 0);
        layout.addView(label, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        setContentView(layout);
    }

    private void showError(String message) {
        LinearLayout layout = new LinearLayout(this);
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(28), dp(28), dp(28), dp(28));
        layout.setBackgroundColor(Color.rgb(11, 18, 32));
        TextView title = text("Não foi possível abrir o jogo", 22, Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        layout.addView(title, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView detail = text(message, 15, 0xFFD7E3F4, false);
        detail.setGravity(Gravity.CENTER);
        detail.setPadding(0, dp(12), 0, dp(16));
        layout.addView(detail, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        Button retry = new Button(this);
        retry.setText("Tentar novamente");
        retry.setOnClickListener(v -> loadGame());
        layout.addView(retry, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));
        setContentView(layout);
    }

    private TextView text(String value, int sizeSp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onDestroy() {
        worker.shutdownNow();
        super.onDestroy();
    }
}
