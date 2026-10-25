package com.slda.wemail;

import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;

import java.util.ArrayList;
import java.util.Properties;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private EditText etTo, etSubject, etBody;
    private ImageView ivEnvelope, ivOutbox, ivInbox;
    private MediaPlayer ringout;
    private Handler handler = new Handler(Looper.getMainLooper());

    private LinearLayout windowCompose, windowInbox;
    private ListView listViewMails;

    // 记录拖拽坐标
    private float dX, dY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        etTo = findViewById(R.id.etTo);
        etSubject = findViewById(R.id.etSubject);
        etBody = findViewById(R.id.etBody);
        ivEnvelope = findViewById(R.id.ivEnvelope);
        ivOutbox = findViewById(R.id.ivOutbox);
        ivInbox = findViewById(R.id.ivInbox);
        
        windowCompose = findViewById(R.id.windowCompose);
        windowInbox = findViewById(R.id.windowInbox);
        listViewMails = findViewById(R.id.listViewMails);

        LinearLayout titleBarCompose = findViewById(R.id.titleBarCompose);
        LinearLayout titleBarInbox = findViewById(R.id.titleBarInbox);
        TextView btnCloseCompose = findViewById(R.id.btnCloseCompose);
        TextView btnCloseInbox = findViewById(R.id.btnCloseInbox);

        Button btnSend = findViewById(R.id.btnSend);
        Button btnNavCompose = findViewById(R.id.btnNavCompose);
        Button btnNavInbox = findViewById(R.id.btnNavInbox);

        initFakeMails();

        ringout = MediaPlayer.create(this, R.raw.ringout);
        ringout.setLooping(true);

        // 🚨 绑定拖拽手势 (点住标题栏就能拖着跑)
        makeDraggable(windowCompose, titleBarCompose);
        makeDraggable(windowInbox, titleBarInbox);

        // 关闭窗口
        btnCloseCompose.setOnClickListener(v -> windowCompose.setVisibility(View.GONE));
        btnCloseInbox.setOnClickListener(v -> windowInbox.setVisibility(View.GONE));

        // 侧滑栏控制窗口显示
        btnNavCompose.setOnClickListener(v -> {
            windowCompose.setVisibility(View.VISIBLE);
            windowCompose.bringToFront(); // 提到最前面
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        btnNavInbox.setOnClickListener(v -> {
            windowInbox.setVisibility(View.VISIBLE);
            windowInbox.bringToFront();
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // 发信
        btnSend.setOnClickListener(v -> sendEmail());
    }

    // 🚨 核心魔法：让 View 可拖动的通用方法
    private void makeDraggable(View view, View touchArea) {
        touchArea.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录按下时，手指与控件左上角的相对距离
                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 移动时更新坐标
                        view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void initFakeMails() {
        ArrayList<String> fakeMails = new ArrayList<>();
        fakeMails.add("Microsoft Windows XP Team\n主题: 欢迎使用 Windows XP");
        fakeMails.add("Serv00 Support\n主题: 您的VPS已就绪 (请查看附件)");
        fakeMails.add("Termux 官方\n主题: pkg upgrade 执行完成");
        fakeMails.add("GitHub Actions\n主题: 编译失败 - Gradle 依赖冲突");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fakeMails) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setTextSize(14);
                textView.setTextColor(Color.parseColor("#333333"));
                textView.setPadding(10, 25, 10, 25);
                textView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                return textView;
            }
        };
        listViewMails.setAdapter(adapter);
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

                Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication("dmptyly@slda.cc.cd", "你的真实密码");
                    }
                });

                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress("dmptyly@slda.cc.cd"));
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
            Toast.makeText(this, "发送成功！", Toast.LENGTH_SHORT).show();
        } else {
            MediaPlayer.create(this, R.raw.chord).start();
            Toast.makeText(this, "发送失败: " + msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
