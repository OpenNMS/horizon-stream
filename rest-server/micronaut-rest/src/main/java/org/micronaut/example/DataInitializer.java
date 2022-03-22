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

package org.micronaut.example;

import java.util.List;

import org.micronaut.example.model.Post;
import org.micronaut.example.repo.PostRepo;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.event.ApplicationStartupEvent;
import io.micronaut.transaction.TransactionOperations;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationEventListener<ApplicationStartupEvent> {
    private final PostRepo postRepo;
    private final TransactionOperations<?> tx;


    @Override
    public void onApplicationEvent(ApplicationStartupEvent event) {
        log.info("initializing sample data...");
        var data = List.of(Post.builder().title("Getting started wit Micronaut").content("test").build(),
                Post.builder().title("Getting started wit Micronaut: part 2").content("test").build());

        tx.executeWrite(status -> {
            this.postRepo.deleteAll();
            this.postRepo.saveAll(data);
            return null;
        });

        tx.executeRead(status -> {
           this.postRepo.findAll().forEach(p -> log.info("Saved Post: {}", p));
           return null;
        });

        log.info("data initialization is done...");
    }
}
