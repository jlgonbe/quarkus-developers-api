package org.jgb.quarkus.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.json.bind.annotation.JsonbDateFormat;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

@Entity
public class Developer extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String firstName;

    public String lastName;

    public Integer age;

    //@ElementCollection(targetClass = String.class)
    public String technologies;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    @CreationTimestamp
    public ZonedDateTime createdAt;

    @JsonbDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    @UpdateTimestamp
    public ZonedDateTime updatedAt;

    public static Uni<Developer> findByDeveloperId(Long id) {
        return findById(id);
    }

    public static Uni<Developer> updateDeveloper(Long id, Developer developer) {
        return Panache
                .withTransaction(() -> findByDeveloperId(id)
                        .onItem().ifNotNull()
                        .transform(entity -> {
                            entity.firstName = developer.firstName;
                            entity.lastName = developer.lastName;
                            entity.age = developer.age;
                            entity.technologies = developer.technologies;
                            return entity;
                        })
                        .onFailure()
                        .recoverWithNull());
    }

    public static Uni<Developer> addDeveloper(Developer developer) {
        return Panache
                .withTransaction(developer::persist)
                .replaceWith(developer)
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .transform(t -> new IllegalStateException(t));
    }

    public static Uni<List<Developer>> getAllDevelopers() {
        return Developer
                .listAll(Sort.by("createdAt"));
    }

    public static Uni<Boolean> deleteDeveloper(Long id) {
        return Panache.withTransaction(() -> deleteById(id));
    }

    public String toString() {
        return this.getClass().getSimpleName() + "<" + this.id + ">";
    }

}
