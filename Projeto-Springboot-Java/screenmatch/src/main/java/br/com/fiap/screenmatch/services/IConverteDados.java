package br.com.fiap.screenmatch.services;

public interface IConverteDados {
    <T> T obterDados(String json, Class<T> classe);
}
