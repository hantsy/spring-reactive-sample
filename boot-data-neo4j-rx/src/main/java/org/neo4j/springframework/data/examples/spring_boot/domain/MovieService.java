/*
 * Copyright (c) 2019 "Neo4j,"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.springframework.data.examples.spring_boot.domain;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.neo4j.springframework.data.core.ReactiveNeo4jClient;
import org.neo4j.springframework.data.examples.spring_boot.support.D3JSGraphElement;
import org.neo4j.springframework.data.examples.spring_boot.support.D3JSLink;
import org.neo4j.springframework.data.examples.spring_boot.support.D3JSNode;
import org.springframework.stereotype.Service;

/**
 * Orchestrates a {@link ReactiveNeo4jClient} for some individual mappings.
 *
 * @author Michael J. Simons
 */
@Service
public class MovieService {

    private final ReactiveNeo4jClient neo4jClient;

    public MovieService(ReactiveNeo4jClient neo4jClient) {
        this.neo4jClient = neo4jClient;
    }

    static class MovieAndActors {
        final D3JSNode movie;

        final List<D3JSNode> actors;

        MovieAndActors(D3JSNode movie,
                       List<D3JSNode> actors) {
            this.movie = movie;
            this.actors = actors;
        }
    }

    public Mono<Map<String, List<D3JSGraphElement>>> createD3JSGraph() {

        // Provides our target object, a map of two lists.
        Supplier<Map<String, List<D3JSGraphElement>>> graphSupplier = () -> {
            var map = new HashMap<String, List<D3JSGraphElement>>();
            map.put("nodes", new ArrayList<>());
            map.put("links", new ArrayList<>());
            return map;
        };

        // Fills the map from movies and their actors
        BiConsumer<Map<String, List<D3JSGraphElement>>, MovieAndActors> nodesAndLinksCollector =
                (map, movieAndActors) -> {
                    var nodes = map.get("nodes");
                    var links = map.get("links");
                    var targetIndex = nodes.size();

                    nodes.add(movieAndActors.movie);
                    movieAndActors.actors.forEach(actor -> {
                        if (nodes.contains(actor)) {
                            links.add(new D3JSLink(nodes.indexOf(actor), targetIndex));
                        } else {
                            nodes.add(actor);
                            links.add(new D3JSLink(nodes.size() - 1, targetIndex));
                        }
                    });
                };

        return
                // Define the query
                neo4jClient.query(""
                        + " MATCH (m:Movie) <- [r:ACTED_IN] - (p:Person)"
                        + " WITH m, p ORDER BY m.title, p.name"
                        + " RETURN m.title AS movie, collect(p.name) AS actors"
                )
                        // Specify the class to which each record should be mapped
                        .fetchAs(MovieAndActors.class)
                        // Specify the mapping function
                        .mappedBy((typeSystem, record) -> {
                            var movie = new D3JSNode(record.get("movie").asString(), "movie");
                            var actors = record.get("actors").asList(a -> new D3JSNode(a.asString(), "actor"));
                            return new MovieAndActors(movie, actors);
                        })
                        // Initialize the reactive sequence by indicating to fetch everything
                        .all()
                        // Use Project reactors facilities to process the resulting flux into the final, desired object
                        .collect(graphSupplier, nodesAndLinksCollector);
    }
}