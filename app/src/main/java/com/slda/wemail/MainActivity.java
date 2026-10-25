package com.slda.wemail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_AVATAR = 1001;

    private DrawerLayout drawerLayout;
    private FrameLayout desktop;

    private View windowInbox, windowCompose, windowSettings;
    private ImageView desktopInboxIcon, desktopSendIcon, topAvatar, drawerAvatar;
    private TextView statusBar, tvFolderTitle, tvFolderContentTitle, tvFolderContent, drawerAccount;
    private EditText etTo, etSubject, etBody, etPass, etMailUser, etSmtpHost, etSmtpPort, etImapHost, etImapPort;
    private CheckBox cbSound;
    private Button btnSend, btnSavePass;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("wemail", MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawerLayout);
        desktop = findViewById(R.id.desktop);

        windowInbox = findViewById(R.id.windowInbox);
        windowCompose = findViewById(R.id.windowCompose);
        windowSettings = findViewById(R.id.windowSettings);

        topAvatar = findViewById(R.id.topAvatar);
        drawerAvatar = findViewById(R.id.drawerAvatar);
        desktopInboxIcon = findViewById(R.id.desktopInboxIcon);
        desktopSendIcon = findViewById(R.id.desktopSendIcon);
        statusBar = findViewById(R.id.statusBar);
        drawerAccount = findViewById(R.id.drawerAccount);

        tvFolderTitle = findViewById(R.id.tvFolderTitle);
        tvFolderContentTitle = findViewById(R.id.tvFolderContentTitle);
        tvFolderContent = findViewById(R.id.tvFolderContent);

        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        etPass = findViewById(R.id.etPass);
        etMailUser = findViewById(R.id.etMailUser);
        etSmtpHost = findViewById(R.id.etSmtpHost);
        etSmtpPort = findViewById(R.id.etSmtpPort);
        etImapHost = findViewById(R.id.etImapHost);
        etImapPort = findViewById(R.id.etImapPort);
        cbSound = findViewById(R.id.cbSound);
        btnSend = findViewById(R.id.btnSend);
        btnSavePass = findViewById(R.id.btnSavePass);

        // 加载保存的配置
        loadSettings();

        String savedAvatar = prefs.getString("avatar_uri", null);
        if (savedAvatar != null) {
            try {
                Uri u = Uri.parse(savedAvatar);
                topAvatar.setImageURI(u);
                drawerAvatar.setImageURI(u);
            } catch (Exception ignored) {}
        }

        findViewById(R.id.btnOpenDrawer).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        setupDrag(R.id.titleBarInbox, windowInbox);
        setupDrag(R.id.titleBarCompose, windowCompose);
        setupDrag(R.id.titleBarSettings, windowSettings);

        findViewById(R.id.btnCloseInbox).setOnClickListener(v -> windowInbox.setVisibility(View.GONE));
        findViewById(R.id.btnCloseCompose).setOnClickListener(v -> windowCompose.setVisibility(View.GONE));
        findViewById(R.id.btnCloseSettings).setOnClickListener(v -> windowSettings.setVisibility(View.GONE));

        findViewById(R.id.desktopInboxBox).setOnClickListener(v -> showFolder("📥 收件箱 - w Email", "暂无邮件", "Serv00 配置完成后，这里会显示收件列表。"));
        findViewById(R.id.desktopSendBox).setOnClickListener(v -> showWindow(windowCompose));

        // 侧滑栏点击逻辑
        findViewById(R.id.navCompose).setOnClickListener(v -> {
            showWindow(windowCompose);
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navInbox).setOnClickListener(v -> {
            showFolder("📥 收件箱 - w Email", "暂无邮件", "暂无收到的邮件。");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navSent).setOnClickListener(v -> {
            showFolder("📤 已发送 - w Email", "暂无已发送", "您还没有发送过邮件。");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navDraft).setOnClickListener(v -> {
            showFolder("📝 草稿箱 - w Email", "暂无草稿", "没有保存的草稿。");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navTrash).setOnClickListener(v -> {
            showFolder("🗑️ 垃圾邮件 - w Email", "空空如也", "垃圾箱里没有邮件。");
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navMyMail).setOnClickListener(v -> {
            String user = prefs.getString("mail_user", "dmptyly@slda.cc.cd");
            Toast.makeText(this, "当前账号：" + user, Toast.LENGTH_LONG).show();
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navSettings).setOnClickListener(v -> {
            showWindow(windowSettings);
            drawerLayout.closeDrawers();
        });
        findViewById(R.id.navAbout).setOnClickListener(v -> {
            Toast.makeText(this, "w Email · GPLv3 · isaquxet08-ai/WEmail", Toast.LENGTH_LONG).show();
            drawerLayout.closeDrawers();
        });

        View.OnClickListener avatarClick = v -> pickAvatar();
        topAvatar.setOnClickListener(avatarClick);
        drawerAvatar.setOnClickListener(avatarClick);

        btnSavePass.setOnClickListener(v -> saveSettings());

        cbSound.setOnCheckedChangeListener((b, checked) ->
                prefs.edit().putBoolean("sound_on", checked).apply());

        btnSend.setOnClickListener(v -> sendMail());
    }

    private void loadSettings() {
        etMailUser.setText(prefs.getString("mail_user", "dmptyly@slda.cc.cd"));
        etPass.setText(prefs.getString("mail_pass", ""));
        etSmtpHost.setText(prefs.getString("smtp_host", "mail.slda.cc.cd"));
        etSmtpPort.setText(prefs.getString("smtp_port", "465"));
        etImapHost.setText(prefs.getString("imap_host", "mail.slda.cc.cd"));
        etImapPort.setText(prefs.getString("imap_port", "993"));
        cbSound.setChecked(prefs.getBoolean("sound_on", true));
        
        drawerAccount.setText(prefs.getString("mail_user", "dmptyly@slda.cc.cd"));
    }

    private void saveSettings() {
        prefs.edit()
                .putString("mail_user", etMailUser.getText().toString().trim())
                .putString("mail_pass", etPass.getText().toString())
                .putString("smtp_host", etSmtpHost.getText().toString().trim())
                .putString("smtp_port", etSmtpPort.getText().toString().trim())
                .putString("imap_host", etImapHost.getText().toString().trim())
                .putString("imap_port", etImapPort.getText().toString().trim())
                .apply();
        
        drawerAccount.setText(etMailUser.getText().toString().trim());
        Toast.makeText(this, "服务器配置已保存", Toast.LENGTH_SHORT).show();
    }

    private void showFolder(String title, String contentTitle, String content) {
        tvFolderTitle.setText(title);
        tvFolderContentTitle.setText(contentTitle);
        tvFolderContent.setText(content);
        showWindow(windowInbox);
    }

    private void setupDrag(int titleBarId, final View window) {
        final View bar = window.findViewById(titleBarId);
        bar.setOnTouchListener(new View.OnTouchListener() {
            final float[] down = new float[2];
            final float[] start = new float[2];

            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        down[0] = e.getRawX();
                        down[1] = e.getRawY();
                        start[0] = window.getX();
                        start[1] = window.getY();
                        window.bringToFront();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float nx = start[0] + (e.getRawX() - down[0]);
                        float ny = start[1] + (e.getRawY() - down[1]);
                        float maxX = Math.max(0, desktop.getWidth() - window.getWidth());
                        float maxY = Math.max(0, desktop.getHeight() - window.getHeight());
                        window.setX(Math.max(0, Math.min(nx, maxX)));
                        window.setY(Math.max(0, Math.min(ny, maxY)));
                        return true;
                }
                return false;
            }
        });
    }

    private void showWindow(View window) {
        window.setVisibility(View.VISIBLE);
        window.bringToFront();
        window.post(() -> {
            float maxX = Math.max(0, desktop.getWidth() - window.getWidth());
            float maxY = Math.max(0, desktop.getHeight() - window.getHeight());
            if (window.getX() < 0) window.setX(0);
            if (window.getY() < 0) window.setY(0);
            if (window.getX() > maxX) window.setX(maxX);
            if (window.getY() > maxY) window.setY(maxY);
        });
    }

    private void playSound(int resId) {
        if (!prefs.getBoolean("sound_on", true)) return;
        try {
            MediaPlayer mp = MediaPlayer.create(this, resId);
            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception ignored) {}
    }

    private void flyAnimation() {
        desktop.post(() -> {
            int[] from = new int[2];
            int[] to = new int[2];
            int[] base = new int[2];
            desktopInboxIcon.getLocationInWindow(from);
            desktopSendIcon.getLocationInWindow(to);
            desktop.getLocationInWindow(base);

            float dx = to[0] - from[0];
            float dy = to[1] - from[1];

            ImageView fly = new ImageView(this);
            fly.setImageResource(R.drawable.folder_icon);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(56, 56);
            fly.setLayoutParams(lp);
            fly.setX(from[0] - base[0]);
            fly.setY(from[1] - base[1]);
            desktop.addView(fly);

            TranslateAnimation anim = new TranslateAnimation(0, dx, 0, dy);
            anim.setDuration(700);
            anim.setFillAfter(false);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override public void onAnimationEnd(Animation a) { desktop.removeView(fly); }
            });
            fly.startAnimation(anim);
        });
    }

    private void sendMail() {
        final String to = etTo.getText().toString().trim();
        final String subject = etSubject.getText().toString().trim();
        final String body = etBody.getText().toString();
        
        final String user = prefs.getString("mail_user", "");
        final String pass = prefs.getString("mail_pass", "");
        final String smtpHost = prefs.getString("smtp_host", "");
        final String smtpPort = prefs.getString("smtp_port", "");

        if (to.isEmpty()) {
            Toast.makeText(this, "请填写收件人", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user.isEmpty() || pass.isEmpty() || smtpHost.isEmpty() || smtpPort.isEmpty()) {
            Toast.makeText(this, "请先在设置里填写完整的服务器配置", Toast.LENGTH_SHORT).show();
            showWindow(windowSettings);
            return;
        }

        playSound(R.raw.ringout);
        flyAnimation();
        btnSend.setEnabled(false);
        statusBar.setText("正在发送 → " + to);

        new Thread(() -> {
            boolean ok = false;
            String err = null;
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", smtpHost);
                props.put("mail.smtp.port", smtpPort);
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", smtpPort);
                props.put("mail.smtp.connectiontimeout", "15000");
                props.put("mail.smtp.timeout", "15000");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });

                MimeMessage msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(user));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                msg.setSubject(subject, "UTF-8");
                msg.setText(body, "UTF-8");
                msg.setSentDate(new Date());

                Transport.send(msg);
                ok = true;
            } catch (Exception e) {
                err = e.getMessage();
            }

            final boolean success = ok;
            final String errorMsg = err;
            runOnUiThread(() -> {
                btnSend.setEnabled(true);
                String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                if (success) {
                    playSound(R.raw.tada);
                    statusBar.setText("发送成功 · " + time);
                    Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
                    etSubject.setText("");
                    etBody.setText("");
                } else {
                    playSound(R.raw.chord);
                    statusBar.setText("发送失败：" + (errorMsg == null ? "未知错误" : errorMsg));
                    Toast.makeText(this, "发送失败", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    private void pickAvatar() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        try {
            startActivityForResult(intent, REQ_AVATAR);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开相册", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_AVATAR && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            topAvatar.setImageURI(uri);
            drawerAvatar.setImageURI(uri);
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                prefs.edit().putString("avatar_uri", uri.toString()).apply();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
