package com.oficina.api.service;

import com.oficina.api.exception.BusinessException;
import com.oficina.api.model.Cliente;
import com.oficina.api.model.OrdemServico;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final String baseUrl;
    private final String remetente;

    public EmailService(ObjectProvider<JavaMailSender> javaMailSenderProvider,
                        @Value("${app.public-base-url:http://localhost:8080}") String baseUrl,
                        @Value("${app.mail.from:noreply@oficina.com}") String remetente) {
        this.javaMailSender = javaMailSenderProvider.getIfAvailable();
        this.baseUrl = baseUrl;
        this.remetente = remetente;
    }

    public void enviarEmailAprovacao(OrdemServico ordemServico, String token) {
        Cliente cliente = ordemServico.getCliente();
        if (cliente == null || cliente.getEmail() == null || cliente.getEmail().isBlank()) {
            throw new BusinessException("Cliente sem e-mail cadastrado para envio do orcamento.");
        }

        if (javaMailSender == null) {
            return;
        }

        String aprovarUrl = baseUrl + "/public/aprovacoes/" + token + "/aprovar";
        String reprovarUrl = baseUrl + "/public/aprovacoes/" + token + "/reprovar";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(remetente);
        message.setTo(cliente.getEmail());
        message.setSubject("Orcamento da ordem de servico #" + ordemServico.getId());
        message.setText("""
                Ola, %s.

                Seu orcamento esta pronto para avaliacao.

                Aprovar: %s
                Reprovar: %s
                """.formatted(cliente.getNome(), aprovarUrl, reprovarUrl));

        javaMailSender.send(message);
    }
}
