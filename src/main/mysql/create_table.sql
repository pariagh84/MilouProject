create table users
(
    id            int primary key auto_increment,
    email         nvarchar(100) not null unique,
    password_hash text          not null
);

create table emails
(
    id         int primary key auto_increment,
    subject    nvarchar(50) not null,
    body       text         not null,
    sender_id  int,
    reply_id   int,
    forward_id int,
    send_time  timestamp default current_timestamp,

    foreign key (sender_id) references users (id) on delete set null,
    foreign key (reply_id) references emails (id) on delete set null,
    foreign key (forward_id) references emails (id) on delete set null
);

create table email_recipients
(
    id          int primary key auto_increment,
    email_id    int not null,
    receiver_id int not null,
    is_read     boolean default false,

    foreign key (email_id) references emails (id) on delete cascade,
    foreign key (receiver_id) references users (id) on delete cascade
);