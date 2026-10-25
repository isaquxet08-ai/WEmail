package com.slda.wemail;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

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

        // 使用 View.post 确保布局已经渲染，能拿到准确的坐标
        ivOutbox.post(() -> {
            // 获取发件箱和收件箱在屏幕上的 X 坐标
            float startX = ivOutbox.getX();
            float endX = ivInbox.getX();
            
            // 计算飞行距离
            float deltaX = endX - startX;

            // 设置逐帧飞行动画（1.5秒，模拟旧电脑的拖影感）
            TranslateAnimation anim = new TranslateAnimation(0, deltaX, 0, 0);
            anim.setDuration(1500);
            anim.setFillAfter(true); // 飞完之后停留在终点

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    // 动画结束后，触发发信和音效结算
                    finishSendingProcess();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });

            ivEnvelope.startAnimation(anim);
        });
    }

    private void finishSendingProcess() {
        // 获取表单数据
        String to = etTo.getText().toString();
        String sub = etSubject.getText().toString();
        String body = etBody.getText().toString();

        // 如果没填收件人，直接演示失败效果
        if(to.isEmpty()) {
            showResult(false, "收件人不能为空");
            return;
        }

        new Thread(() -> {
            boolean success = false;
            String errMsg = "";
            try {
                // ===== 🚨 明天拿到 Serv00 账号后修改这里 =====
                Properties props = new Properties();
                props.put("mail.smtp.host", "sX.serv00.com"); 
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

                Session session = Session.getInstance(props, new Authenticator() {
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
        ivEnvelope.setVisibility(View.GONE); // 隐藏信件

        if (success) {
            MediaPlayer.create(this, R.raw.tada).start();
            Toast.makeText(this, "发送成功！文件已送达。", Toast.LENGTH_SHORT).show();
        } else {
            MediaPlayer.create(this, R.raw.chord).start();
            Toast.makeText(this, "发送失败: " + msg, Toast.LENGTH_LONG).show();
        }
    }
}
