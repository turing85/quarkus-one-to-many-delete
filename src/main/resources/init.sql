-- table foo
CREATE TABLE public.foo (
    id BIGINT CONSTRAINT foo__pk__id PRIMARY KEY,
    name VARCHAR(255) CONSTRAINT foo__uniq__name UNIQUE
);

CREATE SEQUENCE seq__foo__id INCREMENT BY 50 OWNED BY public.foo.id;
ALTER TABLE public.foo
    ALTER COLUMN id SET DEFAULT nextval('seq__foo__id');

CREATE UNIQUE INDEX foo__idx__id ON public.foo USING btree(id);
CREATE UNIQUE INDEX foo__idx__name ON public.foo USING btree(name);

-- table bar
CREATE TABLE public.bar (
    id BIGINT CONSTRAINT bar__pk__id PRIMARY KEY,
    name VARCHAR(255) CONSTRAINT bar__uniq__name UNIQUE,
    fk_foo BIGINT REFERENCES foo(id) CONSTRAINT bar__not_null__fk_foo NOT NULL
);

CREATE SEQUENCE seq__bar__id INCREMENT BY 50 OWNED BY public.bar.id;
ALTER TABLE public.bar
    ALTER COLUMN id SET DEFAULT nextval('seq__bar__id');

CREATE UNIQUE INDEX bar__idx__id ON public.bar USING btree(id);
CREATE UNIQUE INDEX bar__idx__name ON public.bar USING btree(name);
CREATE UNIQUE INDEX bar__idx__fk_foo ON public.bar USING btree(fk_foo);