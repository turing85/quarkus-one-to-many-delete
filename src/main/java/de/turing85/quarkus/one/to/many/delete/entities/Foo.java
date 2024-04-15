package de.turing85.quarkus.one.to.many.delete.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Entity
@Table(name = "foo", schema = "public")
@Jacksonized
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter(AccessLevel.PACKAGE)
public class Foo extends PanacheEntityBase {
  @JsonIgnore
  @Id
  @SequenceGenerator(name = "FooIdGenerator", sequenceName = "seq__foo__id")
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FooIdGenerator")
  @Column(name = "id")
  private Long id;

  @Column(name = "name", unique = true)
  private String name;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "foo",
      orphanRemoval = true)
  @Builder.Default
  private List<Bar> bars = new ArrayList<>();

  @SuppressWarnings("unused")
  public Foo(Long id, String name, List<Bar> bars) {
    setId(id);
    setName(name);
    setBars(bars);
  }

  void setBars(List<Bar> bars) {
    this.bars = Objects.requireNonNull(bars);
    this.bars.forEach(bar -> bar.setFoo(this));
  }

  public void addBar(Bar bar) {
    this.bars.add(bar);
    bar.setFoo(this);
  }
}
