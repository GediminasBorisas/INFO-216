package Database;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;


import java.util.ArrayList;

public class SparqlQueries {

    private Dataset dataset;
    private Model model;
    private ArrayList<Movie> movies;

    public SparqlQueries() {
        this.dataset = TDBFactory.createDataset("movie_tdb");
        this.model = dataset.getDefaultModel();
        this.movies = new ArrayList<>();
    }

    public ArrayList<Movie> getMovies() {
        return movies;
    }


    public void createMovieObjects() {
        String query =
                ""
                + "PREFIX info216: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/page/> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
                + "SELECT ?title ?year ?genres ?runtime ?language ?country ?gross ?votes ?rating ?link ?id ?directorName WHERE {"
                + "?movie info216:title ?title ;"
                        + "dbo:year ?year ;"
                        + "info216:genres ?genres ;"
                        + "dbo:filmRuntime ?runtime ;"
                        + "dbo:language ?language ;"
                        + "dbo:country ?country ;"
                        + "dbo:gross ?gross ;"
                        + "info216:imdb_votes ?votes ;"
                        + "dbo:rating ?rating ;"
                        + "dbp:Hyperlink ?link ;"
                        + "info216:imdbId ?id ;"
                        + "dbo:director ?director."
                             + "?director vcard:FN ?directorName."
                + "}";

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();

        while(resultSet.hasNext()) {
            QuerySolution qsol = resultSet.next();
//            ArrayList<String> actors = actorsOfTitle(qsol.get("?title").toString());
//            ArrayList<String> keywords = keywordsOfTitle(qsol.get("?title").toString());

            if (qsol.get("?title") != null) {
                if (!(qsol.get("?title").toString().equals("The Host ")) || !(qsol.get("?title").toString().equals("Out of the Blue "))) {
                    Movie movie = new Movie(
                            qsol.get("?title").toString(),
                            qsol.get("?year").toString(),
                            qsol.get("?genres").toString(),
                            qsol.get("?runtime").toString(),
                            qsol.get("?language").toString(),
                            qsol.get("?country").toString(),
                            qsol.get("?gross").toString(),
                            qsol.get("?votes").toString(),
                            qsol.get("?rating").toString(),
                            "unknwown score",
                            qsol.get("?link").toString(),
                            qsol.get("?id").toString(),
                            qsol.get("?directorName").toString(),
                            null,
                            null);
                    movies.add(movie);
                }
            }

        }
    }



    public void sparqlEndpoint(String title) {
        title = title.replace(" ", "_");
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbp: <http://dbpedia.org/ontology/> " +
                "SELECT DISTINCT ?title " +
                "WHERE { " +
                    "?movie a dbp:Film ." +
                    "?movie rdfs:label ?title." +
                    "FILTER regex(str(?title), \"" + title + "\", \"i\")." +
                "}";
        System.out.println("QUERY: " + query);

        ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();
        resultSet.forEachRemaining(qsol -> System.out.println("?title"));
    }

    public boolean sparqlEndpointGetSubjects(String title) {
        boolean match = false;
        title = title.replace(" ", "_");
        title += "_(film)";
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/page/> PREFIX dc: <http://purl.org/dc/terms/>" +
                "SELECT DISTINCT ?subject " +
                "WHERE {" +
                  //"<http://dbpedia.org/resource/" + title + "> rdfs:label ?title." +
                    "<http://dbpedia.org/resource/" + title + "> dc:subject ?subject." +
                   //"FILTER (lang(?subject) = 'en')." +
                "}";
        System.out.println("QUERY: " + query);
        ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();
        if(resultSet.hasNext()) {
            match = true;
            resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?subject")));
        }
        return match;
    }


