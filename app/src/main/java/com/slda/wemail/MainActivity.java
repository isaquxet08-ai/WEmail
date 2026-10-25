package com.slda.wemail;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {
    private EditText etTo, etSubject, etBody;
    private ImageView ivEnvelope, ivOutbox, ivInbox;
    private MediaPlayer ringout;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        ivEnvelope = findViewById(R.id.ivEnvelope);
        ivOutbox = findViewById(R.id.ivOutbox);
        ivInbox = findViewById(R.id.ivInbox);
        Button btnSend = findViewById(R.id.btnSend);

        ringout = MediaPlayer.create(this, R.raw.ringout);
        ringout.setLooping(true);

        btnSend.setOnClickListener(v -> sendEmail());
    }

    private void sendEmail() {
        ringout.start();
        ivEnvelope.setVisibility(View.VISIBLE);

        ivOutbox.post(() -> {
            float startX = ivOutbox.getX();
            float endX = ivInbox.getX();
            float deltaX = endX - startX;

            TranslateAnimation anim = new TranslateAnimation(0, deltaX, 0, 0);
            anim.setDuration(1500);
            anim.setFillAfter(true);

            anim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
                @Override
                public void onAnimationStart(android.view.animation.Animation animation) {}

                @Override
                public void onAnimationEnd(android.view.animation.Animation animation) {
                    finishSendingProcess();
                }

                @Override
                public void onAnimationRepeat(android.view.animation.Animation animation) {}
            });

            ivEnvelope.startAnimation(anim);
        });
    }

    private void finishSendingProcess() {
        String to = etTo.getText().toString();
        String sub = etSubject.getText().toString();
        String body = etBody.getText().toString();

        if (to.isEmpty()) {
            showResult(false, "收件人不能为空");
            return;
        }

        new Thread(() -> {
            boolean success = false;
            String errMsg = "";
            try {
                Properties props = new Properties();
                props.put("mail.smtp.host", "sX.serv00.com");
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                // 🚨 核心修复：使用全限定名 jakarta.mail.Authenticator 避免和 java.net 冲突
                Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("admin@slda.cc.cd", "你的真实密码");
                    }
                });

                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("admin@slda.cc.cd"));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
                msg.setSubject(sub);
                msg.setText(body);
                Transport.send(msg);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                errMsg = e.getMessage();
            }

            boolean finalSuccess = success;
            String finalErrMsg = errMsg;

            handler.post(() -> showResult(finalSuccess, finalErrMsg));
        }).start();
    }

    private void showResult(boolean success, String msg) {
        if (ringout.isPlaying()) {
            ringout.pause();
            ringout.seekTo(0);
        }
        ivEnvelope.setVisibility(View.GONE);

        if (success) {
            MediaPlayer.create(this, R.raw.tada).start();
            Toast.makeText(this, "发送成功！文件已送达。", Toast.LENGTH_SHORT).show();
        } else {
            MediaPlayer.create(this, R.raw.chord).start();
            Toast.makeText(this, "发送失败: " + msg, Toast.LENGTH_LONG).show();
        }
    }
}
