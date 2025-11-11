-- PostgreSQL database dump for Testcontainers

-- Cria o schema test caso não exista
CREATE SCHEMA IF NOT EXISTS test;

-- Configurações
SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

-- Remove apenas objetos que vamos recriar
DROP VIEW IF EXISTS public.users;
DROP TABLE IF EXISTS public.plantas;
DROP VIEW IF EXISTS public.indicador_economico;
DROP VIEW IF EXISTS public.indicador_agregado_mensal;
DROP VIEW IF EXISTS public.indicador_agregado_anual;
DROP FUNCTION IF EXISTS public.get_latest_indicators();

-- Tabelas mínimas para views
CREATE TABLE test.indicador_agregado_anual (
    id BIGINT,
    ano INT,
    codigo_bc INT,
    valor_maximo NUMERIC,
    valor_medio NUMERIC,
    valor_minimo NUMERIC
);

CREATE TABLE test.indicador_agregado_mensal (
    id BIGINT,
    ano INT,
    mes INT,
    codigo_bc INT,
    valor_maximo NUMERIC,
    valor_medio NUMERIC,
    valor_minimo NUMERIC
);

CREATE TABLE test.indicador_economico (
    id BIGINT,
    nome VARCHAR(255),
    codigo_bc INT,
    valor NUMERIC,
    data DATE,
    frequencia VARCHAR(50),
    created_at TIMESTAMP
);

CREATE TABLE test.users (
    id BIGINT,
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    data_nascimento DATE
);

-- Função
CREATE FUNCTION public.get_latest_indicators() RETURNS TABLE(codigo_bc integer, valor numeric)
LANGUAGE plpgsql AS $$
BEGIN
    RETURN QUERY
    SELECT
        t.codigo_bc,
        t.valor
    FROM (
        SELECT
            ie.codigo_bc,
            ie.valor,
            ROW_NUMBER() OVER(PARTITION BY ie.codigo_bc ORDER BY ie.data DESC) as rn
        FROM test.indicador_economico ie
    ) t
    WHERE t.rn = 1;
END;
$$;

-- Views
CREATE VIEW public.indicador_agregado_anual AS
SELECT id, ano, codigo_bc, valor_maximo, valor_medio, valor_minimo
FROM test.indicador_agregado_anual;

CREATE VIEW public.indicador_agregado_mensal AS
SELECT id, ano, codigo_bc, mes, valor_maximo, valor_medio, valor_minimo
FROM test.indicador_agregado_mensal;

CREATE VIEW public.indicador_economico AS
SELECT id, nome, codigo_bc, valor, data, frequencia, created_at
FROM test.indicador_economico;

CREATE VIEW public.users AS
SELECT id, data_nascimento, email, nome, senha
FROM test.users;

-- Tabela principal
CREATE TABLE public.plantas (
    id BIGINT NOT NULL,
    ambiente_ideal VARCHAR(255),
    categoria VARCHAR(255),
    nome_cientifico VARCHAR(255),
    nome_popular VARCHAR(255),
    rega VARCHAR(255)
);

-- Sequence para id
ALTER TABLE public.plantas ALTER COLUMN id ADD GENERATED ALWAYS AS IDENTITY (
    SEQUENCE NAME public.plantas_id_seq
    START WITH 1 INCREMENT BY 1
);

-- Dados iniciais - plantas
INSERT INTO public.plantas (id, ambiente_ideal, categoria, nome_cientifico, nome_popular, rega)
OVERRIDING SYSTEM VALUE VALUES (1, 'Sol pleno', 'Ornamental', 'Rosa gallica', 'Rosa Vermelha', 'Diária');

INSERT INTO public.plantas (id, ambiente_ideal, categoria, nome_cientifico, nome_popular, rega)
OVERRIDING SYSTEM VALUE VALUES (2, 'Meia sombra', 'Ornamental', 'Ficus benjamina', 'Figueira', 'Semanal');

-- Ajusta sequência
SELECT setval('public.plantas_id_seq', 3, true);

-- PK
ALTER TABLE ONLY public.plantas
ADD CONSTRAINT plantas_pkey PRIMARY KEY (id);

-- Índices
CREATE INDEX idx_plantas_categoria ON public.plantas (categoria);
CREATE INDEX idx_plantas_nome_popular ON public.plantas (nome_popular);

-- Dados de exemplo - users
INSERT INTO test.users (id, nome, email, senha, data_nascimento)
VALUES (1, 'Admin', 'admin@example.com', '123456', '1990-01-01');
