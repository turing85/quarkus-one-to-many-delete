package de.turing85.quarkus.one.to.many.delete.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bar", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Bar extends PanacheEntityBase {
  @JsonIgnore
  @Id
  @SequenceGenerator(name = "BarIdGenerator", sequenceName = "seq__bar__id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BarIdGenerator")
  @Column(name = "id")
  private Long id;

  @Column(name = "name", unique = true)
  private String name;

  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "fk_foo", nullable = false)
  private Foo foo;
}
