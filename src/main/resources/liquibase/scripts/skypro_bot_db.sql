--liquibase formatted sql
--changeset kulich51:create-notification-task-table

create table notification_task (
    id serial primary key,
    chat_id bigint not null,
    task text not null,
    task_date date not null,
    task_time time not null
);
