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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import java.io.File;
import java.io.FileOutputStream;
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
    private static final int REQ_CAMERA = 1002;

    private DrawerLayout drawerLayout;
    private FrameLayout desktop;
    private View windowInbox, windowSent, windowDraft, windowTrash, windowCompose;
    private View windowSettings, windowAvatarMenu, windowAvatarSource;
    private ImageView desktopInboxIcon, desktopSendIcon, topAvatar, drawerAvatar;
    private TextView statusBar, drawerAccount, navMyMail;
    
    private EditText etTo, etSubject, etBody, etMailPrefix, etMailDomain;
    private EditText etImapHost, etImapUser, etImapPass, etImapPort;
    private EditText etSmtpHost, etSmtpUser, etSmtpPass, etSmtpPort;
    private CheckBox cbImapSsl, cbSmtpSsl;
    
    private Button btnSend, btnSavePass;
    private SharedPreferences prefs;
    private Uri currentAvatarUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("wemail", MODE_PRIVATE);

        drawerLayout = findViewById(R.id.drawerLayout);
        desktop = findViewById(R.id.desktop);

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
        findViewById(R.id.btnCloseCompose).setOnClickListener(v -> windowCompose.setVisibility(View.GONE));
        findViewById(R.id.btnCloseSettings).setOnClickListener(v -> windowSettings.setVisibility(View.GONE));

        findViewById(R.id.desktopInboxBox).setOnClickListener(v -> showWindow(windowInbox));
        findViewById(R.id.desktopSendBox).setOnClickListener(v -> showWindow(windowCompose));

        findViewById(R.id.navCompose).setOnClickListener(v -> { showWindow(windowCompose); drawerLayout.closeDrawers(); });
        findViewById(R.id.navInbox).setOnClickListener(v -> { showWindow(windowInbox); drawerLayout.closeDrawers(); });
        findViewById(R.id.navSent).setOnClickListener(v -> { showWindow(windowSent); drawerLayout.closeDrawers(); });
        findViewById(R.id.navDraft).setOnClickListener(v -> { showWindow(windowDraft); drawerLayout.closeDrawers(); });
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

        btnSavePass.setOnClickListener(v -> saveSettings());
        btnSend.setOnClickListener(v -> sendMail());
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
        btnClose.setBackgroundResource(R.drawable.xp_close_btn);
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
        btnClose.setBackgroundResource(R.drawable.xp_close_btn);
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
        etMailPrefix.setText(prefs.getString("mail_prefix", "dtmbemail"));
        etMailDomain.setText(prefs.getString("mail_domain", "slda.cc.cd"));
        etImapHost.setText(prefs.getString("imap_host", "imap.qq.com"));
        etImapUser.setText(prefs.getString("imap_user", "dtmbinxcvb@qq.com"));
        etImapPass.setText(prefs.getString("imap_pass", ""));
        etImapPort.setText(prefs.getString("imap_port", "993"));
        cbImapSsl.setChecked(prefs.getBoolean("imap_ssl", true));
        etSmtpHost.setText(prefs.getString("smtp_host", "smtp.qq.com"));
        etSmtpUser.setText(prefs.getString("smtp_user", "dtmbinxcvb@qq.com"));
        etSmtpPass.setText(prefs.getString("smtp_pass", ""));
        etSmtpPort.setText(prefs.getString("smtp_port", "465"));
        cbSmtpSsl.setChecked(prefs.getBoolean("smtp_ssl", true));

        updateUIWithEmail();
    }

    private void saveSettings() {
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
        Toast.makeText(this, "配置已保存", Toast.LENGTH_SHORT).show();
    }

    private void updateUIWithEmail() {
        String fullMail = getFullEmail();
        String domain = etMailDomain.getText().toString().trim();
        drawerAccount.setText(fullMail.isEmpty() ? "未设置账号" : fullMail);
        navMyMail.setText("📮   " + (domain.isEmpty() ? "未设置域名" : domain));
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

        playSound(R.raw.ringout);
        flyAnimation();
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
                    etSubject.setText(""); etBody.setText("");
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
    private void flyAnimation() {
        desktop.post(() -> {
            int[] from = new int[2]; int[] to = new int[2]; int[] base = new int[2];
            desktopInboxIcon.getLocationInWindow(from); desktopSendIcon.getLocationInWindow(to); desktop.getLocationInWindow(base);
            ImageView fly = new ImageView(this);
            fly.setImageResource(R.drawable.mail_icon);
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
