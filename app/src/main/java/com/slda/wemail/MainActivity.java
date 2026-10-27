package com.slda.wemail;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.security.MessageDigest;
import javax.crypto.spec.SecretKeySpec;
import android.widget.EditText;
import android.app.AlertDialog;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import jakarta.mail.Folder;
import jakarta.mail.Store;
import java.util.Properties;

import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_AVATAR = 1001;
    private static final int REQ_CAMERA = 1002;

    private DrawerLayout drawerLayout;
    private FrameLayout desktop;
    private View windowInbox, windowSent, windowDraft, windowTrash, windowCompose;
    private View windowSettings, windowAvatarMenu, windowAvatarSource;
    private ImageView desktopInboxIcon, desktopSendIcon, topAvatar, drawerAvatar;
    private LinearLayout dialBubbleView;
    private TextView statusBar, drawerAccount, navMyMail;
    private ImageView ivTrashNav;
    private LinearLayout listInbox, listSent, listDraft, listTrash;
    private TextView tvInboxEmpty;
    
    private EditText etTo, etSubject, etBody, etMailPrefix, etMailDomain;
    private EditText etImapHost, etImapUser, etImapPass, etImapPort;
    private EditText etSmtpHost, etSmtpUser, etSmtpPass, etSmtpPort;
    private CheckBox cbImapSsl, cbSmtpSsl;
    
    private Button btnSend, btnSavePass;
    private SharedPreferences prefs;
    private MediaPlayer dialPlayer;
    private Uri currentAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("wemail", MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawerLayout);
        desktop = findViewById(R.id.desktop);
        windowContacts = buildContactsWindow();
        desktop.addView(windowContacts);

        windowInbox = findViewById(R.id.windowInbox);
        windowSent = findViewById(R.id.windowSent);
        windowDraft = findViewById(R.id.windowDraft);
        windowTrash = findViewById(R.id.windowTrash);
        windowCompose = findViewById(R.id.windowCompose);
        windowSettings = findViewById(R.id.windowSettings);

        topAvatar = findViewById(R.id.topAvatar);
        drawerAvatar = findViewById(R.id.drawerAvatar);
        desktopInboxIcon = findViewById(R.id.desktopInboxIcon);
        desktopSendIcon = findViewById(R.id.desktopSendIcon);
        statusBar = findViewById(R.id.statusBar);
        drawerAccount = findViewById(R.id.drawerAccount);
        navMyMail = findViewById(R.id.navMyMail);
        ivTrashNav = findViewById(R.id.ivTrashNav);
        listInbox = findViewById(R.id.listInbox);
        listSent = findViewById(R.id.listSent);
        listDraft = findViewById(R.id.listDraft);
        listTrash = findViewById(R.id.listTrash);
        tvInboxEmpty = findViewById(R.id.tvInboxEmpty);
        listInbox = findViewById(R.id.listInbox);
        listSent = findViewById(R.id.listSent);
        listDraft = findViewById(R.id.listDraft);
        listTrash = findViewById(R.id.listTrash);
        tvInboxEmpty = findViewById(R.id.tvInboxEmpty);

        etTo = findViewById(R.id.etTo); etSubject = findViewById(R.id.etSubject); etBody = findViewById(R.id.etBody);
        etMailPrefix = findViewById(R.id.etMailPrefix); etMailDomain = findViewById(R.id.etMailDomain);
        etImapHost = findViewById(R.id.etImapHost); etImapUser = findViewById(R.id.etImapUser); etImapPass = findViewById(R.id.etImapPass); etImapPort = findViewById(R.id.etImapPort); cbImapSsl = findViewById(R.id.cbImapSsl);
        etSmtpHost = findViewById(R.id.etSmtpHost); etSmtpUser = findViewById(R.id.etSmtpUser); etSmtpPass = findViewById(R.id.etSmtpPass); etSmtpPort = findViewById(R.id.etSmtpPort); cbSmtpSsl = findViewById(R.id.cbSmtpSsl);
        
        btnSend = findViewById(R.id.btnSend); btnSavePass = findViewById(R.id.btnSavePass);

        windowAvatarMenu = buildAvatarMenuWindow();
        windowAvatarSource = buildAvatarSourceWindow();
        desktop.addView(windowAvatarMenu);
        desktop.addView(windowAvatarSource);

        loadSettings();
        loadAvatar();

        findViewById(R.id.btnOpenDrawer).setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        setupDrag(R.id.titleBarInbox, windowInbox);
        setupDrag(R.id.titleBarSent, windowSent);
        setupDrag(R.id.titleBarDraft, windowDraft);
        setupDrag(R.id.titleBarTrash, windowTrash);
        setupDrag(R.id.titleBarCompose, windowCompose);
        setupDrag(R.id.titleBarSettings, windowSettings);

        findViewById(R.id.btnCloseInbox).setOnClickListener(v -> windowInbox.setVisibility(View.GONE));
        findViewById(R.id.btnCloseSent).setOnClickListener(v -> windowSent.setVisibility(View.GONE));
        findViewById(R.id.btnCloseDraft).setOnClickListener(v -> windowDraft.setVisibility(View.GONE));
        findViewById(R.id.btnCloseTrash).setOnClickListener(v -> windowTrash.setVisibility(View.GONE));
        findViewById(R.id.btnCloseCompose).setOnClickListener(v -> {
            windowCompose.setVisibility(View.GONE);
            String to = etTo.getText().toString().trim();
            String sub = etSubject.getText().toString().trim();
            String body = etBody.getText().toString().trim();
            if (!to.isEmpty() || !sub.isEmpty() || !body.isEmpty()) {
                saveMail("draft", to, sub, body);
                Toast.makeText(this, "已保存至草稿箱", Toast.LENGTH_SHORT).show();
                loadEmails();
            }
        });
        findViewById(R.id.btnCloseSettings).setOnClickListener(v -> windowSettings.setVisibility(View.GONE));

        findViewById(R.id.desktopInboxBox).setOnClickListener(v -> { showWindow(windowInbox); fetchServerEmails("INBOX", "inbox", listInbox, tvInboxEmpty); });
        findViewById(R.id.desktopSendBox).setOnClickListener(v -> showWindow(windowCompose));

        findViewById(R.id.navCompose).setOnClickListener(v -> { showWindow(windowCompose); drawerLayout.closeDrawers(); });
        findViewById(R.id.navInbox).setOnClickListener(v -> { showWindow(windowInbox); fetchServerEmails("INBOX", "inbox", listInbox, tvInboxEmpty); drawerLayout.closeDrawers(); });
        findViewById(R.id.navSent).setOnClickListener(v -> { showWindow(windowSent); fetchServerEmails("SENT", "sent", listSent, null); drawerLayout.closeDrawers(); });
        findViewById(R.id.navDraft).setOnClickListener(v -> { showWindow(windowDraft); fetchServerEmails("DRAFT", "draft", listDraft, null); drawerLayout.closeDrawers(); });
        findViewById(R.id.navTrash).setOnClickListener(v -> { showWindow(windowTrash); drawerLayout.closeDrawers(); });
        findViewById(R.id.navMyMail).setOnClickListener(v -> { Toast.makeText(this, "当前账号：" + getFullEmail(), Toast.LENGTH_LONG).show(); drawerLayout.closeDrawers(); });
        findViewById(R.id.navSettings).setOnClickListener(v -> { showWindow(windowSettings); drawerLayout.closeDrawers(); });

        topAvatar.setOnClickListener(v -> showWindow(windowAvatarMenu));
        
        drawerAvatar.setOnClickListener(v -> {
            drawerLayout.closeDrawers();
            drawerAvatar.postDelayed(() -> {
                windowAvatarMenu.setVisibility(View.VISIBLE);
                windowAvatarMenu.bringToFront();
            }, 300);
        });

        findViewById(R.id.navContacts).setOnClickListener(v -> {
            showWindow(windowContacts);
            loadContacts();
            drawerLayout.closeDrawers();
        });
        btnSavePass.setOnClickListener(v -> saveSettings());
        btnSend.setOnClickListener(v -> sendMail());
        loadEmails();
        loadEmails();
    }
    
    private String getFullEmail() {
        String p = etMailPrefix.getText().toString().trim();
        String d = etMailDomain.getText().toString().trim();
        if (p.isEmpty() || d.isEmpty()) return "";
        return p + "@" + d;
    }
    private View buildAvatarMenuWindow() {
        int width = (int) (280 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.xp_window_bg);
        root.setElevation(14);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int) (40 * getResources().getDisplayMetrics().density); 
        lp.topMargin = (int) (80 * getResources().getDisplayMetrics().density);
        root.setLayoutParams(lp);
        root.setVisibility(View.GONE);

        LinearLayout title = new LinearLayout(this);
        title.setBackgroundResource(R.drawable.xp_title_bar);
        title.setOrientation(LinearLayout.HORIZONTAL);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(20, 10, 10, 10);

        TextView tv = new TextView(this);
        tv.setText("👤 头像操作");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        title.addView(tv);

        ImageButton btnClose = new ImageButton(this);
        btnClose.setBackgroundResource(R.drawable.xp_title_bar_close);
        btnClose.setOnClickListener(v -> root.setVisibility(View.GONE));
        title.addView(btnClose);
        root.addView(title);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(30, 30, 30, 30);

        Button btnChange = new Button(this);
        btnChange.setText("更换头像");
        btnChange.setBackgroundResource(R.drawable.xp_black_btn);
        btnChange.setTextColor(Color.BLACK);
        btnChange.setOnClickListener(v -> { root.setVisibility(View.GONE); showWindow(windowAvatarSource); });
        body.addView(btnChange);

        Button btnView = new Button(this);
        btnView.setText("查看头像");
        btnView.setBackgroundResource(R.drawable.xp_black_btn);
        btnView.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.topMargin = 20;
        btnView.setLayoutParams(btnLp);
        btnView.setOnClickListener(v -> { root.setVisibility(View.GONE); showAvatarPreview(); });
        body.addView(btnView);

        root.addView(body);
        setupDrag(title, root);
        return root;
    }

    private View buildAvatarSourceWindow() {
        int width = (int) (280 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.xp_window_bg);
        root.setElevation(14);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = (int) (60 * getResources().getDisplayMetrics().density); 
        lp.topMargin = (int) (140 * getResources().getDisplayMetrics().density);
        root.setLayoutParams(lp);
        root.setVisibility(View.GONE);

        LinearLayout title = new LinearLayout(this);
        title.setBackgroundResource(R.drawable.xp_title_bar);
        title.setOrientation(LinearLayout.HORIZONTAL);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(20, 10, 10, 10);

        TextView tv = new TextView(this);
        tv.setText("📸 选择来源");
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        title.addView(tv);

        ImageButton btnClose = new ImageButton(this);
        btnClose.setBackgroundResource(R.drawable.xp_title_bar_close);
        btnClose.setOnClickListener(v -> root.setVisibility(View.GONE));
        title.addView(btnClose);
        root.addView(title);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(30, 30, 30, 30);

        Button btnCamera = new Button(this);
        btnCamera.setText("通过相机");
        btnCamera.setBackgroundResource(R.drawable.xp_black_btn);
        btnCamera.setTextColor(Color.BLACK);
        btnCamera.setOnClickListener(v -> { root.setVisibility(View.GONE); takePhoto(); });
        body.addView(btnCamera);

        Button btnGallery = new Button(this);
        btnGallery.setText("通过相片库");
        btnGallery.setBackgroundResource(R.drawable.xp_black_btn);
        btnGallery.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.topMargin = 20;
        btnGallery.setLayoutParams(btnLp);
        btnGallery.setOnClickListener(v -> { root.setVisibility(View.GONE); pickAvatar(); });
        body.addView(btnGallery);

        root.addView(body);
        setupDrag(title, root);
        return root;
    }
    private void loadSettings() {
        // 基础默认值
        etMailPrefix.setText(prefs.getString("mail_prefix", "dtmbemail"));
        etMailDomain.setText(prefs.getString("mail_domain", "slda.cc.cd"));
        etImapHost.setText(prefs.getString("imap_host", "imap.qq.com"));
        etImapUser.setText(prefs.getString("imap_user", ""));
        etImapPass.setText(prefs.getString("imap_pass", ""));
        etImapPort.setText(prefs.getString("imap_port", "993"));
        cbImapSsl.setChecked(prefs.getBoolean("imap_ssl", true));
        etSmtpHost.setText(prefs.getString("smtp_host", "smtp.qq.com"));
        etSmtpUser.setText(prefs.getString("smtp_user", ""));
        etSmtpPass.setText(prefs.getString("smtp_pass", ""));
        etSmtpPort.setText(prefs.getString("smtp_port", "465"));
        cbSmtpSsl.setChecked(prefs.getBoolean("smtp_ssl", true));

        // 如果本地为空（重装后），检查公共目录是否有加密文件，有则弹窗要求输入密码
        if (etImapUser.getText().toString().isEmpty()) {
            File configFile = new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "WEmail/config.enc");
            if (configFile.exists()) {
                showMasterPasswordDialog(configFile);
            }
        }
        updateUIWithEmail();
    }

    private void showMasterPasswordDialog(File configFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔐 发现加密配置");
        builder.setMessage("检测到公共目录有配置文件，请输入主密码解密：");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("解密", (dialog, which) -> {
            String password = input.getText().toString();
            decryptAndLoad(password, configFile);
        });
        builder.setNegativeButton("取消", (dialog, which) -> {
            Toast.makeText(this, "已取消恢复", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }

    private void decryptAndLoad(String password, File configFile) {
        try {
            byte[] encryptedBytes = new byte[(int) configFile.length()];
            FileInputStream fis = new FileInputStream(configFile);
            fis.read(encryptedBytes);
            fis.close();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(password.getBytes("UTF-8"));
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String jsonStr = new String(decryptedBytes, "UTF-8");
            org.json.JSONObject json = new org.json.JSONObject(jsonStr);

            etImapHost.setText(json.optString("imap_host"));
            etImapUser.setText(json.optString("imap_user"));
            etImapPass.setText(json.optString("imap_pass"));
            etSmtpHost.setText(json.optString("smtp_host"));
            etSmtpUser.setText(json.optString("smtp_user"));
            etSmtpPass.setText(json.optString("smtp_pass"));
            etMailPrefix.setText(json.optString("mail_prefix"));
            etMailDomain.setText(json.optString("mail_domain"));
            updateUIWithEmail();

            // 保存到本地 SharedPreferences 避免下次再弹
            prefs.edit()
                .putString("imap_host", etImapHost.getText().toString())
                .putString("imap_user", etImapUser.getText().toString())
                .putString("imap_pass", etImapPass.getText().toString())
                .putString("smtp_host", etSmtpHost.getText().toString())
                .putString("smtp_user", etSmtpUser.getText().toString())
                .putString("smtp_pass", etSmtpPass.getText().toString())
                .putString("mail_prefix", etMailPrefix.getText().toString())
                .putString("mail_domain", etMailDomain.getText().toString())
                .apply();
            Toast.makeText(this, "🔓 解密成功，配置已恢复！", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ 密码错误或文件损坏！", Toast.LENGTH_LONG).show();
        }
    }

    private void saveSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🔐 设置主密码");
        builder.setMessage("请设置一个主密码用于加密配置（以后重装App恢复配置时需输入此密码）：");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("加密保存", (dialog, which) -> {
            String password = input.getText().toString();
            if (password.isEmpty()) {
                Toast.makeText(this, "密码不能为空，保存取消", Toast.LENGTH_SHORT).show();
                return;
            }
            // 写入本地
            prefs.edit()
                .putString("mail_prefix", etMailPrefix.getText().toString().trim())
                .putString("mail_domain", etMailDomain.getText().toString().trim())
                .putString("imap_host", etImapHost.getText().toString().trim())
                .putString("imap_user", etImapUser.getText().toString().trim())
                .putString("imap_pass", etImapPass.getText().toString())
                .putString("imap_port", etImapPort.getText().toString().trim())
                .putBoolean("imap_ssl", cbImapSsl.isChecked())
                .putString("smtp_host", etSmtpHost.getText().toString().trim())
                .putString("smtp_user", etSmtpUser.getText().toString().trim())
                .putString("smtp_pass", etSmtpPass.getText().toString())
                .putString("smtp_port", etSmtpPort.getText().toString().trim())
                .putBoolean("smtp_ssl", cbSmtpSsl.isChecked())
                .apply();
                
            updateUIWithEmail();
            encryptAndSave(password);
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void encryptAndSave(String password) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("imap_host", etImapHost.getText().toString().trim());
            json.put("imap_user", etImapUser.getText().toString().trim());
            json.put("imap_pass", etImapPass.getText().toString());
            json.put("smtp_host", etSmtpHost.getText().toString().trim());
            json.put("smtp_user", etSmtpUser.getText().toString().trim());
            json.put("smtp_pass", etSmtpPass.getText().toString());
            json.put("mail_prefix", etMailPrefix.getText().toString().trim());
            json.put("mail_domain", etMailDomain.getText().toString().trim());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] key = digest.digest(password.getBytes("UTF-8"));
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(json.toString().getBytes("UTF-8"));

            File dir = new File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS), "WEmail");
            if (!dir.exists()) dir.mkdirs();
            File configFile = new File(dir, "config.enc");
            
            FileOutputStream fos = new FileOutputStream(configFile);
            fos.write(encryptedBytes);
            fos.close();

            Toast.makeText(this, "✅ 配置已用主密码加密保存！", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "加密保存失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUIWithEmail() {
        String fullMail = getFullEmail();
        String domain = etMailDomain.getText().toString().trim();
        drawerAccount.setText(fullMail.isEmpty() ? "未设置账号" : fullMail);
        navMyMail.setText("📮   " + (domain.isEmpty() ? "未设置域名" : domain));
    }

    
    private void saveMail(String folder, String to, String subject, String body) {
        try {
            org.json.JSONArray arr = new org.json.JSONArray(prefs.getString("mails_" + folder, "[]"));
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("to", to);
            obj.put("subject", subject);
            obj.put("body", body);
            obj.put("time", new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date()));
            arr.put(obj);
            prefs.edit().putString("mails_" + folder, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    private void loadEmails() {
        loadList("sent", listSent);
        loadList("draft", listDraft);
        loadList("trash", listTrash);
    }

    private void loadList(String folder, LinearLayout container) {
        container.removeAllViews();
        try {
            org.json.JSONArray arr = new org.json.JSONArray(prefs.getString("mails_" + folder, "[]"));
            for (int i = arr.length() - 1; i >= 0; i--) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                TextView tv = new TextView(this);
                tv.setText(obj.getString("to") + "\n" + obj.getString("subject") + "  (" + obj.getString("time") + ")");
                tv.setTextSize(13);
                tv.setPadding(10, 10, 10, 10);
                tv.setBackgroundColor(folder.equals("draft") ? 0xFFFFF0D0 : 0x00000000);
                container.addView(tv);
            }
        } catch (Exception ignored) {}
    }

    
    
    
    private void fetchServerEmails(String targetFolderType, String cacheKey, LinearLayout listContainer, TextView emptyView) {
        final String imapHost = prefs.getString("imap_host", "");
        final String imapPort = prefs.getString("imap_port", "");
        final String imapUser = prefs.getString("imap_user", "");
        final String imapPass = prefs.getString("imap_pass", "");
        final boolean imapSsl = prefs.getBoolean("imap_ssl", true);

        Toast.makeText(this, "开始连接服务器...", Toast.LENGTH_SHORT).show();

        if (imapHost.isEmpty() || imapUser.isEmpty() || imapPass.isEmpty()) {
            if (emptyView != null) emptyView.setText("请先在设置中填写收件服务器配置");
            Toast.makeText(this, "配置为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emptyView != null) {
            emptyView.setText("正在从服务器拉取...");
            emptyView.setVisibility(View.VISIBLE);
        }
        listContainer.removeAllViews();

        new Thread(() -> {
            List<String> mailList = new ArrayList<>();
            String error = null;
            try {
                Properties props = new Properties();
                props.setProperty("mail.store.protocol", imapSsl ? "imaps" : "imap");
                props.put("mail.imaps.ssl.trust", "*");
                props.put("mail.imaps.connectiontimeout", "10000");
                props.put("mail.imaps.timeout", "10000");

                Session session = Session.getInstance(props, null);
                Store store = session.getStore();
                store.connect(imapHost, Integer.parseInt(imapPort), imapUser, imapPass);
                
                Folder targetFolder = null;
                if (targetFolderType.equals("INBOX")) {
                    targetFolder = store.getFolder("INBOX");
                } else {
                    Folder[] folders = store.getDefaultFolder().list("*");
                    for (Folder f : folders) {
                        String name = f.getName().toUpperCase();
                        if (targetFolderType.equals("SENT") && name.contains("SENT")) { targetFolder = f; break; }
                        if (targetFolderType.equals("DRAFT") && name.contains("DRAFT")) { targetFolder = f; break; }
                        if (targetFolderType.equals("TRASH") && (name.contains("TRASH") || name.contains("DELETED"))) { targetFolder = f; break; }
                    }
                }

                if (targetFolder == null) {
                    error = "找不到文件夹: " + targetFolderType;
                } else {
                    targetFolder.open(Folder.READ_ONLY);
                    Message[] messages = targetFolder.getMessages();
                    int start = Math.max(0, messages.length - 10);
                    for (int i = messages.length - 1; i >= start; i--) {
                        Message msg = messages[i];
                        String from = msg.getFrom() != null && msg.getFrom().length > 0 ? msg.getFrom()[0].toString() : "未知";
                        String subject = msg.getSubject() != null ? msg.getSubject() : "(无主题)";
                        Date date = msg.getSentDate() != null ? msg.getSentDate() : new Date();
                        String time = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(date);
                        mailList.add(from + System.lineSeparator() + subject + "  (" + time + ")");
                    }
                    targetFolder.close(false);
                }
                store.close();
            } catch (Exception e) {
                error = e.getClass().getSimpleName() + ": " + e.getMessage();
            }

            final String err = error;
            final List<String> finalList = mailList;
            runOnUiThread(() -> {
                if (err != null) {
                    if (emptyView != null) emptyView.setText("拉取失败: " + err);
                    Toast.makeText(this, "失败: " + err, Toast.LENGTH_LONG).show();
                } else if (finalList.isEmpty()) {
                    if (emptyView != null) emptyView.setText("此文件夹为空");
                    Toast.makeText(this, "拉取成功，但文件夹为空", Toast.LENGTH_SHORT).show();
                    if (targetFolderType.equals("TRASH")) ivTrashNav.setImageResource(R.drawable.trash_empty_icon);
                } else {
                    if (emptyView != null) emptyView.setVisibility(View.GONE);
                    Toast.makeText(this, "成功拉取 " + finalList.size() + " 封邮件", Toast.LENGTH_SHORT).show();
                    if (targetFolderType.equals("TRASH")) ivTrashNav.setImageResource(R.drawable.trash_full_icon);
                    
                    for (String mailStr : finalList) {
                        TextView tv = new TextView(this);
                        tv.setText(mailStr);
                        tv.setTextSize(13);
                        tv.setPadding(10, 10, 10, 10);
                        listContainer.addView(tv);
                        
                        View divider = new View(this);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                        divider.setBackgroundColor(0xFFCCCCCC);
                        listContainer.addView(divider);
                    }
                }
            });
        }).start();
    }
    
    
    

    
    private void stopDialSound() {
        if (dialBubbleView != null) {
            desktop.removeView(dialBubbleView);
            dialBubbleView = null;
        }
        if (dialPlayer != null) {
            try {
                if (dialPlayer.isPlaying()) dialPlayer.stop();
                dialPlayer.release();
            } catch (Exception ignored) {}
            dialPlayer = null;
        }
    }

    private void showDialBubble() {
        desktop.post(() -> {
            if (dialBubbleView != null) {
                desktop.removeView(dialBubbleView);
            }

            int[] to = new int[2];
            int[] base = new int[2];
            desktopSendIcon.getLocationInWindow(to);
            desktop.getLocationInWindow(base);

            dialBubbleView = new LinearLayout(this);
            dialBubbleView.setOrientation(LinearLayout.VERTICAL);
            dialBubbleView.setGravity(android.view.Gravity.CENTER);
            
            // 用一张白底的背景，让绿色进度条更明显
            android.graphics.drawable.GradientDrawable panelBg = new android.graphics.drawable.GradientDrawable();
            panelBg.setColor(0xFFFFFFFF);
            panelBg.setCornerRadius(8);
            panelBg.setStroke(2, 0xFF0000FF);
            dialBubbleView.setBackground(panelBg);
            dialBubbleView.setPadding(16, 16, 16, 16);

            ImageView bubble = new ImageView(this);
            // 这里用系统原生的电话图标，绝对能找到
            bubble.setImageResource(android.R.drawable.sym_action_call);
            LinearLayout.LayoutParams bubbleLp = new LinearLayout.LayoutParams(64, 64);
            bubbleLp.gravity = android.view.Gravity.CENTER;
            bubble.setLayoutParams(bubbleLp);
            dialBubbleView.addView(bubble);

            android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true); 
            
            android.graphics.drawable.GradientDrawable bgShape = new android.graphics.drawable.GradientDrawable();
            bgShape.setColor(0xFF000000); 
            bgShape.setCornerRadius(2);
            
            android.graphics.drawable.GradientDrawable progressShape = new android.graphics.drawable.GradientDrawable();
            progressShape.setColor(0xFF00FF00); 
            progressShape.setCornerRadius(2);
            
            android.graphics.drawable.ClipDrawable clipDrawable = new android.graphics.drawable.ClipDrawable(progressShape, android.view.Gravity.LEFT, android.graphics.drawable.ClipDrawable.HORIZONTAL);
            android.graphics.drawable.LayerDrawable layerDrawable = new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{bgShape, clipDrawable});
            
            progressBar.setProgressDrawable(layerDrawable);
            
            LinearLayout.LayoutParams progressLp = new LinearLayout.LayoutParams(120, 12);
            progressLp.topMargin = 10;
            progressLp.gravity = android.view.Gravity.CENTER;
            progressBar.setLayoutParams(progressLp);
            dialBubbleView.addView(progressBar);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            dialBubbleView.setLayoutParams(lp);
            dialBubbleView.setX(to[0] - base[0] - 20);
            dialBubbleView.setY(to[1] - base[1] - 100);

            desktop.addView(dialBubbleView);

            android.view.animation.ScaleAnimation scale = new android.view.animation.ScaleAnimation(
                    0f, 1f, 0f, 1f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                    android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
            );
            scale.setDuration(300);
            dialBubbleView.startAnimation(scale);
        });
    }

    
    

    

    

    
    private View buildContactsWindow() {
        int width = (int) (300 * getResources().getDisplayMetrics().density);
        int height = (int) (400 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.xp_window_bg);
        root.setElevation(15);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(width, height);
        lp.leftMargin = (int) (30 * getResources().getDisplayMetrics().density); 
        lp.topMargin = (int) (50 * getResources().getDisplayMetrics().density);
        root.setLayoutParams(lp);
        root.setVisibility(View.GONE);

        LinearLayout title = new LinearLayout(this);
        title.setBackgroundResource(R.drawable.xp_title_bar);
        title.setOrientation(LinearLayout.HORIZONTAL);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(15, 8, 5, 8);

        TextView tvTitle = new TextView(this);
        tvTitle.setText("📇 联系人");
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setTextSize(13);
        tvTitle.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        title.addView(tvTitle);

        ImageButton btnClose = new ImageButton(this);
        btnClose.setBackgroundResource(R.drawable.xp_close_btn);
        btnClose.setOnClickListener(v -> root.setVisibility(View.GONE));
        title.addView(btnClose);
        root.addView(title);

        LinearLayout inputArea = new LinearLayout(this);
        inputArea.setOrientation(LinearLayout.VERTICAL);
        inputArea.setPadding(10, 10, 10, 10);
        
        etContactName = new EditText(this);
        etContactName.setHint("备注（如：张三）");
        etContactName.setTextSize(13);
        inputArea.addView(etContactName);

        etContactEmail = new EditText(this);
        etContactEmail.setHint("邮箱（如：test@qq.com）");
        etContactEmail.setTextSize(13);
        etContactEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputArea.addView(etContactEmail);

        Button btnAdd = new Button(this);
        btnAdd.setText("保存联系人");
        btnAdd.setOnClickListener(v -> saveContact());
        inputArea.addView(btnAdd);

        root.addView(inputArea);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        listContacts = new LinearLayout(this);
        listContacts.setOrientation(LinearLayout.VERTICAL);
        listContacts.setPadding(10, 10, 10, 10);
        scrollView.addView(listContacts);
        root.addView(scrollView);

        setupDrag(title, root);
        return root;
    }

    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String email = etContactEmail.getText().toString().trim();
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "备注和邮箱不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            org.json.JSONArray arr = new org.json.JSONArray(prefs.getString("contacts", "[]"));
            org.json.JSONObject obj = new org.json.JSONObject();
            obj.put("name", name);
            obj.put("email", email);
            arr.put(obj);
            prefs.edit().putString("contacts", arr.toString()).apply();
            etContactName.setText("");
            etContactEmail.setText("");
            loadContacts();
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadContacts() {
        listContacts.removeAllViews();
        try {
            org.json.JSONArray arr = new org.json.JSONArray(prefs.getString("contacts", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                String name = obj.getString("name");
                String email = obj.getString("email");
                
                TextView tv = new TextView(this);
                tv.setText(name + "\n" + email);
                tv.setTextSize(14);
                tv.setPadding(10, 10, 10, 10);
                tv.setOnClickListener(v -> {
                    etTo.setText(email);
                    windowContacts.setVisibility(View.GONE);
                });
                listContacts.addView(tv);
                
                View divider = new View(this);
                divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                divider.setBackgroundColor(0xFFCCCCCC);
                listContacts.addView(divider);
            }
        } catch (Exception ignored) {}
    }

    private void sendMail() {
        final String to = etTo.getText().toString().trim();
        final String subject = etSubject.getText().toString().trim();
        final String body = etBody.getText().toString();
        final String from = getFullEmail();
        final String smtpHost = prefs.getString("smtp_host", "");
        final String smtpPort = prefs.getString("smtp_port", "");
        final String smtpUser = prefs.getString("smtp_user", "");
        final String smtpPass = prefs.getString("smtp_pass", "");
        final boolean smtpSsl = prefs.getBoolean("smtp_ssl", true);

        if (to.isEmpty()) { Toast.makeText(this, "请填写收件人", Toast.LENGTH_SHORT).show(); return; }
        if (from.isEmpty() || smtpHost.isEmpty() || smtpUser.isEmpty() || smtpPass.isEmpty()) {
            Toast.makeText(this, "请先填写完整的发件服务器配置", Toast.LENGTH_SHORT).show();
            showWindow(windowSettings); return;
        }

        startDialSound();
        showDialBubble();
        btnSend.setEnabled(false);
        statusBar.setText("正在发送 → " + to);

        new Thread(() -> {
            boolean ok = false; String err = null;
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.auth", "true");
                if (smtpSsl) {
                    props.put("mail.smtp.ssl.enable", "true");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.port", smtpPort);
                } else {
                    props.put("mail.smtp.starttls.enable", "true");
                }
                props.put("mail.smtp.connectiontimeout", "15000");
                props.put("mail.smtp.timeout", "15000");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(smtpUser, smtpPass);
                    }
                });

                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(from));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                msg.setSubject(subject, "UTF-8");
                msg.setText(body, "UTF-8");
                msg.setSentDate(new Date());

                Transport.send(msg);
                ok = true;
            } catch (Exception e) { err = e.getMessage(); }

            final boolean success = ok; final String errorMsg = err;
            runOnUiThread(() -> {
                btnSend.setEnabled(true);
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                if (success) {
                    playSound(R.raw.tada);
                    statusBar.setText("发送成功 · " + time);
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                    // 发件成功，不在本地存根，等用户点开已发送时会自动从服务器拉取
                    etTo.setText(""); etSubject.setText(""); etBody.setText("");
                    loadEmails();
                } else {
                    playSound(R.raw.chord);
                    statusBar.setText("发送失败：" + (errorMsg == null ? "未知错误" : errorMsg));
                    Toast.makeText(this, "发送失败", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }
    private void showAvatarPreview() {
        ImageView iv = new ImageView(this);
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setAdjustViewBounds(true);
        iv.setPadding(20, 20, 20, 20);
        String type = prefs.getString("avatar_type", "");
        if ("uri".equals(type) && currentAvatarUri != null) { iv.setImageURI(currentAvatarUri); }
        else if ("file".equals(type)) {
            File f = new File(getFilesDir(), "avatar.jpg");
            if (f.exists()) iv.setImageBitmap(BitmapFactory.decodeFile(f.getAbsolutePath()));
            else iv.setImageResource(R.mipmap.ic_launcher);
        } else { iv.setImageResource(R.mipmap.ic_launcher); }
        new AlertDialog.Builder(this).setTitle("查看当前头像").setView(iv).setPositiveButton("关闭", null).show();
    }
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try { startActivityForResult(intent, REQ_CAMERA); } catch (Exception e) {}
    }
    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try { startActivityForResult(intent, REQ_AVATAR); } catch (Exception e) {}
    }
    private void loadAvatar() {
        String type = prefs.getString("avatar_type", "");
        if ("uri".equals(type)) {
            String uriStr = prefs.getString("avatar_uri", "");
            if (!uriStr.isEmpty()) {
                currentAvatarUri = Uri.parse(uriStr);
                topAvatar.setImageURI(currentAvatarUri);
                drawerAvatar.setImageURI(currentAvatarUri);
            }
        } else if ("file".equals(type)) {
            File f = new File(getFilesDir(), "avatar.jpg");
            if (f.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath());
                if (bmp != null) {
                    topAvatar.setImageBitmap(bmp);
                    drawerAvatar.setImageBitmap(bmp);
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            currentAvatarUri = data.getData();
            topAvatar.setImageURI(currentAvatarUri);
            drawerAvatar.setImageURI(currentAvatarUri);
            try {
                getContentResolver().takePersistableUriPermission(currentAvatarUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefs.edit().putString("avatar_type", "uri").putString("avatar_uri", currentAvatarUri.toString()).apply();
            } catch (Exception ignored) {}
        } else if (requestCode == REQ_CAMERA && resultCode == RESULT_OK && data != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            if (imageBitmap != null) {
                topAvatar.setImageBitmap(imageBitmap);
                drawerAvatar.setImageBitmap(imageBitmap);
                try {
                    File f = new File(getFilesDir(), "avatar.jpg");
                    FileOutputStream fos = new FileOutputStream(f);
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                    prefs.edit().putString("avatar_type", "file").apply();
                } catch (Exception e) {}
            }
        }
    }
    private void setupDrag(int titleBarId, final View window) { setupDrag(window.findViewById(titleBarId), window); }
    private void setupDrag(View titleBar, final View window) {
        titleBar.setOnTouchListener(new View.OnTouchListener() {
            final float[] down = new float[2]; final float[] start = new float[2];
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        down[0] = e.getRawX(); down[1] = e.getRawY();
                        start[0] = window.getX(); start[1] = window.getY();
                        window.bringToFront(); return true;
                    case MotionEvent.ACTION_MOVE:
                        float nx = start[0] + (e.getRawX() - down[0]);
                        float ny = start[1] + (e.getRawY() - down[1]);
                        window.setX(Math.max(0, Math.min(nx, desktop.getWidth() - window.getWidth())));
                        window.setY(Math.max(0, Math.min(ny, desktop.getHeight() - window.getHeight())));
                        return true;
                }
                return false;
            }
        });
    }
    private void showWindow(View window) {
        window.setVisibility(View.VISIBLE);
        window.bringToFront();
    }
    private void playSound(int resId) {
        if (!prefs.getBoolean("sound_on", true)) return;
        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) { mp.setOnCompletionListener(MediaPlayer::release); mp.start(); }
        } catch (Exception ignored) {}
    }
    private void startDialSound() {
        if (!prefs.getBoolean("sound_on", true)) return;
        stopDialSound(); 
        try {
            dialPlayer = new MediaPlayer();
            android.content.res.AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.ringout);
            if (afd == null) return;
            dialPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            afd.close();
            dialPlayer.setLooping(true); 
            dialPlayer.prepare();
            dialPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    

    private void flyAnimation() {
        desktop.post(() -> {
            int[] from = new int[2]; int[] to = new int[2]; int[] base = new int[2];
            desktopInboxIcon.getLocationInWindow(from); desktopSendIcon.getLocationInWindow(to); desktop.getLocationInWindow(base);
            ImageView fly = new ImageView(this);
            fly.setImageResource(android.R.drawable.sym_action_call);
            fly.setLayoutParams(new FrameLayout.LayoutParams(56, 56));
            fly.setX(from[0] - base[0]); fly.setY(from[1] - base[1]);
            desktop.addView(fly);
            TranslateAnimation anim = new TranslateAnimation(0, to[0] - from[0], 0, to[1] - from[1]);
            anim.setDuration(700);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) { desktop.removeView(fly); }
            });
            fly.startAnimation(anim);
        });
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawers();
        else super.onBackPressed();
    }
}
