module infra.core {
    requires com.fasterxml.jackson.annotation;
    requires jakarta.persistence;
    requires jakarta.validation;
    requires static lombok;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.jpa;
    requires spring.security.core;
    requires spring.web;
    requires spring.webmvc;
    requires org.hibernate.orm.core;
}