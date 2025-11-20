drop table if exists users cascade;
drop table if exists comments_likes cascade;
drop table if exists comments cascade;
drop table if exists posts_likes cascade;
drop table if exists posts cascade;
drop table if exists member_follows cascade;
drop table if exists members cascade;



create table if not exists members
(
    id         BIGINT auto_increment primary key,
    email      varchar(255) not null unique,
    password   varchar(255) not null,
    nickname   varchar(255) not null,
    created_at TIMESTAMP not null,
    updated_at TIMESTAMP  not null,
    version BIGINT default 0

);
create index email_index on members(email);

create table if not exists member_follows
(
    id BIGINT auto_increment PRIMARY KEY ,
    follower_id BIGINT not null,
    following_id BIGINT not null,
    created_at TIMESTAMP  not null,
    updated_at TIMESTAMP  not null ,
    foreign key (follower_id) references members (id) on delete cascade,
    foreign key (following_id) references members (id) on delete cascade
);


create table if not exists posts
(
    id         BIGINT auto_increment primary key,
    content    TEXT              not null,
    like_count BIGINT    default 0,
    created_at TIMESTAMP  not null,
    updated_at TIMESTAMP  not null ,
    member_id BIGINT,
    version BIGINT default 0,
    foreign key (member_id) references members(id) on delete cascade
);

-- create index if not exists posts_created_index ON POSTS(CREATED_AT);
create index posts_created_index on posts (created_at DESC, id DESC);



create table if not exists posts_likes (
    id BIGINT auto_increment PRIMARY KEY,
    post_id BIGINT,
    member_id BIGINT,
    created_at TIMESTAMP  not null,
    updated_at TIMESTAMP  not null ,
    constraint u_member_post unique (post_id, member_id),
    foreign key (post_id) references posts(id) on delete cascade,
    foreign key (member_id) references members(id) on delete cascade
);


create table if not exists comments
(
    id       BIGINT auto_increment primary key,
    content    TEXT      not null,
    like_count BIGINT    default 0,
    post_id    BIGINT     not null,
    created_at TIMESTAMP  not null,
    updated_at TIMESTAMP  not null ,
    member_id BIGINT,
    version BIGINT default 0,
    constraint FK_COMMENTS_TO_POSTS
        foreign key (post_id) references posts (id) on delete cascade,
    foreign key (member_id) references members(id) on delete cascade
);

create index comments_created_index ON comments(created_at DESC, id DESC);

create table if not exists comments_likes (
      id BIGINT auto_increment PRIMARY KEY,
       comment_id BIGINT,
       member_id BIGINT,
       created_at TIMESTAMP  not null,
       updated_at TIMESTAMP  not null ,
        constraint u_member_comment unique (comment_id, member_id),
       foreign key (comment_id) references comments(id) on delete cascade,
       foreign key (member_id) references members(id) on delete cascade
);
