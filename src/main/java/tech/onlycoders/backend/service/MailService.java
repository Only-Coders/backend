package tech.onlycoders.backend.service;

import net.sargue.mailgun.Configuration;
import net.sargue.mailgun.Mail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {

  private final Configuration configuration;

  public MailService(
    @Value("${only-coders.mailgun.domain}") String domain,
    @Value("${only-coders.mailgun.api-key}") String apiKey,
    @Value("${only-coders.mailgun.sender-mail}") String senderEmail
  ) {
    this.configuration = new Configuration().domain(domain).apiKey(apiKey).from("OnlyCoders", senderEmail);
  }

  public void sendMail(String subject, String to, String body) {
    Mail.using(this.configuration).to(to).subject(subject).text(body).build().send();
  }
}
