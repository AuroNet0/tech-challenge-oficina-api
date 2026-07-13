INSERT INTO usuarios (nome, email, senha, perfil, ativo) VALUES
('Atendente Padrao', 'atendente@oficina.com', '$2a$10$TctJISQSO07HmRwks3lFCeug0WhlNcbRr6G2Sz4CGXoRsG/P6i6Kq', 'ATENDENTE', true),
('Mecanico Padrao', 'mecanico@oficina.com', '$2a$10$TctJISQSO07HmRwks3lFCeug0WhlNcbRr6G2Sz4CGXoRsG/P6i6Kq', 'MECANICO', true),
('Gerente Padrao', 'gerente@oficina.com', '$2a$10$TctJISQSO07HmRwks3lFCeug0WhlNcbRr6G2Sz4CGXoRsG/P6i6Kq', 'GERENTE', true)
ON CONFLICT (email) DO NOTHING;
