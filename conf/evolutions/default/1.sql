# --- !Ups
create table if not exists public.products (
  name varchar(100) not null,
  code varchar(255) not null,
  description varchar(1000) not null,
  price int not null,
  primary key (code)
);

insert into public.products(name, code, description, price)
values ('pepper', 'ald2', 'Pepper is a robot on wheels with LCD screen.', 7000);

insert into public.products(name, code, description, price)
values ('nao', 'ald1', 'NAO is a humanoid robot.', 3500);

insert into public.products(name, code, description, price)
values ('beobot', 'beo1', 'Beobot is a multipurpose robot.', 159.0);

create table if not exists public.cart (
  id bigint auto_increment,
  user varchar(255) not null,
  code varchar(255) not null,
  qty int not null,
  primary key (id),
  constraint uc_cart unique (user, code)
);

# --- !Downs

drop table products;
drop table cart;
