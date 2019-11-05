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
package org.neo4j.springframework.data.examples.spring_boot.support;

import java.util.Objects;

/**
 * Represents a generic node in the graph, without a lifecycle. It is mainly used in d3.js
 *
 * @author Michael J. Simons
 */
public class D3JSNode implements D3JSGraphElement {

    private final String title;

    private final String label;

    public D3JSNode(String title, String label) {
        this.title = title;
        this.label = label;
    }

    public String getTitle() {
        return title;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof D3JSNode))
            return false;
        D3JSNode d3JSNode = (D3JSNode) o;
        return title.equals(d3JSNode.title) &&
                label.equals(d3JSNode.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, label);
    }
}