    public boolean sparqlEndpointGetComment(String title) {
        boolean match = false;
        title = title.replace(" ", "_");
        title += "_(film)";
        String query = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX dbp: <http://dbpedia.org/page/>" +
                "SELECT DISTINCT ?comment " +
                "WHERE {" +
                  //"<http://dbpedia.org/resource/" + title + "> rdfs:label ?title." +
                    "<http://dbpedia.org/resource/" + title + "> rdfs:comment ?comment." +
                    "FILTER (lang(?comment) = 'en')." +
                "}";
        System.out.println("QUERY: " + query);

        ResultSet resultSet = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query).execSelect();
        if(resultSet.hasNext()) {
            match = true;
            resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?comment")));
        }
        return match;
    }

    public Boolean searchForAMovie(String title) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> SELECT DISTINCT ?movie ?property ?value WHERE { ?movie m:title ?title .?movie ?property ?value .FILTER regex(str(?title), \""+title+"\") .}";
        System.out.println("QUERY: " + query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }

        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?movie").toString()+ qsol.get("?property").toString() + qsol.get("?value").toString()));

        return match;
    }

    public Boolean searchForATitle(String title) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> SELECT DISTINCT ?title WHERE { ?movie m:title ?title.FILTER regex(str(?title), \"" + title + "\", \"i\") .}";
        System.out.println("QUERY: " + query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        System.out.println(resultSet.hasNext() + " ??");
        if(resultSet.hasNext()) {
            match = true;
        }

//        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));

        return match;
    }


    public ArrayList<String> actorsOfTitle(String title) {
        ArrayList<String> list = new ArrayList<>();
        boolean match = false;
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> "+
                "SELECT DISTINCT ?name WHERE { ?movie info216:title ?value .?movie info216:actors ?actors. ?actors dbp:starring ?actor. ?actor vcard:FN ?name." +
                "FILTER regex(str(?value), \"" + title + "\", \"i\") .}";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        resultSet.forEachRemaining(qsol ->
                list.add(qsol.get("?name").toString())
        );
        return list;
    }

    public ArrayList<String> keywordsOfTitle(String title) {
        ArrayList<String> list = new ArrayList<>();
        String query = "PREFIX info216: <http://info216.no/v2019/vocabulary/>  PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
                "SELECT DISTINCT ?keywordLabel WHERE { ?movie info216:title ?value .?movie info216:keywords ?keywords. ?keywords info216:keyword ?keyword. ?keyword rdfs:label ?keywordLabel." +
                "FILTER regex(str(?value), \"" + title + "\", \"i\") .}";

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        resultSet.forEachRemaining(qsol ->
                list.add(qsol.get("?keywordLabel").toString())
        );
        return list;
    }


    public Boolean AllMoviesOfDirector(String director) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title .?movie dbp:director ?director. ?director vcard:FN ?name."+
                "FILTER regex(str(?name), \""+director+"\", \"i\") .}";
        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }
        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));

        return match;
    }

    public Boolean AllMoviesOfActor(String actor) {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/> PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title .?movie m:actors ?actors. ?actors dbp:starring ?actor. ?actor vcard:FN ?name." +
                "FILTER regex(str(?name), \"" + actor + "\",\"i\").}";
        System.out.println(query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }

        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("?title")));

        return match;
    }

    public Boolean personDirectorAndActor() {
        boolean match = false;
        String query = "PREFIX m: <http://info216.no/v2019/vocabulary/> PREFIX dbp: <http://dbpedia.org/ontology/>" +
                "SELECT DISTINCT ?title WHERE { ?movie m:title ?title.?movie dbp:director ?director.?movie m:actors ?actors. ?actors dbp:starring ?actor." +
                "FILTER (?director = ?actor).}";
        System.out.println(query);

        ResultSet resultSet = QueryExecutionFactory
                .create(query, model)
                .execSelect();
        if(resultSet.hasNext()) {
            match = true;
        }

        resultSet.forEachRemaining(qsol -> System.out.println(qsol.get("title")));

        return match;
    }

    public void printAllTriples() {
        ResultSet resultSet = QueryExecutionFactory
                .create(""
                        + "SELECT ?s ?p ?o WHERE {"
                        + "?s ?p ?o ."
                        + "}", model)
                .execSelect();
        resultSet.forEachRemaining(qsol -> System.out.println(qsol.toString()));
    }


    public Model getModel() {
        return this.model;
    }

    public void close() {
        this.dataset.close();
    }
}
