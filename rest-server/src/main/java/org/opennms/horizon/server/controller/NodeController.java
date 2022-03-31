/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.server.controller;

import java.util.Optional;

import org.opennms.horizon.server.model.entity.Node;
import org.opennms.horizon.server.repository.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/nodes")
public class NodeController {
    private final NodeRepository nodeRepo;

    @Autowired
    public NodeController(NodeRepository nodeRepo) {
        this.nodeRepo = nodeRepo;
    }

    @GetMapping
    public Flux<Node> listAll() {
        return Flux.fromIterable(nodeRepo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<Node>> findById(@PathVariable Integer id) {
        Optional<Node> node = nodeRepo.findById(id);
        if(node.isPresent()) {
            return ResponseEntity.ok().body(Mono.just(node.get()));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public Mono<Node> create(@RequestBody Node node) {
        return Mono.just(nodeRepo.save(node));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Mono<Node>> update(@PathVariable Integer id, @RequestBody Node node) {
        //TODO convert to DTO mapping
        Optional<Node> dbnode = nodeRepo.findById(id);
        if(dbnode.isPresent()) {
            node.setId(id);
            return ResponseEntity.ok().body(Mono.just(nodeRepo.save(node)));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable Integer id) {
        Optional<Node> dbNode = nodeRepo.findById(id);
        if(dbNode.isPresent()) {
            nodeRepo.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
