package br.com.fiap.screenmatch.principal;

import br.com.fiap.screenmatch.model.DadosEpisodio;
import br.com.fiap.screenmatch.model.DadosSerie;
import br.com.fiap.screenmatch.model.DadosTemporada;
import br.com.fiap.screenmatch.model.Episodio;
import br.com.fiap.screenmatch.services.ConsumoApi;
import br.com.fiap.screenmatch.services.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private Scanner entrada = new Scanner(System.in);
    private ConsumoApi api = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String APIKEY = "&apikey=6585022c";

    public void exibirMenu() {
        System.out.println("\nMenu Principal\n");
        System.out.println("Digite o nome da série para obter as informações");
        var nomeSerie = entrada.nextLine();
        var json = api.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + APIKEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);


        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dados.totalTemporadas(); i++) {
            json = api.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + APIKEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }

        for (DadosTemporada temporada : temporadas) {
            System.out.println(temporada);
        }

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));
        System.out.println("Top 5 episodios");

        List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream()).collect(Collectors.toList());
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .map(e -> e.titulo().toUpperCase())
                .forEach(System.out::println);

       List<Episodio> episodios = temporadas.stream()
                       .flatMap(t -> t.episodios().stream()
                               .map(d -> new Episodio(t.numero(), d))
                       ).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.println("Apartir de que ano voce deseja ver os episodios?");
        var ano = entrada.nextInt();
        entrada.nextLine();

        LocalDate dataBusca = LocalDate.of(ano,1,1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream().filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
                .forEach(e -> System.out.println("Temporada: "+ e.getTemporada()
                        + " - Epidosio: "+ e.getTitulo()
                        + "Data lançamento: "+ formatter.format(e.getDataLancamento())));


        System.out.println("Episódios em ordem Alfabética");
        episodios.stream().sorted(Comparator.comparing(Episodio::getTitulo)).forEach(e -> System.out.println(e));

        System.out.println("Digite o nome do episódio para bruscar");
        var trechoTitulo = entrada.nextLine();

        Optional<Episodio> epBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase())).findFirst();

        if(epBuscado.isPresent()) {
            System.out.println("Episódio escontrado");
            System.out.println("Temporada: "+ epBuscado.get().getTemporada());
        }else{
            System.out.println("Episódio não encontrado");
        }


        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada, Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println(avaliacoesPorTemporada);

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Média: "+est.getAverage());
        System.out.println("Melhor episódio: "+est.getMax());
        System.out.println("Pior episódio: "+est.getMin());
        System.out.println("Quantidade: "+est.getCount());


    }


}
