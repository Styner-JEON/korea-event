package com.auth.email;

import com.auth.exception.CustomEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {

    private final SesV2Client sesV2Client;

    @Value("${aws.ses.otp.from}")
    private String fromEmailAddress;

    public void send(
            String recipient,
            String subject,
            String bodyHTML
    ) {
        Destination destination = Destination.builder()
                .toAddresses(recipient)
                .build();

        Content content = Content.builder()
                .data(bodyHTML)
                .build();

        Content sub = Content.builder()
                .data(subject)
                .build();

        Body body = Body.builder()
                .html(content)
                .build();

        Message msg = Message.builder()
                .subject(sub)
                .body(body)
                .build();

        EmailContent emailContent = EmailContent.builder()
                .simple(msg)
                .build();

        SendEmailRequest emailRequest = SendEmailRequest.builder()
                .destination(destination)
                .content(emailContent)
                .fromEmailAddress(fromEmailAddress)
                .build();

        try {
            SendEmailResponse emailResponse = sesV2Client.sendEmail(emailRequest);
            log.debug("Signup OTP email sent. recipient={}, messageId={}", recipient, emailResponse.messageId());
        } catch (SesV2Exception e) {
//            log.warn(e.awsErrorDetails().errorMessage(), e);
            throw new CustomEmailException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to send signup OTP email", e);
        }
    }

    public void sendSignupOtp(String recipient, String otp) {
        String subject = "[KoreaEvent] 회원가입 인증번호 안내";

        String bodyHTML =
                """
                <html>
                  <head>
                    <meta charset="UTF-8" />
                  </head>
                  <body>
                    <h1>KoreaEvent 회원가입 인증번호</h1>
                    <p style="font-size: 16px;">
                      안녕하세요, <strong>KoreaEvent</strong>입니다.
                    </p>
                    <p style="font-size: 16px;">
                      아래 인증번호를 입력하시면 회원가입이 완료됩니다.
                    </p>
                    <p style="font-size: 20px; font-weight: bold;">
                      인증번호: %s
                    </p>
                    <p style="font-size: 14px; color: #555;">
                      유효시간은 5분이며, 타인에게 절대 공유하지 마세요.
                    </p>
                    <hr />
                    <p style="font-size: 12px; color: #888;">
                      본 메일은 발신 전용이며, 회신이 불가능합니다.<br/>
                      잘못 수신하셨다면 이 메일을 무시하셔도 됩니다.
                    </p>
                  </body>
                </html>
                """.formatted(otp);

        send(recipient, subject, bodyHTML);
    }

}
